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
public class SVIFormulaDataTest {
  private static final double A = 1;
  private static final double B = 2;
  private static final double RHO = 0.45;
  private static final double SIGMA = 0.34;
  private static final double M = 0.35;
  private static final SVIFormulaData DATA = new SVIFormulaData(A, B, RHO, SIGMA, M);

  @Test
  public void test() {
    assertEquals(DATA.getA(), A, 0);
    assertEquals(DATA.getB(), B, 0);
    assertEquals(DATA.getM(), M, 0);
    assertEquals(DATA.getRho(), RHO, 0);
    assertEquals(DATA.getNu(), SIGMA, 0);
    SVIFormulaData other = new SVIFormulaData(A, B, RHO, SIGMA, M);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new SVIFormulaData(A + 1, B, RHO, SIGMA, M);
    assertFalse(other.equals(DATA));
    other = new SVIFormulaData(A, B + 1, RHO, SIGMA, M);
    assertFalse(other.equals(DATA));
    other = new SVIFormulaData(A, B, RHO * .5, SIGMA, M);
    assertFalse(other.equals(DATA));
    other = new SVIFormulaData(A, B, RHO, SIGMA + 1, M);
    assertFalse(other.equals(DATA));
    other = new SVIFormulaData(A, B, RHO, SIGMA, M + 1);
    assertFalse(other.equals(DATA));
  }
}
