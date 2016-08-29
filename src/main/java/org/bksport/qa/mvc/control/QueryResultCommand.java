package org.bksport.qa.mvc.control;

import java.util.ArrayList;
import java.util.List;

import org.bksport.qa.connector.AGraphConnector;
import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.mvc.model.ConfigProxy;
import org.bksport.sparql.AskQuery;
import org.bksport.sparql.Query;
import org.bksport.sparql.SelectQuery;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * 
 * @author congnh
 * 
 */
public class QueryResultCommand extends SimpleCommand implements Runnable {

  private Query query = null;

  @Override
  public void execute(INotification notification) {
    query = (Query) notification.getBody();
    Thread thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    sendNotification(ApplicationFacade.QUERYING_RESULT);
    ConfigProxy confProxy = (ConfigProxy) facade
        .retrieveProxy(ApplicationFacade.CONF_PROXY);
    AGraphConnector connector = new AGraphConnector(confProxy.getAGHost(),
        confProxy.getAGPort(), confProxy.getAGUsername(),
        confProxy.getAGPassword());
    connector.setCatalogID("vtio-catalog");
    connector.setRepositoryID("bksport-repository");
    connector.connect();
    connector.openCatalog();
    connector.openRepository();
    List<Object[]> results = new ArrayList<Object[]>();
    if (query != null) {
      if (query.getClass() == AskQuery.class) {
        results.add(new Object[] { "result" });
        results.add(new Object[] { connector.execAsk(query.toString()) });
      } else if (query.getClass() == SelectQuery.class) {
    	  System.out.println(query);
        ResultSet rs = connector.execSelectInf(query.toString());
        List<String> vars = rs.getResultVars();
        results.add(vars.toArray());
        while (rs.hasNext()) {
          QuerySolution sol = rs.nextSolution();
          Object[] r = new Object[vars.size()];
          for (int i = 0; i < vars.size(); i++) {
            r[i] = sol.get(vars.get(i)).toString();
          }
          results.add(r);
        }
      }
    }
    connector.disconnect();
    sendNotification(ApplicationFacade.RESULT_QUERIED, results);
  }

}
