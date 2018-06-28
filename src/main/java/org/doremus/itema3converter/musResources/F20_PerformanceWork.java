package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.files.*;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class F20_PerformanceWork extends DoremusResource {

  public F20_PerformanceWork(Omu omu) {
    super(omu);
    this.resource.addProperty(RDF.type, FRBROO.F20_Performance_Work);
  }

  public F20_PerformanceWork(Item item) {
    super(item);

    // Performance: General Topic
    List<ItemIdxFile> generalTopics = ItemIdxNc.byItem(item.getId());
    generalTopics.addAll(ItemIdxRencontre.byItem(item.getId()));
    for (ItemIdxFile t : generalTopics) {
      try {
        String uri = ConstructURI.build("rfi", "theme", t.getIdxId()).toString();

        Resource topic = model.createResource(uri)
          .addProperty(RDF.type, CIDOC.E1_CRM_Entity)
          .addProperty(DC.identifier, t.getIdxId())
          .addProperty(RDFS.label, t.getIdxLabel());
        this.resource.addProperty(CIDOC.P129_is_about, topic);

      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    // Performance: Geo Topic
    for (ItemIdxGeo t : ItemIdxGeo.byItem(item.getId())) {
      E53_Place p = new E53_Place(t.getLieuGeoId());
      this.resource.addProperty(MUS.U22_is_about_place, p.asResource());
    }

    // Performance: Person + Corporate Body
    // create list of persons
    List<Resource> actors = ItemIdxPersonne.byItem(item.getId()).stream()
      .map(it -> new E21_Person(it.getIdxId()).asResource())
      .collect(Collectors.toList());

    // add corporate bodies
    actors.addAll(ItemIdxMorale.byItem(item.getId()).stream()
      .map(it -> new F11_Corporate_Body(it.getIdxId()).asResource())
      .collect(Collectors.toList()));


    for (Resource x : actors)
      this.resource.addProperty(MUS.U21_is_about_actor, x.asResource());
  }

  public void add(F25_PerformancePlan plan) {
    this.asResource().addProperty(FRBROO.R9_is_realised_in, plan.asResource());
  }
}
