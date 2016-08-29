package org.bksport.qa.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * 
 * Utilities for configuration
 * 
 * @author congnh
 * 
 */
public class ConfigUtil {

  public static String AG_SERVER_HOST     = "172.245.22.156";
  public static int    AG_SERVER_PORT     = 10035;
  public static String AG_SERVER_USER     = "congnh";
  public static String AG_SERVER_PASSWORD = "a@2a@2";
  public static String KIM_SERVER_HOST    = "localhost";
  public static int    KIM_SERVER_PORT    = 1099;

  public static void reload() {
    System.setProperty(
        "log4j.configuration",
        "file:/"
            + FileUtil.getAbsolutePath("conf", "log4j.properties").replace('\\', '/'));
    try {
      FileInputStream fis = new FileInputStream(FileUtil.getAbsolutePath(
          "conf", "config.properties"));
      Properties properties = new Properties();
      properties.load(fis);
      AG_SERVER_HOST = properties.getProperty("AG_SERVER_HOST");
      AG_SERVER_PORT = Integer.parseInt(properties
          .getProperty("AG_SERVER_PORT"));
      AG_SERVER_USER = properties.getProperty("AG_SERVER_USER");
      AG_SERVER_PASSWORD = properties.getProperty("AG_SERVER_PASSWORD");
    } catch (FileNotFoundException ex) {
      Logger.getLogger(ConfigUtil.class)
          .error(ExceptionUtils.getStackTrace(ex));
    } catch (IOException ex) {
      Logger.getLogger(ConfigUtil.class)
          .error(ExceptionUtils.getStackTrace(ex));
    } catch (NumberFormatException ex) {
      Logger.getLogger(ConfigUtil.class)
          .error(ExceptionUtils.getStackTrace(ex));
    }
  }

}
