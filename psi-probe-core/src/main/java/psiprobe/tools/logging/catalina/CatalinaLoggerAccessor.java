package psiprobe.tools.logging.catalina;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import psiprobe.controllers.deploy.DirectoryTraversalException;
import psiprobe.tools.Instruments;
import psiprobe.tools.logging.AbstractLogDestination;

public class CatalinaLoggerAccessor extends AbstractLogDestination {

  @Override
  public boolean isContext() {
    return true;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getLogType() {
    return "catalina";
  }

  @Override
  public File getFile() throws IllegalAccessException {
    String dir = (String) invokeMethod(getTarget(), "getDirectory", null, null);
    String prefix = (String) invokeMethod(getTarget(), "getPrefix", null, null);
    String suffix = (String) invokeMethod(getTarget(), "getSuffix", null, null);
    boolean timestamp = Instruments.getField(getTarget(), "timestamp") != null;
    String date = timestamp ? new SimpleDateFormat("yyyy-MM-dd").format(new Date()) : "";
    Path filePath = Paths.get(dir, prefix + date + suffix);

    // Validate the file path to prevent directory traversal
    if (!filePath.startsWith(Paths.get(dir).normalize())) {
      throw new DirectoryTraversalException("Potential directory traversal attempt");
    }

    File file = filePath.toFile();
    if (!file.isAbsolute()) {
      ensurePathIsRelative(System.getProperty("catalina.base"));
      ensurePathIsRelative(file.toURI());
      return new File(System.getProperty("catalina.base"), file.getPath());
    }
    return file;
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

  private boolean notNull(String... strings) {
    for (String string : strings) {
      if (string == null) {
        return false;
      }
    }
    return true;
  }
}
