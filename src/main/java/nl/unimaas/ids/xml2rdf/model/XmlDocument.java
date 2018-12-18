package nl.unimaas.ids.xml2rdf.model;

class XmlDocument extends XmlNode {
	
	public XmlDocument() {
		name = "XML Document";
	}
	
	@Override
	String getType() {
		return "RootNode";
	}
	
	@Override
	String getRelativeXPath() {
		return "";
	}
	
	@Override
	String getAbsoluteXpath() {
		return "";
	}
	
	@Override
	String getPathString() {
		return "";
	}
	
	@Override
	boolean isRoot() {
		return true;
	}

}
