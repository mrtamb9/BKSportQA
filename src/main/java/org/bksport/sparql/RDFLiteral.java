package org.bksport.sparql;

/**
 * 
 * @author congnh
 * 
 */
public class RDFLiteral implements Node, Expression {

  private String string;
  private String lang;
  private IRI    iri;

  /**
   * Construct RDF literal with specific <code>string</code>
   * 
   * @param string
   */
  public RDFLiteral(String string) {
    this.string = string;
  }

  /**
   * Construct RDF literal with specific <code>string</code> and
   * <code>lang</code>
   * 
   * @param string
   * @param lang
   */
  public RDFLiteral(String string, String lang) {
    if (string == null || !GraphTermValidator.validateLang(lang)) {
      throw new IllegalArgumentException(
          "Invalid arguments of RDFLiteral's constructor");
    } else {
      setString(string);
      setLang(lang);
    }
  }

  /**
   * Construct RDF literal with specific IRI
   * 
   * @param value
   * @param iri
   */
  public RDFLiteral(String value, IRI iri) {
    if (value == null) {
      throw new IllegalArgumentException(
          "Invalid arguments of RDFLiteral's constructor");
    } else {
      setString(value);
      setIRI(iri);
    }
  }

  private void setString(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }

  private void setLang(String lang) {
    this.lang = lang;
  }

  public String getLang() {
    return lang;
  }

  private void setIRI(IRI iri) {
    this.iri = iri;
  }

  public IRI getIRI() {
    return iri;
  }

  @Override
  public String getResource() {
    if (lang == null) {
      if (iri == null) {
        return "\"" + string + "\"";
      } else {
        return "\"" + string + "\"" + "^^" + iri.toString();
      }
    } else {
      return "\"" + string + "\"" + "@" + lang;
    }
  }

  @Override
  public RDFLiteral evaluate() {
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o.getClass() == RDFLiteral.class) {
      if (((RDFLiteral) o).getString().equals(getString())) {
        if (((RDFLiteral) o).getIRI() != null
            && ((RDFLiteral) o).getIRI().equals(getIRI())) {
          return true;
        } else if (((RDFLiteral) o).getLang() != null
            && ((RDFLiteral) o).getLang().equals(getLang())) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    if (lang == null) {
      if (iri == null) {
        return "\"" + string + "\"";
      } else {
        return "\"" + string + "\"" + "^^" + iri.toString();
      }
    } else {
      return "\"" + string + "\"" + "@" + lang;
    }
  }
}
