/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 * 
 */
public class RankTestIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis RANK_TEST = new RankTestIIDHypothesis(0.05);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new RankTestIIDHypothesis(-0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new RankTestIIDHypothesis(1.5);
  }

  @Test
  public void test() {
    super.testNullTS(RANK_TEST);
    super.testEmptyTS(RANK_TEST);
    assertTrue(RANK_TEST.evaluate(RANDOM));
    assertTrue(RANK_TEST.evaluate(SIGNAL));
    assertFalse(RANK_TEST.evaluate(INCREASING));
  }
}
