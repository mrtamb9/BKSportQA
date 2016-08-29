package org.bksport.sparql;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author congnh
 * 
 */
public class NewGroupGraphPattern {

  private List<Triple> patternList;
  private GroupGraphPattern childGraph;

  public NewGroupGraphPattern() {
    patternList = new ArrayList<Triple>();
    childGraph = new GroupGraphPattern();
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
  
  public void setChildGraph(GroupGraphPattern child)
  {
	  childGraph = child;
  }

  @Override
  public String toString() 
  {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\n   GRAPH ?g ");
    
    if(childGraph!=null)
    {
    	childGraph.toString();
    }
    
    sb.append("\n            }");
    
    if (!patternList.isEmpty()) 
    {
      for (int i = 0; i < patternList.size(); i++) 
      {
        sb.append('\n').append("  ").append(patternList.get(i).toString())
            .append(".");
      }
    }
            
    if (sb.indexOf("\n") > 0)
      sb.append("\n}");
    else
      sb.append("}");
    return sb.toString();
  }
}
