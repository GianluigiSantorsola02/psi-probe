
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="${lang}">
    <head>
        <title><spring:message code="probe.jsp.title.servlet_source"/></title>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='java_syntax.css'/>"/>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}<spring:theme code='scroller.css'/>"/>
        <script src="<c:url value='/js/prototype.js'/>"></script>
        <script src="<c:url value='/js/behaviour.js'/>"></script>
        <script src="<c:url value='/js/scriptaculous/scriptaculous.js'/>"></script>
        <script src="<c:url value='/js/areascroller.js'/>"></script>
    </head>

    <c:set var="navTabApps" value="active" scope="request"/>
    <c:set var="use_decorator" value="application" scope="request"/>
    <c:set var="appTabJSPs" value="active" scope="request"/>

    <body>

        <ul class="options">
            <li id="back">
                <a href="<c:url value='/app/jsp.htm'><c:param name='webapp' value='${fn:escapeXml(param.webapp)}'/></c:url>">
                    <spring:message code="probe.jsp.viewsource.opt.back"/>
                </a>
            </li>
            <li id="viewJSP">
                <a href="<c:url value='/app/viewsource.htm'><c:param name='webapp' value='${fn:escapeXml(param.webapp)}'/><c:param name='source' value='${fn:escapeXml(param.source)}'/></c:url>">
                    <spring:message code="probe.jsp.servlet_source.opt.jsp"/>
                </a>
            </li>
            <li id="download">
                <a href="<c:url value='/app/downloadserv.htm'><c:param name='webapp' value='${fn:escapeXml(param.webapp)}'/><c:param name='source' value='${fn:escapeXml(param.source)}'/></c:url>">
                    <spring:message code="probe.jsp.follow.menu.download"/>
                </a>
            </li>

        </ul>
        <div class="embeddedBlockContainer">
            <h3><spring:message code="probe.jsp.servlet_source.h3.source"/></h3>

            <table id="resultsTable" style="border-spacing:0;border-collapse:separate;">
                <caption>Description of view_servlet_source.jsp</caption>
                <tr>
                    <th>View_servlet_Source.jsp</th>
                </tr>
                <tr>
                    <td id="left_scroller" class="scroller">&#160;</td>
                    <td id="separator" style="display: none;">&#160;</td>
                    <td>
                        <div class="scrollable_content" id="srccontent">
                            <code>
                                <c:out value="${content}" escapeXml="false"/>
                            </code>
                        </div>
                    </td>
                    <td id="right_scroller" class="scroller">&#160;</td>
                </tr>
            </table>        </div>

        <script>
            setupScrollers('srccontent');
        </script>

    </body>
</html>
