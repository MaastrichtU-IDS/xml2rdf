package nl.unimaas.ids.xml2rdf.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
import org.springframework.context.support.GenericApplicationContextExtensionsKt;

public class Xml2RdfConverter {
	static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
	public static final String X2RM = "http://ids.unimaas.nl/rdf2xml/model#";
	public static final String X2RD = "http://ids.unimaas.nl/rdf2xml/data/";
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
	private XmlNode xmlNode = null;
	private File inputFile = null;
	private File outputFile = null;
	private IRI graphIRI = null;
	private boolean generateXpath = false;
	
	public Xml2RdfConverter(File inputFile, File outputFile, String graphUri, boolean generateXpath) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.generateXpath = generateXpath;
		graphIRI = valueFactory.createIRI(graphUri);
		
		xmlDocument = new XmlDocument();
		xmlNode = xmlDocument;
	}
	
	public Xml2RdfConverter doWork() throws XMLStreamException, UnsupportedRDFormatException, IOException {
		OutputStream outputStream = new FileOutputStream(outputFile, false);
		if(outputFile.getName().endsWith(".gz"))
			outputStream = new GZIPOutputStream(outputStream);
		RDFWriter rdfWriter = Rio.createWriter(RDFFormat.NQUADS, outputStream);
		rdfWriter.startRDF();
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		InputStream inputStream = new FileInputStream(inputFile);
		if(inputFile.getName().toLowerCase().endsWith(".gz"))
			inputStream = new GZIPInputStream(inputStream);
		XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
		
		String name = null;
		
		while(xmlStreamReader.hasNext()) {
			int event = xmlStreamReader.next();
			if(event==XMLStreamConstants.START_ELEMENT) {
				name = xmlStreamReader.getLocalName();
				xmlNode = xmlNode.registerChild(name, null);
				xmlNode.value = null;
				for(int i=0; i<xmlStreamReader.getAttributeCount(); i++) {
					xmlNode.registerAttribute(xmlStreamReader.getAttributeLocalName(i), xmlStreamReader.getAttributeValue(i));
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
		outputStream.close();
		
		return this;
	}
	
	private void toRdf(XmlNode node, final RDFWriter rdfWriter) {
		// first element, let's create the XmlNode subclass
		if(node.isNew) {
			rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, SUB_CLASS_OF, XML_ELEMENT, graphIRI));
			rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, HAS_NAME, valueFactory.createLiteral(node.name), graphIRI));
			if(generateXpath)
				rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, HAS_XPATH, valueFactory.createLiteral(node.getRelativeXPath()), graphIRI));
			if(!node.parent.isRoot()) {
				rdfWriter.handleStatement(valueFactory.createStatement(node.parent.class_iri, HAS_CHILD, node.class_iri, graphIRI));
			}
			node.isNew = false;
		}
		node.attributes.values().stream().filter(v -> v.isNew).forEach(attribute -> {
			rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, SUB_CLASS_OF, XML_ATTRIBUTE, graphIRI));
			rdfWriter.handleStatement(valueFactory.createStatement(node.class_iri, HAS_ATTRIBUTE, attribute.class_iri, graphIRI));
			rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_NAME, valueFactory.createLiteral(attribute.name), graphIRI));
			if(generateXpath)
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_XPATH, valueFactory.createLiteral(attribute.getRelativeXPath()), graphIRI));
			attribute.isNew = false;
		});
		
		// now the data
		rdfWriter.handleStatement(valueFactory.createStatement(node.iri, TYPE, node.class_iri, graphIRI));
		if(!node.parent.isRoot())
			rdfWriter.handleStatement(valueFactory.createStatement(node.parent.iri, HAS_CHILD, node.iri, graphIRI));
		if(node.value != null)
			rdfWriter.handleStatement(valueFactory.createStatement(node.iri, HAS_VALUE, valueFactory.createLiteral(node.value), graphIRI));
		if(generateXpath)
			rdfWriter.handleStatement(valueFactory.createStatement(node.iri, HAS_XPATH, valueFactory.createLiteral(node.getAbsoluteXpath()), graphIRI));
		
		for(XmlAttribute attribute : node.actualAttributes.values()) {
			rdfWriter.handleStatement(valueFactory.createStatement(node.iri, HAS_ATTRIBUTE, attribute.iri, graphIRI));
			rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, TYPE , attribute.class_iri, graphIRI));
			if(generateXpath)
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, HAS_XPATH, valueFactory.createLiteral(attribute.getAbsoluteXpath()), graphIRI));
			if(attribute.value != null && !attribute.value.isEmpty()) {
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, HAS_VALUE, valueFactory.createLiteral(attribute.value), graphIRI));
			}
		}
	}
	
	public Xml2RdfConverter structuredPrint() {
		printStructureAndStats(xmlDocument, "" , "| ");
		return this;
	}
	
	private void printStructureAndStats(XmlNode node, String indent, String baseIndent) {
		System.out.println(indent + "# " + node.toString());
		
		for(XmlAttribute attribute : node.attributes.values())
			System.out.println(indent + baseIndent + "* " + attribute.toString());
		
		for(XmlNode child : node.childs.values())
			printStructureAndStats(child, indent + baseIndent, baseIndent);
	}

}
