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
package psiprobe.scheduler.triggers;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import psiprobe.tools.TimeExpression;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class DatasourceStatsTrigger.
 */
public class DatasourceStatsTrigger extends CronTriggerFactoryBean {

  public void setCronExpression() {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      String filename = "config.properties";
      input = getClass().getClassLoader().getResourceAsStream(filename);
      prop.load(input);
      String periodExpression = prop.getProperty("psiprobe.beans.stats.collectors.datasource.period");
      String phaseExpression = prop.getProperty("psiprobe.beans.stats.collectors.datasource.phase");
      super.setCronExpression(TimeExpression.cronExpression(periodExpression, phaseExpression));
    } catch (IOException ex) {
      ex.fillInStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.fillInStackTrace();
        }
      }
    }

  }

  @Override
  public void setCronExpression(String cronExpression) {
    super.setCronExpression(cronExpression);
  }
  static {
    new DatasourceStatsTrigger();
  }
}
