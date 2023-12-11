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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class BackwardsFileStream extends InputStream {

  private final RandomAccessFile raf;
  private long seekPos;

  public BackwardsFileStream(File file, long pos) throws IOException {
    raf = new RandomAccessFile(file, "r");
    seekPos = pos;
  }

  @Override
  public int read() throws IOException {
    if (seekPos > 0) {
      raf.seek(--seekPos);
      return raf.read();
    }
    // return EOF (so to speak)
    return -1;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (seekPos <= 0) {
      return -1; // EOF
    }

    int bytesRead = 0;
    while (len > 0 && seekPos > 0) {
      raf.seek(--seekPos);
      int readByte = raf.read();
      if (readByte == -1) {
        // reached the beginning of the file
        break;
      }
      b[off++] = (byte) readByte;
      len--;
      bytesRead++;
    }

    return bytesRead > 0 ? bytesRead : -1; // return -1 if no bytes were read
  }

  @Override
  public void close() throws IOException {
    raf.close();
  }
}
