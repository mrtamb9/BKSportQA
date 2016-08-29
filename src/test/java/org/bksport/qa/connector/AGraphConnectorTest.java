package org.bksport.qa.connector;

import static org.junit.Assert.*;

import org.bksport.qa.connector.AGraphConnector;
import org.bksport.qa.util.ConfigUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;

import com.hp.hpl.jena.query.ResultSet;

//@RunWith(JUnit4.class)
public class AGraphConnectorTest {

  @Test
  public void test() {
    ConfigUtil.reload();
    AGraphConnector connector = new AGraphConnector("192.168.44.132", 10035,
        "congnh", "a@2a@2");
    connector.setCatalogID("bksport");
    connector.setRepositoryID("main-repository");
    connector.connect();
    connector.openCatalog();
    connector.openRepository();
    ResultSet rsInf = connector.execSelectInf("select ?c where {?c ?p ?v}");
    assertTrue(rsInf != null && rsInf.hasNext());
    int numRsInf = 0;
    while (rsInf.hasNext()) {
      numRsInf++;
      rsInf.next();
    }
    ResultSet rs = connector.execSelect("select ?c where {?c ?p ?v}");
    assertTrue(rs != null && rs.hasNext());
    int numRs = 0;
    while (rs.hasNext()) {
      numRs++;
      rs.next();
    }
    assertTrue(numRsInf > numRs);
    connector.closeRepository();
    connector.disconnect();
  }

}
