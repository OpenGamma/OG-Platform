/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class SVIVolatilityFunctionTest {
  private static final double A = 1.2;
  private static final double B = 0.4;
  private static final double RHO = 0.4;
  private static final double SIGMA = 1.5;
  private static final double M = 3.6;
  private static final double K = 3;
  private static final double T = 1.5;
  private static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(K, T, true);
  private static final SVIVolatilityFunction F = new SVIVolatilityFunction();
  private static final Function1D<SVIFormulaData, Double> VOL = F.getVolatilityFunction(OPTION);
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption() {
    F.getVolatilityFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    VOL.evaluate((SVIFormulaData) null);
  }

  //@Test(expected = IllegalArgumentException.class)
  //TODO fix me
  @Test
  public void testNoArbitrageNegativeVerticalSpread() {
    final double t = 4 / (B * (1 + Math.abs(RHO))) + 10;
    F.getVolatilityFunction(new EuropeanVanillaOption(K, t, true)).evaluate(new SVIFormulaData(A, B, RHO, SIGMA, M));
  }

  @Test
  public void testZeroB() {
    assertEquals(VOL.evaluate(new SVIFormulaData(A, 0, RHO, SIGMA, M)), Math.sqrt(A / T), EPS);
  }

  @Test
  public void testKEqualsM() {
    assertEquals(VOL.evaluate(new SVIFormulaData(A, B, RHO, SIGMA, K)), Math.sqrt((A + B * SIGMA) / T), EPS);
  }

  @Test
  public void testZeroSigma() {
    assertEquals(VOL.evaluate(new SVIFormulaData(A, B, RHO, 0, M)), Math.sqrt((A + B * ((K - M) * (RHO - 1))) / T), EPS);
  }

}
