package org.bksport.sparql;

/**
 * 
 * @author congnh
 * 
 */
public class Var implements Node, Expression {

  public static final char QUESTION_MARK_SYMBOL = '?';
  public static final char DOLLAR_MARK_SYMBOL   = '$';
  private int              id;
  private String           varName;
  private char             varSymbol;
  private static int       currentId            = 0;

  private static synchronized int getCurrentId() {
    return currentId++;
  }

  /**
   * Construct a variable, automatically initialize it's name and question
   * mark(?) symbol
   */
  public Var() {
    id = getCurrentId();
    setVarName("x" + id);
    setVarSymbol(QUESTION_MARK_SYMBOL);
  }

  /**
   * Construct a variable with specific name and question mark(?) symbol
   * 
   * @param name
   *          name of variable (without symbol)
   */
  public Var(String name) {
    if (name == null
        || (name.length() > 0 && (name.charAt(0) == QUESTION_MARK_SYMBOL || name
            .charAt(0) == DOLLAR_MARK_SYMBOL))) {
      throw new IllegalArgumentException("Invalid variable's name");
    } else {
      id = getCurrentId();
      setVarName(name);
      setVarSymbol(QUESTION_MARK_SYMBOL);
    }
  }

  /**
   * Construct a variable with specific name and specific symbol;
   * 
   * @param name
   */
  public Var(String name, char symbol) {
	  if(symbol==' ') // truong hop COUNT() khong co ?COUNT(?x)
	  {
		  id = getCurrentId();
	      setVarName(name);
	      setVarSymbol(symbol);
	  }
	  else
	  {
		  if (name == null || (name.length() > 0
		            && (name.charAt(0) == QUESTION_MARK_SYMBOL || name.charAt(0) == DOLLAR_MARK_SYMBOL) || (symbol != QUESTION_MARK_SYMBOL && symbol != DOLLAR_MARK_SYMBOL))) {
		      throw new IllegalArgumentException("Invalid variable's name or symbol");
		    } else {
		      id = getCurrentId();
		      setVarName(name);
		      setVarSymbol(symbol);
		    }
	  }
  }

  private void setVarName(String name) {
    varName = name;
  }

  public String getVarName() {
    return varName;
  }

  private void setVarSymbol(char symbol) {
    varSymbol = symbol;
  }

  public char getVarSymbol() {
    return varSymbol;
  }

  public int getId() {
    return id;
  }

  public RDFLiteral evaluate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getResource() {
    return varName;
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o.getClass() == Var.class
        && ((Var) o).getResource().equals(getResource())) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return varSymbol + varName;
  }

}