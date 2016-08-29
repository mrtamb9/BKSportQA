package org.bksport.sparql;

import java.util.ArrayList;
import java.util.List;

public class SelectQuery extends Query {

  private List<Var>        varList;
  private SolutionModifier solution;

  public SelectQuery() {
    super();
    varList = new ArrayList<Var>();
    solution = new SolutionModifier();
  }

  public void setSolution(SolutionModifier solution) {
    this.solution = solution;
  }

  public SolutionModifier getSolution() {
    return solution;
  }

  public void addVar(Var var) {
    varList.add(var);
  }

  public void removeVar(Var var) {
    varList.remove(var);
  }

  public Var getVar(int index) {
    return varList.get(index);
  }

  public int getNumOfVar() {
    return varList.size();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (base != null) {
      sb.append("BASE ").append(base.toString());
    }
    if (prefixMap.size() > 0) {
      for (String prefix : prefixMap.keySet()) {
        if (sb.length() > 0)
          sb.append('\n');
        sb.append("PREFIX ").append(prefix).append(": ")
            .append(prefixMap.get(prefix));
      }
    }
    if (sb.length() > 0)
      sb.append('\n');
    sb.append("SELECT DISTINCT");
    if (!varList.isEmpty()) {
      for (int i = 0; i < varList.size(); i++) {
        sb.append(' ').append(varList.get(i).toString());
      }
    } else {
      sb.append(" *");
    }
    sb.append('\n');
    if (where != null) {
      sb.append("WHERE ").append(where.toString());
    } else {
      sb.append("WHERE {}");
    }
    if (solution != null) {
      sb.append('\n').append(solution);
    }
    return sb.toString();
  }
}
