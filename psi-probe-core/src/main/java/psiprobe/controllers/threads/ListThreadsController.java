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
package psiprobe.controllers.threads;

import org.apache.catalina.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.java.ThreadModel;
import psiprobe.tools.Instruments;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Class ListThreadsController.
 */
@Controller
public class ListThreadsController extends AbstractTomcatContainerController {

  @GetMapping(path = "/th_impl1.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    /*
     * Create a list of webapp classloaders. This will help us to associate threads with
     * applications.
     */
    List<Context> contexts = getContainerWrapper().getTomcatContainer().findContexts();
    Map<String, String> classLoaderMap = new TreeMap<>();
    for (Context context : contexts) {
      if (context.getLoader() != null && context.getLoader().getClassLoader() != null) {
        classLoaderMap.put(toUid(context.getLoader().getClassLoader()), context.getName());
      }
    }

    return new ModelAndView(getViewName(), "threads", enumerateThreads(classLoaderMap));
  }

  /**
   * Enumerate threads.
   *
   * @param classLoaderMap the class loader map
   *
   * @return the list
   */
  private List<ThreadModel> enumerateThreads(final Map<String, String> classLoaderMap) throws IllegalAccessException {
    List<ThreadModel> threadList = new ArrayList<>();

    Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();

    for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
      Thread thread = entry.getKey();

      ThreadModel threadModel = new ThreadModel();
      threadModel.setThreadClass(thread.getClass().getName());
      threadModel.setName(thread.getName());
      threadModel.setPriority(thread.getPriority());
      threadModel.setDaemon(thread.isDaemon());
      threadModel.setInterrupted(thread.isInterrupted());
      threadModel.setGroupName(thread.getThreadGroup().getName());

      Object target = Instruments.getField(thread, "target");
      if (target != null) {
        threadModel.setRunnableClassName(target.getClass().getName());
      }

      ClassLoader cl = thread.getContextClassLoader();
      if (cl != null) {
        if (classLoaderMap != null) {
          threadModel.setAppName(classLoaderMap.get(toUid(cl)));
        }
        threadModel.setClassLoader(toUid(cl));
      }

      threadList.add(threadModel);
    }

    return threadList;
  }

  /**
   * To uid.
   *
   * @param obj the obj
   *
   * @return the string
   */
  private static String toUid(Object obj) {
    return obj.getClass().getName() + "@" + obj.hashCode();
  }

  @Value("threads")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
