package org.bksport.qa.connector;

import java.util.Iterator;

import org.bksport.qa.connector.KIMConnector;
import org.bksport.qa.util.ConfigUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;
import org.openrdf.query.BindingSet;

import static org.junit.Assert.*;

import com.ontotext.kim.client.corpora.KIMAnnotation;
import com.ontotext.kim.client.corpora.KIMAnnotationSet;
import com.ontotext.kim.client.corpora.KIMDocument;
import com.ontotext.kim.client.semanticrepository.ClosableIterator;

//@RunWith(JUnit4.class)
public class KIMConnectorTest {

  @Test
  public void test() {
    ConfigUtil.reload();
    KIMConnector connector = new KIMConnector("localhost", 1099);
    connector.connect();
    KIMAnnotationSet annSet = connector
        .annotate("Lionel Messi is best player in MU - Arsenal match.");
    Iterator<KIMAnnotation> annIterator = annSet.iterator();
    while (annIterator.hasNext()) {
      KIMAnnotation ann = annIterator.next();
      System.out.println(ann);
    }
    assertTrue(annSet != null && !annSet.isEmpty());
    KIMDocument doc = connector
        .create("Wayne Rooney is best player in MU - Arsenal match");
    assertTrue(doc != null);
    ClosableIterator<BindingSet> bdIterator1 = connector
        .execSelect("select ?s where{ ?s ?p ?v}");
    assertTrue(bdIterator1.hasNext());
    // while(bdIterator1.hasNext()){
    // BindingSet bSet = bdIterator1.next();
    // System.out.println(bSet.getValue("s"));
    // }
    ClosableIterator<BindingSet> bdIterator2 = connector
        .execSelectInf("select ?s where{ ?s ?p ?v filter regex(str(?s), \"http://bk.sport.owl#\")}");
    assertTrue(bdIterator2.hasNext());
    // while(bdIterator2.hasNext()){
    // BindingSet bSet = bdIterator2.next();
    // System.out.println(bSet);
    // }
    bdIterator2 = connector
        .execSelectInf("select ?s where{ ?s ?p ?v filter regex(str(?s), \"http://bk.sport.owl#\")}");
    assertTrue(bdIterator2.hasNext());
    // while(bdIterator2.hasNext()){
    // BindingSet bSet = bdIterator2.next();
    // System.out.println(bSet);
    // }

  }

}
