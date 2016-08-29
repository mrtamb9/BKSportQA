package org.bksport.qa;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.http.exception.AGHttpException;
import com.franz.agraph.repository.AGServer;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;

import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGTupleQuery;

public class LabelList {
  static private final String SERVER_URL    = "http://172.245.22.156:10035";
  static private final String CATALOG_ID    = "vtio-catalog";
  static private final String REPOSITORY_ID = "bksport-repository";
  static private final String USERNAME      = "bksport";
  static private final String PASSWORD      = "a@2a@2";
  static AGCatalog            agCatalog;

  private String[]            listString;
  private String[]            listUri;
  private long                count;

  public LabelList() {
    try {
      queryAgraph();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void queryAgraph() throws Exception {
    // org.apache.log4j.BasicConfigurator.configure(new NullAppender());
    org.apache.log4j.BasicConfigurator.configure();

    // PropertyConfigurator.configure("log4j-jms.properties");
    System.out.println("\nStarting example1().");

    AGServer server = new AGServer(SERVER_URL, USERNAME, PASSWORD);

    agCatalog = server.getRootCatalog();
    System.out.println("Available repositories in catalog "
        + (agCatalog.getCatalogName()));// + ": " + agCatalog.listRepositories()
    // System.out.println("Available catalogs: " + server.listCatalogs());
    try {
      agCatalog = server.getCatalog(CATALOG_ID);
      System.out.println("Available repositories in catalog "
          + (agCatalog.getCatalogName()) + ": " + agCatalog.listRepositories());
    } catch (AGHttpException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // agCatalog.deleteRepository(REPOSITORY_ID);
    AGRepository myRepository = agCatalog.createRepository(REPOSITORY_ID);
    myRepository.initialize();
    System.out.println("myRepositoryID: " + myRepository.getRepositoryID()
        + " || " + "myRepositoryURL: " + myRepository.getRepositoryURL());

    AGRepositoryConnection conn = myRepository.getConnection();
    System.out.println("Repository " + (myRepository.getRepositoryID())
        + " is up! It contains " + (conn.size()) + " statements.");

    // Truy Van
    try {
      String query = "select ?l ?s " + "where { "
          + "?s rdf:type owl:NamedIndividual." + "?s proton:mainLabel ?l."
          + "}";
      AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL,
          query);
      TupleQueryResult result = tupleQuery.evaluate();

      // Dem so luong ket qua truy van
      count = tupleQuery.count();

      listString = new String[(int) (count + 1)];
      listUri = new String[(int) (count + 1)];
      int dem2 = 0;

      // for (int i = 0; i < count; i++) {
      // listString[i] = new String("");
      // }
      try {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value l = bindingSet.getValue("l");
          // System.out.format("%s\n", l);
          listString[dem2] = l.toString();
          listUri[dem2] = bindingSet.getValue("s").stringValue();
          dem2++;
        }
        for (int i = 0; i < count; i++) {
          // System.out.println(listString[i]);
        }
      } finally {
        result.close();
      }
      // Just the count now. The count is done server-side,
      // and only the count is returned.

      conn.close();
    } catch (Exception e) {

    }
  }

  public String[] getListString() {
    return listString;
  }

  public String[] getListUri() {
    return listUri;
  }

  public int getCount() {
    int temp = (int) (count);
    return temp;
  }
}
