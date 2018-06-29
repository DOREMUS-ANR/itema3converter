package org.doremus.itema3converter.musResources;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.MODS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F22_SelfContainedExpression extends DoremusResource {
  private static final String numRegexString = "(?: n(?:[°º]|o\\.?) ?(\\d+))";
  private static final Pattern opusRegex = Pattern.compile(" op(?:us|[. ]) ?(?:posth )?(\\d+[a-z]*)" +
      numRegexString + "?",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern orderNumRegex = Pattern.compile(numRegexString, Pattern.CASE_INSENSITIVE);
  private static final Pattern livreRegex = Pattern.compile("Livre [0-9I]+", Pattern.CASE_INSENSITIVE);
  private static final Pattern keyRegex = Pattern.compile(" en ([^ ]+(?: (dièse|bémol))? (maj|min)(eur)?)", Pattern
    .CASE_INSENSITIVE);
  private static final Pattern engKeyRegex = Pattern.compile(" in (.+ (maj|min)(or)?)", Pattern.CASE_INSENSITIVE);
  private Property MODSidProp;

  public F22_SelfContainedExpression(Omu omu, List<String> composers) {
    super(omu);

    MODSidProp = model.createProperty(MODS.uri, "identifier");
    String title = omu.getTitle();
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression)
      .addProperty(CIDOC.P102_has_title, title)
      .addProperty(RDFS.label, title);

//    title = extractTokens(title, composers);

    // genre
    for (OmuTypeMusicalDoc ot : OmuTypeMusicalDoc.byOmu(omu.getId())) {
      switch (ot.getType()) {
        case "period":
          continue;
        case "categorization":
          Resource style = model.createResource()
            .addProperty(RDF.type, MUS.M19_Style)
            .addProperty(RDFS.label, ot.getLabel());
          this.resource.addProperty(MUS.U19_has_style, style);
          // I do not break because categorizations are also genres
        case "genre":
          this.resource.addProperty(MUS.U12_has_genre, model.createResource(ot.getUri()));
          break;

        case "geocontext":
          Resource geoctx = model.createResource()
            .addProperty(RDF.type, MUS.M40_Context)
            .addProperty(RDFS.label, ot.getLabel());
          this.resource.addProperty(MUS.U65_has_geographical_context, geoctx);
          break;
      }
    }

    // note
    addNote(omu.workNote);
    addNote(omu.getNote(this.className));
  }

  private String extractTokens(String title, List<String> composers) {
//    if(!"Sonate en trio en ré mineur op 1 n°12 RV 63 P 1 n°12".equals(title)) return "";

    String origTitle = title;

    // opus number
    String opus = "", opusSub = "";
    Matcher opusMatch = opusRegex.matcher(title);
    if (opusMatch.find()) {
      opus = opusMatch.group(1);
      opusSub = opusMatch.group(2);
      title = title.replace(opusMatch.group(0), "");
    }
    title = title.replaceAll("op\\.? ?posth\\.?", "");

    // WoO number
    String woo = "", subWoo = "";
    Pattern wooPattern = Pattern.compile("woo (\\d[0-9a-z])*" + numRegexString + "?", Pattern.CASE_INSENSITIVE);
    Matcher wooMatch = wooPattern.matcher(title);
    if (wooMatch.find()) {
      woo = wooMatch.group(1);
      subWoo = wooMatch.group(2);
      title = title.replace(wooMatch.group(0), "");
    }


    // catalogs
    String catLabel = "", catNum = "";
    for (String u : composers) {
      for (Resource res : VocabularyManager.getMODS("catalogue").bySubject(u)) {
        StmtIterator it = res.listProperties(MODSidProp);
        while (it.hasNext()) {
          String code = it.nextStatement().getString();
          Pattern catPattern = Pattern.compile(" " + code + "[ .] ?(\\d[0-9a-z]*)" + numRegexString + "?",
            Pattern.CASE_INSENSITIVE);
          Matcher catMatch = catPattern.matcher(title);
          if (catMatch.find()) {
            catLabel = code;
            catNum = catMatch.group(1).trim();
            title = title.replace(catMatch.group(0), "");
            break;
          }
        }
      }
    }

    // order number
    String orderNum = "";
    Matcher livreMatcher = livreRegex.matcher(title);
    if (livreMatcher.find()) {
      orderNum = livreMatcher.group(0);
      title = title.replace(livreMatcher.group(0), "");
    }
    Matcher orderNumMatch = orderNumRegex.matcher(title);
    if (orderNumMatch.find()) {
      orderNum = orderNumMatch.group(1);
      title = title.replace(orderNumMatch.group(0), "");
    }

    // key
    String key = "";
    Matcher keyMatch = keyRegex.matcher(title);
    if (keyMatch.find()) {
      key = keyMatch.group(1).toLowerCase()
        .replaceAll("maj$", "majeur")
        .replaceAll("min$", "mineur");
      title = title.replace(keyMatch.group(0), "");
    } else {
      keyMatch = engKeyRegex.matcher(title);
      if (keyMatch.find()) {
        key = keyMatch.group(1).replace('-', ' ');
        title = title.replace(keyMatch.group(0), "");
      }
    }

    // extraits
    String _old = title.trim();
    title = title.replaceAll("\\(extraits?\\)", "")
      .replaceAll(": extraits?", "").trim();
    boolean extrait = !title.equals(_old);

    // alternate title
    String alternate = "";
    Pattern altRegex = Pattern.compile("\\(([^)]+)\\)");
    Matcher altMatch = altRegex.matcher(title);
    while (altMatch.find()) {
      alternate = altMatch.group(1).trim();
      title = title.replace(altMatch.group(0), "").trim();
    }

    // casting
    String casting = "";
    String[] parts = title.split("pour", 2);
    if (parts.length > 1
      && !parts[1].startsWith(" précéder")
      && !parts[1].startsWith(" quatuor")
      && !parts[1].startsWith(" les fous")
      ) {
      title = parts[0].trim();
      casting = "pour " + parts[1].trim();
      if (casting.contains(":")) {
        String[] _parts = casting.split(":");
        title = title + " : " + _parts[1];
        casting = _parts[0].trim();
      }
    }

    // movement
    String movement = "";
    parts = title.split(": ", 2);
    if (parts.length > 1 && areQuotesBalanced(parts)) {
      title = parts[0].trim();
      movement = parts[1].trim();
    }

    // subtitle
    String subtitle = "";
    parts = title.split(", ", 2);
    if (parts.length > 1 && areQuotesBalanced(parts)) {
      title = parts[0].trim();
      subtitle = parts[1].trim();
    }


    title = title.replaceAll(", ?$", "").trim();

    String[] toPrint = new String[]{origTitle.replaceAll(";", "-"), catLabel, catNum, woo, opus, opusSub, orderNum,
      key,
      movement.replaceAll(";", "-"), casting.replaceAll(";", "-"),
      subtitle.replaceAll(";", "-"), String.valueOf(extrait), alternate.replaceAll(";", "-"), title.replaceAll(";", "-")};
    System.out.println(String.join(";", toPrint));
    return title;
  }

  private boolean areQuotesBalanced(String[] parts) {
    return Arrays.stream(parts)
      .noneMatch(p -> (StringUtils.countMatches(p, "\"") % 2) != 0 ||
        (StringUtils.countMatches(p, "(") % 2) != (StringUtils.countMatches(p, "(") % 2));
  }

}
