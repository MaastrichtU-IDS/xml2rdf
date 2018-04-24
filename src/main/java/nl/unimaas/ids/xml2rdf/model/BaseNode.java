package nl.unimaas.ids.xml2rdf.model;

import java.text.DecimalFormat;

import org.eclipse.rdf4j.model.IRI;

abstract class BaseNode {
	
	private static final DecimalFormat percentFormat = new DecimalFormat("#.#%");
	
	public boolean isNew = true;
	
	public XmlNode parent = null;
	public String name = null;
	public String value = null;
	public long count = 1;
	public long index = 0;
	public long valueCount = 0;
	public long minLength = -1;
	public long maxLength = -1;
	
	public IRI class_iri = null;
	public IRI iri = null;
	
	public void registerValue(String value) {
		if(value!=null) {
			String val = value.trim();
			long length = val.trim().length();
			if(length > 0) {
				if(this.value==null) {
					this.value = val;
					valueCount++;
				} else {
					this.value += val;
					length = this.value.length();
				}
				minLength = minLength == -1 || length < minLength ? length : minLength;
				maxLength = length > maxLength ? length : maxLength;
			}
		}
	}
	
	abstract String getType();
	
	abstract String getRelativeXPath();
	
	abstract String getAbsoluteXpath();
	
	@Override
	public String toString() {
		return getType() 
				+ "{name: \"" + name + "\""
				+ ", count: " + count + " " +  toPercent(index, parent!=null ? parent.index : 0) + ""
				+ ", valueCount: " + valueCount + " " +  toPercent(valueCount, index)
				+ ", minLength: " + (minLength != -1 ? minLength : "N/A")
				+ ", maxLength: " + (maxLength != -1 ? maxLength : "N/A")
				+ ", xPath: \"" + getRelativeXPath() + "\""
				+ (parent != null ? ", parent.name: \"" + parent.name + "\"" : "")
				+ "}"; 
	}
	
	String toPercent(long x, long total) {
		if(total!=0)
			return "(" + percentFormat.format( x / (double)total) + ")";
		else
			return "";
	}
	
}
