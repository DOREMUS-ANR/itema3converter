package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class M42_PerformedExpressionCreation extends DoremusResource {

  public M42_PerformedExpressionCreation(Omu omu, F31_Performance parent) {
    super(omu);

    this.resource.addProperty(RDF.type, MUS.M42_Performed_Expression_Creation)
      .addProperty(CIDOC.P4_has_time_span, parent.getTimeSpan().asResource());

    addNote(omu.getNote());

    for (E53_Place p : parent.getPlaces())
      this.resource.addProperty(CIDOC.P7_took_place_at, p.asResource());

    // Performers
    int ipCount = 0;
    for (OmuPersonne op : OmuPersonne.byOmu(omu.getId())) {
      M31_ActorFunction af = op.professionID > 0 ?
        M31_ActorFunction.get(op.professionID) :
        M31_ActorFunction.getMorale(op.typeMoraleID);

      if ((af == null || !af.isInterprete()) && !op.hasInstrumentOrTessiture()) continue;

      try {
        M28_IndividualPerformance ip = new M28_IndividualPerformance(op, new URI(this.uri + "/" + ++ipCount));
        model.add(ip.getModel());
        this.resource.addProperty(CIDOC.P9_consists_of, ip.asResource());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
  }

  public M42_PerformedExpressionCreation setOrderNumber(int orderNumber) {
    if (orderNumber > 0)
      this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(orderNumber));
    return this;
  }
}
