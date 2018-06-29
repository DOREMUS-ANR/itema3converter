package org.doremus.itema3converter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Arrays;

public class Utils {

  public static QuerySolution queryDoremus(String sparql) {
    Query query = QueryFactory.create();
    try {
      QueryFactory.parse(query, sparql, "", Syntax.syntaxSPARQL_11);
      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.doremus.org/sparql", query);
      ResultSet r = qexec.execSelect();
      if (!r.hasNext()) return null;
      return r.next();

    } catch (QueryParseException e) {
      System.out.println(query);
      e.printStackTrace();
      return null;
    }
  }

  public static RDFNode queryDoremus(String sparql, String var) {
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
        (StringUtils.countMatches(p, "(") % 2) != (StringUtils.countMatches(p, "(") % 2));
  }


}
