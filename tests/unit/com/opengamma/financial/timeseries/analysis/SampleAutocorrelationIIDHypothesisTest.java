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
public class SampleAutocorrelationIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis<DoubleTimeSeries<Long>> SAMPLE_ACF = new SampleAutocorrelationIIDHypothesis<DoubleTimeSeries<Long>>(0.05, 100);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new SampleAutocorrelationIIDHypothesis<DoubleTimeSeries<Long>>(-0.1, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new SampleAutocorrelationIIDHypothesis<DoubleTimeSeries<Long>>(1.5, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroLag() {
    new SampleAutocorrelationIIDHypothesis<DoubleTimeSeries<Long>>(0.05, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    SAMPLE_ACF.evaluate((DoubleTimeSeries<Long>) RANDOM.subSeries(RANDOM.getTime(0), RANDOM.getTime(3)));
  }

  @Test
  public void test() {
    super.testNullTS(SAMPLE_ACF);
    super.testEmptyTS(SAMPLE_ACF);
    assertTrue(SAMPLE_ACF.evaluate(RANDOM));
    assertFalse(SAMPLE_ACF.evaluate(SIGNAL));
    assertFalse(SAMPLE_ACF.evaluate(INCREASING));
  }
}
