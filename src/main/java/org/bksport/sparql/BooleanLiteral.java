package org.bksport.sparql;

/**
 * 
 * @author congnh
 * 
 */
public class BooleanLiteral extends RDFLiteral {

  public BooleanLiteral(boolean b) {
    super(b + "", IRI.XSD_BOOLEAN_IRI);
  }

  @Override
  public String toString() {
    return getString();
  }

}
