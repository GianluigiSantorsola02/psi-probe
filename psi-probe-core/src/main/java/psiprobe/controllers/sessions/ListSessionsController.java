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
package psiprobe.controllers.sessions;

import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.controllers.AbstractContextHandlerController;
import psiprobe.model.ApplicationSession;
import psiprobe.model.Attribute;
import psiprobe.model.SessionSearchInfo;
import psiprobe.tools.ApplicationUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class ListSessionsController extends AbstractContextHandlerController {

  @GetMapping(path = "/sessions.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ServletRequestBindingException {

    SessionSearchInfo searchInfo = createSessionSearchInfo(request);
    HttpSession sess = request.getSession(false);
    handleSearchInfo(searchInfo, sess);
    List<Context> ctxs = getContexts(context);
    List<ApplicationSession> sessionList = getSessionList(ctxs, searchInfo);

    handleEmptySessionList(sessionList, searchInfo, sess);

    ModelAndView modelAndView = new ModelAndView(getViewName(), "sessions", sessionList);
    modelAndView.addObject("searchInfo", searchInfo);

    return modelAndView;
  }

  private void handleEmptySessionList(List<ApplicationSession> sessionList, SessionSearchInfo searchInfo, HttpSession sess) {
    if (sessionList.isEmpty()) {
      sess.setAttribute("searchInfo", searchInfo);
    }
  }

  private SessionSearchInfo createSessionSearchInfo(HttpServletRequest request) throws ServletRequestBindingException {
    SessionSearchInfo searchInfo = new SessionSearchInfo();
    searchInfo.setSearchAction(StringUtils.trimToNull(ServletRequestUtils
            .getStringParameter(request, "searchAction", SessionSearchInfo.ACTION_NONE)));
    String sessionId = StringUtils.trimToNull( "searchSessionId");
    searchInfo.setSessionId(sessionId);
    searchInfo.setLastIp(
            StringUtils.trimToNull(ServletRequestUtils.getStringParameter(request, "searchLastIP")));
    searchInfo.setAgeFrom(
            StringUtils.trimToNull(ServletRequestUtils.getStringParameter(request, "searchAgeFrom")));
    searchInfo.setAgeTo(
            StringUtils.trimToNull(ServletRequestUtils.getStringParameter(request, "searchAgeTo")));
    searchInfo.setIdleTimeFrom(StringUtils
            .trimToNull(ServletRequestUtils.getStringParameter(request, "searchIdleTimeFrom")));
    searchInfo.setIdleTimeTo(StringUtils
            .trimToNull(ServletRequestUtils.getStringParameter(request, "searchIdleTimeTo")));
    searchInfo.setAttrName(StringUtils.trimToNull(ServletRequestUtils
            .getStringParameter(request,"searchAttrName", SessionSearchInfo.ACTION_NONE)));

    return searchInfo;
  }

  private void handleSearchInfo(SessionSearchInfo searchInfo, HttpSession sess) {
    if (searchInfo.isApply()) {
      if (sess != null) {
        sess.setAttribute(SessionSearchInfo.SESS_ATTR_NAME, searchInfo);
      }
    } else if (sess != null) {
      if (searchInfo.isClear()) {
        sess.removeAttribute(SessionSearchInfo.SESS_ATTR_NAME);
      } else {
        SessionSearchInfo ss =
                (SessionSearchInfo) sess.getAttribute(SessionSearchInfo.SESS_ATTR_NAME);
        if (ss != null) {
          searchInfo.setSearchAction(ss.getSearchAction());
          searchInfo.setSessionId(ss.getSessionId());
          searchInfo.setLastIp(ss.getLastIp());
          searchInfo.setAgeFrom(ss.getAgeFrom());
          searchInfo.setAgeTo(ss.getAgeTo());
          searchInfo.setIdleTimeFrom(ss.getIdleTimeFrom());
          searchInfo.setIdleTimeTo(ss.getIdleTimeTo());
          searchInfo.setAttrName(ss.getAttrName());}
      }
    }
  }

  private List<Context> getContexts(Context context) {
    List<Context> ctxs;
    if (context == null) {
      ctxs = getContainerWrapper().getTomcatContainer().findContexts();
    } else {
      ctxs = new ArrayList<>();
      ctxs.add(context);
    }
    return ctxs;
  }

  private List<ApplicationSession> getSessionList(List<Context> ctxs, SessionSearchInfo searchInfo) {
    List<ApplicationSession> sessionList = new ArrayList<>();

    for (Context ctx : ctxs) {
      if (shouldAddSessions(ctx, searchInfo)) {
        Session[] sessions = getSessions(ctx);
        for (Session session : sessions) {
          ApplicationSession appSession = getApplicationSession(session, searchInfo);
          if (appSession != null && matchSession(appSession, searchInfo)) {
            setApplicationName(appSession, ctx);
            sessionList.add(appSession);
          }
        }
      }
    }

    return sessionList;
  }

  private boolean shouldAddSessions(Context ctx, SessionSearchInfo searchInfo) {
    return ctx != null && ctx.getManager() != null && (!searchInfo.isApply() || searchInfo.isUseSearch());
  }

  private Session[] getSessions(Context ctx) {
    return ctx.getManager().findSessions();
  }

  private ApplicationSession getApplicationSession(Session session, SessionSearchInfo searchInfo) {
    return ApplicationUtils.getApplicationSession(session, searchInfo.isUseAttr());
  }

  private void setApplicationName(ApplicationSession appSession, Context ctx) {
    if (ctx.getName() != null) {
      appSession.setApplicationName(!ctx.getName().isEmpty() ? ctx.getName() : "/");
    }
  }

  /**
   * Match session.
   *
   * @param appSession the app session
   * @param searchInfo the search info
   *
   * @return true, if successful
   */
  private boolean matchSession(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    if (!searchInfo.isUseSearch()) {
      return true;
    }

    boolean sessionMatches = true;

    if (searchInfo.isUseSessionId()) {
      sessionMatches = matchesSessionId(appSession, searchInfo);
    }
    if (sessionMatches && searchInfo.isUseAgeFrom()) {
      sessionMatches = matchesAgeFrom(appSession, searchInfo);
    }
    if (sessionMatches && searchInfo.isUseAgeTo()) {
      sessionMatches = matchesAgeTo(appSession, searchInfo);
    }
    if (sessionMatches && searchInfo.isUseIdleTimeFrom()) {
      sessionMatches = matchesIdleTimeFrom(appSession, searchInfo);
    }
    if (sessionMatches && searchInfo.isUseIdleTimeTo()) {
      sessionMatches = matchesIdleTimeTo(appSession, searchInfo);
    }
    if (searchInfo.isUseLastIp()) {
      sessionMatches = matchesLastIp(appSession, searchInfo);
    }
    if (sessionMatches && searchInfo.isUseAttrName()) {
      sessionMatches = matchesAttrName(appSession, searchInfo);
    }

    return sessionMatches;
  }

  private boolean matchesSessionId(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    String sessionId = appSession.getId();
    if (sessionId != null) {
      return searchInfo.getSessionIdPattern().matcher(sessionId).matches();
    }
    return true;
  }

  private boolean matchesAgeFrom(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    long age = appSession.getAge();
    long ageFrom = searchInfo.getAgeFromSec().longValue() * 1000;
    return age >= ageFrom;
  }

  private boolean matchesAgeTo(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    long age = appSession.getAge();
    long ageTo = searchInfo.getAgeToSec().longValue() * 1000;
    return age <= ageTo;
  }

  private boolean matchesIdleTimeFrom(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    long idleTime = appSession.getIdleTime();
    long idleTimeFrom = searchInfo.getIdleTimeFromSec().longValue() * 1000;
    return idleTime >= idleTimeFrom;
  }

  private boolean matchesIdleTimeTo(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    long idleTime = appSession.getIdleTime();
    long idleTimeTo = searchInfo.getIdleTimeToSec().longValue() * 1000;
    return idleTime <= idleTimeTo;
  }

  private boolean matchesLastIp(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    String lastIp = appSession.getLastAccessedIp();
    if (lastIp != null) {
      return lastIp.contains(searchInfo.getLastIp());
    }
    return true;
  }

  private boolean matchesAttrName(ApplicationSession appSession, SessionSearchInfo searchInfo) {
    List<Pattern> namePatterns = searchInfo.getAttrNamePatterns();
    for (Attribute attr : appSession.getAttributes()) {
      String attrName = attr.getName();
      if (attrName != null) {
        for (Pattern pattern : namePatterns) {
          if (pattern.matcher(attrName).matches()) {
            return true;
          }
        }
      }
    }
    return false;
  }
  @Override
  protected boolean isContextOptional() {
    return true;
  }

  @Value("sessions")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
