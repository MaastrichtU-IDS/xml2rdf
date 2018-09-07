package nl.unimaas.ids.xml2rdf;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "autodrill")
public class CliOptions {
	
	@Option(names = { "-?", "--help" }, usageHelp = true, description = "display a help message")
	boolean help = false;
	
	@Option(names= {"-i", "--inputfile"}, description = "Path to input file", required = true)
	String inputFilePath = null;
	
	@Option(names = {"-o", "--outputfile"}, description = "Path to output file", required = true )
	String outputFilePath = null;
	
	@Option(names = {"-g", "--graphuri"}, description = "Graph URI", required = true )
	String graphUri = null;

}
