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
public class JarqueBeraIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis JARQUE_BERA = new JarqueBeraIIDHypothesis(0.05);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new JarqueBeraIIDHypothesis(-0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new JarqueBeraIIDHypothesis(1.5);
  }

  @Test
  public void test() {
    super.assertNullTS(JARQUE_BERA);
    super.assertEmptyTS(JARQUE_BERA);
    assertTrue(JARQUE_BERA.evaluate(RANDOM));
    assertFalse(JARQUE_BERA.evaluate(SIGNAL));
    assertFalse(JARQUE_BERA.evaluate(INCREASING));
  }

}
