package org.bksport.qa;

import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.nlq.ResourceRecognizer;

/**
 * 
 * Main entry for program
 * 
 * @author congnh
 * 
 */
public class BKSportQA {

  static {
    ResourceRecognizer.loadResourceDataset();
  }

  /**
   * 
   * @param args
   *          the command line arguments
   * 
   */
  public static void main(String[] args) {
    ApplicationFacade.getInstance().startup();
  }
}
