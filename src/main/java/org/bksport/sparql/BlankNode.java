package org.bksport.sparql;

/**
 * 
 * @author congnh
 * 
 */
public class BlankNode implements Node {

  private int        id;
  private String     label;
  private static int currentId = 0;

  private static synchronized int getCurrentId() {
    return currentId++;
  }

  /**
   * Construct blank node and automatically initialize it's name
   */
  public BlankNode() {
    id = getCurrentId();
    label = "_:x" + id;
  }

  /**
   * Construct blank node with specific label<br/>
   * There're 2 cases:<br/>
   * <ul>
   * <li>BLANK_NODE_LABEL ::= '[' ( 0x20 | 0x9 | 0xD | 0xA ) ']'</li>
   * <li>BLANK_NODE_LABEL ::= '_:' PN_LOCAL</li>
   * </ul>
   * 
   * @param label
   */
  public BlankNode(String label) {
    if (label == null) {
      throw new IllegalArgumentException("Invalid blank node's label");
    }
    if (label.startsWith("_:") || GraphTermValidator.validateANON(label)) {
      id = getCurrentId();
      this.label = label;
    } else {
      throw new IllegalArgumentException("Invalid blank node's label");
    }
  }

  public int getId() {
    return id;
  }

  @Override
  public String getResource() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o.getClass() == BlankNode.class
        && ((BlankNode) o).getId() == getId()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return label;
  }
}
