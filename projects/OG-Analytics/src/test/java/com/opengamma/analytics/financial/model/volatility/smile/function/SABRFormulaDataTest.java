/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRFormulaDataTest {
  private static final double NU = 0.8;
  private static final double RHO = -0.65;
  private static final double BETA = 0.76;
  private static final double ALPHA = 1.4;
  private static final SABRFormulaData DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeBETA() {
    new SABRFormulaData(ALPHA, -BETA, RHO, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNu() {
    new SABRFormulaData(ALPHA, BETA, RHO, -NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowRho() {
    new SABRFormulaData(ALPHA, BETA, RHO - 10, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighRho() {
    new SABRFormulaData(ALPHA, BETA, RHO + 10, NU);
  }

  @Test
  public void test() {
    assertEquals(DATA.getAlpha(), ALPHA, 0);
    assertEquals(DATA.getBeta(), BETA, 0);
    assertEquals(DATA.getNu(), NU, 0);
    assertEquals(DATA.getRho(), RHO, 0);
    SABRFormulaData other = new SABRFormulaData(ALPHA, BETA, RHO, NU);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());

    other = new SABRFormulaData(ALPHA - 0.01, BETA, RHO, NU);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(ALPHA, BETA * 0.5, RHO, NU);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(ALPHA, BETA, RHO, NU * 0.5);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(ALPHA, BETA, RHO * 0.5, NU);
    assertFalse(other.equals(DATA));
  }
}
