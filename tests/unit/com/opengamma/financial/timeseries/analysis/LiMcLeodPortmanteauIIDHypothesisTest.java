/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class LiMcLeodPortmanteauIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis<DoubleTimeSeries<Long>> LI_MCLEOD = new LiMcLeodPortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(0.05, 20);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new LiMcLeodPortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(-0.1, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new LiMcLeodPortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(1.5, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLag() {
    new LiMcLeodPortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(0.05, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    LI_MCLEOD.evaluate((DoubleTimeSeries<Long>) RANDOM.subSeries(RANDOM.getTime(0), RANDOM.getTime(3)));
  }

  @Test
  public void test() {
    super.testNullTS(LI_MCLEOD);
    super.testEmptyTS(LI_MCLEOD);
    assertTrue(LI_MCLEOD.evaluate(RANDOM));
    assertFalse(LI_MCLEOD.evaluate(SIGNAL));
    assertFalse(LI_MCLEOD.evaluate(INCREASING));
  }
}
