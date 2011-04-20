/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 * 
 */
public class SABRFormulaDataTest {
  private static final double NU = 0.8;
  private static final double RHO = -0.65;
  private static final double BETA = 0.76;
  private static final double ALPHA = 1.4;
  private static final double F = 103;
  private static final SABRFormulaData DATA = new SABRFormulaData(F, ALPHA, BETA, NU, RHO);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeForward() {
    new SABRFormulaData(-F, ALPHA, BETA, NU, RHO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeBETA() {
    new SABRFormulaData(F, ALPHA, -BETA, NU, RHO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNu() {
    new SABRFormulaData(F, ALPHA, BETA, -NU, RHO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowRho() {
    new SABRFormulaData(F, ALPHA, BETA, NU, RHO - 10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighRho() {
    new SABRFormulaData(F, ALPHA, BETA, NU, RHO + 10);
  }

  @Test
  public void test() {
    assertEquals(DATA.getAlpha(), ALPHA, 0);
    assertEquals(DATA.getBeta(), BETA, 0);
    assertEquals(DATA.getForward(), F, 0);
    assertEquals(DATA.getNu(), NU, 0);
    assertEquals(DATA.getRho(), RHO, 0);
    SABRFormulaData other = new SABRFormulaData(F, ALPHA, BETA, NU, RHO);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new SABRFormulaData(F + 1, ALPHA, BETA, NU, RHO);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(F, ALPHA - 0.01, BETA, NU, RHO);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(F, ALPHA, BETA * 0.5, NU, RHO);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(F, ALPHA, BETA, NU * 0.5, RHO);
    assertFalse(other.equals(DATA));
    other = new SABRFormulaData(F, ALPHA, BETA, NU, RHO * 0.5);
    assertFalse(other.equals(DATA));
  }
}
