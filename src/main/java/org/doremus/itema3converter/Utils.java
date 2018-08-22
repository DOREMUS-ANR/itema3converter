package org.doremus.itema3converter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {

  public static QuerySolution queryDoremus(ParameterizedSparqlString sparql) {
    QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.doremus.org/sparql", sparql.toString());
    ResultSet r = qexec.execSelect();
    if (!r.hasNext()) return null;
    return r.next();
  }

  public static RDFNode queryDoremus(ParameterizedSparqlString sparql, String var) {
    QuerySolution result = queryDoremus(sparql);
    if (result == null) return null;
    else return result.get(var);
  }

  public static Literal toSafeNumLiteral(String str) {
    if (str.matches("\\d+"))
      return ResourceFactory.createTypedLiteral(Integer.parseInt(str));
    else return ResourceFactory.createTypedLiteral(str);
  }

  public static boolean areQuotesBalanced(String[] parts) {
    return Arrays.stream(parts)
      .noneMatch(p -> (StringUtils.countMatches(p, "\"") % 2) != 0 ||
        (StringUtils.countMatches(p, "(") % 2) != (StringUtils.countMatches(p, ")") % 2));
  }

  public static String notEmptyString(String text) {
    if (text == null) return null;
    text = text.trim();
    if (text.isEmpty() || text.equals(".")) return null;
    else return text;
  }


  public static boolean startsLowerCase(String text) {
    String first = text.substring(0, 1);
    return first.matches("[a-z]");
  }

  public static String fixCase(String str) {
    return fixCase(str, false);
  }

  public static String fixCase(String str, boolean numberIncluded) {
    str = notEmptyString(str);
    if (str == null) return null;

    String test = str.replaceAll("[^\\w]", "")
      .replaceAll("(WoO|Kaul|Hob)", "");
    if (numberIncluded) test = test.replaceAll("\\d", "");

    if (!StringUtils.isAllUpperCase(test)) return str;

    return Arrays.stream(str.split(" "))
      .map(String::toLowerCase)
      .map(StringUtils::capitalize)
      .collect(Collectors.joining(" "));
  }

}
