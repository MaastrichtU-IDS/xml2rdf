package nl.unimaas.ids.xml2rdf.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
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
	
	public void registerValue(String value, boolean append) {
		if(append && value!=null) {
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
		} else 
			this.value = value;
	}
	
	abstract String getType();
	
	abstract String getRelativeXPath();
	
	abstract String getAbsoluteXpath();
	
	@Override
	public String toString() {
		return getType() 
				+ "{name: \"" + name + "\""
				+ ", count: " + count + " " +  toPercent(count, parent!=null ? parent.count : 0) + ""
				+ ", valueCount: " + valueCount + " " +  toPercent(valueCount, count)
				+ ", minLength: " + (minLength != -1 ? minLength : "N/A")
				+ ", maxLength: " + (maxLength != -1 ? maxLength : "N/A")
				+ ", xPath: \"" + getRelativeXPath() + "\""
				+ (parent != null ? ", parent.name: \"" + parent.name + "\"" : "")
				+ "}"; 
	}
	
	// Generate file with template SPARQL mappings for the current node
	// public void generateSparqlTemplate(Map<String, XmlNode> childsMap, Map<String, XmlAttribute> attributesMap, String baseDir) {
	public void generateSparqlTemplate(XmlNode node, String baseDir) {
		try {
			// Generate SPARQL mapping template file
			PrintStream ps = new PrintStream(new FileOutputStream(new File(baseDir + "/" + this.getPathString().substring(1) + ".rq")));
			PrintWriter upper = new PrintWriter(ps);
			PrintWriter lower = new PrintWriter(ps);
			
			upper.println("PREFIX x2rm: <http://ids.unimaas.nl/xml2rdf/model#>");
			upper.println("PREFIX x2rd: <http://ids.unimaas.nl/xml2rdf/data/>");
			upper.println("PREFIX d2s: <https://w3id.org/data2services/vocab/>");
			upper.println("PREFIX ido: <http://identifiers.org/>");
			upper.println("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
			upper.println("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
			upper.println("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>");
			upper.println("PREFIX obo: <http://purl.obolibrary.org/obo/>");
			upper.println("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
			upper.println("PREFIX dcterms: <http://purl.org/dc/terms/>");
			upper.println("PREFIX bl: <https://w3id.org/biolink/vocab/>");
			upper.println("INSERT {");
			upper.println("  GRAPH <?_outputGraph> {");
			upper.println("    ?nodeUri a owl:Thing ;");

			lower.println("    .");
			lower.println("} WHERE {");
			lower.println("  SERVICE <?_serviceUrl>  {");
			lower.println("    GRAPH <?_inputGraph> {");
			lower.println("");
			lower.println("      ?" + node.name + "Node a x2rm:" + this.getPathString().substring(1) + " .");
			lower.println("      # Example for building URI using md5 string hashing");
			lower.println("      # BIND(iri(concat(\"https://identifiers.org/\", md5(?nodeId))) AS ?nodeUri)");
			lower.println("");
			
			// Map attributes
			for (String key : node.attributes.keySet()) {
				XmlAttribute attribute = node.attributes.get(key);
				// For expand:
//				String variableLabel = attribute.getPathString().substring(1).replaceAll("(\\.|-)", "_");
//				upper.println("      property ?" + variableLabel + " ;");
//				lower.println("      ?node x2rm:hasAttribute [");
//				lower.println("        a x2rm:" + attribute.getPathString().substring(1) + " ;");
//				lower.println("        x2rm:hasValue ?" + variableLabel);
//				lower.println("      ] .");
		        upper.println("      property ?" + attribute.name + " ;");
		        lower.println("      OPTIONAL{ ?" + node.name + "Node x2rm:attribute:" + attribute.name + " ?" + attribute.name + " . }");
				lower.println("");
			}
			
			// If Node have a direct value
			// TODO: error with embedded fieldslike <i> or <sup> in pubmed
			upper.println("      property ?" + node.name + "Value ;");
			lower.println("      # To get the value of the node, to remove if node only has children");
	        lower.println("      OPTIONAL{ ?" + node.name + "Node x2rm:hasValue ?" + node.name + "Value . }");
			
			// Map children (childs)
			for (String key : node.childs.keySet()) {
				XmlNode child = node.childs.get(key);
				upper.println("      property ?" + child.name + " ;");
				lower.println("      OPTIONAL{");
		        lower.println("        ?" + node.name + "Node x2rm:child:" + child.name + " [");
		        lower.println("          x2rm:hasValue ?" + child.name);
		        lower.println("        ] .");
		        lower.println("      }");
				// For expand:
//				String variableLabel = child.getPathString().substring(1).replaceAll("(\\.|-)", "_");
//				upper.println("      property ?" + variableLabel + " ;");
//				lower.println("      ?node x2rm:hasChild [");
//				lower.println("        a x2rm:" + child.getPathString().substring(1) + " ;");
//				lower.println("        x2rm:hasValue ?" + variableLabel);
//				lower.println("      ] .");
			}
			
			upper.println("  }");
			lower.println("    }");
			lower.println("  }");
			lower.println("}");
			
			upper.flush();
			lower.flush();
			ps.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Error creating the SPARQL template file: ");
			e.printStackTrace();
		}
	}
	
	String toPercent(long x, long total) {
		if(total!=0)
			return "(" + percentFormat.format( x / (double)total) + ")";
		else
			return "";
	}

	abstract String getPathString();
	
}
