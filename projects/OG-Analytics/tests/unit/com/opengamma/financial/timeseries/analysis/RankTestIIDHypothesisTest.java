/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class RankTestIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis RANK_TEST = new RankTestIIDHypothesis(0.05);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new RankTestIIDHypothesis(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
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
