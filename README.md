Itema3Converter
===============

Convert the XML files of the ITEMA3 database by Radio France in RDF following the [DOREMUS model][1].

## How to run:

1. Setup the `config.properties` file as you need.
2. Run `gradle run` in the project folder.

### Commands

    gradle run                  ## start conversion of files
    gradle schemagen            ## align ontology packages to ontologies in `doremus-ontology` github project
[comment]: # (   gradle updateVocabularies   ## sync the vocabularies with the knowledge-base repo )

[1]: https://drive.google.com/file/d/0B_nxZpGQv9GKZmpKRGl2dmRENGc/view