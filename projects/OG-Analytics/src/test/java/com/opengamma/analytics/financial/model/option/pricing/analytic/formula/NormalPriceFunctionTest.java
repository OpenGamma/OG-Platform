/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NormalPriceFunctionTest {

  private static final double T = 4.5;
  private static final double F = 104;
  private static final double DELTA = 10;
  private static final EuropeanVanillaOption ITM_CALL = new EuropeanVanillaOption(F - DELTA, T, true);
  private static final EuropeanVanillaOption OTM_CALL = new EuropeanVanillaOption(F + DELTA, T, true);
  private static final EuropeanVanillaOption ITM_PUT = new EuropeanVanillaOption(F + DELTA, T, false);
  private static final EuropeanVanillaOption OTM_PUT = new EuropeanVanillaOption(F - DELTA, T, false);
  private static final double DF = 0.9;
  private static final double SIGMA = 20.0;
  private static final NormalFunctionData VOL_DATA = new NormalFunctionData(F, DF, SIGMA);
  private static final NormalFunctionData ZERO_VOL_DATA = new NormalFunctionData(F, DF, 0);
  private static final NormalPriceFunction FUNCTION = new NormalPriceFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption1() {
    FUNCTION.getPriceFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption2() {
    FUNCTION.getPriceFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    FUNCTION.getPriceFunction(ITM_CALL).evaluate((NormalFunctionData) null);
  }

  @Test
  public void testZeroVolPrice() {
    assertEquals(DF * DELTA, FUNCTION.getPriceFunction(ITM_CALL).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(0, FUNCTION.getPriceFunction(OTM_CALL).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(DF * DELTA, FUNCTION.getPriceFunction(ITM_PUT).evaluate(ZERO_VOL_DATA), 1e-15);
    assertEquals(0, FUNCTION.getPriceFunction(OTM_PUT).evaluate(ZERO_VOL_DATA), 1e-15);
  }

  @Test
  public void testPriceAdjoint() {
    // Price
    double price = FUNCTION.getPriceFunction(ITM_CALL).evaluate(VOL_DATA);
    double[] priceDerivative = new double[3];
    double priceAdjoint = FUNCTION.getPriceAdjoint(ITM_CALL, VOL_DATA, priceDerivative);
    assertEquals(price, priceAdjoint, 1E-10);
    // Price with 0 volatility
    double price0 = FUNCTION.getPriceFunction(ITM_CALL).evaluate(ZERO_VOL_DATA);
    double[] price0Derivative = new double[3];
    double price0Adjoint = FUNCTION.getPriceAdjoint(ITM_CALL, ZERO_VOL_DATA, price0Derivative);
    assertEquals(price0, price0Adjoint, 1E-10);
    // Derivative forward.
    double deltaF = 0.01;
    NormalFunctionData dataFP = new NormalFunctionData(F + deltaF, DF, SIGMA);
    NormalFunctionData dataFM = new NormalFunctionData(F - deltaF, DF, SIGMA);
    double priceFP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFP);
    double priceFM = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataFM);
    double derivativeF_FD = (priceFP - priceFM) / (2 * deltaF);
    assertEquals(derivativeF_FD, priceDerivative[0], 1E-7);
    // Derivative strike.
    double deltaK = 0.01;
    EuropeanVanillaOption optionKP = new EuropeanVanillaOption(F - DELTA + deltaK, T, true);
    EuropeanVanillaOption optionKM = new EuropeanVanillaOption(F - DELTA - deltaK, T, true);
    double priceKP = FUNCTION.getPriceFunction(optionKP).evaluate(VOL_DATA);
    double priceKM = FUNCTION.getPriceFunction(optionKM).evaluate(VOL_DATA);
    double derivativeK_FD = (priceKP - priceKM) / (2 * deltaK);
    assertEquals(derivativeK_FD, priceDerivative[2], 1E-7);
    // Derivative volatility.
    double deltaV = 0.0001;
    NormalFunctionData dataVP = new NormalFunctionData(F, DF, SIGMA + deltaV);
    NormalFunctionData dataVM = new NormalFunctionData(F, DF, SIGMA - deltaV);
    double priceVP = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataVP);
    double priceVM = FUNCTION.getPriceFunction(ITM_CALL).evaluate(dataVM);
    double derivativeV_FD = (priceVP - priceVM) / (2 * deltaV);
    assertEquals(derivativeV_FD, priceDerivative[1], 1E-6);
  }

  private static final EuropeanVanillaOption ATM_CALL = new EuropeanVanillaOption(F, T, true);
  private static final EuropeanVanillaOption ATM_PUT = new EuropeanVanillaOption(F, T, false);

  /**
   * Test getDelta, getGamma and getVega
   */
  @Test
  public void greeksTest() {
    double tol = 1.0e-12;
    double eps = 1.0e-5;
    double[] priceDerivative = new double[3];
    for (EuropeanVanillaOption option : new EuropeanVanillaOption[] {ITM_CALL, ITM_PUT, OTM_CALL, OTM_PUT, ATM_CALL,
        ATM_PUT }) {
      // consistency with getPriceFunction for first order derivatives
      FUNCTION.getPriceAdjoint(option, VOL_DATA, priceDerivative);
      double delta = FUNCTION.getDelta(option, VOL_DATA);
      double vega = FUNCTION.getVega(option, VOL_DATA);
      assertEquals(delta, priceDerivative[0], tol);
      assertEquals(vega, priceDerivative[1], tol);

      // testing second order derivative against finite difference approximation
      NormalFunctionData dataUp = new NormalFunctionData(F + eps, DF, SIGMA);
      NormalFunctionData dataDw = new NormalFunctionData(F - eps, DF, SIGMA);
      double deltaUp = FUNCTION.getDelta(option, dataUp);
      double deltaDw = FUNCTION.getDelta(option, dataDw);
      double ref = 0.5 * (deltaUp - deltaDw) / eps;
      double gamma = FUNCTION.getGamma(option, VOL_DATA);
      assertEquals(ref, gamma, eps);
    }
  }

  /**
   * Testing the branch for sigmaRootT < 1e-16
   */
  @Test
  public void smallParameterGreeksTest() {
    double eps = 1.0e-5;
    double[] der = new double[3];
    NormalFunctionData dataVolUp = new NormalFunctionData(F, DF, eps);
    NormalFunctionData dataFwUp = new NormalFunctionData(F + eps, DF, 0.0);
    NormalFunctionData dataFwDw = new NormalFunctionData(F - eps, DF, 0.0);

    for (EuropeanVanillaOption option : new EuropeanVanillaOption[] {ITM_CALL, ITM_PUT, OTM_CALL, OTM_PUT, ATM_CALL,
        ATM_PUT }) {
      double delta = FUNCTION.getDelta(option, ZERO_VOL_DATA);
      double priceUp = FUNCTION.getPriceAdjoint(option, dataFwUp, der);
      double priceDw = FUNCTION.getPriceAdjoint(option, dataFwDw, der);
      double refDelta = 0.5 * (priceUp - priceDw) / eps;
      assertEquals(refDelta, delta, eps);

      double vega = FUNCTION.getVega(option, ZERO_VOL_DATA);
      double priceVolUp = FUNCTION.getPriceAdjoint(option, dataVolUp, der);
      double price = FUNCTION.getPriceAdjoint(option, ZERO_VOL_DATA, der);
      double refVega = (priceVolUp - price) / eps;
      assertEquals(refVega, vega, eps);

      double gamma = FUNCTION.getGamma(option, ZERO_VOL_DATA);
      double deltaUp = FUNCTION.getDelta(option, dataFwUp);
      double deltaDw = FUNCTION.getDelta(option, dataFwDw);
      double refGamma = 0.5 * (deltaUp - deltaDw) / eps;
      if (Math.abs(refGamma) > 0.1 / eps) {
        assertTrue(Double.isInfinite(gamma));
      } else {
        assertEquals(refGamma, gamma, eps);
      }
    }
  }

}
