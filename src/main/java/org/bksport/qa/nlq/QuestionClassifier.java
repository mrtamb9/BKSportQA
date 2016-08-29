package org.bksport.qa.nlq;

import edu.stanford.nlp.trees.Tree;

/**
 * 
 * Classifying questions to categories
 * 
 * @author congnh
 * 
 */
public class QuestionClassifier {

  public static final int ENTITY_QUESTION        = 0x000001;
  public static final int DESCRIPTION_QUESTION   = 0x000010;
  public static final int HUMAN_QUESTION         = 0x000100;
  public static final int LOCATION_QUESTION      = 0x001000;
  public static final int NUMERIC_VALUE_QUESTION = 0x010000;
  public static final int UNKNOWN_QUESTION       = 0x100000;

  /**
   * <p>
   * Classify specific question to categories
   * </p>
   * 
   * @param questionTree
   * @return
   */
  public static int classify(Tree questionTree) {
    return UNKNOWN_QUESTION;
  }

  public static boolean isQuestionWord(String word) {
    if (word == null) {
      return false;
    } else {
      String wordLC = word.toLowerCase();
      if (wordLC.equals("what") || wordLC.equals("when")
          || wordLC.equals("where") || wordLC.equals("which")
          || wordLC.equals("who") || wordLC.equals("whose")
          || wordLC.equals("why") || wordLC.equals("which")) {
        return true;
      } else {
        return false;
      }
    }
  }

}
