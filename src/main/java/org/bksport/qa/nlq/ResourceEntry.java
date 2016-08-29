package org.bksport.qa.nlq;

import org.bksport.sparql.Node;

/**
 * Represent a resource with specific type and URI
 * 
 * @author congnh
 * 
 */
public class ResourceEntry {

  private Node res;	// resource, Node là một interface.
  private Node type;

  public ResourceEntry() {
    res = null;
    type = null;
  }

  public ResourceEntry(Node res, Node type) {
    this.res = res;
    this.type = type;
  }

  public void setResource(Node uri) {
    this.res = uri;
  }

  public void setType(Node type) {
    this.type = type;
  }

  public Node getResource() {
    return res;
  }

  public Node getType() {
    return type;
  }

  @Override
  public String toString() {
    return res.toString();
  }
}
