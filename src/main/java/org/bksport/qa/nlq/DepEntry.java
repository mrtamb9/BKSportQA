package org.bksport.qa.nlq;

public class DepEntry {

  private LexicalEntry dep;
  private LexicalEntry gov;
  private String       type;
  private String       specific;

  public DepEntry() {
    dep = gov = null;
    type = specific = null;
  }

  public LexicalEntry getDep() {
    return dep;
  }

  public LexicalEntry getGov() {
    return gov;
  }

  public String getType() {
    return type;
  }

  public String getSpecific() {
    return specific;
  }

  public void setDep(LexicalEntry dep) {
    this.dep = dep;
  }

  public void setGov(LexicalEntry gov) {
    this.gov = gov;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setSpecific(String specific) {
    this.specific = specific;
  }

  public static boolean isSupportType(String type) {
    String[] supportTypes = new String[] { "accomp", "advmod", "agent", "amod",
        "appos", "attr", "cc", "ccomp", "complm", "conj", "cop",/*
                                                                 * "csubj",
                                                                 * "csubjpass",
                                                                 */"det",
        "dobj", "expl", "infmod", "iobj", "mwe", "neg", "npadvmod", "nsubj",
        "nsubjpass", "num", "number", "partmod", "pcomp", "pobj", "poss",
        "possesive", "preconj", "predet", "prep", "prepc", "prt", "purpcl",
        "quantmod", "rcmod", "ref", "tmod", "xcomp", "xsubj", "dep" };
    if (type != null) {
      for (int i = 0; i < supportTypes.length; i++) {
        if (supportTypes[i].equals(type.toLowerCase())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    if (specific == null) {
      return type + "(" + gov + "," + dep + ")";
    } else {
      return type + "_" + specific + "(" + gov + "," + dep + ")";
    }
  }

}
