/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class PortmanteauIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis TEST = new PortmanteauIIDHypothesis(0.05, 20);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new PortmanteauIIDHypothesis(-0.1, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new PortmanteauIIDHypothesis(1.5, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLag() {
    new PortmanteauIIDHypothesis(0.05, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    final TimeSeries<Long, Double> subSeries = RANDOM.subSeries(RANDOM.getTime(0), RANDOM.getTime(3));
    TEST.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, subSeries.timesArray(), subSeries.valuesArray()));
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
