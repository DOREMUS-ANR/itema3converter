package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.Utils;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.MODS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F22_SelfContainedExpression extends DoremusResource {
  private static final String numRegexString = "(?: n(?:[°º.]|o\\.?) ?(\\d+))";
  private static final Pattern opusRegex = Pattern.compile(" op(?:us|[. ]) ?(?:posth )?(\\d+[a-z]*)" +
    numRegexString + "?", Pattern.CASE_INSENSITIVE);
  private static final Pattern wooPattern = Pattern.compile("woo ([0-9a-z]+)" + numRegexString + "?",
    Pattern.CASE_INSENSITIVE);

  private static final Pattern orderNumRegex = Pattern.compile(numRegexString, Pattern.CASE_INSENSITIVE);
  private static final Pattern livreRegex = Pattern.compile("Livre [0-9I]+", Pattern.CASE_INSENSITIVE);
  private static final Pattern keyRegex = Pattern.compile(" en ([^ ]+(?: (dièse|bémol))? (maj|min)(eur)?)",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern engKeyRegex = Pattern.compile(" in (.+ (maj|min)(or)?)", Pattern.CASE_INSENSITIVE);

  private static final Property MODSidProp = ResourceFactory.createProperty(MODS.uri, "identifier");


  private String motherWorkTitle;
  private boolean isMother;

  public F22_SelfContainedExpression(Omu omu, List<String> composers, String title) {
    super(omu);
    System.out.println(" op(?:us|[. ]) ?(?:posth )?(\\d+[a-z]*)" +
      numRegexString + "?");
    isMother = title != null;
    if (isMother) {
      this.identifier = "m" + omu.getId();
      regenerateResource();
    } else title = omu.getTitle();

    System.out.println(title);

    title = extractTokens(title, composers);

    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression)
      .addProperty(CIDOC.P102_has_title, title)
      .addProperty(RDFS.label, title);

    if (!isMother) return;

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


  public F22_SelfContainedExpression(Omu omu, List<String> composers) {
    this(omu, composers, null);
  }


  private String extractTokens(String text, List<String> composers) {
    String originalTitle = text;

    this.isMother = true;
    // movement
    String[] parts = originalTitle.split(": ", 2);
    if (parts.length > 1 && Utils.areQuotesBalanced(parts)) {
      // Trois mélodies : 1. La mer est plus belle que les cathédrales

      String movement = parts[1].trim();
      if (!movement.matches("\\d+")) {
        originalTitle = parts[0].trim();

        if (Utils.startsLowerCase(movement))
          movement = originalTitle + " | " + movement;

        setMotherWorkTitle(originalTitle);
        originalTitle = movement;
        this.isMother = false;
      }
    }

    // opus number
    Matcher opusMatch = opusRegex.matcher(text);
    if (opusMatch.find()) {
      String note = opusMatch.group(0);
      String opus = opusMatch.group(1);
      String opusSub = opusMatch.group(2);

      addOpus(note, opus, opusSub);
      text = text.replace(note, "");
    }
    text = text.replaceAll("op\\.? ?posth\\.?", "");

    // WoO number
    Matcher wooMatch = wooPattern.matcher(text);
    if (wooMatch.find()) {
      String note = wooMatch.group(0);
      String woo = wooMatch.group(1);
      String subWoo = wooMatch.group(2);
      addOpus(note, woo, subWoo);
      text = text.replace(note, "");
    }

    // catalogs
    for (String u : composers) {
      for (Resource res : VocabularyManager.getMODS("catalogue").bySubject(u)) {
        StmtIterator it = res.listProperties(MODSidProp);
        while (it.hasNext()) {
          String code = it.nextStatement().getString();
          Pattern catPattern = Pattern.compile(" " + code + "[ .] ?(\\d[0-9a-z]*)" + numRegexString + "?",
            Pattern.CASE_INSENSITIVE);
          Matcher catMatch = catPattern.matcher(text);
          if (catMatch.find()) {
            String note = catMatch.group(0);
            String catNum = catMatch.group(1).trim();
            addCatalogue(note, code, catNum, u);
            text = text.replace(note, "");
            break;
          }
        }
      }
    }

    // order number
    String orderNum = "";
    Matcher livreMatcher = livreRegex.matcher(text);
    if (livreMatcher.find()) {
      orderNum = livreMatcher.group(0);
      if (text.contains("Livre I et II"))
        orderNum = "Livre I et II";
      text = text.replace(orderNum, "");
    }
    Matcher orderNumMatch = orderNumRegex.matcher(text);
    if (orderNumMatch.find()) {
      orderNum = orderNumMatch.group(1);
      text = text.replace(orderNumMatch.group(0), "");
    }
    if (!orderNum.isEmpty())
      this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNum));


    // key
    String key = "";
    Matcher keyMatch = keyRegex.matcher(text);
    if (keyMatch.find()) {
      key = keyMatch.group(1).toLowerCase()
        .replaceAll("maj$", "majeur")
        .replaceAll("min$", "mineur");
      text = text.replace(keyMatch.group(0), "");
    } else {
      keyMatch = engKeyRegex.matcher(text);
      if (keyMatch.find()) {
        key = keyMatch.group(1).replace('-', ' ');
        text = text.replace(keyMatch.group(0), "");
      }
    }
    if (!key.isEmpty())
      this.resource.addProperty(MUS.U11_has_key, key);


    // extraits
    String _old = text.trim();
    text = removeExtrait(text);
    originalTitle = removeExtrait(originalTitle);
    boolean extrait = !text.equals(_old);


    // note
    Pattern apresRegex = Pattern.compile(", d'après .+");
    Matcher apresMatcher = apresRegex.matcher(text);
    if (apresMatcher.find()) {
      String note = apresMatcher.group(0).substring(1).trim();
      text = text.replace(note, "");
      originalTitle = originalTitle.replace(note, "");
      addNote(note);
    }

    // alternate title
    Pattern altRegex = Pattern.compile("\\(([^)]+)\\)");
    Matcher altMatch = altRegex.matcher(text);
    while (altMatch.find()) {
      String alternate = altMatch.group(1).trim();

      if (alternate.matches("\\d+")) continue;

      text = text.replace(altMatch.group(0), "").trim();
      originalTitle = originalTitle.replace(altMatch.group(0), "").trim();
      if (alternate.equalsIgnoreCase("bis")) continue;

      if (alternate.equalsIgnoreCase("instrumental") || alternate.startsWith("version ")) {
        this.addNote(alternate);
        continue;
      }
      if (alternate.equalsIgnoreCase("primo")) {
        this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(1));
        continue;
      }
      if (alternate.equalsIgnoreCase("secondo")) {
        this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(2));
        continue;
      }

      if (alternate.startsWith("or "))
        alternate = alternate.replace("or ", "");

      this.resource.addProperty(MUS.U68_has_variant_title, alternate);
    }

    // casting
    parts = text.split("pour ", 2);
    if (parts.length > 1
      && !parts[1].startsWith(" précéder")
      && !parts[1].startsWith(" quatuor")
      && !parts[1].startsWith(" le")) {
      text = parts[0].trim();

      String casting = parts[1].trim();
      for (String x : ":,dans,\"".split(","))
        if (casting.contains(":")) {
          String[] _parts = casting.split(x);
          text += " " + x + " " + _parts[1];
          casting = _parts[0].trim();
        }

      String castingUri = this.uri + "/casting/1";
      try {
        M6_Casting cast = new M6_Casting(castingUri, casting);
        this.resource.addProperty(MUS.U13_has_casting, cast.asResource());
        this.model.add(cast.getModel());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    // subtitle
    //    String subtitle = "";
    //    parts = text.split(", ", 2);
    //    if (parts.length > 1 && areQuotesBalanced(parts)) {
    //      text = parts[0].trim();
    //      subtitle = parts[1].trim();
    //    }


    originalTitle = originalTitle
      .replaceAll(" +", " ") // remove double spaces
      .replaceAll(" +,", ",") // compress space + comma
      .replaceAll(", ?$", "")  // remove last comma
      .trim();

    return originalTitle;
  }

  public void setMotherWorkTitle(String originalTitle) {
    this.motherWorkTitle = originalTitle.trim();
  }

  public String getMotherWorkTitle() {
    return this.motherWorkTitle;
  }

  public boolean foreseesMother() {
    return this.motherWorkTitle != null;
  }

  private static String removeExtrait(String text) {
    return text.replaceAll("(?i)\\(extraits?\\)", "")
      .replaceAll(": extraits?", "")
      .replaceAll("^Extraits de (la)?", "").trim();
  }

  private void addCatalogue(String note, String code, String num, String composer) {
    String label = (code != null) ? (code + " " + num) : note;

    Resource M1CatalogStatement =
      model.createResource(this.uri.toString() + "/catalog/" + label.replaceAll("[ /]", "_"))
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P3_has_note, note.trim());

    this.resource.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);

    if (null == num) {
      System.out.println("Not parsable catalog: " + note);
      // Should never happen normally
      return;
    }

    Resource match = VocabularyManager.getMODS("catalogue")
      .findModsResource(code, Collections.singletonList(composer));

    if (match == null)
      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, code);
    else M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, match);

    M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, num);
  }

  private void addOpus(String note, String number, String subnumber) {
    Property numProp = MUS.U42_has_opus_number,
      subProp = MUS.U17_has_opus_statement;

    if (note.substring(0, 3).equalsIgnoreCase("WoO")) {
      numProp = MUS.U69_has_WoO_number;
      subProp = MUS.U76_has_WoO_subnumber;
    }

    String id = number;
    if (subnumber != null) id += "-" + subnumber;

    Resource M2OpusStatement = model.createResource(this.uri + "/opus/" + id.replaceAll(" ", "_"))
      .addProperty(RDF.type, MUS.M2_Opus_Statement)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(RDFS.label, note)
      .addProperty(numProp, number);

    if (subnumber != null)
      M2OpusStatement.addProperty(subProp, subnumber);

    this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
  }


}
