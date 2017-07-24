package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.RecordConverter;
import org.doremus.itema3converter.files.Itema3File;

import java.net.URI;
import java.net.URISyntaxException;


public abstract class DoremusResource {
    protected String className;
    protected final String sourceDb=  "rfi"; // radio france itema3
    protected Itema3File record;

    protected Model model;
    protected URI uri;
    protected Resource resource;
    protected String identifier;
    protected Resource publisher;

    public DoremusResource() {
        // do nothing, enables customisation for child class
        this.model = ModelFactory.createDefaultModel();
        this.className = this.getClass().getSimpleName();
        this.publisher = RecordConverter.RadioFrance;
    }

    public DoremusResource(URI uri) {
        this();
        this.uri = uri;
        this.resource = model.createResource(this.uri.toString());
    }

    public DoremusResource(String identifier) {
        this();
        this.identifier = identifier;


        this.resource = null;
        /* create RDF resource */
        regenerateResource();
    }

    protected void regenerateResource() {
        // delete old one
        if (this.resource != null) this.resource.removeProperties();

        // generate the new one
        try {
            this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);
            this.resource = model.createResource(this.uri.toString())
                    .addProperty(DCTerms.identifier, this.identifier);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public DoremusResource(Itema3File record) {
        this(record.getId());
        this.record = record;
    }

    public DoremusResource(Itema3File record, String identifier) {
        this(identifier);
        this.record = record;
    }

    public Resource asResource() {
        return this.resource;
    }

    public Model getModel() {
        return this.model;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}