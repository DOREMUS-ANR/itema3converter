package org.doremus.itema3converter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.*;
import org.doremus.itema3converter.musResources.*;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.ontology.PROV;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Convert entirely an entire single ITEM of ITEMA3, from performance to track
public class RecordConverter {
  public static Resource RadioFrance;
  //    public static final DateFormat ISODateTimeFormat = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss");
//    public static final DateFormat ISOTimeFormat = new SimpleDateFormat("hh:mm:ss");
  public static final DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static Logger log = MyLogger.getLogger(RecordConverter.class.getName());
  private Resource provEntity, provActivity;

  private Model model;


  public RecordConverter(File mc) throws URISyntaxException {
    log.setLevel(Level.OFF);

    model = ModelFactory.createDefaultModel();
    RadioFrance = model.createResource("http://data.doremus.org/organization/Radio_France");

    MagContenu mag = MagContenu.fromFile(mc);

    System.out.println("\n\nMAG_CONTENU " + mag.getId());

    Item item = Item.fromFile(getFile("ITEM", mag.getItemId()));
    assert item != null;
    log.info("ITEM " + item.getId());

    if (item.getStatus() != 4) {
      log.warning("Item with status " + item.getStatus() + " (expected 4). Skipping it");
      return;
    }

    // PROV-O tracing
    provEntity = model.createResource("http://data.doremus.org/source/itema3/" + mag.getItemId())
      .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, RadioFrance);

