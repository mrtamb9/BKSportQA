package org.bksport.sparql.function;

import org.bksport.sparql.Expression;
import org.bksport.sparql.Function;
import org.bksport.sparql.RDFLiteral;

public class RegexFunction extends Function {

  public RegexFunction(Expression ex1, Expression ex2) {
    super(REGEX_IRI, 2, 3);
    setExpression(ex1, ex2);
  }

  public RegexFunction(Expression ex1, Expression ex2, Expression ex3) {
    super(REGEX_IRI, 2, 3);
    setExpression(ex1, ex2, ex3);
  }

  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

}
