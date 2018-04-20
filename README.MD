[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)


# About
This tool converts xml files into a generic rdf model.
## RDF model
```
PREFIX x2rm: <http://ids.unimaas.nl/rdf2xml/model#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

insert data {
    x2rm:XmlElement a rdfs:type ;
        rdf:Property x2rm:hasName ;
        rdf:Property x2rm:hasValue ;
	rdf:Property x2rm:hasXPath .
		
    
    x2rm:XmlNode rdfs:subClassOf x2rm:XmlElement ;
        rdf:Property x2rm:hasChild ;
        rdf:Property x2rm:hasAttribute .
    
    x2rm:XmlAttribute rdfs:subClassOf x2rm:XmlElement .
    
    x2rm:isChildOf owl:inverseOf x2rm:hasChild .
    x2rm:isAttributeOf owl:inverseOf x2rm:hasAttribute .
	x2rm:isXPathOf owl:inverseOf x2rm:hasXPath .
}
```
# Docker
## Build
```
docker build -t xml2rdf .
```
## Run

### Linux / OSX
```
docker run --rm -it -v /data/xml2rdfdata/:/data/ xml2rdf "/data/input.xml" "/data/output.nt.gz"
```
### Windows
```
docker run --rm -it -v c:/data/xml2rdfdata/:/data/ xml2rdf "/data/input.xml" "/data/output.nt.gz"
```
