
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="https://github.com/psi-probe/psi-probe/jsp/tags" prefix="probe" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="${lang}">
    <head>
        <title><spring:message htmlEscape="true" code="probe.jsp.title.viewsource" arguments="${fn:escapeXml(param.source)}"/></title>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='syntax.css'/>"/>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='scroller.css'/>"/>
        <script src="<c:url value='/js/prototype.js'/>"></script>
        <script src="<c:url value='/js/behaviour.js'/>"></script>
        <script src="<c:url value='/js/scriptaculous/scriptaculous.js'/>"></script>
        <script src="<c:url value='/js/areascroller.js'/>"></script>
    </head>

    <body>

        <c:set var="navTabApps" value="active" scope="request"/>
        <c:set var="use_decorator" value="application" scope="request"/>
        <c:set var="appTabJSPs" value="active" scope="request"/>

        <ul class="options">
            <li id="back">
                <a href="<c:url value='/app/jsp.htm'><c:param name='webapp' value='${fn:escapeXml(param.webapp)}'/></c:url>">
                    <spring:message code="probe.jsp.viewsource.opt.back"/>
                </a>
            </li>
            <c:if test="${! empty content || ! empty highlightedContent}">
                <c:if test="${item.state == 2}">
                    <li id="viewservlet">
                        <a href="<c:url value='/app/viewservlet.htm'><c:param name='webapp' value='${fn:escapeXml(param.webapp)}'/><c:param name='source' value='${fn:escapeXml(param.source)}'/></c:url>">
                            <spring:message code="probe.jsp.viewsource.opt.viewServlet"/>
                        </a>
                    </li>
                </c:if>
                <li id="compilesingle">
                    <a href="<c:url value='/app/recompile.htm'><c:param name='webapp' value='${fn:escapeXml(param.webapp)}'/><c:param name='source' value='${fn:escapeXml(param.source)}'/><c:param name='view' value='/app/viewsource.htm'/></c:url>">
                        <spring:message code="probe.jsp.viewsource.opt.compile"/>
                    </a>
                </li>
            </c:if>
        </ul>

        <c:choose>
            <c:when test="${empty content && empty highlightedContent}">
                <div class="errorMessage">
                    <p>
                        <spring:message code="probe.jsp.viewsource.notfound"/>
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="embeddedBlockContainer">
                    <h3><spring:message code="probe.jsp.viewsource.h3.info"/></h3>

                    <div class="shadow">
                        <div class="info">
                            <p><spring:message code="probe.jsp.viewsource.appname"/>&#160;<span class="value"><c:out value="${fn:escapeXml(param.webapp)}" /></span>
                                <spring:message code="probe.jsp.viewsource.filename"/>&#160;<span class="value">${item.name}</span>
                                <spring:message code="probe.jsp.viewsource.size"/>&#160;<span class="value"><probe:volume value="${item.size}"/></span>
                                <spring:message code="probe.jsp.viewsource.lastmodified"/>&#160;<span class="value">${item.timestamp}</span>
                                <spring:message code="probe.jsp.viewsource.encoding"/>&#160;<span class="value">${item.encoding}</span>
                                <spring:message code="probe.jsp.viewsource.state"/>&#160;<span class="value">
                                    <c:choose>
                                        <c:when test="${item.state == 1}"><spring:message code="probe.jsp.jsps.status.outdated"/>
                                        </c:when>
                                        <c:when test="${item.state == 2}"><spring:message code="probe.jsp.jsps.status.compiled"/>
                                        </c:when>
                                        <c:otherwise><span class="fail"><spring:message code="probe.jsp.jsps.status.failed"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </p>
                        </div>
                    </div>

                    <c:if test="${! empty item.exception.message}">
                        <div class="errors">
                            <p>${item.exception.message}</p>
                        </div>
                    </c:if>

                    <h3><spring:message code="probe.jsp.viewsource.h3.source"/></h3>

                    <table id="resultsTable" style="border-spacing:0;border-collapse:separate;">
                        <caption>Description of view_jsp_source.jsp</caption>
                        <tr>
                            <th>View_jsp_source.jsp</th>
                        </tr>
                        <tr>
                            <td id="left_scroller" class="scroller">&#160;</td>
                            <td >&#160;</td>
                            <td>
                                <div id="srccontent" class="scrollable_content">
                                    <code>
                                        <c:choose>
                                            <c:when test="${! empty highlightedContent}">
                                                <c:out value="${highlightedContent}" escapeXml="false"/>
                                            </c:when>
                                            <c:otherwise>
                                                ${content}
                                            </c:otherwise>
                                        </c:choose>
                                    </code>
                                </div>
                            </td>
                            <td id="right_scroller" class="scroller">&#160;</td>
                        </tr>
                    </table>
                </div>
                <script>
                    setupScrollers('srccontent');
                </script>

            </c:otherwise>
        </c:choose>
    </body>
</html>
