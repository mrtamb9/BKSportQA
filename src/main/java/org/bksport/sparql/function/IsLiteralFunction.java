package org.bksport.sparql.function;

import org.bksport.sparql.Expression;
import org.bksport.sparql.Function;
import org.bksport.sparql.RDFLiteral;

public class IsLiteralFunction extends Function {

  public IsLiteralFunction(Expression ex) {
    super(ISLITERAL_IRI, 1, 1);
    setExpression(ex);
  }

  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

}
