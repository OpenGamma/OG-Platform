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
import com.opengamma.analytics.financial.timeseries.analysis.SampleAutocorrelationIIDHypothesis;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class SampleAutocorrelationIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis SAMPLE_ACF = new SampleAutocorrelationIIDHypothesis(0.05, 100);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new SampleAutocorrelationIIDHypothesis(-0.1, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new SampleAutocorrelationIIDHypothesis(1.5, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLag() {
    new SampleAutocorrelationIIDHypothesis(0.05, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    final TimeSeries<Long, Double> subSeries = RANDOM.subSeries(RANDOM.getTimeAt(0), RANDOM.getTimeAt(3));
    SAMPLE_ACF.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, subSeries.timesArray(), subSeries.valuesArray()));
  }

  @Test
  public void test() {
    super.assertNullTS(SAMPLE_ACF);
    super.assertEmptyTS(SAMPLE_ACF);
    assertTrue(SAMPLE_ACF.evaluate(RANDOM));
    assertFalse(SAMPLE_ACF.evaluate(SIGNAL));
    assertFalse(SAMPLE_ACF.evaluate(INCREASING));
  }
}
