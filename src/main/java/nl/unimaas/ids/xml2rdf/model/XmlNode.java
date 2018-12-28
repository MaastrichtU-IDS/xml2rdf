package nl.unimaas.ids.xml2rdf.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

class XmlNode extends BaseNode {
	private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
	
	public Map<String, XmlNode> childs = new HashMap<>();
	public Map<String, XmlAttribute> attributes = new HashMap<>();
	public Map<String, XmlAttribute> actualAttributes = new HashMap<>();
	
	public XmlNode registerChild(final String name, final String value, String namespace) {
		XmlNode child = null;
		if(childs.containsKey(name)) {
			child = childs.get(name);
			child.index++;
			child.count++;
			child.value = null;
		} else {
			child = new XmlNode();
			child.parent = this;
			child.name = name;			
			child.class_iri = valueFactory.createIRI(Xml2RdfConverter.X2RM, child.getPathString().substring(1));
			childs.put(name, child);
		}
		child.registerValue(value, false);
		child.actualAttributes.clear();
		child.iri = valueFactory.createIRI(namespace, UUID.randomUUID().toString());
		return child;
	}
	
	public XmlAttribute registerAttribute(final String name, final String value, String namespace) {
		XmlAttribute attribute = null;
		if(attributes.containsKey(name)) {
			attribute = attributes.get(name);
			attribute.index++;
			attribute.count++;
			attribute.value = null;
		} else {
			attribute = new XmlAttribute();
			attribute.parent = this;
			attribute.name = name;
			attribute.class_iri = valueFactory.createIRI(Xml2RdfConverter.X2RM, attribute.getPathString().substring(1));
			attributes.put(name, attribute);
		}
		attribute.registerValue(value, false);
		attribute.iri = valueFactory.createIRI(namespace, UUID.randomUUID().toString());
		actualAttributes.put(name, attribute);
		return attribute;
	}
	
	
	
	@Override
	String getType() {
		return XmlNode.class.getSimpleName();
	}
	
	@Override
	String getPathString() {
		return parent.getPathString() + "." + name;
	}

	@Override
	String getRelativeXPath() {
		return parent.getRelativeXPath() + "/" + name;
	}
	
	@Override
	String getAbsoluteXpath() {
		return parent.getAbsoluteXpath() + "/" + name + "[" + (index + 1) + "]";
	}
	
	boolean isRoot() {
		return false;
	}

}
