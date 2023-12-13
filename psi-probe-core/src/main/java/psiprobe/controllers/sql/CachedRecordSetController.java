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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.PostParameterizableViewController;
import psiprobe.model.sql.DataSourceTestInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Displays a result set cached in an attribute of HttpSession object to support result set
 * pagination feature without re-executing a query that created the result set.
 */
@Controller
public class CachedRecordSetController extends PostParameterizableViewController {

  /** The Constant logger. */
  private static final Logger mylogger = LoggerFactory.getLogger(CachedRecordSetController.class);

  @GetMapping(path = "/sql/cachedRecordset.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }
  private static final String MESSAGE_FAILED = "Failed to get message source accessor. Failure in cached result set test.";

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {

    int rowsPerPage = ServletRequestUtils.getIntParameter(request, "rowsPerPage", 0);
    List<Map<String, String>> results = null;
    int rowsAffected = 0;
    HttpSession sess = request.getSession(false);
    String errorMessageString = "errorMessage";
    String probeSrcString = "probe.src.dataSourceTest.cachedResultSet.failure";

    if (sess == null) {
      handleSessionNull(request, errorMessageString, probeSrcString);
    } else {
      DataSourceTestInfo sessData = (DataSourceTestInfo) sess.getAttribute(DataSourceTestInfo.DS_TEST_SESS_ATTR);
      if (sessData == null) {
        handleSessionDataNull(request, errorMessageString, probeSrcString);
      } else {
        synchronized (sess) {
          sessData.setRowsPerPage(rowsPerPage);
        }

        results = sessData.getResults();

        if (results == null) {
          handleResultsNull(request, errorMessageString, probeSrcString);
        } else {
          rowsAffected = results.size();
        }
      }
    }

    assert results != null;
    ModelAndView mv = new ModelAndView(Objects.requireNonNull(getViewName()), "results", results);
    mv.addObject("rowsAffected", String.valueOf(rowsAffected));
    mv.addObject("rowsPerPage", String.valueOf(rowsPerPage));

    return mv;
  }

  private void handleSessionNull(HttpServletRequest request, String errorMessageString, String probeSrcString) {
    MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
    if (messageSourceAccessor != null) {
      request.setAttribute(errorMessageString, messageSourceAccessor.getMessage(probeSrcString));
    } else {
      request.setAttribute(errorMessageString, MESSAGE_FAILED);
    }
    mylogger.error("Cannot retrieve a cached result set. Http session is NULL.");
  }

  private void handleSessionDataNull(HttpServletRequest request, String errorMessageString, String probeSrcString) {
    MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
    if (messageSourceAccessor != null) {
      request.setAttribute(errorMessageString, messageSourceAccessor.getMessage(probeSrcString));
    } else {
      request.setAttribute(errorMessageString, MESSAGE_FAILED);
    }
    mylogger.error("Cannot retrieve a cached result set. {} session attribute is NULL.", DataSourceTestInfo.DS_TEST_SESS_ATTR);
  }

  private void handleResultsNull(HttpServletRequest request, String errorMessageString, String probeSrcString) {
    MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
    if (messageSourceAccessor != null) {
      request.setAttribute(errorMessageString, messageSourceAccessor.getMessage(probeSrcString));
    } else {
      request.setAttribute(errorMessageString, MESSAGE_FAILED);
    }
    mylogger.error("Cached results set is NULL.");
  }

  @Value("ajax/sql/recordset")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
