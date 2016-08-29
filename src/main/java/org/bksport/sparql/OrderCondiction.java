package org.bksport.sparql;

public class OrderCondiction {

  private boolean    isAsc = false;
  private Expression ex;

  public OrderCondiction(Expression ex) {
    isAsc = true;
    this.ex = ex;
  }

  public void setAsc(boolean b) {
    isAsc = b;
  }

  public boolean isAsc() {
    return isAsc;
  }

  public void setDesc(boolean b) {
    isAsc = !b;
  }

  public boolean isDesc() {
    return !isAsc;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    if (isAsc) {
      sb.append("ASC");
    } else {
      sb.append("DESC");
    }
    if (ex != null) {
      sb.append(" ").append(ex.toString());
    }
    return sb.toString();
  }
}
