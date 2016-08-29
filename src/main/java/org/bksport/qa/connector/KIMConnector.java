package org.bksport.qa.connector;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;

import com.ontotext.kim.client.GetService;
import com.ontotext.kim.client.KIMService;
import com.ontotext.kim.client.coredb.CoreDbAPI;
import com.ontotext.kim.client.corpora.CorporaAPI;
import com.ontotext.kim.client.corpora.KIMAnnotationSet;
import com.ontotext.kim.client.corpora.KIMCorporaException;
import com.ontotext.kim.client.corpora.KIMDocument;
import com.ontotext.kim.client.entity.EntityAPI;
import com.ontotext.kim.client.query.KIMQueryException;
import com.ontotext.kim.client.query.QueryAPI;
import com.ontotext.kim.client.query.SemanticQuery;
import com.ontotext.kim.client.semanticannotation.SemanticAnnotationAPI;
import com.ontotext.kim.client.semanticrepository.ClosableIterator;
import com.ontotext.kim.client.semanticrepository.SemanticRepositoryAPI;

/**
 * 
 * Connector to KIM's service
 * 
 * @author congnh
 * 
 */
public class KIMConnector {

  private String                host;
  private int                   port;
  private KIMService            kimService;
  private CorporaAPI            corporaAPI;
  private CoreDbAPI             coreDbAPI;
  private EntityAPI             entityAPI;
  private SemanticAnnotationAPI semAnnAPI;
  private SemanticRepositoryAPI semRepoAPI;
  private QueryAPI              queryAPI;

  public KIMConnector() {
    host = null;
    port = -1;
  }

  public KIMConnector(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void connect() {
    if (host != null && port != -1) {
      try {
        kimService = GetService.from(host, port);
        corporaAPI = kimService.getCorporaAPI();
        coreDbAPI = (CoreDbAPI) kimService.getCoreDbAPI();
        entityAPI = kimService.getEntityAPI();
        queryAPI = kimService.getQueryAPI();
        semAnnAPI = kimService.getSemanticAnnotationAPI();
        semRepoAPI = kimService.getSemanticRepositoryAPI();
      } catch (RemoteException ex) {
        Logger.getLogger(KIMConnector.class).error(
            ExceptionUtils.getStackTrace(ex));
      } catch (NotBoundException ex) {
        Logger.getLogger(KIMConnector.class).error(
            ExceptionUtils.getStackTrace(ex));
      }
    } else {
      Logger.getLogger(KIMConnector.class).warn("Invalid host or port!");
    }
  }

  public void disconnect() {
    throw new UnsupportedOperationException();
  }

  public KIMAnnotationSet annotate(String text) {
    if (semAnnAPI != null && text != null) {
      try {
        KIMAnnotationSet kimAnnSet = semAnnAPI.execute(text);
        return kimAnnSet;
      } catch (RemoteException ex) {
        Logger.getLogger(KIMConnector.class).error(
            ExceptionUtils.getStackTrace(ex));
      }
    } else {
      Logger.getLogger(KIMConnector.class).warn(
          "Invalid semantic annotation api or text");
    }
    return null;
  }

  public KIMDocument create(String text) {
    if (corporaAPI != null && text != null) {
      try {
        return corporaAPI.createDocument(text, false);
      } catch (KIMCorporaException ex) {
        Logger.getLogger(KIMConnector.class).error(
            ExceptionUtils.getStackTrace(ex));
      }
    } else {

    }
    return null;
  }

  public boolean execAsk(String query) {
    try {
      ClosableIterator<BindingSet> iterator = semRepoAPI.evaluateQuery(query,
          "SPARQL");
      while (iterator.hasNext()) {
        System.out.println(iterator.next().toString());
      }
      return true;
    } catch (KIMQueryException ex) {
      Logger.getLogger(KIMConnector.class).error(
          ExceptionUtils.getStackTrace(ex));
    }
    return false;
  }

  public ClosableIterator<BindingSet> execSelect(String query) {
    try {
      ClosableIterator<BindingSet> iterator = semRepoAPI.evaluateQuery(query,
          "SPARQL");
      return iterator;
    } catch (KIMQueryException ex) {
      Logger.getLogger(KIMConnector.class).error(
          ExceptionUtils.getStackTrace(ex));
    }
    return null;
  }

  public ClosableIterator<BindingSet> execSelectInf(String query) {
    try {
      ClosableIterator<BindingSet> iterator = semRepoAPI.evaluateQuery(query,
          "SPARQL", true);
      return iterator;
    } catch (KIMQueryException ex) {
      Logger.getLogger(KIMConnector.class).error(
          ExceptionUtils.getStackTrace(ex));
    }
    return null;
  }

  public void queryDocument(SemanticQuery semanticQuery) {
    if (queryAPI != null) {
      try {
        queryAPI.getDocumentIds(semanticQuery);
      } catch (KIMQueryException ex) {
        Logger.getLogger(KIMConnector.class).error(
            ExceptionUtils.getStackTrace(ex));
      }
    }
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void shutdownServer() {
    if (kimService != null)
      kimService.shutdown();
  }

}
