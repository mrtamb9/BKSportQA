package org.bksport.qa.mvc.control;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.tmp.NLQParser;
import org.bksport.qa.nlq.NLQParser2;
import org.bksport.sparql.Query;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.command.SimpleCommand;

/**
 * 
 * @author congnh
 * 
 */
public class ParseQuestionCommand extends SimpleCommand implements Runnable {

  private String question = null;

  @Override
  public void execute(INotification notification) {
    question = notification.getBody().toString();
    Thread thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    sendNotification(ApplicationFacade.PARSING_QUESTION);
    try {
      Query query = NLQParser2.parse2(question);
      //Query query = NLQParser.parse(question);
      sendNotification(ApplicationFacade.QUESTION_PARSED, query);      
    } catch (Exception ex) {
      Logger.getLogger(getClass()).error(ExceptionUtils.getStackTrace(ex));
    }
  }
}
