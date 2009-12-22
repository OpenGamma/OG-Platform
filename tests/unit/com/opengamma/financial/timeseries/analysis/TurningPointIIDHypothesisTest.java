/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class TurningPointIIDHypothesisTest extends IIDHypothesisTest {
  private static final IIDHypothesis TURNING_POINT = new TurningPointIIDHypothesis(0.05);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new TurningPointIIDHypothesis(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new TurningPointIIDHypothesis(1.5);
  }

  @Test
  public void test() {
    super.testNullTS(TURNING_POINT);
    super.testEmptyTS(TURNING_POINT);
    assertTrue(TURNING_POINT.evaluate(RANDOM));
    assertTrue(TURNING_POINT.evaluate(SIGNAL));
    assertFalse(TURNING_POINT.evaluate(INCREASING));
  }
}
