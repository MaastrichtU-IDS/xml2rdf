package nl.unimaas.ids.xml2rdf.model;

class RootNode extends XmlNode {
	
	public RootNode() {
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
	boolean isRoot() {
		return true;
	}

}
