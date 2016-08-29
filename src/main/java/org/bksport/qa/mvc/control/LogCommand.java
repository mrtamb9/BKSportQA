package org.bksport.qa.mvc.control;

import org.bksport.qa.mvc.ApplicationFacade;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

public class LogCommand extends SimpleCommand {

  @Override
  public void execute(INotification notification) {
    sendNotification(ApplicationFacade.LOGGED, notification.getBody());
  }

}
