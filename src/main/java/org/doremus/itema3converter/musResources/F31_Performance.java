package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Invite;
import org.doremus.itema3converter.files.Item;
import org.doremus.itema3converter.files.ItemThLieuGeo;
import org.doremus.itema3converter.files.MagContenu;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class F31_Performance extends DoremusResource {
  final static List<Integer> performanceProfession = Arrays.asList(13, 205, 2, 10, 7);
  private E52_TimeSpan timeSpan;
  private List<E53_Place> placesList;
  private F25_PerformancePlan plan;

  public F31_Performance(MagContenu mag, Item item) {
    super(mag);
    this.resource.addProperty(RDF.type, FRBROO.F31_Performance);

    this.plan = new F25_PerformancePlan(mag);
    F20_PerformanceWork work = new F20_PerformanceWork(item);

    this.resource.addProperty(FRBROO.R25_performed, this.plan.asResource());
    work.add(this.plan);

    this.model.add(this.plan.getModel()).add(work.getModel());

    // Performance: Date
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
//    System.out.println(item.getLabel());




    // Performance: comment
    for (String s : new String[]{item.getDescription(), item.getAnalyseDoc()}) {
      if (!"?".equals(s)) addNote(s);
    }

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

  public Resource getPlan() {
    return plan.asResource();
  }
}
