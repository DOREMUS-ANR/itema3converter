package org.doremus.itema3converter.musResources;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Morale;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class F11_Corporate_Body extends DoremusResource {
    private String name;
    private String birthDate, deathDate;

    public F11_Corporate_Body(Morale record) throws URISyntaxException, NullPointerException {
        super(record.getId());
        this.record = record;
        this.name = fixCase(record.getName());
        if (this.name == null) throw new NullPointerException("The name of a Corporate Body cannot be null");
        this.birthDate = record.getBirthDate();
        this.deathDate = record.getDeathDate();

        initResource();
    }

    public F11_Corporate_Body(String id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    private String fixCase(String str) {
        if (str == null) return null;
        str = str.trim();
        if (str.isEmpty()) return null;
        if (!StringUtils.isAllUpperCase(str.replaceAll("[^\\w]", ""))) return str;

        return Arrays.stream(str.toLowerCase().split(" "))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    private Resource initResource() {
        Morale r = (Morale) record;
        resource.addProperty(RDF.type, FRBROO.F11_Corporate_Body)
                .addProperty(RDFS.label, this.name)
                .addProperty(CIDOC.P131_is_identified_by, this.name);

        if (birthDate != null) {
            this.resource.addProperty(CIDOC.P95i_was_formed_by,
                    model.createResource(this.uri + "/formation")
                            .addProperty(RDF.type, CIDOC.E66_Formation)
                            .addProperty(CIDOC.P4_has_time_span, toTimeSpan(birthDate, this.uri + "/formation/time"))
            );
        }
        if (deathDate != null) {
            this.resource.addProperty(CIDOC.P99i_was_dissolved_by,
                    model.createResource(this.uri + "/dissolution")
                            .addProperty(RDF.type, CIDOC.E68_Dissolution)
                            .addProperty(CIDOC.P4_has_time_span, toTimeSpan(deathDate, this.uri + "/dissolution/time"))
            );
        }

        // TODO add type with this?
        //  a) F11 Corporate Body P14i performed F51 Pursuit P2 has type E55 Type
        //  b) F11 Corporate Body P14i performed F51 Pursuit R59 had typical subject E1 CRM Entity

        String comment = r.getComment().trim();
        if (!comment.isEmpty() && !comment.equals(this.name))
            this.resource.addProperty(CIDOC.P3_has_note, comment, "fr");
        return resource;
    }

    private Resource toTimeSpan(String date, String uri) {
        date = date
                .replace("Janvier ", "01/")
                .replace("Mai ", "05/")
                .replace("Septembre ", "09/")
                .trim();

        Literal d = null, dEnd = null;
        String note = null;
        if (date.matches("\\d{4}")) { // 1937
            d = model.createTypedLiteral(date, XSDDatatype.XSDgYear);
            dEnd = d;
        } else if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {// 05/12/2000
            String[] parts = date.split("/");
            Collections.reverse(Arrays.asList(parts));
            d = model.createTypedLiteral(String.join("-", parts), XSDDatatype.XSDdate);
            dEnd = d;
        } else if (date.matches("\\d{2}/\\d{4}")) {// 01/1945
            String[] parts = date.split("/");
            Collections.reverse(Arrays.asList(parts));
            d = model.createTypedLiteral(String.join("-", parts), XSDDatatype.XSDgMonth);
            dEnd = d;
        } else if (date.matches("\\d{8}")) { // 19951123
            String[] parts = new String[]{
                    date.substring(0, 4),
                    date.substring(4, 6),
                    date.substring(6)};
            d = model.createTypedLiteral(String.join("-", parts), XSDDatatype.XSDdate);
            dEnd = d;
        } else if (date.startsWith("ann\u00e9es ")) {
            note = date;
            String year = date.replace("ann\u00e9es ", "");
            String startYear = null, endYear = null;
            if (year.length() == 2) {
                startYear = 19 + year;
                endYear = "19" + year.charAt(0) + "9";
            } else if (year.length() == 3) {
                startYear = year + 0;
                endYear = year + 9;
            }
            if (startYear != null) {
                d = model.createTypedLiteral(startYear, XSDDatatype.XSDgYear);
                dEnd = model.createTypedLiteral(endYear, XSDDatatype.XSDgYear);
            }
        } else if (date.contains("-") || date.contains("/")) {
            note = date;
            String[] parts = (date + " ").split("[-/]");
            String startYear = parts[0].trim(),
                    endYear = parts[1].trim();

            if (startYear.length() == 3) startYear += "0";
            if (startYear.length() != 0) d = model.createTypedLiteral(startYear, XSDDatatype.XSDgYear);

            if (endYear.length() == 3) endYear += "9";
            if (endYear.length() != 0) dEnd = model.createTypedLiteral(endYear, XSDDatatype.XSDgYear);
        } else
            note = date;

        Resource ts = model.createResource(uri)
                .addProperty(RDF.type, CIDOC.E52_Time_Span);
        if (d != null) ts.addProperty(CIDOC.P79_beginning_is_qualified_by, d);
        if (dEnd != null) ts.addProperty(CIDOC.P80_end_is_qualified_by, dEnd);
        if (note != null) ts.addProperty(CIDOC.P3_has_note, note);
        return ts;
    }

}