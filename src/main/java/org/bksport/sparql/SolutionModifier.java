package org.bksport.sparql;

import java.util.ArrayList;
import java.util.List;

public class SolutionModifier {

  private NumericLiteral        offset;
  private NumericLiteral        limit;
  private List<OrderCondiction> condictionList;

  public SolutionModifier() {
    condictionList = new ArrayList<OrderCondiction>();
  }

  public void setOffset(NumericLiteral offset) {
    this.offset = offset;
  }

  public NumericLiteral getOffset() {
    return offset;
  }

  public void setLimit(NumericLiteral limit) {
    this.limit = limit;
  }

  public NumericLiteral getLimit() {
    return limit;
  }

  public void addCondiction(OrderCondiction condiction) {
    condictionList.add(condiction);
  }

  public void removeCondiction(OrderCondiction condiction) {
    condictionList.remove(condiction);
  }

  public OrderCondiction getCondiction(int index) {
    return condictionList.get(index);
  }

  public int getNumOfCondiction() {
    return condictionList.size();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    if (!condictionList.isEmpty()) {
      sb.append("ORDER BY ").append(condictionList.get(0));
      for (int i = 1; i < condictionList.size(); i++) {
        sb.append('\n').append(condictionList.get(i));

      }
    }
    if (limit != null) {
      if (sb.length() > 0)
        sb.append('\n');
      sb.append("LIMIT ").append(limit.toString());
    }
    if (offset != null) {
      if (sb.length() > 0)
        sb.append('\n');
      sb.append("OFFSET ").append(offset.toString());
    }
    return sb.toString();
  }
}
