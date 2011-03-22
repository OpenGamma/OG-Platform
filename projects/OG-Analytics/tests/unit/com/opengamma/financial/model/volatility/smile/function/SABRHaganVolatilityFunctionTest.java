/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class SABRHaganVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {
  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();

  @Override
  protected VolatilityFunctionProvider<SABRFormulaData> getFunction() {
    return FUNCTION;
  }

  @Test
  public void testATMSmothness() {
    // Test if the Hagan volatility function implementation around ATM is numerically stable enough (the finite difference slope should be small enough).
    double timeToExpiry = 1;
    boolean isCall = true;
    EuropeanVanillaOption option;
    double alpha = 0.05;
    double beta = 0.5;
    double nu = 0.50;
    double rho = -0.25;
    int nbPoints = 100;
    double forward = 0.05;
    double[] sabrVolatilty = new double[2 * nbPoints + 1];
    double range = 5E-9;
    double strike[] = new double[2 * nbPoints + 1];
    for (int looppts = -nbPoints; looppts <= nbPoints; looppts++) {
      strike[looppts + nbPoints] = forward + ((double) looppts) / nbPoints * range;
      option = new EuropeanVanillaOption(strike[looppts + nbPoints], timeToExpiry, isCall);
      SABRFormulaData SabrData = new SABRFormulaData(forward, alpha, beta, nu, rho);
      sabrVolatilty[looppts + nbPoints] = FUNCTION.getVolatilityFunction(option).evaluate(SabrData);
    }
    for (int looppts = -nbPoints; looppts < nbPoints; looppts++) {
      assertEquals(true, Math.abs(sabrVolatilty[looppts + nbPoints + 1] - sabrVolatilty[looppts + nbPoints]) / (strike[looppts + nbPoints + 1] - strike[looppts + nbPoints]) < 20.0);
    }

  }
}
