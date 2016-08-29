package org.bksport.qa.mvc;

import org.bksport.qa.mvc.control.ShutdownCommand;
import org.bksport.qa.mvc.control.StartupCommand;
import org.puremvc.java.patterns.facade.Facade;

public class ApplicationFacade extends Facade {

  public static final String STARTUP_CMD        = "startup";
  public static final String SHUTDOWN_CMD       = "shutdown";

  public static final String PARSE_QUESTION_CMD = "parse question";
  public static final String PARSING_QUESTION   = "parsing question";
  public static final String QUESTION_PARSED    = "question parsed";

  public static final String QUERY_RESULT_CMD   = "query result";
  public static final String QUERYING_RESULT    = "quering result";
  public static final String RESULT_QUERIED     = "result queried";

  public static final String LOG_CMD            = "log";
  public static final String LOGGED             = "logged";

  public static final String READ_CONF_CMD      = "read configuration";
  public static final String WRITE_CONF_CMD     = "write configuration";
  public static final String CONF_PROXY         = "configuration proxy";

  public ApplicationFacade() {
    super();
  }

  public static ApplicationFacade getInstance() {
    if (instance == null) {
      instance = new ApplicationFacade();
    }
    return (ApplicationFacade) instance;
  }

  public void startup() {
    sendNotification(STARTUP_CMD);
  }

  public void shutdown() {
    sendNotification(SHUTDOWN_CMD);
  }

  @Override
  protected final void initializeController() {
    super.initializeController();
    registerCommand(STARTUP_CMD, new StartupCommand());
    registerCommand(SHUTDOWN_CMD, new ShutdownCommand());
  }

}
