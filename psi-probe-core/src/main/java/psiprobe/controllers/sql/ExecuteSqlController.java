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
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

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

    int maxRows = ServletRequestUtils.getIntParameter(request, "maxRows", 0);
    int rowsPerPage = ServletRequestUtils.getIntParameter(request, "rowsPerPage", 0);
    int historySize = ServletRequestUtils.getIntParameter(request, "historySize", 0);

    // store current option values and query history in a session attribute

    HttpSession sess = request.getSession(false);
    DataSourceTestInfo sessData =
        (DataSourceTestInfo) sess.getAttribute(DataSourceTestInfo.DS_TEST_SESS_ATTR);

    synchronized (sess) {
      if (sessData == null) {
        sessData = new DataSourceTestInfo();
        sess.setAttribute(DataSourceTestInfo.DS_TEST_SESS_ATTR, sessData);
      }

      sessData.setMaxRows(maxRows);
      sessData.setRowsPerPage(rowsPerPage);
      sessData.setHistorySize(historySize);
      sessData.addQueryToHistory(sql);
    }

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

    if (dataSource == null) {
      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null) {
        request.setAttribute(errorMessageString, messageSourceAccessor.getMessage(
            "probe.src.dataSourceTest.resource.lookup.failure", new Object[] {resourceName}));
      } else {

        request.setAttribute(errorMessageString, errorString);
      }
    } else {
      List<Map<String, String>> results = null;
      int rowsAffected;
      
      

      try {

        try (Connection conn = dataSource.getConnection()) {
          conn.setAutoCommit(true);

          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            boolean hasResultSet = stmt.execute();

            if (!hasResultSet) {
              rowsAffected = stmt.getUpdateCount();
            } else {
              results = new ArrayList<>();

              try (ResultSet rs = stmt.getResultSet()) {
                ResultSetMetaData metaData = rs.getMetaData();

                while (rs.next() && (maxRows < 0 || results.size() < maxRows)) {
                  Map<String, String> record1 = new LinkedHashMap<>();

                  for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String value = rs.getString(i);

                    if (rs.wasNull()) {
                      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
                      if (messageSourceAccessor != null) {
                        value =
                            messageSourceAccessor.getMessage("probe.src.dataSourceTest.sql.null");
                      } else {
                        value = "Null value"; // Ometti questo passaggio se non hai un valore di
                                              // fallback specifico
                      }
                    } else {
                      value = HtmlUtils.htmlEscape(value);
                    }

                    // a work around for IE browsers bug of not displaying
                    // a border around an empty table column

                    if (value.isEmpty()) {
                      value = "&nbsp;";
                    }

                    // Pad the keys of columns with existing labels so they are distinct
                    StringBuilder key = new StringBuilder(metaData.getColumnLabel(i));
                    while (record1.containsKey(key.toString())) {
                      key.append(" ");
                    }
                    record1.put(HtmlUtils.htmlEscape(key.toString()), value);
                  }

                  results.add(record1);
                }
              }

              rowsAffected = results.size();
            }
          }
        }

        // store the query results in the session attribute in order
        // to support a result set pagination feature without re-executing the query

        synchronized (sess) {
          sessData.setResults(results);
        }

          assert results != null;
          ModelAndView mv = new ModelAndView(getViewName(), "results", results);
        mv.addObject("rowsAffected", String.valueOf(rowsAffected));
        mv.addObject("rowsPerPage", String.valueOf(rowsPerPage));

        return mv;
      } catch (SQLException e) {
        MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
        if (messageSourceAccessor != null) {
          String message = messageSourceAccessor.getMessage("probe.src.dataSourceTest.sql.failure",
              new Object[] {e.getMessage()});
          mylogger.error(message, e);
          request.setAttribute(errorMessageString, message);
        } else {
          // Gestione alternativa nel caso in cui getMessageSourceAccessor() sia nullo
          String errorMessage = "Error during resource lookup.";
          mylogger.error(errorMessage, e);

          request.setAttribute(errorMessageString, errorMessage);
        }
      }
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
