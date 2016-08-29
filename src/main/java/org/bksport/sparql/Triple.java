package org.bksport.sparql;

/**
 * 
 * @author congnh
 * 
 */
public class Triple {

  private Node subject;
  private Node predicate;
  private Node object;
  private String objectString;

  /**
   * Construct a Triple with predicate a
   * 
   * @param subject
   * @param object
   */
  public Triple(Node subject, Node object) {
    this(subject, IRI.RDF_TYPE_IRI, object);
  }

  public Triple(Node subject, Node predicate, Node object) {
    this.subject = subject;
    this.predicate = predicate;
    this.object = object;
  }
  
  public Triple(Node subject, Node predicate, String objectString) {
	    this.subject = subject;
	    this.predicate = predicate;
	    this.objectString = objectString;
	  }
  

  public Node getSubject() {
    return subject;
  }

  public Node getPredicate() {
    return predicate;
  }

  public Node getObject() {
    return object;
  }

  public String toString() {
		if (objectString != null) {
			return subject.toString() + " " + predicate.toString() + " " + objectString;
		} else if (subject == null || predicate == null || object == null) {
			System.out.println("ERRRRRRR: " + subject + "+++" + predicate + "+++" + object);
			return "";
		} else {
			return subject.toString() + " " + predicate.toString() + " " + object.toString();
		}
  }

}
