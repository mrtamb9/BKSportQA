package org.bksport.sparql.function;

import org.bksport.sparql.Expression;
import org.bksport.sparql.Function;
import org.bksport.sparql.RDFLiteral;

public class SameTermFunction extends Function {

  public SameTermFunction(Expression ex1, Expression ex2) {
    super(DATATYPE_IRI, 2, 2);
    setExpression(ex1, ex2);
  }

  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

}
