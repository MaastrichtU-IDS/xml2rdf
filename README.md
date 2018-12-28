[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)


# About
This tool converts any xml file into a generic rdf model.
## RDF model
```shell
PREFIX x2rm: <http://ids.unimaas.nl/rdf2xml/model#>
PREFIX x2rd: <http://ids.unimaas.nl/rdf2xml/data/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl:  <http://www.w3.org/2002/07/owl#>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

insert data {
    # Base XmlElement
    x2rm:XmlElement a rdfs:type .
    # Attributes
    x2rm:hasName a rdf:Property ;
        rdfs:domain x2rm:XmlElement;
        rdfs:type rdfs:Literal .
    x2rm:hasValue a rdf:Property ;
        rdfs:domain x2rm:XmlElement;
        rdfs:type rdfs:Literal .
    x2rm:hasXPath a rdf:Property ;
        rdfs:domain x2rm:XmlElement;
        rdfs:type rdfs:Literal . 
        
    # XmlAttribue
    x2rm:XmlAttribute rdfs:subClassOf x2rm:XmlElement .
    
    # XmlNode
    x2rm:XmlNode rdfs:subClassOf x2rm:XmlElement .
    # Attributes
    x2rm:hasChild a rdf:Property ;
        rdfs:domain x2rm:XmlNode ;
        rdfs:range x2rm:XmlNode .
    x2rm:hasAttribute a rdf:Property ;
        rdfs:domain x2rm:XmlNode ;
        rdfs:range x2rm:XmlAttribute .
        
    # Inverse attributes 
    x2rm:isNameOf owl:inverseOf x2rm:hasName .
    x2rm:isValueOf owl:inverseOf x2rm:hasValue.
    x2rm:isXPathOf owl:inverseOf x2rm:hasXPath .
    x2rm:isChildOf owl:inverseOf x2rm:hasChild .
    x2rm:isAttributeOf owl:inverseOf x2rm:hasAttribute .
}
```
# Docker
## Build
```shell
docker build -t xml2rdf .
```
## Usage
```shell
docker run --rm -it xml2rdf -?
```
```
Usage: xml2rdf [-?] [-xp] -g=<graphUri> -i=<inputFilePath> [-n=<namespace>]
               -o=<outputFilePath>
  -?, --help         display a help message
  -g, --graphuri=<graphUri>
                     Graph URI
  -i, --inputfile=<inputFilePath>
                     Path to input file (.xml or .xml.gz). Wildcards are also
                       supported. (see: Apache Ant DirectoryScanner)
  -n, --namespace=<namespace>
                     Namespace for data nodes.
                       Default: http://ids.unimaas.nl/xml2rdf/data/
  -o, --outputfile=<outputFilePath>
                     Path to output file (.nq or .nq.gz)
      -xp, --xpath   Generate XPath paths in output
```
## Run

### Linux / OSX
```shell
docker run --rm -it -v /data/xml2rdfdata:/data xml2rdf  -i "/data/input.xml.gz" -o "/data/output.nq.gz" -g "http://kraken/xml2rdf/graph"
```
### Windows
```shell
docker run --rm -it -v c:/data/xml2rdfdata:/data xml2rdf  -i "/data/input.xml.gz" -o "/data/output.nq.gz" -g "http://kraken/xml2rdf/graph"
```
