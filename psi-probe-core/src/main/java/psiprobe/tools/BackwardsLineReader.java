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
package psiprobe.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Reads lines from "backwards" InputStream. This class facilitates reading files from bottom up.
 *
 * <p>
 * This source code was kindly contributed by Kan Ogawa.
 * </p>
 */
public class BackwardsLineReader {

  /** The bis. */
  private BufferedInputStream bis;

  /** The skip line feed. */
  private boolean skipLineFeed = true;

  /** The encoding. */
  private String encoding;

  /**
   * Instantiates a new backwards line reader.
   *
   * @param is the is
   */
  public BackwardsLineReader(InputStream is) {
    this(is, null);
  }

  /**
   * Instantiates a new backwards line reader.
   *
   * @param is the is
   * @param encoding the encoding
   */
  public BackwardsLineReader(InputStream is, String encoding) {
    this.bis = new BufferedInputStream(is, 8192);
    this.encoding = encoding;
  }

  /**
   * Read line.
   *
   * @return the string
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String readLine() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
    boolean empty = false;

    boolean shouldBreak = false;
    while (!shouldBreak) {
      int chr = bis.read();
      if (chr == -1) {
        if (baos.size() == 0) {
          empty = true;
        }
        shouldBreak = true;
      } else if (chr == '\n') {
        skipLineFeed = false;
        shouldBreak = true;
      } else if (chr == '\r') {
        if (skipLineFeed) {
          shouldBreak = true;
        } else {
          continue;
        }
      }
      baos.write(chr);
    }
    if (!empty) {
      byte[] byteArray = baos.toByteArray();
      reverse(byteArray);
      Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
      return new String(byteArray, charset);
    }

    return null;
  }

  /**
   * Close.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void close() throws IOException {
    if (bis != null) {
      bis.close();
    }
  }

  /**
   * Reverse.
   *
   * @param byteArray the byte array
   */
  private void reverse(byte[] byteArray) {
    for (int i = 0; i < byteArray.length / 2; i++) {
      byte temp = byteArray[i];
      byteArray[i] = byteArray[byteArray.length - i - 1];
      byteArray[byteArray.length - i - 1] = temp;
    }
  }

}
