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
public class PortmanteauIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis<DoubleTimeSeries<Long>> TEST = new PortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(0.05, 20);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new PortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(-0.1, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new PortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(1.5, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLag() {
    new PortmanteauIIDHypothesis<DoubleTimeSeries<Long>>(0.05, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    TEST.evaluate((DoubleTimeSeries<Long>) RANDOM.subSeries(RANDOM.getTime(0), RANDOM.getTime(3)));
  }

  @Test
  public void test() {
    super.testNullTS(TEST);
    super.testEmptyTS(TEST);
    assertTrue(TEST.evaluate(RANDOM));
    assertFalse(TEST.evaluate(SIGNAL));
    assertFalse(TEST.evaluate(INCREASING));
  }
}
