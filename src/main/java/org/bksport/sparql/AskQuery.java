package org.bksport.sparql;

public class AskQuery extends Query {

  @Override
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
    sb.append("ASK ");

    if (where != null) {
      sb.append("WHERE ").append(where.toString());
    } else {
      sb.append("WHERE {}");
    }
    return sb.toString();
  }

}
