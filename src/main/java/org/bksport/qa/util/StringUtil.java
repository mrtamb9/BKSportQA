package org.bksport.qa.util;

public class StringUtil {

  public static String shiftRight(String s) {
    if (s == null)
      return s;
    else {
      String[] split = s.split("\n|\r");
      StringBuilder sb = new StringBuilder();
      if (split.length > 0) {
        sb.append("  ").append(split[0]);
        for (int i = 1; i < split.length; i++)
          sb.append("\n  ").append(split[i]);
      }
      return sb.toString();
    }
  }

  public static String shiftLeft(String s) {
    if (s == null)
      return s;
    else {
      String[] split = s.split("\n|\r");
      StringBuilder sb = new StringBuilder();
      if (split.length > 0) {
        if (split[0].startsWith("  "))
          sb.append(split[0].substring(2));
        else
          sb.append(split[0]);
        for (int i = 1; i < split.length; i++)
          if (split[i].startsWith("  "))
            sb.append("\n").append(split[i].substring(2));
          else
            sb.append("\n").append(split[i]);
      }
      return sb.toString();
    }
  }
}
