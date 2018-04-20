package nl.unimaas.ids.xml2rdf.model;

import java.security.KeyStore.Entry.Attribute;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFWriter;

class XmlNode extends BaseNode {
	private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
	private static final String X2RM = "http://ids.unimaas.nl/rdf2xml/model#";
	private static final String X2RD = "http://ids.unimaas.nl/rdf2xml/data/";
	private static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
//	private static final String OWL = "http://www.w3.org/2002/07/owl#";
	private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	private static final IRI XML_ELEMENT = valueFactory.createIRI(X2RM, "XmlNode");
	private static final IRI XML_ATTRIBUTE = valueFactory.createIRI(X2RM, "XmlAttribute");
	
	private static final IRI HAS_NAME = valueFactory.createIRI(X2RM, "hasName");
	private static final IRI HAS_VALUE = valueFactory.createIRI(X2RM, "hasValue");
	private static final IRI HAS_XPATH = valueFactory.createIRI(X2RM, "hasXPath");
	private static final IRI IS_CHILD_OF = valueFactory.createIRI(X2RM, "isChildOf");
	
	private static final IRI SUB_CLASS_OF = valueFactory.createIRI(RDFS, "subClassOf");
	private static final IRI TYPE = valueFactory.createIRI(RDF, "type");
	
	public Map<String, XmlNode> childs = new HashMap<>();
	public Map<String, XmlAttribute> attributes = new HashMap<>();
	public Map<String, XmlAttribute> actualAttributes = new HashMap<>();
	
	public XmlNode registerChild(final String name, final String value) {
		XmlNode child = null;
		if(childs.containsKey(name)) {
			child = childs.get(name);
			child.count++;
			child.value = null;
			child.registerValue(value);
		} else {
			child = new XmlNode();
			child.parent = this;
			child.name = name;
			child.registerValue(value);
			child.class_iri = valueFactory.createIRI(X2RM, child.getRelativeXPath());
			childs.put(name, child);
		}
		child.actualAttributes = new HashMap<>();
		child.iri = valueFactory.createIRI(X2RD, UUID.randomUUID().toString());
		return child;
	}
	
	public XmlAttribute registerAttribute(final String name, final String value) {
		XmlAttribute attribute = null;
		if(attributes.containsKey(name)) {
			attribute = attributes.get(name);
			attribute.count++;
			attribute.value = null;
			attribute.registerValue(value);
		} else {
			attribute = new XmlAttribute();
			attribute.parent = this;
			attribute.name = name;
			attribute.registerValue(value);
			attribute.class_iri = valueFactory.createIRI(X2RM, attribute.getRelativeXPath());
			attributes.put(name, attribute);
		}
		attribute.iri = valueFactory.createIRI(X2RD, UUID.randomUUID().toString());
		actualAttributes.put(name, attribute);
		return attribute;
	}
	
	public void toRdf(final RDFWriter rdfWriter) {
		// first element, let's create the XmlNode subclass
		if(count==1) {
			rdfWriter.handleStatement(valueFactory.createStatement(class_iri, SUB_CLASS_OF, XML_ELEMENT));
			rdfWriter.handleStatement(valueFactory.createStatement(class_iri, HAS_NAME, valueFactory.createLiteral(name)));
			rdfWriter.handleStatement(valueFactory.createStatement(class_iri, HAS_XPATH, valueFactory.createLiteral(getRelativeXPath())));
			if(!parent.isRoot()) {
				rdfWriter.handleStatement(valueFactory.createStatement(class_iri, IS_CHILD_OF, parent.class_iri));
			}
		}
		// let's check for new attributes
		for(XmlAttribute attribute : attributes.values()) {
			if(attribute.isNew) {
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, SUB_CLASS_OF, XML_ATTRIBUTE));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_NAME, valueFactory.createLiteral(name)));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_XPATH, valueFactory.createLiteral(attribute.getRelativeXPath())));
				attribute.isNew = false;
			}
		}
		// now the data
		rdfWriter.handleStatement(valueFactory.createStatement(iri, TYPE, class_iri));
		if(value != null)
			rdfWriter.handleStatement(valueFactory.createStatement(iri, HAS_VALUE, valueFactory.createLiteral(value)));
		rdfWriter.handleStatement(valueFactory.createStatement(iri, HAS_XPATH, valueFactory.createLiteral(getAbsoluteXpath())));
		
		for(XmlAttribute attribute : actualAttributes.values()) {
			if(attribute.isNew) {
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, TYPE , XML_ATTRIBUTE));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.iri, HAS_VALUE, valueFactory.createLiteral(value)));
				rdfWriter.handleStatement(valueFactory.createStatement(attribute.class_iri, HAS_XPATH, valueFactory.createLiteral(attribute.getAbsoluteXpath())));
				attribute.isNew = false;
			}
		}
	}
	
	@Override
	String getType() {
		return XmlNode.class.getSimpleName();
	}

	@Override
	String getRelativeXPath() {
		return parent.getRelativeXPath() + "/" + name;
	}
	
	@Override
	String getAbsoluteXpath() {
		return parent.getAbsoluteXpath() + "/" + name + "[" + count + "]";
	}
	
	boolean isRoot() {
		return false;
	}

}