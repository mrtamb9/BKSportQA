package org.bksport.qa.mvc.control;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.mvc.model.ConfigProxy;
import org.bksport.qa.util.FileUtil;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

public class ConfigCommand extends SimpleCommand {

  @Override
  public void execute(INotification notification) {
    ConfigProxy confProxy = (ConfigProxy) facade
        .retrieveProxy(ApplicationFacade.CONF_PROXY);
    if (notification.getName().equals(ApplicationFacade.READ_CONF_CMD)) {
      try {
        FileInputStream fis = new FileInputStream(FileUtil.getAbsolutePath(
            "conf", "config.properties"));
        Properties properties = new Properties();
        properties.load(fis);
        confProxy.setAGHost(properties.getProperty("AG_SERVER_HOST",
            "172.245.22.156"));
        confProxy.setAGPort(Integer.parseInt(properties.getProperty(
            "AG_SERVER_PORT", "10035")));
        confProxy.setAGUsername(properties.getProperty("AG_SERVER_USER",
            "congnh"));
        confProxy.setAGPassword(properties.getProperty("AG_SERVER_PASSWORD",
            "a@2a@2"));
        confProxy.setKIMHost(properties.getProperty("KIM_SERVER_HOST",
            "localhost"));
        confProxy.setKIMPort(Integer.parseInt(properties.getProperty(
            "KIM_SERVER_PORT", "1099")));
        fis.close();
      } catch (FileNotFoundException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      } catch (IOException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      } catch (NumberFormatException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      }
    } else if (notification.getName().equals(ApplicationFacade.WRITE_CONF_CMD)) {
      try {
        FileOutputStream fos = new FileOutputStream(FileUtil.getAbsolutePath(
            "conf", "config.properties"));
        Properties properties = new Properties();
        properties.put("AG_SERVER_HOST", confProxy.getAGHost());
        properties.put("AG_SERVER_PORT", confProxy.getAGPort());
        properties.put("AG_SERVER_USER", confProxy.getAGUsername());
        properties.put("KIM_SERVER_HOST", confProxy.getKIMHost());
        properties.put("KIM_SERVER_PORT", confProxy.getKIMPort());
        properties.store(fos, "# Allegrograph server's configuration");
        fos.close();
      } catch (FileNotFoundException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      } catch (IOException e) {
        Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(e));
      }

    }
  }
}