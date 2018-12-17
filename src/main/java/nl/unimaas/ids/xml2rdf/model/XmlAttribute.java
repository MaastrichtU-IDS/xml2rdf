package nl.unimaas.ids.xml2rdf.model;

class XmlAttribute extends BaseNode {
	@Override
	String getType() {
		return XmlAttribute.class.getSimpleName();
	}
	
	@Override
	String getRelativeXPath() {
		return parent.getRelativeXPath() + "/@" + name;
	}
	
	@Override
	String getAbsoluteXpath() {
		return parent.getAbsoluteXpath() + "/@" + name;
	}

	@Override
	String getPathString() {
		return parent.getPathString() + ".@" + name;
	}

}