    provActivity = model.createResource(ConstructURI.build("rf", "prov", mag.getId()).toString())
      .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
      .addProperty(PROV.used, provEntity)
      .addProperty(RDFS.comment, "Reprise et conversion de la notice avec MAG_CONTENU_ID " + mag.getId() +
        " de la base ITEMA3 de Radio France", "fr")
      .addProperty(RDFS.comment, "Resumption and conversion of the record with MAG_CONTENU_ID " + mag.getId()
        + " of the dataset ITEMA3 of Radio France", "en")
      .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);


    // start parsing

    F31_Performance f31 = new F31_Performance(mag, item);
    addProvenanceTo(f31);

    List<M43_PerformedExpression> peList = new ArrayList<>();

    // Performed expression for this performance
    for (ItemOmu io : ItemOmu.byItem(item.getId())) {
      Omu omu = io.getOmu();

      M42_PerformedExpressionCreation pec = new M42_PerformedExpressionCreation(omu, f31);
      M43_PerformedExpression pe = new M43_PerformedExpression(omu);
      M44_PerformedWork pw = new M44_PerformedWork(omu);
      pec.setOrderNumber(io.omuOrderNum);

      F25_PerformancePlan pp = new F25_PerformancePlan(omu);
      F28_ExpressionCreation ppc = new F28_ExpressionCreation(omu, true);
      F20_PerformanceWork ppw = new F20_PerformanceWork(omu);

      f31.asResource()
        .addProperty(CIDOC.P9_consists_of, pec.asResource())
        .addProperty(FRBROO.R17_created, pp.asResource());
      pec.asResource()
        .addProperty(CIDOC.P9i_forms_part_of, f31.asResource())
        .addProperty(FRBROO.R19_created_a_realisation_of, pw.asResource())
        .addProperty(FRBROO.R17_created, pe.asResource());
      pw.asResource().addProperty(FRBROO.R9_is_realised_in, pe.asResource());
      ppc.asResource().addProperty(FRBROO.R19_created_a_realisation_of, ppw.asResource())
        .addProperty(FRBROO.R17_created, pp.asResource());

      peList.add(pe);

      // The work performed
      F15_ComplexWork f15 = new F15_ComplexWork(omu);
      F14_IndividualWork f14 = new F14_IndividualWork(omu);
      F28_ExpressionCreation f28 = new F28_ExpressionCreation(omu, false);
      F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(omu);

      f15.asResource()
        .addProperty(FRBROO.R10_has_member, f14.asResource())
        .addProperty(MUS.U38_has_descriptive_expression, f22.asResource())
        .addProperty(FRBROO.R3_is_realised_in, pe.asResource());
      f14.asResource()
        .addProperty(FRBROO.R9_is_realised_in, f22.asResource());
      f28.asResource()
        .addProperty(FRBROO.R19_created_a_realisation_of, f14.asResource())
        .addProperty(FRBROO.R17_created, f22.asResource());
      pe.asResource()
        .addProperty(MUS.U54_is_performed_expression_of, f22.asResource());
      f31.asResource()
        .addProperty(FRBROO.R66_included_performed_version_of, f22.asResource());

      model.add(pec.getModel());
      model.add(pe.getModel());
      model.add(pw.getModel());

      model.add(f15.getModel());
      model.add(f14.getModel());
      model.add(f28.getModel());
      model.add(f22.getModel());
    }

    // Recording
    F29_RecordingEvent re = new F29_RecordingEvent(item);
    for (E53_Place place : f31.getPlaces()) re.setPlace(place);
    re.setTimeSpan(f31.getTimeSpan());
    F21_RecordingWork rw = new F21_RecordingWork(item);
    F26_Recording rc = new F26_Recording(mag);

    re.asResource()
      .addProperty(FRBROO.R20_recorded, f31.asResource())
      .addProperty(FRBROO.R22_created_a_realisation_of, rw.asResource())
      .addProperty(FRBROO.R21_created, rc.asResource());
    rw.asResource()
      .addProperty(FRBROO.R13_is_realised_in, rc.asResource());

    model.add(re.getModel());
    model.add(rw.getModel());
    model.add(rc.getModel());

    // Editing
    M29_Editing ed = new M29_Editing(mag, item);
    M46_Set_of_Tracks set = new M46_Set_of_Tracks(mag);

    ed.asResource()
      .addProperty(MUS.U29_edited, rc.asResource())
      .addProperty(FRBROO.R17_created, set.asResource());

    for (M43_PerformedExpression pe : peList)
      set.asResource()
        .addProperty(MUS.U51_is_partial_or_full_recording_of, pe.asResource());

    for (MagSupport support : MagSupport.byMag(mag.getId())) {
      F4_ManifestationSingleton ms = new F4_ManifestationSingleton(support);

      ed.asResource()
        .addProperty(FRBROO.R18_created, ms.asResource());

      ms.asResource()
        .addProperty(CIDOC.P128_carries, rc.asResource())
        .addProperty(CIDOC.P165_incorporates, set.asResource());

      model.add(ms.getModel());
    }

    int trackNum = 0;
    List<M24_Track> tracks = new ArrayList<>();
    for (Sequence sequence : Sequence.byMag(mag.getId())) {
      if (sequence.getType() == Sequence.TYPE.AMBIENT) continue;

      M24_Track track = new M24_Track(sequence, ++trackNum);
      tracks.add(track);
      set.asResource().addProperty(FRBROO.R5_has_component, track.asResource());
    }

    // sort PerformedExpression
    Collections.sort(peList);

    // assing PerformedExpression to Track
    for (int i = 0; i < peList.size(); i++) {

      M43_PerformedExpression currentPe = peList.get(i);
      System.out.println(currentPe.title + " | " + currentPe.timecode);

      int threshold;
      if (i == peList.size() - 1) //last one
        threshold = 1000 * 60 * 60 * 24; // one day in millis
      else threshold = peList.get(i + 1).timecode; // else next timecode

      M24_Track t = tracks.get(0);
      int tc = t.timecode;
      while (tc < threshold) {
        t.asResource()
          .addProperty(MUS.U51_is_partial_or_full_recording_of, currentPe.asResource());
        model.add(t.getModel());
        tracks.remove(t);
        System.out.println("-- " + t.title+ " | " + t.timecode);

        if (tracks.isEmpty()) break;
        t = tracks.get(0);
        tc = t.timecode;
      }
    }

    model.add(ed.getModel());
    model.add(set.getModel());


    model.add(f31.getModel());

    log.info("\n");
  }

  private void addProvenanceTo(DoremusResource res) {
    res.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, model.createResource("http://data.doremus.org/organization/DOREMUS"))
      .addProperty(PROV.wasDerivedFrom, this.provEntity)
      .addProperty(PROV.wasGeneratedBy, this.provActivity);
  }


  public static File getFile(String type, String id) {
    return new File(Paths.get(Converter.dataFolderPath, type, id + ".xml").toString());
  }

  public static List<File> getFilesStartingWith(String type, String start) {
    List<File> fileList = new ArrayList<>();
    File dir = new File(Paths.get(Converter.dataFolderPath, type).toString());
    for (File file : dir.listFiles()) {
      if (file.getName().startsWith(start))
        fileList.add(file);
    }

    return fileList;
  }

  public Model getModel() {
    return model;
  }
}
