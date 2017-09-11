package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.files.*;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class F31_Performance extends DoremusResource {
  final static List<Integer> performanceProfession = Arrays.asList(13, 205, 2, 10, 7);
  private E52_TimeSpan timeSpan;
  private List<E53_Place> placesList;

  public F31_Performance(MagContenu mag, Item item) {
    super(mag);
    this.resource.addProperty(RDF.type, FRBROO.F31_Performance);

    // Performance: Date
    // TODO ask for end time (if any)
    Date start = mag.getDateEnreg();
    Date end = mag.getDateEnreg();


    try {
      timeSpan = new E52_TimeSpan(new URI(this.uri + "/interval"), start, end);
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      model.add(timeSpan.getModel());

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }


    // Performance: Place
    this.placesList = new ArrayList<>();
    for (ItemThLieuGeo t : ItemThLieuGeo.byItem(item.getId())) {
      E53_Place p = new E53_Place(t.getLieuGeoId());
      placesList.add(p);
      this.resource.addProperty(CIDOC.P7_took_place_at, p.asResource());
    }

    // Performance: Title
    this.resource.addProperty(RDFS.label, item.getLabel());
    this.resource.addProperty(CIDOC.P102_has_title, item.getLabel());
    System.out.println(item.getLabel());

    // Performance: General Topic
    List<ItemIdxFile> generalTopics = ItemIdxNc.byItem(item.getId());
    generalTopics.addAll(ItemIdxRencontre.byItem(item.getId()));
    for (ItemIdxFile t : generalTopics) {
      try {
        String uri = ConstructURI.build("rfi", "theme", t.getIdxId()).toString();

        Resource topic = model.createResource(uri)
          .addProperty(RDF.type, CIDOC.E1_CRM_Entity)
          .addProperty(DCTerms.identifier, t.getIdxId())
          .addProperty(RDFS.label, t.getIdxLabel());
        this.resource.addProperty(CIDOC.P129_is_about, topic);

      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    // Performance: Geo Topic
    // FIXME the property U22 is not yet in the ontology
//        for (ItemIdxGeo t : ItemIdxGeo.byOmu(item.getId())) {
//            E53_Place p = new E53_Place(t.getLieuGeoId());
//            this.resource.addProperty(MUS.U22_is_about_place, p.asResource());
//        }


    // Performance: Person + Corporate Body
    // create list of persons
    List<Resource> actors = ItemIdxPersonne.byItem(item.getId()).stream()
      .map(it -> new E21_Person(it.getIdxId()).asResource())
      .collect(Collectors.toList());

    // add corporate bodies
    actors.addAll(ItemIdxMorale.byItem(item.getId()).stream()
      .map(it -> new F11_Corporate_Body(it.getIdxId()).asResource())
      .collect(Collectors.toList()));


    // for (Resource x : actors) {
    // FIXME the property U21 is not yet in the ontology
//     this.resource.addProperty(MUS.U21_is_about_actor, x.asResource());
    // }

    // Performance: comment
    for (String s : new String[]{item.getDescription(), item.getAnalyseDoc()})
      addNote(s);

    // Performance: invited
    int invI = 0;
    for (Invite inv : Invite.byItem(item.getId())) {
      DoremusResource ip = null;
      try {
        if ((!inv.personneID.isEmpty() && performanceProfession.contains(inv.professionID))
          || inv.typeMoraleID > 0)
          ip = new M28_IndividualPerformance(inv, new URI(this.uri + "/" + ++invI));
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }

      if (ip != null) {
        this.model.add(ip.getModel());
        this.resource.addProperty(CIDOC.P9_consists_of, ip.asResource());
      }
    }

  }

  public List<E53_Place> getPlaces() {
    return placesList;
  }

  public E52_TimeSpan getTimeSpan() {
    return timeSpan;
  }
}
