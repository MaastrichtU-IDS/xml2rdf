package nl.unimaas.ids.xml2rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.DirectoryScanner;
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
				throw new IllegalArgumentException("Input file-name has to end with \".xml\" or \".xml.gz\". Wildcards can be used for files and directories e.g. \"/data/**/*.xml.gz\"");
			
			if(!(cli.outputFilePath.endsWith(".nq") || cli.outputFilePath.endsWith(".nq.gz")))
				throw new IllegalArgumentException("Output file-name has to end with \".nq\" or \".nq.gz\"");
			
			
			File outputFile = new File(cli.outputFilePath);
			File outputFileAbsolutePath = new File(outputFile.getAbsolutePath());
			if(outputFile.exists()) {
				log.warn("Outputfile already exists and will be overwritten");
			} else {
				// Create the file and its parents
				if (outputFileAbsolutePath.getParentFile() != null) {
					outputFileAbsolutePath.getParentFile().mkdirs();
				}
				outputFileAbsolutePath.createNewFile();
			}
			if(!outputFileAbsolutePath.getParentFile().canWrite())
				throw new IllegalArgumentException("Can not write to directory of output file.");
			
			OutputStream outputStream = new FileOutputStream(outputFile, false);
			if(outputFile.getName().endsWith(".gz"))
				outputStream = new GZIPOutputStream(outputStream);

			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setIncludes(new String[]{cli.inputFilePath.substring(cli.inputFilePath.startsWith("/") ? 1 : 0)});
			scanner.setBasedir(new File("/"));
			scanner.setCaseSensitive(false);
			scanner.scan();
			for(String inputFilePath : scanner.getIncludedFiles()) {
				InputStream inputStream = new FileInputStream(new File("/", inputFilePath));
				if(inputFilePath.toLowerCase().endsWith(".gz"))
					inputStream = new GZIPInputStream(inputStream);
				
				new Xml2RdfConverter(inputStream, outputStream, cli.graphUri, cli.namespace, cli.xpath)
					.doWork()
					.structuredPrint();
				
				inputStream.close();
			}
			outputStream.close();
			
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
