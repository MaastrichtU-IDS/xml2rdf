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
	
	// TODO: generate template file with SPARQL mappings for this node
	public String generateSparqltemplate() {
		System.out.println(this.getPathString());
		
		// TODO: fix hard coded path. Take the directory of output file.
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(new File("/data/data2services/" + this.getPathString().substring(1))));
			
			PrintWriter sparqlWriter = new PrintWriter(ps);
			
			sparqlWriter.println("PREFIX x2rm: <http://ids.unimaas.nl/xml2rdf/model#>");
			sparqlWriter.println("PREFIX x2rd: <http://ids.unimaas.nl/xml2rdf/data/>");
			sparqlWriter.println("PREFIX d2s: <https://w3id.org/data2services/vocab/>");
			sparqlWriter.println("PREFIX ido: <http://identifiers.org/>");
			sparqlWriter.println("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
			sparqlWriter.println("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
			sparqlWriter.println("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>");
			sparqlWriter.println("PREFIX obo: <http://purl.obolibrary.org/obo/>");
			sparqlWriter.println("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
			sparqlWriter.println("PREFIX dcterms: <http://purl.org/dc/terms/>");
			sparqlWriter.println("PREFIX bl: <https://w3id.org/biolink/vocab/>");
			sparqlWriter.println("INSERT { ");
			sparqlWriter.println("  GRAPH <?_outputGraph> {   ");
			sparqlWriter.println("    ?node a owl:Thing .");
			sparqlWriter.println("  }");
			sparqlWriter.println("} WHERE {");
			sparqlWriter.println("  SERVICE <?_serviceUrl>  {");
			sparqlWriter.println("    GRAPH <?_inputGraph> {");
			sparqlWriter.println("      ?node a x2rm:" + this.getPathString().substring(1) + " . ");
			sparqlWriter.println("    }");
			sparqlWriter.println("  }");
			sparqlWriter.println("}");
			
			// Test with ?_serviceUrl = http://localhost:7200/repositories/test
			// ?_inputGraph = https://w3id.org/data2services/graph/xml2rdf/drugbank-sample
			// ?_outputGraph = http://xml2rdf/test
			
//			Get childs
//			x2rm:hasChild [ 
//			    a x2rm:drugbank.drug.carriers.carrier.id ; 
//			    x2rm:hasValue ?carrierId
//			] ;
			
			sparqlWriter.flush();
			ps.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Error creating the SPARQL template file: ");
			e.printStackTrace();
		}
		
		return null; 
	}
	
	String toPercent(long x, long total) {
		if(total!=0)
			return "(" + percentFormat.format( x / (double)total) + ")";
		else
			return "";
	}

	abstract String getPathString();
	
}
