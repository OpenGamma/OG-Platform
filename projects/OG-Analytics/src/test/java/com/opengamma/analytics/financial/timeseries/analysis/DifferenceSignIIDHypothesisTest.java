/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DifferenceSignIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis DIFFERENCE_SIGN = new DifferenceSignIIDHypothesis(0.05);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new DifferenceSignIIDHypothesis(-0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new DifferenceSignIIDHypothesis(1.5);
  }

  @Test
  public void test() {
    super.assertNullTS(DIFFERENCE_SIGN);
    super.assertEmptyTS(DIFFERENCE_SIGN);
    assertTrue(DIFFERENCE_SIGN.evaluate(RANDOM));
    assertTrue(DIFFERENCE_SIGN.evaluate(SIGNAL));
    assertFalse(DIFFERENCE_SIGN.evaluate(INCREASING));
  }
}
