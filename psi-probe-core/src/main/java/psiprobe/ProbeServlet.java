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
package psiprobe;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import psiprobe.beans.ContainerWrapperBean;

/**
 * Main dispatcher servlet. Spring default dispatcher servlet had to be superseded to handle
 * "privileged" application context features. The actual requirement is to capture passed Wrapper
 * instance into ContainerWrapperBean. Wrapper instance is our gateway to Tomcat.
 */
@WebServlet
public class ProbeServlet extends DispatcherServlet implements ContainerServlet {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant logger. */
  private static final Logger mylogger = LoggerFactory.getLogger(ProbeServlet.class);

  private static final Wrapper wrapper = null;


  @Override
  public Wrapper getWrapper() {
    return wrapper;
  }

  @Override
  public void setWrapper(Wrapper wrapper) {
    mylogger.info("setWrapper() called");
  }

  /**
   * Associates the {@link Wrapper} with the {@link ContainerWrapperBean}.
   *
   * @param config this servlet's configuration and initialization parameters
   *
   * @throws ServletException if the wrapper is null or another servlet-interrupting error occurs
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
      throw new ServletException("Wrapper is null");
  }

  /**
   * Dispatch.
   *
   * @param httpServletRequest the request
   * @param httpServletResponse the response
   *
   * @throws Exception if setting UTF-8 encoding fails or if the super implementation throws an
   *         exception
   */
  @Override
  protected void doDispatch(HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse) throws Exception {
    try {
      httpServletRequest.setCharacterEncoding(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      mylogger.error("Unsupported encoding: {}", e.getMessage());
    }
    super.doDispatch(httpServletRequest, httpServletResponse);
  }

  @Override
  public void destroy() {
    getContainerWrapperBean().setWrapper(null);
    super.destroy();
  }

  /**
   * Gets the container wrapper bean.
   *
   * @return the container wrapper bean
   */
  protected ContainerWrapperBean getContainerWrapperBean() {
    // Verifica se getWebApplicationContext() restituisce un valore non nullo
    WebApplicationContext webApplicationContext = getWebApplicationContext();
    if (webApplicationContext != null) {
      return (ContainerWrapperBean) webApplicationContext.getBean("containerWrapper");
    }   else {
      // Gestisci il caso in cui getWebApplicationContext() restituisce null
      throw new IllegalStateException("WebApplicationContext is null");
    }
  }

}
