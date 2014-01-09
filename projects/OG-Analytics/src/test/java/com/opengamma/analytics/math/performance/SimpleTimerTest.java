/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, enabled = false)
public class SimpleTimerTest {

  private static SimpleTimer timer = new SimpleTimer();

  @Test
  public void test() {
    final Logger log = LoggerFactory.getLogger(SimpleTimerTest.class);

    log.info("Calling some work!");
    timer.startTimer();
    timer.startTimer();
    pointlessWork();
    log.info("elapsed time " + timer.elapsedTime() + " ns");
    log.info("Calling some work!");
    pointlessWork();
    log.info("elapsed time " + timer.elapsedTime() + " ns");
    timer.stopTimer();
    log.info("Test run, total time " + timer.totalTime() + " ns");
  }

  private void pointlessWork() {
    long i;
    long count = 0;
    for (i = 0; i < 2 >> 10; i++)
    {
      count = count + 1;
    }
  }

}
