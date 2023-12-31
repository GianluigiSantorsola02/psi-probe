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
package psiprobe.controllers.sql;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import psiprobe.controllers.AbstractContextHandlerController;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Verifies if a database connection can be established through a given datasource. Displays basic
 * information about the database.
 */
@Controller
public class ConnectionTestController extends AbstractContextHandlerController {
  private static final String ERROR_MESSAGE_KEY1 = "errorMessage";

  /** The Constant logger. */
  private static final Logger log6 = LoggerFactory.getLogger(ConnectionTestController.class);

  @GetMapping(path = "/sql/connection.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ServletRequestBindingException {

    String resourceName = ServletRequestUtils.getStringParameter(request, "resource");
    // Validate the input (example: allow only alphanumeric characters and underscores)
      if (resourceName == null) {
          throw new AssertionError();
      }
      if (!Objects.requireNonNull(resourceName).matches("^\\w+$")) {
      // Handle invalid input, e.g., throw an exception or provide a default value
      throw new IllegalArgumentException("Invalid resource name");
    }

    // Sanitize the input
    resourceName = HtmlUtils.htmlEscape(resourceName);

    DataSource dataSource = null;

    try {
      dataSource = getContainerWrapper().getResourceResolver().lookupDataSource(context,
          resourceName, getContainerWrapper());
    } catch (NamingException e) {
      request.setAttribute(ERROR_MESSAGE_KEY1, Objects.requireNonNull(getMessageSourceAccessor()).getMessage(
          "probe.src.dataSourceTest.resource.lookup.failure", new Object[] {resourceName}));
      log6.trace("", e);
    }

    if (dataSource == null) {
      request.setAttribute(ERROR_MESSAGE_KEY1, Objects.requireNonNull(getMessageSourceAccessor()).getMessage(
          "probe.src.dataSourceTest.resource.lookup.failure", new Object[] {resourceName}));
    } else {
      try {
        try (Connection conn = dataSource.getConnection()) {
          DatabaseMetaData md = conn.getMetaData();

          List<Map<String, String>> dbMetaData = new ArrayList<>();

          addDbMetaDataEntry(dbMetaData, "probe.jsp.dataSourceTest.dbMetaData.dbProdName",
              md.getDatabaseProductName());
          addDbMetaDataEntry(dbMetaData, "probe.jsp.dataSourceTest.dbMetaData.dbProdVersion",
              md.getDatabaseProductVersion());
          addDbMetaDataEntry(dbMetaData, "probe.jsp.dataSourceTest.dbMetaData.jdbcDriverName",
              md.getDriverName());
          addDbMetaDataEntry(dbMetaData, "probe.jsp.dataSourceTest.dbMetaData.jdbcDriverVersion",
              md.getDriverVersion());
          addDbMetaDataEntry(dbMetaData, "probe.jsp.dataSourceTest.dbMetaData.jdbcVersion",
              String.valueOf(md.getJDBCMajorVersion()));

          return new ModelAndView(getViewName(), "dbMetaData", dbMetaData);
        }
      } catch (SQLException e) {
        String message;
        if (getMessageSourceAccessor() != null) {
          message = getMessageSourceAccessor().getMessage(
              "probe.src.dataSourceTest.connection.failure", new Object[] {e.getMessage()});
        } else {
          message = "Error occurred while getting the error message.";
        }
        log6.error(message, e);
        request.setAttribute(ERROR_MESSAGE_KEY1, message);
      }
    }

    return new ModelAndView(getViewName());
  }

  @Override
  protected boolean isContextOptional() {
    return true;
  }

  /**
   *
   * Adds the db metadata entry.
   *
   * @param list the list
   * @param name the name
   * @param value the value
   */
  private void addDbMetaDataEntry(List<Map<String, String>> list, String name, String value) {
    Map<String, String> entry = new LinkedHashMap<>();
    entry.put("propertyName", Objects.requireNonNull(Objects.requireNonNull(getMessageSourceAccessor())).getMessage(name));
    entry.put("propertyValue", value);
    list.add(entry);
  }

  @Value("ajax/sql/connection")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
