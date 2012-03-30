/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.timeseries.analysis.IIDHypothesis;
import com.opengamma.analytics.financial.timeseries.analysis.PortmanteauIIDHypothesis;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
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
    final TimeSeries<Long, Double> subSeries = RANDOM.subSeries(RANDOM.getTimeAt(0), RANDOM.getTimeAt(3));
    TEST.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, subSeries.timesArray(), subSeries.valuesArray()));
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
