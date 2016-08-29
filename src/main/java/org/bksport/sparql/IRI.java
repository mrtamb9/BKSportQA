package org.bksport.sparql;

import java.util.HashMap;

import org.bksport.qa.util.NSUtil;

/**
 * 
 * @author congnh
 * 
 */
public class IRI implements Node {

  public static final IRI             RDF_IRI  = new IRI(NSUtil.rdf);
  public static final IRI             RDFS_IRI = new IRI(NSUtil.rdfs);
  public static final IRI             OWL_IRI  = new IRI(NSUtil.owl);
  public static final IRI             XSD_IRI  = new IRI(NSUtil.xsd);
  public static final IRI             TIME_IRI  = new IRI(NSUtil.time);
  private static HashMap<String, IRI> namespacesMap;
  private static HashMap<String, IRI> iriRef;
  static {
    namespacesMap = new HashMap<String, IRI>();
    iriRef = new HashMap<String, IRI>();
    addNS("rdf", RDF_IRI);
    addNS("rdfs", RDFS_IRI);
    addNS("owl", OWL_IRI);
    addNS("xsd", XSD_IRI);
    addNS("time", TIME_IRI);
  }

  /**
   * Add new namespace
   * 
   * @param prefix
   * @param uri
   */
  public static void addNS(String prefix, IRI uri) {
    namespacesMap.put(prefix, uri);
  }

  /**
   * Remove available namespace
   * 
   * @param prefix
   */
  public static void removeNS(String prefix) {
    namespacesMap.remove(prefix);
  }

  /**
   * Get IRI with specific prefix of available namespace
   * 
   * @param prefix
   * @return
   */
  public static IRI getNS(String prefix) {
    return namespacesMap.get(prefix);
  }

  /**
   * Get prefixes of all available namespace
   * 
   * @return
   */
  public static String[] getAvailableNS() {
    return (String[]) namespacesMap.keySet().toArray();
  }

  public static void addIRIRef(String iri) {
    if (!iriRef.containsKey(iri)) {
      iriRef.put(iri, new IRI(iri));
    }
  }

  public static void removeIRIRef(String iri) {
    iriRef.remove(iri);
  }

  public static IRI getIRIRef(String iri) {
    return iriRef.get(iri);
  }

  public static final IRI RDF_TYPE_IRI             = new IRI(NSUtil.rdf("type"));
  public static final IRI RDFS_LABEL_IRI           = new IRI(
                                                       NSUtil.rdfs("label"));
  public static final IRI OWL_CLASS_IRI            = new IRI(
                                                       NSUtil.owl("Class"));
  public static final IRI OWL_OBJECTPROPERTY_IRI   = new IRI(
                                                       NSUtil
                                                           .owl("ObjectProperty"));
  public static final IRI OWL_DATATYPEPROPERTY_IRI = new IRI(
                                                       NSUtil
                                                           .owl("DatatypeProperty"));
  public static final IRI XSD_STRING_IRI           = new IRI(
                                                       NSUtil.xsd("string"));
  public static final IRI XSD_INTEGER_IRI          = new IRI(
                                                       NSUtil.xsd("integer"));
  public static final IRI XSD_BOOLEAN_IRI          = new IRI(
                                                       NSUtil.xsd("boolean"));
  public static final IRI XSD_DOUBLE_IRI           = new IRI(
                                                       NSUtil.xsd("double"));
  public static final IRI XSD_DATE_IRI             = new IRI(NSUtil.xsd("date"));
  public static final IRI XSD_DATETIME_IRI         = new IRI(
                                                       NSUtil.xsd("datetime"));

  private String          iri;
  private IRI             reference;
  private String          prefix;
  private String          relative;

  /**
   * Construct an IRI
   * 
   * @param iri
   *          specific IRI
   */
  public IRI(String iri) {
    if (iri == null) {
      throw new IllegalArgumentException("Null argument of IRI's constructor");
    } else {
      this.iri = iri;
      reference = null;
      relative = null;
      prefix = null;
    }
  }

  /**
   * <p>
   * Construct an IRI from IRI reference and relative<br/>
   */
  public IRI(IRI reference, String relative) {
    if (reference == null || relative == null) {
      throw new IllegalArgumentException("Null arguments of IRI's constructor");
    } else {
      iri = null;
      this.reference = reference;
      this.relative = relative;
      prefix = null;
    }
  }

  /**
   * <p>
   * Construct an IRI from IRI reference of default namespace<br/>
   */
  public IRI(String prefix, String relative) {
    if (prefix == null || relative == null) {
      throw new IllegalArgumentException("Null arguments of IRI's constructor");
    } else {

      iri = null;
      reference = getNS(prefix);
      if (reference == null) {
        throw new IllegalArgumentException(
            "Environment does'nt contains any namespace with prefix: " + prefix);
      }
      this.relative = relative;
      this.prefix = prefix;
    }
  }

  /**
   * <p>
   * Construct an IRI using <code>prefix</code> and <code>local</code>
   * </p>
   * 
   * @param context
   *          query context which declare <code>prefix</code>
   * @param prefix
   * @param local
   */
  public IRI(Query context, String prefix, String local) {
    if (context == null || prefix == null || local == null) {
      throw new IllegalArgumentException("Null arguments of IRI's constructor");
    } else {
      if (prefix.isEmpty()) {
        if (context.getBase() != null) {
          iri = null;
          this.reference = context.getBase();
          this.relative = local;
          this.prefix = "";
        }
      } else {
        if (context.getPrefix(prefix) == null) {
          throw new IllegalArgumentException(
              "Query does'nt contains any namespace with prefix: " + prefix);
        } else {
          iri = null;
          reference = context.getPrefix(prefix);
          this.relative = local;
          this.prefix = prefix;
        }
      }
    }
  }

  @Override
  public String getResource() {
    if (iri != null)
      return iri;
    else {
      return reference.getResource() + relative;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o.getClass() == IRI.class
        && ((IRI) o).getResource().equals(getResource())) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    if (iri != null)
      return "<" + getResource() + ">";
    else
      return prefix + ":" + relative;
  }
}