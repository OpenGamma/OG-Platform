/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.timeseries.precise.instant.InstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PortmanteauIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis TEST = new PortmanteauIIDHypothesis(0.05, 20);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new PortmanteauIIDHypothesis(-0.1, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new PortmanteauIIDHypothesis(1.5, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLag() {
    new PortmanteauIIDHypothesis(0.05, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    final InstantDoubleTimeSeries subSeries = RANDOM.subSeries(RANDOM.getTimeAtIndex(0), RANDOM.getTimeAtIndex(3));
    TEST.evaluate(subSeries);
  }

  @Test
  public void test() {
    super.assertNullTS(TEST);
    super.assertEmptyTS(TEST);
    assertTrue(TEST.evaluate(RANDOM));
    assertFalse(TEST.evaluate(SIGNAL));
    assertFalse(TEST.evaluate(INCREASING));
  }
}
