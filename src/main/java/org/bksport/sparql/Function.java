package org.bksport.sparql;

import java.util.ArrayList;
import java.util.List;

public class Function implements Expression {

  protected static final IRI STR_IRI         = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/str");
  protected static final IRI LANG_IRI        = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/lang");
  protected static final IRI LANGMATCHES_IRI = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/langmatches");
  protected static final IRI DATATYPE_IRI    = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/datatype");
  protected static final IRI BOUND_IRI       = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/bound");
  protected static final IRI SAMETERM_IRI    = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/sameterm");
  protected static final IRI ISIRI_IRI       = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/isiri");
  protected static final IRI ISURI_IRI       = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/isuri");
  protected static final IRI ISBLANK_IRI     = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/isblank");
  protected static final IRI ISLITERAL_IRI   = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/isliteral");
  protected static final IRI REGEX_IRI       = new IRI(
                                                 "http://www.w3.org/TR/rdf-sparql-query/op/regex");

  private IRI                iri;
  private int                minNumEx;
  private int                maxNumEx;
  private List<Expression>   expressionList;

  /**
   * @param iri
   * @param min
   * @param max
   */
  public Function(IRI iri, int min, int max) {
    if (iri == null) {
      throw new IllegalArgumentException(
          "Null iri paramters of Function's constructor");
    } else {
      this.iri = iri;
    }
    // TODO check whether min < max, iri is null or not
    if (min >= 0 && min <= max) {
      minNumEx = min;
      maxNumEx = max;
      expressionList = new ArrayList<Expression>(min);
    } else {
      throw new IllegalArgumentException(
          "Invalid min, max paramters of Function's constructor");
    }
  }

  public IRI getIRI() {
    return iri;
  }

  public void setExpression(Expression... ex) {
    if (ex.length >= minNumEx && ex.length <= maxNumEx) {
      expressionList.clear();
      for (int i = 0; i < ex.length; i++) {
        expressionList.add(ex[i]);
      }
    } else {
      throw new IllegalArgumentException("Invalid number of expressions");
    }
  }

  public Expression[] getExpression() {
    return (Expression[]) expressionList.toArray();
  }

  public List<Expression> getExpressionList() {
    return expressionList;
  }

  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (iri.equals(STR_IRI))
      sb.append("STR");
    else if (iri.equals(LANG_IRI))
      sb.append("LANG");
    else if (iri.equals(LANGMATCHES_IRI))
      sb.append("LANGMATCHES");
    else if (iri.equals(DATATYPE_IRI))
      sb.append("DATATYPE");
    else if (iri.equals(BOUND_IRI))
      sb.append("BOUND");
    else if (iri.equals(SAMETERM_IRI))
      sb.append("SAMETERM");
    else if (iri.equals(ISIRI_IRI))
      sb.append("ISIRI");
    else if (iri.equals(ISURI_IRI))
      sb.append("ISURI");
    else if (iri.equals(ISBLANK_IRI))
      sb.append("ISBLANK");
    else if (iri.equals(ISLITERAL_IRI))
      sb.append("ISLITERAL");
    else if (iri.equals(REGEX_IRI))
      sb.append("REGEX");
    else {
      sb.append(iri.toString());
    }
    sb.append("(");
    if (expressionList.size() > 0) {
      sb.append(expressionList.get(0).toString());
      for (int i = 1; i < expressionList.size(); i++) {
        sb.append(", ").append(expressionList.get(i).toString());
      }
    }
    sb.append(")");
    return sb.toString();
  }

}
