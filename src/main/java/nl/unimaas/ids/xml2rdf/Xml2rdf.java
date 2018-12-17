package nl.unimaas.ids.xml2rdf;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import nl.unimaas.ids.xml2rdf.model.Xml2RdfConverter;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

public class Xml2rdf {
	
	public static void main(String[] args) {
		final Log log = LogFactory.getLog(Xml2rdf.class.getName());
		
		try {
			CliOptions cli = CommandLine.populateCommand(new CliOptions(), args);
			if(cli.help) 
				printUsageAndExit();
			
			if(!(cli.inputFilePath.endsWith(".xml") || cli.inputFilePath.endsWith(".xml.gz")))
				throw new IllegalArgumentException("Input file-name has to end with \".xml\" or \".xml.gz\"");
			
			if(!(cli.outputFilePath.endsWith(".nq") || cli.outputFilePath.endsWith(".nq.gz")))
				throw new IllegalArgumentException("Output file-name has to end with \".nt.gz\"");
			
			File inputFile = new File(cli.inputFilePath);
			if(!inputFile.exists())
				throw new IllegalArgumentException("Unable to find the input-file in the specified location \"" + inputFile.getAbsolutePath() + "\"");
			
			File outputFile = new File(cli.outputFilePath);
			if(outputFile.exists())
				log.warn("Outputfile already exists and will be overwritten");
			
			if(!new File(outputFile.getAbsolutePath()).getParentFile().canWrite())
				throw new IllegalArgumentException("Can not write to directory of output file.");
			
			new Xml2RdfConverter(inputFile, outputFile, cli.graphUri)
				.doWork()
				.structuredPrint();
		} catch (MissingParameterException | IllegalArgumentException e) {
			printUsageAndExit(e);
		} catch (XMLStreamException | UnsupportedRDFormatException | IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void printUsageAndExit() {
		printUsageAndExit(null);
	}
	
	private static void printUsageAndExit(Throwable e) {
		CommandLine.usage(new CliOptions(), System.err);
		if(e == null)
			System.exit(0);
		e.printStackTrace();
		System.exit(-1);
	}

}
