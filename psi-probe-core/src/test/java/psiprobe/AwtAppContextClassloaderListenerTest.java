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
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.servlet.ServletContextEvent;

import static org.apache.webbeans.util.Asserts.assertNotNull;

/**
 * The Class AwtAppContextClassloaderListenerTest.
 */
class AwtAppContextClassloaderListenerTest {

  /** The listener. */
  @Tested
  AwtAppContextClassloaderListener listener;

  /** The event. */
  @Mocked
  ServletContextEvent event;

  /** The image IO. */
  @Mocked
  ImageIO imageIO;

  /**
   * Context initialized test.
   */
  @Test
  void contextInitializedTest() {
    listener.contextInitialized(event);

    new Verifications() {
      {
        ImageIO.getCacheDirectory();
        times = 1;
      }
    };
  }

  /**
   * Context initialized error test.
   */
  @Test
  void contextInitializedErrorTest() {
    new Expectations() {
      {
        ImageIO.getCacheDirectory();
        result = new Exception();
        assertNotNull( imageIO );
      }
    };

    listener.contextInitialized(event);
  }



}
