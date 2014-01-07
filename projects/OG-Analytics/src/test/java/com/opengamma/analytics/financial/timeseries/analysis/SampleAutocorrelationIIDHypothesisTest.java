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
    final InstantDoubleTimeSeries subSeries = RANDOM.subSeries(RANDOM.getTimeAtIndex(0), RANDOM.getTimeAtIndex(3));
    SAMPLE_ACF.evaluate(subSeries);
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
