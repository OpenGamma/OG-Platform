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
public class LiMcLeodPortmanteauIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis LI_MCLEOD = new LiMcLeodPortmanteauIIDHypothesis(0.05, 20);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new LiMcLeodPortmanteauIIDHypothesis(-0.1, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new LiMcLeodPortmanteauIIDHypothesis(1.5, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLag() {
    new LiMcLeodPortmanteauIIDHypothesis(0.05, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    final InstantDoubleTimeSeries subSeries = RANDOM.subSeries(RANDOM.getTimeAtIndex(0), RANDOM.getTimeAtIndex(3));
    LI_MCLEOD.evaluate(subSeries);
  }

  @Test
  public void test() {
    super.assertNullTS(LI_MCLEOD);
    super.assertEmptyTS(LI_MCLEOD);
    assertTrue(LI_MCLEOD.evaluate(RANDOM));
    assertFalse(LI_MCLEOD.evaluate(SIGNAL));
    assertFalse(LI_MCLEOD.evaluate(INCREASING));
  }
}
