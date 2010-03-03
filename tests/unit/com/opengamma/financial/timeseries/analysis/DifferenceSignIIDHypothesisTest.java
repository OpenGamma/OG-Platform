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
public class DifferenceSignIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis<DoubleTimeSeries<Long>> DIFFERENCE_SIGN = new DifferenceSignIIDHypothesis<DoubleTimeSeries<Long>>(0.05);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new DifferenceSignIIDHypothesis<DoubleTimeSeries<Long>>(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new DifferenceSignIIDHypothesis<DoubleTimeSeries<Long>>(1.5);
  }

  @Test
  public void test() {
    super.testNullTS(DIFFERENCE_SIGN);
    super.testEmptyTS(DIFFERENCE_SIGN);
    assertTrue(DIFFERENCE_SIGN.evaluate(RANDOM));
    assertTrue(DIFFERENCE_SIGN.evaluate(SIGNAL));
    assertFalse(DIFFERENCE_SIGN.evaluate(INCREASING));
  }
}
