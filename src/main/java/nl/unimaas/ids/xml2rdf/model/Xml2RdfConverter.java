package nl.unimaas.ids.xml2rdf.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class Xml2RdfConverter {
	
	private XmlNode rootNode = null;
	private XmlNode xmlNode = null;
	private File inputFile = null;
	private File outputFile = null;
	
	public Xml2RdfConverter(File inputFile, File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
		rootNode = new RootNode();
		xmlNode = rootNode;
	}
	
	public Xml2RdfConverter doWork() throws XMLStreamException, UnsupportedRDFormatException, IOException {
		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile, false));
		RDFWriter rdfWriter = Rio.createWriter(RDFFormat.NTRIPLES, gzipOutputStream);
		rdfWriter.startRDF();
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream(inputFile));
		
		String name = null;
		
		while(xmlStreamReader.hasNext()) {
			int event = xmlStreamReader.next();
			if(event==XMLStreamConstants.START_ELEMENT) {
				name = xmlStreamReader.getLocalName();
				xmlNode = xmlNode.registerChild(name, null);
				for(int i=0; i<xmlStreamReader.getAttributeCount(); i++) {
					xmlNode.registerAttribute(xmlStreamReader.getAttributeLocalName(i), xmlStreamReader.getAttributeValue(i));
				}
			} else if (event == XMLStreamConstants.CHARACTERS) {
				xmlNode.registerValue(xmlStreamReader.getText());
			} else if (event==XMLStreamConstants.END_ELEMENT) {
				xmlNode.toRdf(rdfWriter);
				xmlNode.childs.values().forEach(child -> child.index = 0);
				xmlNode = xmlNode.parent;
			}
		}
		
		xmlStreamReader.close();
		rdfWriter.endRDF();
		gzipOutputStream.close();
		
		return this;
	}
	
	public Xml2RdfConverter structuredPrint() {
		printStructureAndStats(rootNode, "" , "| ");
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
