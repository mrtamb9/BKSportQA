package org.bksport.sparql;

public class Constraint {

  private Expression ex;

  public Constraint(Expression ex) {
    this.ex = ex;
  }

  public boolean isSatisfactory() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    if (ex.getClass() == Function.class || ex.getClass() == RDFLiteral.class
        || ex.getClass().getSuperclass() == Function.class
        || ex.getClass().getSuperclass() == RDFLiteral.class) {
      return "+" + ex.toString();
    } else
      return "(" + ex.toString() + ")";
  }
}
