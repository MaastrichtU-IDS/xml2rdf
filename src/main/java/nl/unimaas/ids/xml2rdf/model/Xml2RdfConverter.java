package nl.unimaas.ids.xml2rdf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class Xml2RdfConverter {
	static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
	public static final String X2RM = "http://ids.unimaas.nl/xml2rdf/model#";
	public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	public static final IRI XML_ELEMENT = valueFactory.createIRI(X2RM, "XmlNode");
	public static final IRI XML_ATTRIBUTE = valueFactory.createIRI(X2RM, "XmlAttribute");
	
	public static final IRI HAS_NAME = valueFactory.createIRI(X2RM, "hasName");
	public static final IRI HAS_VALUE = valueFactory.createIRI(X2RM, "hasValue");
	public static final IRI HAS_XPATH = valueFactory.createIRI(X2RM, "hasXPath");
	public static final IRI HAS_CHILD = valueFactory.createIRI(X2RM, "hasChild");
	public static final IRI HAS_ATTRIBUTE = valueFactory.createIRI(X2RM, "hasAttribute");
	
	public static final IRI SUB_CLASS_OF = valueFactory.createIRI(RDFS, "subClassOf");
	public static final IRI TYPE = valueFactory.createIRI(RDF, "type");
	
	
	private XmlDocument xmlDocument = null;
	private String namespace = null;
	private XmlNode xmlNode = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private IRI graphIRI = null;
	private boolean expandRdf = false;
	
	public Xml2RdfConverter(InputStream inputStream, OutputStream outputStream, String graphUri, String namespace, boolean expandRdf) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.expandRdf = expandRdf;
		this.namespace = namespace;
		graphIRI = valueFactory.createIRI(graphUri);
		
		xmlDocument = new XmlDocument();
		xmlNode = xmlDocument;
	}
	
	public Xml2RdfConverter doWork() throws XMLStreamException, UnsupportedRDFormatException, IOException {
		RDFWriter rdfWriter = Rio.createWriter(RDFFormat.NQUADS, outputStream);
		rdfWriter.startRDF();
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
		
		String name = null;
		
		while(xmlStreamReader.hasNext()) {
			int event = xmlStreamReader.next();
			if(event==XMLStreamConstants.START_ELEMENT) {
				name = xmlStreamReader.getLocalName();
				xmlNode = xmlNode.registerChild(name, null, namespace);
				xmlNode.value = null;
				for(int i=0; i<xmlStreamReader.getAttributeCount(); i++) {
					xmlNode.registerAttribute(xmlStreamReader.getAttributeLocalName(i), xmlStreamReader.getAttributeValue(i), namespace);
				}
			} else if (event == XMLStreamConstants.CHARACTERS) {
				xmlNode.registerValue(xmlStreamReader.getText(), true);
			} else if (event==XMLStreamConstants.END_ELEMENT) {
				toRdf(xmlNode, rdfWriter);
				// because it is already part of the child-map (for statistics)
				// index will be incremented immediately when registered
				xmlNode.childs.values().forEach(child -> {child.index = -1; child.value = null;});
				xmlNode.attributes.values().forEach(attribute -> {attribute.index = -1; attribute.value = null;});
				xmlNode = xmlNode.parent;
			}
		}
		
		xmlStreamReader.close();
		rdfWriter.endRDF();
		
		return this;
	}
	
	private void toRdf(XmlNode node, final RDFWriter rdfWriter) {
		// first element, let's create the XmlNode subclass
		if(node.isNew) {
			rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, SUB_CLASS_OF, XML_ELEMENT, graphIRI));
			if(expandRdf) {
				rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, HAS_NAME, valueFactory.createLiteral(node.name), graphIRI));
				rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, HAS_XPATH, valueFactory.createLiteral(node.getRelativeXPath()), graphIRI));
			}
			if(!node.parent.isRoot()) {
				if (expandRdf) {
					rdfWriter.handleStatement(valueFactory.createStatement(node.parent.class_iri, HAS_CHILD, node.class_iri, graphIRI));
				} else {
			        rdfWriter.handleStatement(valueFactory.createStatement(node.parent.iri, valueFactory.createIRI(X2RM, "child:" + node.name), node.iri, graphIRI));
				}
			}
			node.isNew = false;
		}
		node.attributes.values().stream().filter(v -> v.isNew).forEach(attribute -> {
			if(expandRdf) {
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, SUB_CLASS_OF, XML_ATTRIBUTE, graphIRI));
				rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, HAS_ATTRIBUTE, attribute.class_iri, graphIRI));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_NAME, valueFactory.createLiteral(attribute.name), graphIRI));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_XPATH, valueFactory.createLiteral(attribute.getRelativeXPath()), graphIRI));
			}
			attribute.isNew = false;
		});
		
		// now the data
		rdfWriter.handleStatement(valueFactory.createStatement(node.iri, TYPE, node.class_iri, graphIRI));
		if(!node.parent.isRoot())
			if(expandRdf) {
				rdfWriter.handleStatement(valueFactory.createStatement(node.parent.iri, HAS_CHILD, node.iri, graphIRI));
			} else {
				rdfWriter.handleStatement(valueFactory.createStatement(node.parent.iri, valueFactory.createIRI(X2RM, "child:" + node.name), node.iri, graphIRI));
			}
		if(node.value != null)
			rdfWriter.handleStatement(valueFactory.createStatement(node.iri, HAS_VALUE, valueFactory.createLiteral(node.value), graphIRI));
		if(expandRdf) {
			rdfWriter.handleStatement(valueFactory.createStatement(node.iri, HAS_XPATH, valueFactory.createLiteral(node.getAbsoluteXpath()), graphIRI));
		}
		
		for(XmlAttribute attribute : node.actualAttributes.values()) {
			if(expandRdf) {
				rdfWriter.handleStatement(valueFactory.createStatement(node.iri, HAS_ATTRIBUTE, attribute.iri, graphIRI));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, TYPE , attribute.class_iri, graphIRI));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, HAS_XPATH, valueFactory.createLiteral(attribute.getAbsoluteXpath()), graphIRI));
				if(attribute.value != null && !attribute.value.isEmpty()) {
					rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, HAS_VALUE, valueFactory.createLiteral(attribute.value), graphIRI));
				}
				
			} else if(attribute.value != null && !attribute.value.isEmpty()) {
		        // Can a attribute value be empty? In this case we don't record the attribute?
		        rdfWriter.handleStatement(valueFactory.createStatement(node.iri, valueFactory.createIRI(X2RM, "attribute:" + attribute.name), valueFactory.createLiteral(attribute.value), graphIRI));
			}
		}
	}
	
	public Xml2RdfConverter structuredPrint(String outputDirectory) {
		printStructureAndStats(xmlDocument, "" , "| ", outputDirectory);
		return this;
	}
	
	private void printStructureAndStats(XmlNode node, String indent, String baseIndent, String baseDir) {
		System.out.println(indent + "# " + node.toString());
		
		// Generate template SPARQL mapping file for nodes that are arrays (more count than parent)
		if (node.parent != null && node.count > node.parent.count) {
			node.generateSparqlTemplate(node, baseDir);
		}
		
		for(XmlAttribute attribute : node.attributes.values())
			System.out.println(indent + baseIndent + "* " + attribute.toString());
		
		for(XmlNode child : node.childs.values())
			printStructureAndStats(child, indent + baseIndent, baseIndent, baseDir);
	}

}
