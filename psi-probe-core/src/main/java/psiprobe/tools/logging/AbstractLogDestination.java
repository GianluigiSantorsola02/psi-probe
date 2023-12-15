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
package psiprobe.tools.logging;
import psiprobe.controllers.deploy.DirectoryTraversalException;

import java.io.File;
import java.sql.Timestamp;
import java.io.IOException;
import java.net.URI;
/**
 * The Class AbstractLogDestination.
 */
public abstract class AbstractLogDestination extends DefaultAccessor implements LogDestination {
  @Override
  public boolean isRoot() {
    return false;
  }
  @Override
  public boolean isContext() {
    return false;
  }
  @Override
  public String getIndex() {
    return null;
  }
  @Override
  public String getConversionPattern() {
    return null;
  }
  /**
   * Gets the stdout file.
   *
   * @return the stdout file
   */
  protected File getStdoutFile() {
    ensurePathIsRelative(System.getProperty("catalina.base"));
    ensurePathIsRelative(new File("stdout").toURI());
    File file = new File(System.getProperty("catalina.base"), "logs/catalina.out");
    return file.exists() ? file : new File("stdout");
  }
  private static void ensurePathIsRelative(String path) {
    ensurePathIsRelative(new File(path));
  }
  private static void ensurePathIsRelative(URI uri) {
    ensurePathIsRelative(new File(uri));
  }
  private static void ensurePathIsRelative(File file) {

    String canonicalPath;
    String absolutePath;

    if (file.isAbsolute()) {
      throw new DirectoryTraversalException("Potential directory traversal attempt - absolute path not allowed");
    }

    try {
      canonicalPath = file.getCanonicalPath();
      absolutePath = file.getAbsolutePath();
    } catch (IOException e) {
      throw new DirectoryTraversalException("Potential directory traversal attempt");
    }

    if (!canonicalPath.startsWith(absolutePath) || !canonicalPath.equals(absolutePath)) {
      throw new DirectoryTraversalException("Potential directory traversal attempt");
    }
  }
  @Override
  public File getFile() {
    return getStdoutFile();
  }
  @Override
  public long getSize() {
    File file = getFile();
    return file != null && file.exists() ? file.length() : 0;
  }
  @Override
  public Timestamp getLastModified() {
    File file = getFile();
    return file != null && file.exists() ? new Timestamp(file.lastModified()) : null;
  }
  @Override
  public String getLevel() {
    return null;
  }
  @Override
  public String[] getValidLevels() {
    return new String[0];
  }
  @Override
  public String getEncoding() {
    return null;
  }
}
