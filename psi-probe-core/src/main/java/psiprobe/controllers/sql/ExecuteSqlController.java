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
/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.controllers.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import psiprobe.controllers.AbstractContextHandlerController;
import psiprobe.model.sql.DataSourceTestInfo;

/**
 * Executes an SQL query through a given datasource to test database connectivity. Displays results
 * returned by the query.
 */
@Controller
public class ExecuteSqlController extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger mylogger = LoggerFactory.getLogger(ExecuteSqlController.class);

  @RequestMapping(path = "/sql/record1set.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ServletRequestBindingException {

    String resourceName = ServletRequestUtils.getStringParameter(request, "resource");
    String errorString = "Error during resource lookup.";
    String sql = ServletRequestUtils.getStringParameter(request, "sql", "");
    final String errorMessageString = "errorMessage";
    if (sql.isEmpty() || sql.trim().isEmpty()) {
      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null) {
        request.setAttribute(errorMessageString,
            messageSourceAccessor.getMessage("probe.src.dataSourceTest.sql.required"));
      } else {
        // Gestione alternativa nel caso in cui getMessageSourceAccessor() sia nullo
        request.setAttribute(errorMessageString, errorString);
      }
      return new ModelAndView(getViewName());
    }


    // store current option values and query history in a session attribute

    HttpSession sess = request.getSession(false);
    DataSourceTestInfo sessData =
        (DataSourceTestInfo) sess.getAttribute(DataSourceTestInfo.DS_TEST_SESS_ATTR);


    DataSource dataSource = null;

    try {
      dataSource = getContainerWrapper().getResourceResolver().lookupDataSource(context,
          resourceName, getContainerWrapper());
    } catch (NamingException e) {
      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null) {
        request.setAttribute(errorMessageString, messageSourceAccessor.getMessage(
            "probe.src.dataSourceTest.resource.lookup.failure", new Object[] {resourceName}));
      } else {
        // Gestione alternativa nel caso in cui getMessageSourceAccessor() sia nullo
        request.setAttribute(errorMessageString, errorString);
      }
      mylogger.trace("", e);
    }

    return new ModelAndView(getViewName());
  }

  @Override
  protected boolean isContextOptional() {
    return true;
  }

  @Value("ajax/sql/record1set")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
