/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;

/**
 * 
 */
public class HestonFFTPricerTest {
  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final FFTPricer PRICER = new FFTPricer();

  @Test
  public void testLowVolOfVol() {
    final double sigma = 0.36;

    final double kappa = 1.0; // mean reversion speed
    final double theta = sigma * sigma; // reversion level
    final double vol0 = theta; // start level
    final double omega = 0.001; // vol-of-vol
    final double rho = -0.3; // correlation

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);

    final int n = 21;
    final double deltaMoneyness = 0.1;
    final double alpha = -0.5;
    final double tol = 1e-9;

    final double[][] strikeNprice = PRICER.price(FORWARD, DF, T, true, heston, n, deltaMoneyness, sigma, alpha, tol);

    for (int i = 0; i < n; i++) {
      final double k = strikeNprice[i][0];
      final double price = strikeNprice[i][1];

      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(FORWARD, DF, 0.0), new EuropeanVanillaOption(k, T, true), price);
      //System.out.println(k + "\t" + impVol);
      assertEquals(sigma, impVol, 1e-3);
    }
  }
}
