package org.bksport.sparql;

/**
 * 
 * @author congnh
 * 
 */
public class NumericLiteral extends RDFLiteral {

  public NumericLiteral(int value) {
    super(value + "", IRI.XSD_INTEGER_IRI);
  }

  public NumericLiteral(double value) {
    super(value + "", IRI.XSD_DOUBLE_IRI);
  }

  @Override
  public String toString() {
    return getString();
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o.getClass() == RDFLiteral.class) {
      RDFLiteral tmp = (RDFLiteral) o;
      // TODO miss case: an IRI is superset of another IRI
      // e.g. xsd:unsingnedInterger is subset of xsd:integer
      if (tmp.getIRI().equals(getIRI())) {
        if (tmp.getString().equals(getString())) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
    return false;
  }
}
