/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link FunctionBlacklistRuleSet} class.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class FunctionBlacklistRuleSetExpiryTest {
  // broken out from FunctionBlacklistRuleSetTest as this is slower

  public void testExpiry() throws Exception {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final int timeout = (int) Timeout.standardTimeoutSeconds();
      final FunctionBlacklistRuleSet bl = new FunctionBlacklistRuleSet(executor, timeout);
      for (int i = 0; i < 10; i++) {
        final FunctionBlacklistRule rule = new FunctionBlacklistRule();
        rule.setFunctionIdentifier("F" + i);
        bl.add(rule, timeout * (i + 1));
      }
      assertEquals(bl.size(), 10);
      Thread.sleep(timeout * 3000);
      final int size = bl.size();
      assertTrue(size < 10);
      assertTrue(size > 5);
      Thread.sleep(timeout * 2000);
      assertTrue(bl.size() < size);
      assertTrue(bl.size() > 0);
    } finally {
      executor.shutdown();
    }
  }

}
