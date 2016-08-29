package org.bksport.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author congnh
 * 
 */
public class GraphTermValidator {

  private static Pattern ANON_PATTERN;

  /**
   * validate specific <code>lang</code><br/>
   * <code>LANG ::= [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*</code>
   * 
   * @param lang
   * @return true if valid, false otherwise
   */
  public static boolean validateLang(String lang) {
    if (lang == null) {
      return false;
    } else {
      if (lang.charAt(0) == '@')
        return false;
      else
        return true;
    }
  }

  /**
   * validate specific <code>varName</code>
   * 
   * @param varName
   * @return true if valid, false otherwise
   */
  public static boolean validateVarName(String varName) {
    if (varName == null) {
      return false;
    } else {
      if (varName.startsWith("?") || varName.startsWith("$")) {
        return false;
      } else {
        return true;
      }
    }
  }

  /**
   * Validate specific <code>iri</code><br/>
   * <code>IRI ::= ([^<>"{}|^`\]-[#x00-#x20])*</code>
   * 
   * @param iri
   * @return true if valid, false otherwise
   */
  public static boolean validateIRI(String iri) {
    if (iri == null) {
      return false;
    } else {
      if (iri.startsWith("<") || iri.endsWith(">"))
        return false;
      else
        return true;
    }
  }

  public static boolean validateANON(String anon) {
    if (anon == null) {
      return false;
    } else {
      if (ANON_PATTERN == null) {
        ANON_PATTERN = Pattern.compile("\\[(\\x20|\\x09|\\x0D|\\x0A)*\\]");
      }
      Matcher m = ANON_PATTERN.matcher(anon);
      return m.matches();
    }
  }
}
