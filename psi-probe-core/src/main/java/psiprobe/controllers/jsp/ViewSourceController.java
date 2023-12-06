/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.controllers.jsp;

import org.apache.catalina.Context;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.Utils;
import psiprobe.controllers.AbstractContextHandlerController;
import psiprobe.model.jsp.Item;
import psiprobe.model.jsp.Summary;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class ViewSourceController.
 */
@Controller
public class ViewSourceController extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger viewSourceLogger = LoggerFactory.getLogger(ViewSourceController.class);
  @RequestMapping(path = "/app/viewsource.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

    Summary summary = getSessionSummary(request);

    if (shouldHandleContext(summary, contextName)) {
      handleContextAction(contextName, summary, request, context);
    }

    return new ModelAndView(getViewName());
  }

  private boolean shouldHandleContext(Summary summary, String contextName) {
    return summary != null && contextName.equals(summary.getName());
  }

  private void handleContextAction(String contextName, Summary summary,
                                   HttpServletRequest request, Context context) throws IOException {
    String jspName = ServletRequestUtils.getStringParameter(request, "source", "");
    boolean highlight = ServletRequestUtils.getBooleanParameter(request, "highlight", true);
    Item item = getSummaryItem(summary, jspName);

    if (item != null) {
      jspName = sanitizeJspName(jspName);

      if (getContainerWrapper().getTomcatContainer().resourceExists(jspName, context)) {
        String descriptorPageEncoding = getDescriptorPageEncoding(jspName, context);
        String encoding = (descriptorPageEncoding != null && !descriptorPageEncoding.isEmpty())
                ? descriptorPageEncoding : getJspEncoding(jspName, context);

        if (highlight) {
          request.setAttribute("highlightedContent", highlightJspContent(jspName, encoding, context));
        } else {
          request.setAttribute("content", readJspContent(jspName, encoding, context));
        }
      } else {
        viewSourceLogger.error("{} does not exist", jspName);
      }

      request.setAttribute("item", item);
    } else {
      viewSourceLogger.error("jsp name passed is not in the summary, ignored");
    }
  }
  private Summary getSessionSummary(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return (session != null) ? (Summary) session.getAttribute(DisplayJspController.SUMMARY_ATTRIBUTE) : null;
  }

  private Item getSummaryItem(Summary summary, String jspName) {
    return summary.getItems().get(jspName);
  }

  private String sanitizeJspName(String jspName) {
    jspName = jspName.replace('\\', '/');
    while (jspName.contains("../")) {
      jspName = jspName.replace("../", "");
    }
    return jspName;
  }

  private String getDescriptorPageEncoding(String jspName, Context context) {
    ServletConfig scfg = (ServletConfig) context.findChild("jsp");
    Options opt = new EmbeddedServletOptions(scfg, context.getServletContext());
    return opt.getJspConfig().findJspProperty(jspName).getPageEncoding();
  }

  private String getJspEncoding(String jspName, Context context) throws IOException {
    try (InputStream encodedStream = getContainerWrapper().getTomcatContainer().getResourceStream(jspName, context)) {
      return Utils.getJspEncoding(encodedStream);
    }
  }

  private String highlightJspContent(String jspName, String encoding, Context context) throws IOException {
    try (InputStream jspStream = getContainerWrapper().getTomcatContainer().getResourceStream(jspName, context)) {
      return Utils.highlightStream(jspName, jspStream, "xhtml", encoding);
    }
  }

  private String readJspContent(String jspName, String encoding, Context context) throws IOException {
    try (InputStream jspStream = getContainerWrapper().getTomcatContainer().getResourceStream(jspName, context)) {
      return Utils.readStream(jspStream, encoding);
    }
  }

  @Value("view_jsp_source")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
