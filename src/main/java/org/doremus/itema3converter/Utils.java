package org.doremus.itema3converter;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

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

}
