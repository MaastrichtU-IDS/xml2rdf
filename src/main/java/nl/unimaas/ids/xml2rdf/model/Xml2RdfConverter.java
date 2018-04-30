package nl.unimaas.ids.xml2rdf.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
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
				xmlNode.toRdf(rdfWriter);
				// because it is already part of the child-map (for statistics)
				// index will be incremented immediately when registered
				xmlNode.childs.values().forEach(child -> {child.index = -1; child.value = null;});
				xmlNode.attributes.values().forEach(attribute -> {attribute.index = -1; attribute.value = null;});
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
