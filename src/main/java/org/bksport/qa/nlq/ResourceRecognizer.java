package org.bksport.qa.nlq;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bksport.qa.connector.AGraphConnector;
import org.bksport.qa.util.ConfigUtil;
import org.bksport.qa.util.FileUtil;
import org.bksport.qa.util.NSUtil;
import org.bksport.sparql.IRI;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class ResourceRecognizer {

  private static Logger                                  logger        = Logger
                                                                           .getLogger(ResourceRecognizer.class);
  private static DocumentBuilder                         docBuilder;
  private static HashMap<String, HashSet<ResourceEntry>> generalIRIMap = new HashMap<String, HashSet<ResourceEntry>>();
  private static HashMap<String, HashSet<ResourceEntry>> domainIRIMap  = new HashMap<String, HashSet<ResourceEntry>>();

  public static AGraphConnector                          connector;

  public static HashSet<ResourceEntry> getGeneralResourceEntrySet(String keyword) 
  {
	  // System.out.println("TTT generalIRIMap ------->: " + generalIRIMap.toString());
    if (generalIRIMap.containsKey(keyword)) {
      return generalIRIMap.get(keyword.toLowerCase());
    } else {
      for (String key : generalIRIMap.keySet()) {
        if (key.toLowerCase().contains(keyword.toLowerCase() + " ")
            || key.toLowerCase().contains(" " + keyword.toLowerCase())) {
          return generalIRIMap.get(key);
        }
      }
    }
    return null;
  }

  public static HashSet<ResourceEntry> getDomainResourceEntrySet(String keyword) 
  {	  
	  /*	   
          ResultSet resultSet = connector.execSelect(NSUtil.prefix
              + "\nSELECT ?class ?label {" 
	        	  + "?class rdf:type owl:ObjectProperty."
	              + "UNION {?class rdf:type owl:DatatypeProperty.}"
	        	  + "?class rdfs:label ?label."
	              + "?class fti:match '" + keyword + "'." 
              + "}");          
	   * */
          
          
    if (domainIRIMap.containsKey(keyword)) 
    {
      return domainIRIMap.get(keyword.toLowerCase());
    } 
    else 
    {
      for (String key : domainIRIMap.keySet()) 
      {
        if (key.toLowerCase().contains(keyword.toLowerCase() + " ")
            || key.toLowerCase().contains(" " + keyword.toLowerCase())) 
        {
          return domainIRIMap.get(key);
        }
      }
    }
    
    if (keyword.toLowerCase().equals("happen")) 
    {
      final ResourceEntry resourceEntry = new ResourceEntry();
      resourceEntry.setResource(new IRI("http://bk.sport.owl#happen"));
      resourceEntry.setType(IRI.OWL_OBJECTPROPERTY_IRI);
      return new HashSet<ResourceEntry>() 
      {
        {
          add(resourceEntry);
        }
      };
    }
    return null;
  }

  // load dataset: class, objectproperty from Server AllegroGraph
  public static void loadResourceDataset() 
  {
	  System.out.println("Running function loadResourceDataset()");
    if (docBuilder == null) 
    {
      try 
      {
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      } 
      catch (ParserConfigurationException e) {
        logger.error(ExceptionUtils.getStackTrace(e));
      }
    }
    
   try 
   {
      Document doc = docBuilder.parse("file:"
          + FileUtil.getAbsolutePath("data", "bksport-resource-dataset.xml"));

      connector = new AGraphConnector(ConfigUtil.AG_SERVER_HOST,
							          ConfigUtil.AG_SERVER_PORT, 
							          ConfigUtil.AG_SERVER_USER,
							          ConfigUtil.AG_SERVER_PASSWORD);
      connector.connect();
      connector.setCatalogID("vtio-catalog");
      connector.setRepositoryID("bksport-repository");
      connector.openCatalog();
      connector.openRepository();
      
      // load Class
      ResultSet resultSet = connector.execSelect(NSUtil.prefix
          + "\nSELECT ?class ?label WHERE {" 
    	  + "?class rdf:type owl:Class."
          + "?class rdfs:label ?label."
          + "FILTER(regex(str(?class),\"http://bk.sport.owl\"))" 
          + "}");
      while (resultSet != null && resultSet.hasNext()) 
      {
        QuerySolution solution = resultSet.next();
        if (solution.getResource("class").toString().startsWith(NSUtil.bksport)) 
        {
          domainIRIMap.put(solution.getLiteral("label").getLexicalForm()
              .toString().toLowerCase(), new HashSet<ResourceEntry>());
          
          ResourceEntry resourceEntry = new ResourceEntry();
          
          resourceEntry.setResource(new IRI(solution.getResource("class").toString()));
          
          resourceEntry.setType(IRI.OWL_CLASS_IRI);
          
          domainIRIMap.get(solution.getLiteral("label").getLexicalForm().toString().toLowerCase()).add(resourceEntry); // hashSet.add(resourceEntry)
        }
      }
      
      // load objectProperty
      resultSet = connector.execSelect(NSUtil.prefix
          + "\nSELECT ?prop ?label ?t WHERE {" 
    	  + "{" + "  ?prop rdf:type owl:ObjectProperty." + "}" 
    	  + "UNION{"
          + "  ?prop rdf:type owl:DatatypeProperty." + "}"
          + "?prop rdfs:label ?label." 
          + "?prop rdf:type ?t."
          + "FILTER(regex(str(?prop),\"http://bk.sport.owl\"))" 
          + "}");
      while (resultSet != null && resultSet.hasNext()) 
      {
        QuerySolution solution = resultSet.next();
        // System.out.println(solution.getLiteral("label").getLexicalForm().toString().toLowerCase());
      
        domainIRIMap.put(solution.getLiteral("label").getLexicalForm()
            .toString().toLowerCase(), new HashSet<ResourceEntry>());
        
        ResourceEntry resourceEntry = new ResourceEntry();
        resourceEntry.setResource(new IRI(solution.getResource("prop").toString()));
        
        if (solution.getResource("t").getURI().equals(IRI.OWL_DATATYPEPROPERTY_IRI)) 
        {
          resourceEntry.setType(IRI.OWL_DATATYPEPROPERTY_IRI);
        } 
        else 
        {
          resourceEntry.setType(IRI.OWL_OBJECTPROPERTY_IRI);
        }
        
        domainIRIMap.get(solution.getLiteral("label").getLexicalForm()).add(resourceEntry);
      }
      connector.disconnect();
    } catch (SAXException e) {
      logger.error(ExceptionUtils.getStackTrace(e));
    } catch (IOException e) {
      logger.error(ExceptionUtils.getStackTrace(e));
    }
  }

  public static Object resolveAlias(String string) {
    if (string.toLowerCase().contains("today"))
      return new Date();
    if (string.toLowerCase().contains("yesterday")) {
      Calendar c = Calendar.getInstance();
      c.roll(Calendar.DATE, false);
      return c.getTime();
    }
    if (string
        .toLowerCase()
        .matches(
            "^.*([0-9]{4}/[0-1]?[0-9]/[0-3]?[0-9]( [0-2]?[0-9]:[0-5]?[0-9](:[0-5]?[0-9])?)?( ict, am|pm)?).*$")) {
      System.out.println("match");
      Pattern p = Pattern
          .compile("^.*([0-9]{4}/[0-1]?[0-9]/[0-3]?[0-9]( [0-2]?[0-9]:[0-5]?[0-9](:[0-5]?[0-9])?)?( ict, am|pm)?).*$");
      Matcher m = p.matcher(string.toLowerCase());
      m.find();
      try {
        return DateFormat.getInstance().parse(m.group(0));
      } catch (ParseException e) {
        logger.error(ExceptionUtils.getStackTrace(e));
      }
    }
    return null;
  }
}
