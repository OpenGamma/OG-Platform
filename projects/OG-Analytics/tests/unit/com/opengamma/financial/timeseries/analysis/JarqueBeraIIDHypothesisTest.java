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
public class JarqueBeraIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis JARQUE_BERA = new JarqueBeraIIDHypothesis(0.05);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new JarqueBeraIIDHypothesis(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new JarqueBeraIIDHypothesis(1.5);
  }

  @Test
  public void test() {
    super.testNullTS(JARQUE_BERA);
    super.testEmptyTS(JARQUE_BERA);
    assertTrue(JARQUE_BERA.evaluate(RANDOM));
    assertFalse(JARQUE_BERA.evaluate(SIGNAL));
    assertFalse(JARQUE_BERA.evaluate(INCREASING));
  }

}
