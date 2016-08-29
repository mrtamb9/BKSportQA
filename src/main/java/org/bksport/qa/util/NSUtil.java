package org.bksport.qa.util;

/**
 * 
 * @author congnh Utilities for namespace
 */
public class NSUtil {
  public static final String bksport = "http://bk.sport.owl#";
  public static final String owl     = "http://www.w3.org/2002/07/owl#";
  public static final String rdf     = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public static final String rdfs    = "http://www.w3.org/2000/01/rdf-schema#";
  public static final String xsd     = "http://www.w3.org/2001/XMLSchema#";
  public static final String time    = "http://www.w3.org/2006/time#";
  public static final String prefix  = "prefix bksport: <" + bksport + ">\n"
                                         + "prefix owl: <" + owl + ">\n"
                                         + "prefix rdf: <" + rdf + ">\n"
                                         + "prefix rdfs: <" + rdfs + ">\n"
                                         + "prefix xsd: <" + xsd + ">\n";

  public static final String bksport(String localName) {
    return bksport + localName;
  }

  public static final String owl(String localName) {
    return owl + localName;
  }

  public static final String rdf(String localName) {
    return rdf + localName;
  }

  public static final String rdfs(String localName) {
    return rdfs + localName;
  }

  public static final String xsd(String localName) {
    return xsd + localName;
  }
  
  public static final String time(String localName) {
	    return time + localName;
	  }
}