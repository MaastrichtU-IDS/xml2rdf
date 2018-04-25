package nl.unimaas.ids.xml2rdf;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import nl.unimaas.ids.xml2rdf.model.Xml2RdfConverter;

public class Xml2rdf {
	
	public static void main(String[] args) {
		try {
			if(args.length != 2)
				throw new IllegalArgumentException("Expecting exactly 2 arguments.");
			
			if(!args[1].toLowerCase().endsWith(".xml"))
				throw new IllegalArgumentException("Input file-name has to end with \".xml\" or \".xml.gz\"");
			
			if(!args[1].toLowerCase().endsWith(".nt.gz"))
				throw new IllegalArgumentException("Output file-name has to end with \".nt.gz\"");
			
			File inputFile = new File(args[0]);
			if(!inputFile.exists())
				throw new IllegalArgumentException("Input-File does not exist");
			
			File outputFile = new File(args[1]);
			if(outputFile.exists())
				System.out.println("WARNING: Outputfile already exists and will be overwritten");
			
			if(!outputFile.getParentFile().canWrite())
				throw new IllegalArgumentException("Can not write to directory of output file.");
			
			new Xml2RdfConverter(inputFile, outputFile)
				.doWork()
				.structuredPrint();
		} catch (IllegalArgumentException | XMLStreamException | UnsupportedRDFormatException | IOException e) {
			e.printStackTrace();
			usage();
		}
		
	}
	
	static void usage() {
		// TODO
	}

}
