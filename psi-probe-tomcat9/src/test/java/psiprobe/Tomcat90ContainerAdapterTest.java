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

import mockit.Expectations;
import mockit.Mocked;
import org.apache.catalina.Context;
import org.apache.catalina.Valve;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.junit.jupiter.api.Test;
import psiprobe.model.ApplicationResource;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Class Tomcat90ContainerAdapterTest.
 */
class Tomcat90ContainerAdapterTest {

  /** The context. */
  @Mocked
  Context context;

  /**
   * Creates the valve.
   */
  @Test
  @SuppressWarnings({
          "Apache Tomcat/9.0, true",
          "Apache Tomcat (TomEE)/9.0, true",
          "Pivotal tc..../9.0, true",
          "Other, false"
  })
  void canBoundTo() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    String containerName = "";
    boolean result = adapter.canBoundTo(containerName);
    boolean expectedResult = false;
    assertEquals(expectedResult, result);
  }

  @Test
  void createValve() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    Valve valve = adapter.createValve();
    assertEquals("Tomcat90AgentValve[Container is null]", valve.toString());
  }
  /**
   * Filter mappings.
   */
  @Test
  void filterMappings() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    FilterMap map = new FilterMap();
    map.addServletName("psi-probe");
    map.addURLPattern("/psi-probe");
    assertEquals(2, adapter.getFilterMappings(map, "dispatcherMap", "filterClass").size());
  }

  /**
   * Creates the jsp compilation context.
   */
  @Test
  void createJspCompilationContext() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    JspCompilationContext context = adapter.createJspCompilationContext("name", null, null, null,
        ClassLoader.getSystemClassLoader());
    assertEquals("org.apache.jsp.name", context.getFQCN());
  }

  /**
   * Adds the context resource link.
   */
  @Test
  void addContextResourceLink() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    adapter.addContextResourceLink(context, new ArrayList<ApplicationResource>(), false);
  }

  /**
   * Adds the context resource.
   */
  @Test
  void addContextResource() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    adapter.addContextResource(context, new ArrayList<ApplicationResource>(), false);
  }

  /**
   * Gets the application filter maps.
   */
  @Test
  void applicationFilterMaps() {
      new Expectations() {
          {
              context.findFilterMaps();
              result = new FilterMap();
          }
      };

      final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    assertEquals(0, adapter.getApplicationFilterMaps(context).size());
  }

  /**
   * Application filters.
   */
  @Test
  void applicationFilters() {
      new Expectations() {
          {
              context.findFilterDefs();
              result = new FilterDef();
          }
      };

      final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    assertEquals(1, adapter.getApplicationFilters(context).size());
  }

  /**
   * Application init params.
   */
  @Test
  void applicationInitParams() {
      new Expectations() {
          {
              context.findApplicationParameters();
              result = new ApplicationParameter();
          }
      };
      final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    assertEquals(0, adapter.getApplicationInitParams(context).size());
  }

  /**
   * Resource exists.
   */
  @Test
  void resourceExists() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    assertTrue(adapter.resourceExists("name", context));
  }

  /**
   * Resource stream.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  void resourceStream() throws IOException {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    adapter.getResourceStream("name", context);
  }

  /**
   * Resource attributes.
   */
  @Test
  void resourceAttributes() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    adapter.getResourceAttributes("name", context);
  }

  /**
   * Gets the naming token.
   */
  @Test
  void getNamingToken() {
    final Tomcat90ContainerAdapter adapter = new Tomcat90ContainerAdapter();
    assertNull(adapter.getNamingToken(context));
  }

}
