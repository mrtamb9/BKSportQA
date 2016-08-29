package org.bksport.qa.mvc.control;

import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

public class ShutdownCommand extends SimpleCommand {

  public void execute(INotification notification) {
    System.exit(0);
  }

}
