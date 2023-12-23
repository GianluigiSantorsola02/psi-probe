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

import org.apache.catalina.Container;
import psiprobe.controllers.deploy.DirectoryTraversalException;
import psiprobe.tools.ApplicationUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
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
  protected File getStdoutFile() throws IOException {


    File catalinaBase = ApplicationUtils.getCatalinaBase();
      assert catalinaBase != null;
      if (!catalinaBase.isDirectory()) {
      throw new DirectoryTraversalException("Invalid catalina.base directory");
    }

    // Resolve the file against the base path and get its canonical path
    File resolvedFile = new File(catalinaBase, "logs/catalina.out").getCanonicalFile();

    // Ensure that the resolved canonical path is still under the base path
    if (!resolvedFile.toPath().startsWith(catalinaBase.toPath())) {
      throw new DirectoryTraversalException("Potential directory traversal attempt");
    }

    return resolvedFile;
  }

  @Override
  public File getFile() throws IllegalAccessException, IOException {
    return getStdoutFile();
  }
  @Override
  public long getSize() throws IllegalAccessException, IOException {
    File file = getFile();
    return file != null && file.exists() ? file.length() : 0;
  }
  @Override
  public Timestamp getLastModified() throws IllegalAccessException, IOException {
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
