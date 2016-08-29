package org.bksport.sparql.expression;

import org.bksport.sparql.Expression;
import org.bksport.sparql.Function;
import org.bksport.sparql.RDFLiteral;

public class MinusExpression implements UnaryExpression {

  private Expression ex;

  public MinusExpression(Expression ex) {
    this.ex = ex;
  }

  @Override
  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    if (ex.getClass() == Function.class || ex.getClass() == RDFLiteral.class
        || ex.getClass().getSuperclass() == Function.class
        || ex.getClass().getSuperclass() == RDFLiteral.class) {
      return "-" + ex.toString();
    } else {
      return "-(" + ex.toString() + ")";
    }
  }
}
