package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.Utils;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M6_Casting extends DoremusResource {
  private final static Pattern NUM_REGEX = Pattern.compile("^(\\d+) ");

  public M6_Casting(String uri, String note) throws URISyntaxException {
    super(new URI(uri));

    this.resource.addProperty(RDF.type, MUS.M6_Casting);
    addNote(note);

    int i = 0;
    for (String part : note.split("(,|et|avec)")) {
      String detUri = this.uri + "/detail/" + ++i;

      part = part.trim();

      Resource r = model.createResource(detUri)
        .addProperty(RDF.type, MUS.M23_Casting_Detail)
        .addProperty(RDFS.label, part);

      part = part.replaceAll("à \\d mains", "")
        .replaceAll("a cappella", "")
        .trim();

      if ("solistes".equals(part))
        part = "voix";

      if (part.contains("seul") || part.contains("solo")) {
        r.addProperty(MUS.U36_foresees_responsibility, "soliste", "fr");
        part = part.replaceAll("(seul|solo)", "").trim();
      }

      Matcher m = NUM_REGEX.matcher(part);
      if (m.find()) {
        r.addProperty(MUS.U30_foresees_quantity_of_mop, Utils.toSafeNumLiteral(m.group(1)));
        part = part.replace(m.group(0), "");
      }

      r.addProperty(MUS.U2_foresees_use_of_medium_of_performance,
        model.createResource(detUri + "/mop")
          .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
          .addProperty(RDFS.label, part)
          .addProperty(CIDOC.P1_is_identified_by, part)
      );
      this.resource.addProperty(MUS.U23_has_casting_detail, r);
    }

  }

}

