package org.bksport.qa.nlq;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bksport.qa.util.FileUtil;
import org.bksport.sparql.IRI;
import org.bksport.sparql.Node;
import org.bksport.sparql.Triple;

public class PatternRecognizer {

  private static Logger       logger     = Logger
                                             .getLogger(PatternRecognizer.class);
  private static List<Triple> tripleList = new ArrayList<Triple>();

  public static void loadPatternDataset() {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(FileUtil.getAbsolutePath("data",
              "bksport-pattern-dataset.csv"))));
      String line = null;
      int i = 1;
      br.readLine();// skip first line
      while ((line = br.readLine()) != null) {
        i++;
        String[] nArr = line.split(",");
        if (nArr.length != 3) {
          logger.error("Invalid format of pattern dataset at line " + i);
        } else {
          Node sub = new IRI(nArr[0].substring(2, nArr[0].length() - 2));
          Node pre = new IRI(nArr[1].substring(2, nArr[1].length() - 2));
          Node obj = new IRI(nArr[2].substring(2, nArr[2].length() - 2));
          tripleList.add(new Triple(sub, pre, obj));
          System.out.println(tripleList.get(tripleList.size() - 1));
        }
      }
      br.close();
    } catch (IOException ex) {
      logger.error(ExceptionUtils.getStackTrace(ex));
    }
  }

  public static boolean recognize(TripleEntry t) {
    for (int i = 0; i < tripleList.size(); i++) {
      // Triple tn = tripleList.get(i);
    }
    return false;
  }

  public static void main(String agrs[]) {
    loadPatternDataset();
  }
}
