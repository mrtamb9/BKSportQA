package org.bksport.qa.mvc.control;

import javax.swing.SwingUtilities;

import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.mvc.model.ConfigProxy;
import org.bksport.qa.mvc.view.ContainerMediator;
import org.bksport.qa.mvc.view.ui.ContainerFrame;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

public class StartupCommand extends SimpleCommand {

  public void execute(INotification notification) {
    facade.registerCommand(ApplicationFacade.PARSE_QUESTION_CMD,
        new ParseQuestionCommand());
    facade.registerCommand(ApplicationFacade.QUERY_RESULT_CMD,
        new QueryResultCommand());
    facade.registerCommand(ApplicationFacade.LOG_CMD, new LogCommand());

    ConfigCommand confCmd = new ConfigCommand();
    facade.registerCommand(ApplicationFacade.READ_CONF_CMD, confCmd);
    facade.registerCommand(ApplicationFacade.WRITE_CONF_CMD, confCmd);
    facade.registerProxy(new ConfigProxy(ApplicationFacade.CONF_PROXY));

    facade.removeCommand(ApplicationFacade.STARTUP_CMD);
    sendNotification(ApplicationFacade.READ_CONF_CMD);

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        ContainerFrame containerFrame = new ContainerFrame();
        containerFrame.setVisible(true);
        containerFrame.setSize(600, 400);
        containerFrame.setLocationRelativeTo(null);
        ContainerMediator containerMediator = new ContainerMediator(
            containerFrame);
        facade.registerMediator(containerMediator);
      }
    });
  }

}
