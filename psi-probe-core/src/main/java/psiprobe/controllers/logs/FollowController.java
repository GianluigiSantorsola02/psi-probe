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
package psiprobe.controllers.logs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.LogResolverBean;
import psiprobe.tools.BackwardsFileStream;
import psiprobe.tools.logging.LogDestination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;

/**
 * The Class FollowController.
 */
@Controller
public class FollowController extends AbstractLogHandlerController {

  private static final LogResolverBean logResolver = new LogResolverBean();

  public FollowController() {
    super(logResolver);
  }

  @GetMapping(path = "/follow.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response, LogDestination logDest) throws IOException {
    ModelAndView mv = new ModelAndView(Objects.requireNonNull(getViewName()));
    File file = logDest.getFile();

    if (file.exists()) {
      LinkedList<String> lines = readLogLines(request, file);

      if (!lines.isEmpty()) {
        mv.addObject("lines", lines);
      }
    }

    return mv;
  }

  private LinkedList<String> readLogLines(HttpServletRequest request, File file) throws IOException {
    LinkedList<String> lines = new LinkedList<>();
    long actualLength = file.length();
    long lastKnownLength = ServletRequestUtils.getLongParameter(request, "lastKnownLength", 0);
    long currentLength = ServletRequestUtils.getLongParameter(request, "currentLength", actualLength);
    long maxReadLines = ServletRequestUtils.getLongParameter(request, "maxReadLines", 0);

    if (shouldResetFileLength(lastKnownLength, currentLength, actualLength)) {
      lastKnownLength = 0;
      lines.add(" ------------- THE FILE HAS BEEN TRUNCATED --------------");
    }

    try (BackwardsFileStream ignored = new BackwardsFileStream(file, currentLength)
    ) {

      long readSize = 0;
      long totalReadSize = currentLength - lastKnownLength;
      String line;
      BufferedReader br = null;
      while (readSize < totalReadSize) {
          assert false;
          if ((line = br.readLine()) == null) break;
          processLogLine(lines, line, maxReadLines);
        readSize += line.length();
      }

      removeFirstIfExceeded(lines, lastKnownLength, readSize, totalReadSize);
    }

    return lines;
  }

  private boolean shouldResetFileLength(long lastKnownLength, long currentLength, long actualLength) {
    return lastKnownLength > currentLength || lastKnownLength > actualLength || currentLength > actualLength;
  }

  private void processLogLine(LinkedList<String> lines, String line, long maxReadLines) {
    if (!line.isEmpty()) {
      lines.addFirst(line);
    }
    if (maxReadLines != 0 && lines.size() >= maxReadLines) {
      lines.removeLast();
    }
  }

  private void removeFirstIfExceeded(LinkedList<String> lines, long lastKnownLength, long readSize, long totalReadSize) {
    if (lastKnownLength != 0 && readSize > totalReadSize) {
      lines.removeFirst();
    }
  }

  @Value("ajax/follow")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
