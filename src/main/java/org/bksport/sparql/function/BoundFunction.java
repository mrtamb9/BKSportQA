package org.bksport.sparql.function;

import org.bksport.sparql.Function;
import org.bksport.sparql.RDFLiteral;
import org.bksport.sparql.Var;

public class BoundFunction extends Function {

  public BoundFunction(Var ex) {
    super(BOUND_IRI, 1, 1);
    setExpression(ex);
  }

  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

}
