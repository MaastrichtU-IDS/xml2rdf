[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)


# About
This tool converts xml files into a generic rdf structure.
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