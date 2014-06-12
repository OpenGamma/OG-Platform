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
public class BoxLjungPortmanteauIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis BOX_LJUNG = new BoxLjungPortmanteauIIDHypothesis(0.05, 20);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new BoxLjungPortmanteauIIDHypothesis(-0.1, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new BoxLjungPortmanteauIIDHypothesis(1.5, 20);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroLag() {
    new BoxLjungPortmanteauIIDHypothesis(0.05, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    BOX_LJUNG.evaluate(RANDOM.subSeries(RANDOM.getTimeAtIndex(0), RANDOM.getTimeAtIndex(3)));
  }

  @Test
  public void test() {
    super.assertNullTS(BOX_LJUNG);
    super.assertEmptyTS(BOX_LJUNG);
    assertTrue(BOX_LJUNG.evaluate(RANDOM));
    assertFalse(BOX_LJUNG.evaluate(SIGNAL));
    assertFalse(BOX_LJUNG.evaluate(INCREASING));
  }
}
