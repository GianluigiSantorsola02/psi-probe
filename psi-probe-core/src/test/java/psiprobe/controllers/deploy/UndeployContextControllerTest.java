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
package psiprobe.controllers.deploy;

import com.codebox.bean.JavaBeanTester;

import org.junit.jupiter.api.Test;

/**
 * The Class UndeployContextControllerTest.
 */
class UndeployContextControllerTest {

    //  @Test
//  void javabeanTester() {
//    JavaBeanTester.builder(BaseUndeployContextController.class)
//        .skip("applicationContext", "supportedMethods");
//  }

  /**
   * Javabean tester summary.
   */
  @Test
  void javabeanTesterSummary() {
    JavaBeanTester.builder(UndeploySummaryContextController.class)
        .skip("applicationContext", "supportedMethods").test();
  }

}
