package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.files.Itema3File;
import org.doremus.ontology.CIDOC;

import java.net.URI;
import java.net.URISyntaxException;


public abstract class DoremusResource {
  String className;
  final String sourceDb = "rfi"; // radio france itema3
  protected Itema3File record;

  protected Model model;
  protected URI uri;
  protected Resource resource;
  protected String identifier;

  public DoremusResource() {
    // do nothing, enables customisation for child class
    this.model = ModelFactory.createDefaultModel();
    this.className = this.getClass().getSimpleName();
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

  protected void setUri(String uri) throws URISyntaxException {
    if (this.uri == null || uri.equals(this.uri.toString())) return;

    this.uri = new URI(uri);
    if (this.resource != null)
      this.resource = ResourceUtils.renameResource(this.resource, uri);
  }

  protected void regenerateResource() {
    // delete old one
    if (this.resource != null) this.resource.removeProperties();

    // generate the new one
    try {
      this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);
      this.resource = model.createResource(this.uri.toString())
        .addProperty(DC.identifier, this.identifier);
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

  protected void addNote(String text) {
    if (text == null) return;
    text = text.trim();
    if (text.isEmpty()) return;

    this.resource
      .addProperty(RDFS.comment, text, "fr")
      .addProperty(CIDOC.P3_has_note, text, "fr");
  }

  public URI getUri() {
    return uri;
  }

}
