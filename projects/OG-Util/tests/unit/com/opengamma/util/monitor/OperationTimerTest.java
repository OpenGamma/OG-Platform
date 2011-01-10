/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class OperationTimerTest {
  private static final Logger s_logger = LoggerFactory.getLogger(OperationTimerTest.class);
  
  @Test
  public void simpleTest() throws InterruptedException {
    OperationTimer timer = new OperationTimer(s_logger, "Testing");
    Thread.sleep(100);
    long result = timer.finished();
    // We're not guaranteed that sleeping for 100ms will be exactly 100ms.
    assertTrue(result > 90);
  }

}
