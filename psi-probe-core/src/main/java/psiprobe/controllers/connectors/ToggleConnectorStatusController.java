
package psiprobe.controllers.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.beans.ContainerListenerBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class to use toggle connector status, like STATED, STOPPED.
 */
@Controller
public class ToggleConnectorStatusController extends ParameterizableViewController {

  /** The static logger. */
  private static final Logger log11 =
      LoggerFactory.getLogger(ToggleConnectorStatusController.class);


  public ToggleConnectorStatusController(ContainerListenerBean containerListener) {
    this.containerListener = containerListener;
  }

  /** The container listener. */
  private final ContainerListenerBean containerListener;

  @RequestMapping(path = "/app/connectorStatus.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String connectorName = ServletRequestUtils.getRequiredStringParameter(request, "cn");

    String port = ServletRequestUtils.getRequiredStringParameter(request, "port");

    String operation = ServletRequestUtils.getRequiredStringParameter(request, "operation");

    containerListener.toggleConnectorStatus(operation, port);

    log11.info("Connector status toggled for {}", connectorName);
    return new ModelAndView(new RedirectView(request.getContextPath() + getViewName()));
  }

  @Value("/connectors.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
