package org.bksport.qa.nlq;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bksport.qa.util.FileUtil;

public class SynRecognizer {

  private Dictionary dictionary;

  public SynRecognizer() {
    try {
      JWNL.initialize(new FileInputStream(FileUtil.getAbsolutePath("conf",
          "jwnl", "file_properties.xml")));
      dictionary = Dictionary.getInstance();
    } catch (FileNotFoundException e) {
      Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
    } catch (JWNLException e) {
      Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
    }
  }

  public List<String> recognize(String word, String tagName) {
    List<String> rtList = new ArrayList<String>();
    IndexWord indexWord = null;
    Synset[] set = null;
    if (tagName.startsWith("NNP")) {
      try {
        indexWord = dictionary.getIndexWord(POS.NOUN, word);
        set = indexWord.getSenses();
      } catch (JWNLException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      }
    } else if (tagName.startsWith("V")) {
      try {
        indexWord = dictionary.getIndexWord(POS.VERB, word);
        set = indexWord.getSenses();
      } catch (JWNLException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      }
    } else if (tagName.startsWith("JJ")) {
      try {
        indexWord = dictionary.getIndexWord(POS.ADJECTIVE, word);
        set = indexWord.getSenses();
      } catch (JWNLException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      }
    } else if (tagName.startsWith("RB")) {
      try {
        indexWord = dictionary.getIndexWord(POS.ADVERB, word);
        set = indexWord.getSenses();
      } catch (JWNLException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      }
    } else {

    }
    if (set != null) {
      for (int i = 0; i < set.length; i++) {
        for (int j = 0; j < set[i].getWordsSize(); j++) {
          rtList.add(set[i].getWord(j).getLemma());
        }
      }
    }
    return rtList;
  }

}
