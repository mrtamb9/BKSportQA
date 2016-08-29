package org.bksport.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bksport.qa.util.StringUtil;

/**
 * 
 * @author congnh
 * 
 */
public class CopyOfGroupGraphPattern {

  private List<Triple>                     patternList;
  private List<CopyOfGroupGraphPattern>          optionalList;
  private List<CopyOfGroupGraphPattern>          unionList;
  private List<Constraint>                 filterList;
  private List<String>                 filterListDate;
  private HashMap<CopyOfGroupGraphPattern, Node> graphMap;
  private List<Triple> 					   listTripleOfGraph;
  private List<String>					havingcountList;
  private List<String>					groupcountList;
  private List<String>					orderByList;
  
  private static int graphID = 0;
  private String nameGraph;
  private static synchronized int getCurrentGraphID() 
  {
	  return graphID++;
  }
  
  private void setNameGraph(String name) 
  {
	  nameGraph = name;
  }

  public String getNameGraph()
  {
	  return nameGraph;
  }


  public CopyOfGroupGraphPattern() {
    patternList = new ArrayList<Triple>();
    optionalList = new ArrayList<CopyOfGroupGraphPattern>();
    unionList = new ArrayList<CopyOfGroupGraphPattern>();
    filterList = new ArrayList<Constraint>();
    filterListDate = new ArrayList<String>();
    graphMap = new HashMap<CopyOfGroupGraphPattern, Node>();
    listTripleOfGraph = new ArrayList<Triple>();
    havingcountList = new ArrayList<String>();
    groupcountList = new ArrayList<String>();
    orderByList = new ArrayList<String>();
    
    setNameGraph("g" + getCurrentGraphID());
  }
  
  public CopyOfGroupGraphPattern(String nameGraph) {
	    patternList = new ArrayList<Triple>();
	    optionalList = new ArrayList<CopyOfGroupGraphPattern>();
	    unionList = new ArrayList<CopyOfGroupGraphPattern>();
	    filterList = new ArrayList<Constraint>();
	    filterListDate = new ArrayList<String>();
	    graphMap = new HashMap<CopyOfGroupGraphPattern, Node>();
	    listTripleOfGraph = new ArrayList<Triple>();
	    havingcountList = new ArrayList<String>();
	    groupcountList = new ArrayList<String>();
	    orderByList = new ArrayList<String>();
	    
	    setNameGraph(nameGraph);
	  }

  public void addTriplePattern(Triple triplePattern) {
    patternList.add(triplePattern);
  }

  public void removeTriplePattern(Triple triplePattern) {
    patternList.remove(triplePattern);
  }

  public Triple getTriplePattern(int i) {
    return patternList.get(i);
  }

  public int getNumberOfTriplePattern() {
    return patternList.size();
  }

  public void addOptional(CopyOfGroupGraphPattern groupGraphPattern2) {
    optionalList.add(groupGraphPattern2);
  }

  public void removeOptional(CopyOfGroupGraphPattern groupGraphPattern2) {
    optionalList.remove(groupGraphPattern2);
  }

  public CopyOfGroupGraphPattern getOptional(int i) {
    return optionalList.get(i);
  }

  public int getNumberOfOptional() {
    return optionalList.size();
  }

  public void addGraph(Node varOrIri, CopyOfGroupGraphPattern groupGraphPattern2) {
    graphMap.put(groupGraphPattern2, varOrIri);
  }

  public void removeGraph(CopyOfGroupGraphPattern groupGraphPattern2) {
    graphMap.remove(groupGraphPattern2);
  }

  public int getNumberOfGraph() {
    return graphMap.size();
  }

  public void addUnion(CopyOfGroupGraphPattern groupGraphPattern2) {
    unionList.add(groupGraphPattern2);
  }

  public void removeUnion(CopyOfGroupGraphPattern groupGraphPattern2) {
    unionList.remove(groupGraphPattern2);
  }

  public CopyOfGroupGraphPattern getUnion(int i) {
    return unionList.get(i);
  }

  public int getNumberOfUnion() {
    return unionList.size();
  }

  public void addFilter(Constraint constraint) {
    filterList.add(constraint);
  }

  public void removeFilter(Constraint constraint) {
    filterList.remove(constraint);
  }

  public Constraint getFilter(int i) {
	  return filterList.get(i);
  }

  public int getNumberOfFilter() {
	  return filterList.size();
  }
  
  public void addTriple(Triple triple)
  {
	  listTripleOfGraph.add(triple);
  }
  
  public void addFilterDate(String filter) {
	    filterListDate.add(filter);
	  }

  public void removeFilterDate(String filter) {
	    filterListDate.remove(filter);
	  }

  public void addhavingcountList(String having) {
	  havingcountList.add(having);
	  }

  public void removehavingcountList(String having) {
	  havingcountList.remove(having);
	  }
  
  public void addgroupcountList(String group) {
	  groupcountList.add(group);
	  }

  public void removegroupcountList(String group) {
	  groupcountList.remove(group);
	  }
  
  public void addOrderByList(String orderString)
  {
	  orderByList.add(orderString);
  }
  
  public void removeOrderByList(String orderString)
  {
	  orderByList.remove(orderString);
  }
  
