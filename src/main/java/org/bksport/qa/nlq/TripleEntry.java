package org.bksport.qa.nlq;

/**
 * 
 * @author congnh
 * 
 */
public class TripleEntry {

  private ResourceEntry subject;
  private ResourceEntry predicate;
  private ResourceEntry object;

  public void setSubject(ResourceEntry sub) {
    this.subject = sub;
  }

  public ResourceEntry getPredicate() {
    return predicate;
  }

  public void setPredicate(ResourceEntry predicate) {
    this.predicate = predicate;
  }

  public ResourceEntry getObject() {
    return object;
  }

  public void setObject(ResourceEntry object) {
    this.object = object;
  }

  public ResourceEntry getSubject() {
    return subject;
  }

}
