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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.naming.NamingException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;

import psiprobe.model.ApplicationParam;
import psiprobe.model.ApplicationResource;
import psiprobe.model.FilterInfo;
import psiprobe.model.FilterMapping;
import psiprobe.model.jsp.Summary;

/**
 * Part of Tomcat container version abstraction layer.
 */
public interface TomcatContainer {

  /**
   * Finds a context based on its path.
   *
   * @param name the context path
   * @return the context deployed to that path
   */
  Context findContext(String name);

  /**
   * Formats a context name to a path that the container will recognize. Usually this means
   * prepending a {@code /} character, although there is special behavior for the root context.
   *
   * @param name the context name
   *
   * @return the context name formatted as the container expects
   */
  String formatContextName(String name);

  /**
   * Formats a context name so that it can be used as a step for the context descriptor .xml or
   * deployed .war file. Usually this means stripping a leading {@code /} character, although there
   * is special behavior for the root context.
   *
   * @param contextName the context name
   *
   * @return the filename stem for this context
   */
  String formatContextFilename(String contextName);

  /**
   * Find contexts.
   *
   * @return all contexts
   */
  List<Context> findContexts();

  /**
   * Find connectors.
   *
   * @return all connectors
   */
  List<Connector> findConnectors();

  /**
   * Stops the context with the given name.
   *
   * @param name the name of the context to stop
   *
   */
  void stop(String name) throws StopException, LifecycleException, InterruptedException;

  /**
   * Starts the context with the given name.
   *
   * @param name the name of the context to start
   *
   */
  void start(String name) throws LifecycleException, StartException, InterruptedException;

  /**
   * Undeploys a context.
   *
   * @param name the context path
   *
   */
  void remove(String name) throws AbstractTomcatContainer.RemoveInternalException, AbstractTomcatContainer.CheckChangesException, RemoveException, IOException;

  /**
   * Installs .war file at the given context name.
   *
   * @param name the name of the context
   * @param url pointer to .war file to be deployed
   *
   */
  void installWar(String name, URL url) throws InstallWarException, AbstractTomcatContainer.CheckChangesException;

  /**
   * This method always returns absolute path, no matter what Tomcat is up to.
   *
   * @return absolute path to applications base (normally "webapps")
   */
  File getAppBase();

  /**
   * Returns the context descriptor filename for the given context.
   *
   * @param context the context
   *
   * @return the context descriptor filename, or {@code null}
   */
  File getConfigFile(Context context);

  /**
   * Gets the config base.
   *
   * @return the config base
   */
  String getConfigBase();

  /**
   * Sets the wrapper.
   *
   * @param wrapper the new wrapper
   */
  void setWrapper(Wrapper wrapper);

  /**
   * Indicates whether this adapter can bind to the container.
   *
   * @param binding the ServerInfo of the container
   *
   * @return true if binding is possible
   */
  boolean canBoundTo(String binding);

  /**
   * Deploys a context, assuming an context descriptor file exists on the server already.
   *
   * @param contextName the context path, which should match the filename
   *
   * @return {@code true} if deployment was successful
   *
   */
  boolean installContext(String contextName) throws InstallContextException, AbstractTomcatContainer.InstallContextInternalException, AbstractTomcatContainer.CheckChangesException;

  /**
   * Lists and optionally compiles all JSPs for the given context. Compilation details are added to
   * the summary.
   *
   * @param context the context
   * @param summary the summary in which the output is stored
   * @param compile whether to compile all of the JSPs or not
   */
  void listContextJsps(Context context, Summary summary, boolean compile);

  /**
   * Compiles a list of JSPs. Names of JSP files are expected to be relative to the webapp root. The
   * method updates summary with compilation details.
   *
   * @param context the context
   * @param summary the summary in which the output is stored
   * @param names the list of JSPs to compile
   */
  void recompileJsps(Context context, Summary summary, List<String> names);

  /**
   * Deletes the "work" directory of the given context.
   *
   * @param context the context
   */
  void discardWorkDir(Context context);

  /**
   * Gets the host name.
   *
   * @return the host name
   */
  String getHostName();

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  /**
   * Returns the JSP servlet filename for the given JSP file.
   *
   * @param context the context
   * @param jspName the JSP filename
   *
   * @return the name of the JSP servlet
   */
  String getServletFileNameForJsp(Context context, String jspName);

  /**
   * Gets the application filter maps.
   *
   * @param context the context
   *
   * @return the application filter maps
   */
  List<FilterMapping> getApplicationFilterMaps(Context context);

  /**
   * Gets the available.
   *
   * @param context the context
   *
   * @return the available
   */
  boolean getAvailable(Context context);

  /**
   * Adds the context resource.
   *
   * @param context the context
   * @param resourceList the resource list
   * @param contextBound the context bound
   */
  void addContextResource(Context context, List<ApplicationResource> resourceList,
      boolean contextBound);

  /**
   * Adds the context resource link.
   *
   * @param context the context
   * @param resourceList the resource list
   * @param contextBound the context bound
   */
  void addContextResourceLink(Context context, List<ApplicationResource> resourceList,
      boolean contextBound);

  /**
   * Gets the application filters.
   *
   * @param context the context
   *
   * @return the application filters
   */
  List<FilterInfo> getApplicationFilters(Context context);

  /**
   * Gets the application init params.
   *
   * @param context the context
   *
   * @return the application init params
   */
  List<ApplicationParam> getApplicationInitParams(Context context);

  /**
   * Resource exists.
   *
   * @param name the name
   * @param context the context
   *
   * @return true, if successful
   */
  boolean resourceExists(String name, Context context);

  /**
   * Gets the resource stream.
   *
   * @param name the name
   * @param context the context
   *
   * @return the resource stream
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  InputStream getResourceStream(String name, Context context) throws IOException;

  /**
   * Gets the resource attributes.
   *
   * @param name the name
   * @param context the context
   *
   * @return the resource attributes
   */
  Long[] getResourceAttributes(String name, Context context);

  /**
   * Binds a naming context to the current thread's classloader.
   *
   * @param context the catalina context
   *
   * @throws NamingException if binding the classloader fails
   */
  void bindToContext(Context context) throws NamingException;

  /**
   * Unbinds a naming context from the current thread's classloader.
   *
   * @param context the catalina context
   *
   * @throws NamingException if unbinding the classloader fails
   */
  void unbindFromContext(Context context) throws NamingException;

  class StopException extends Exception {
    public StopException(String message) {
      super(message);
    }
  }

  class StartException extends Exception {
    public StartException(String message) {
      super(message);
    }
  }

  class RemoveException extends Exception {
    public RemoveException(String message) {
      super(message);
    }
  }

  class InstallWarException extends Exception {
    public InstallWarException(String message) {
      super(message);
    }
  }

  class InstallContextException extends Exception {
    public InstallContextException(String message) {
      super(message);
    }
  }
}
