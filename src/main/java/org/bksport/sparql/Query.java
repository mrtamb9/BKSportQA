package org.bksport.sparql;

import java.util.HashMap;

/**
 * 
 * @author congnh
 * 
 */
public abstract class Query {

  protected IRI                  base;
  protected HashMap<String, IRI> prefixMap;
  protected GroupGraphPattern    where;

  protected Query() {
    prefixMap = new HashMap<String, IRI>();
    where = new GroupGraphPattern();
  }

  /**
   * Set base's IRI for query
   * 
   * @param iriRef
   */
  public void setBase(IRI iriRef) {
    base = iriRef;
  }

  /**
   * Get base's IRI
   * 
   * @return
   */
  public IRI getBase() {
    return base;
  }

  /**
   * Add specific prefix for query
   * 
   * @param prefix
   * @param iriRef
   */
  public void addPrefix(String prefix, IRI iriRef) {
    prefixMap.put(prefix, iriRef);
  }

  /**
   * remove specific prefix for query
   * 
   * @param prefix
   */
  public void removePrefix(String prefix) {
    prefixMap.remove(prefix);
  }

  public IRI getPrefix(String prefix) {
    return prefixMap.get(prefix);
  }

  public void setWhere(GroupGraphPattern groupGraphPattern) {
    this.where = groupGraphPattern;
  }

  public GroupGraphPattern getWhere() {
    return where;
  }
}
