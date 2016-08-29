package org.bksport.qa.mvc.view;

import java.util.List;

import org.bksport.qa.mvc.ApplicationFacade;
import org.bksport.qa.mvc.view.ui.ContainerFrame;
import org.bksport.sparql.Query;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class ContainerMediator extends Mediator {

  public static final String NAME = "MainMediator";

  public ContainerMediator(ContainerFrame container) {
    super(NAME, container);
  }

  @Override
  public String[] listNotificationInterests() {
    return new String[] { ApplicationFacade.PARSING_QUESTION,
        ApplicationFacade.QUESTION_PARSED, ApplicationFacade.QUERYING_RESULT,
        ApplicationFacade.RESULT_QUERIED, ApplicationFacade.LOGGED };
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleNotification(INotification notification) {
    String name = notification.getName();
    if (name.equals(ApplicationFacade.PARSING_QUESTION)) {
      ((ContainerFrame) getViewComponent()).updateStatus("Parsing question");
      ((ContainerFrame) getViewComponent()).updateProgress(-1);
    } else if (name.equals(ApplicationFacade.QUESTION_PARSED)) {
      ((ContainerFrame) getViewComponent()).setQuery((Query) notification
          .getBody());
      ((ContainerFrame) getViewComponent()).updateProgress(0);
      ((ContainerFrame) getViewComponent()).updateStatus("Question parsed");
    } else if (name.equals(ApplicationFacade.QUERYING_RESULT)) {
      ((ContainerFrame) getViewComponent()).updateProgress(-1);
      ((ContainerFrame) getViewComponent()).updateStatus("Querying result");
    } else if (name.equals(ApplicationFacade.RESULT_QUERIED)) {
      ((ContainerFrame) getViewComponent())
          .setResult((List<Object[]>) notification.getBody());
      ((ContainerFrame) getViewComponent()).updateProgress(0);
      ((ContainerFrame) getViewComponent()).updateStatus("Result queried");
    } else if (name.equals(ApplicationFacade.LOGGED)) {
      ((ContainerFrame) getViewComponent()).setLog((String) notification
          .getBody());
    }
  }

}
