package org.bksport.qa.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * 
 * Utilities for file
 * 
 * @author congnh
 * 
 */
public class FileUtil {

  /**
   * HTML's {@link FileFilter}
   */
  public static FileFilter htmlFilter = new FileFilter() {

                                        @Override
                                        public boolean accept(File f) {
                                          return f.isDirectory()
                                              || f.getName().endsWith(".htm")
                                              || f.getName().endsWith(".html")
                                              || f.getName().endsWith(".xhtml");
                                        }

                                        @Override
                                        public String getDescription() {
                                          return "Hyper Text Markup Language file(*.htm, *.html, *.xhtml)";
                                        }

                                      };

  /**
   * XML's {@link FileFilter}
   */
  public static FileFilter xmlFilter  = new FileFilter() {

                                        @Override
                                        public boolean accept(File f) {
                                          if (f.isDirectory()) {
                                            return true;
                                          }
                                          if (f.getAbsolutePath().endsWith(
                                              ".xml")) {
                                            return true;
                                          }
                                          if (f.getAbsolutePath().endsWith(
                                              ".xsl")) {
                                            return true;
                                          }
                                          if (f.getAbsolutePath().endsWith(
                                              ".xsd")) {
                                            return true;
                                          }
                                          return false;
                                        }

                                        @Override
                                        public String getDescription() {
                                          return "eXtensible Markup Language file(*.xml, *.xsl, *. xsd)";
                                        }
                                      };

  /**
   * Ontology's {@link FileFilter}
   */
  public static FileFilter ontFilter  = new FileFilter() {

                                        @Override
                                        public boolean accept(File f) {
                                          if (f.isDirectory()) {
                                            return true;
                                          }
                                          if (f.getAbsolutePath().endsWith(
                                              ".rdf")) {
                                            return true;
                                          }
                                          if (f.getAbsolutePath().endsWith(
                                              ".nt")) {
                                            return true;
                                          }
                                          if (f.getAbsolutePath().endsWith(
                                              ".owl")) {
                                            return true;
                                          }
                                          return false;
                                        }

                                        @Override
                                        public String getDescription() {
                                          return "Ontology file(*.rdf, *.nt, *. owl)";
                                        }
                                      };

  /**
   * <p>
   * get file/directory in <code>System.getProperty("user.dir")</code> path
   * </p>
   * 
   * @param relPaths
   *          relative paths, e.g. "/dir1/dir2", "file1", "dirorfile1"
   * @return System.getProperty("user.dir") if failed
   */
  public static String getAbsolutePath(String... relPaths) {
    StringBuilder result = new StringBuilder();
    result.append(System.getProperty("user.dir"));
    for (int i = 0; i < relPaths.length; i++) {
      result.append(File.separatorChar);
      result.append(relPaths[i]);
    }
    String c = File.separatorChar == '\\' ? "/" : "\\";
    int i;
    while ((i = result.indexOf(c)) != -1) {
      result.replace(i, i + 1, File.separator);
    }
    while ((i = result.indexOf(File.separator + File.separator)) != -1) {
      result.replace(i, i + 2, File.separator);
    }
    return result.toString();
  }

  public static List<String> readFileAsList(String filePath) {
    List<String> result = new ArrayList<String>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(filePath));
      while (br.ready()) {
        result.add(br.readLine());
      }
      br.close();
    } catch (FileNotFoundException ex) {
      Logger.getLogger(FileUtil.class).error(ExceptionUtils.getStackTrace(ex));
    } catch (IOException ex) {
      Logger.getLogger(FileUtil.class).error(ExceptionUtils.getStackTrace(ex));
    }
    return result;
  }
}