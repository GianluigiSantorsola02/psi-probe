
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<!DOCTYPE html>
<html lang="${lang}">
    <head>
        <title><spring:message htmlEscape="true" code="probe.jsp.title.app.viewXMLConf" arguments="${param.webapp},${fileDesc}"/></title>
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
        <c:choose>
            <c:when test="${displayTarget eq 'web.xml'}">
                <c:set var="appTabDeploymentDescriptor" value="active" scope="request"/>
            </c:when>
            <c:when test="${displayTarget eq 'context.xml'}">
                <c:set var="appTabContextDescriptor" value="active" scope="request"/>
            </c:when>
        </c:choose>

        <c:choose>
            <c:when test="${empty content}">
                <div class="infoMessage">
                    <p>
                        <spring:message code="probe.jsp.app.viewXMLConf.notfound" arguments="${fileDesc}"/>
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <ul class="options">
                    <li id="download">
                        <a href="<c:url value='${downloadUrl}'><c:param name='webapp' value='${param.webapp}'/></c:url>">
                            <spring:message code="probe.jsp.follow.menu.download"/>
                        </a>
                    </li>
                </ul>

                <div class="embeddedBlockContainer">
                    <h3><spring:message code="probe.jsp.app.viewXMLConf.h3.scr" arguments="${fileName}"/></h3>
                    <table id="resultsTable" style="border-spacing:0;border-collapse:separate;">
                        <caption>Description of view_xml_conf.jsp</caption>

                        <tr>
                            <th>View_xml_Conf.jsp</th>
                        </tr>
                        <tr>
                            <td id="left_scroller" class="scroller">&#160;</td>
                            <td >&#160;</td>
                            <td>
                                <div id="srccontent" class="scrollable_content">
                                    <code>
                                        <c:out value="${content}" escapeXml="false"/>
                                    </code>
                                </div>
                            </td>
                            <td id="right_scroller" class="scroller">&#160;</td>
                        </tr>
                    </table>                    <script>
                        setupScrollers('srccontent');
                    </script>
                </div>
            </c:otherwise>
        </c:choose>
    </body>
</html>