  @Override
  public String toString() 
  {
	// Them cac xu ly hien tuong trung lap cac triple (4/2/2015)
	  
    StringBuilder sb = new StringBuilder();
    sb.append("{");

    if(!patternList.isEmpty())
    {
        sb.append("\n   GRAPH ?" + nameGraph + " {");
        
        if (!patternList.isEmpty()) 
        {
        	if(patternList.size()>=2)	// xet hien tuong trung nhau
        	{
        		for(int i=0; i<patternList.size()-1; i++)
            	{
            		for(int j=i+1; j<patternList.size(); j++)
            		{
            			if(patternList.get(i).toString().equals(patternList.get(j).toString()))
            			{
            				patternList.remove(j);
            				j--;
            			}
            		}
            	}
        	}
        	
          for (int i = 0; i < patternList.size(); i++)
          {
            sb.append('\n').append("               ").append(patternList.get(i).toString()).append(".");
          }
        }        
        
        sb.append("\n   }");	// close Graph
    }
    
    if (!unionList.isEmpty()) 
    {
    	if(unionList.size()>=2)	// xet hien tuong trung nhau
    	{
    		for(int i=0; i<unionList.size()-1; i++)
        	{
        		for(int j=i+1; j<unionList.size(); j++)
        		{
        			if(unionList.get(i).toString().equals(unionList.get(j).toString()))
        			{
        				unionList.remove(j);
        				j--;
        			}
        		}
        	}
    	}
    	
      sb.append('\n').append(StringUtil.shiftRight(unionList.get(0).toString()));
      for (int i = 1; i < unionList.size(); i++) 
      {
        sb.append('\n').append(StringUtil.shiftRight("UNION " + unionList.get(i).toString()));
      }
    }
    
    if(!listTripleOfGraph.isEmpty())
    {
    	if(listTripleOfGraph.size()>=2)	// xet hien tuong trung nhau
    	{
    		for(int i=0; i<listTripleOfGraph.size()-1; i++)
        	{
        		for(int j=i+1; j<listTripleOfGraph.size(); j++)
        		{
        			if(listTripleOfGraph.get(i).toString().equals(listTripleOfGraph.get(j).toString()))
        			{
        				listTripleOfGraph.remove(j);
        				j--;
        			}
        		}
        	}
    	}
    	
    	for(int i=0; i<listTripleOfGraph.size(); i++)
    	{
    		sb.append("\n   ").append(listTripleOfGraph.get(i).toString()).append('.');
    	}
    }
    
    if (!filterList.isEmpty()) 
    {
    	if(filterList.size()>=2)	// xet hien tuong trung nhau
    	{
    		for(int i=0; i<filterList.size()-1; i++)
        	{
        		for(int j=i+1; j<filterList.size(); j++)
        		{
        			if(filterList.get(i).toString().equals(filterList.get(j).toString()))
        			{
        				filterList.remove(j);
        				j--;
        			}
        		}
        	}
    	}
    	
      for (int i = 0; i < filterList.size(); i++) 
      {
        sb.append('\n').append("           FILTER ").append(filterList.get(i).toString()).append(".");
      }
    }
    
    if (!filterListDate.isEmpty()) 
    {
    	if(filterListDate.size()>=2)	// xet hien tuong trung nhau
    	{
    		for(int i=0; i<filterListDate.size()-1; i++)
        	{
        		for(int j=i+1; j<filterListDate.size(); j++)
        		{
        			if(filterListDate.get(i).toString().equals(filterListDate.get(j).toString()))
        			{
        				filterListDate.remove(j);
        				j--;
        			}
        		}
        	}
    	}
    	
      for (int i = 0; i < filterListDate.size(); i++) 
      {
        sb.append('\n').append("           FILTER ").append("(" + filterListDate.get(i).toString() + ")").append(".");
      }
    }
    
    if (!optionalList.isEmpty()) 
    {
    	if(optionalList.size()>=2)	// xet hien tuong trung nhau
    	{
    		for(int i=0; i<optionalList.size()-1; i++)
        	{
        		for(int j=i+1; j<optionalList.size(); j++)
        		{
        			if(optionalList.get(i).toString().equals(optionalList.get(j).toString()))
        			{
        				optionalList.remove(j);
        				j--;
        			}
        		}
        	}
    	}
    	
      for (int i = 0; i < optionalList.size(); i++) 
      {
        sb.append('\n').append(StringUtil.shiftRight("OPTIONAL " + optionalList.get(i).toString()));
      }
    }
    
    if (sb.indexOf("\n") > 0)
      sb.append("\n}");
    else
      sb.append("}");	// close WHERE
        
    
    // GROUP BY
    for(int i=0; i<groupcountList.size(); i++)
    {
    	sb.append('\n').append(groupcountList.get(i).toString());
    }
    
    if(!havingcountList.isEmpty())
    {
    	if(havingcountList.size()>=2) // xet hien tuong trung dieu kien
    	{
    		for(int i=0; i<havingcountList.size()-1; i++)
    		{
    			for(int j=i+1; j<havingcountList.size(); j++)
    			{
    				if(havingcountList.get(i).toString().equals(havingcountList.get(j).toString()))
    				{
    					havingcountList.remove(j);	// remove by index    					
    					j--;
    				}
    			}
    		}
    	}
    }
    
    // HAVING COUNT
    for(int i=0; i<havingcountList.size(); i++)
    {
    	sb.append('\n').append("HAVING ( ").append(havingcountList.get(i).toString() + " ).");
    }
    
    // ORDER BY
    if(orderByList.size()>0)
    {
    	sb.append('\n').append(orderByList.get(0).toString());
    }
    
    return sb.toString();
  }
}
