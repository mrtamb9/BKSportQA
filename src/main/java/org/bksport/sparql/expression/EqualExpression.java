package org.bksport.sparql.expression;

import org.bksport.sparql.Expression;
import org.bksport.sparql.RDFLiteral;

public class EqualExpression implements RelationalExpression {

  private Expression ex1;
  private Expression ex2;
  private Expression varDate;
  private String instantDate;

  public EqualExpression(Expression ex1, Expression ex2) {
    this.ex1 = ex1;
    this.ex2 = ex2;
  }

  @Override
  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return ex1.toString() + " = " + ex2.toString();
  }
}
