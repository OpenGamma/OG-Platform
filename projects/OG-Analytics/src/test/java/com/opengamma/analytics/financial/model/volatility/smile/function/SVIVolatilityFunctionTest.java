/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SVIVolatilityFunctionTest {
  private static final double A = 1.2;
  private static final double B = 0.4;
  private static final double RHO = 0.4;
  private static final double SIGMA = 1.5;
  private static final double M = 1.2;
  private static final double FORWARD = 2.8;
  private static final double STRIKE = 3;
  private static final double T = 1.5;
  private static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(STRIKE, T, true);
  private static final SVIVolatilityFunction FUNC = new SVIVolatilityFunction();
  private static final Function1D<SVIFormulaData, Double> VOL = FUNC.getVolatilityFunction(OPTION, FORWARD);
  private static final Function1D<SVIFormulaData, double[]> VOL_ADJOINT = FUNC.getVolatilityAdjointFunction(OPTION, FORWARD);

  private static final double EPS = 1e-12;

  private static final VolatilityFunctionProvider<SVIFormulaData> FUNC_FD = new VolatilityFunctionProvider<SVIFormulaData>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Function1D<SVIFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
      return FUNC.getVolatilityFunction(option, forward);
    }
  };

  private static final Function1D<SVIFormulaData, double[]> VOL_ADJOINT_FD = FUNC_FD.getVolatilityAdjointFunction(OPTION, FORWARD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption() {
    FUNC.getVolatilityFunction(null, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    VOL.evaluate((SVIFormulaData) null);
  }

  @Test
  public void testZeroB() {
    assertEquals(VOL.evaluate(new SVIFormulaData(A, 0, RHO, SIGMA, M)), Math.sqrt(A), EPS);
  }

  @Test
  public void testMEqualsKappa() {
    final double m = Math.log(STRIKE / FORWARD);
    assertEquals(VOL.evaluate(new SVIFormulaData(A, B, RHO, SIGMA, m)), Math.sqrt((A + B * SIGMA)), EPS);
  }

  @Test
  public void testZeroSigma() {
    final double kappa = Math.log(STRIKE / FORWARD);
    assertEquals(VOL.evaluate(new SVIFormulaData(A, B, RHO, 0, M)), Math.sqrt((A + B * (RHO * (kappa - M) + Math.abs(kappa - M)))), EPS);
  }

  @Test
  public void testVolAdjoint() {
    final SVIFormulaData data = new SVIFormulaData(0.05, 0.2, -0.4, 0.2, -0.1);

    final double[] res = VOL_ADJOINT.evaluate(data);
    final double[] resFD = VOL_ADJOINT_FD.evaluate(data);

    final int n = resFD.length;
    assertEquals(n, res.length);
    for (int i = 0; i < n; i++) {
      assertEquals("parameter " + i, resFD[i], res[i], 1e-6);
    }
  }

}
