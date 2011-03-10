/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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

  @Test
  public void testLowVolOfVol() {
    final FFTPricer pricer = new FFTPricer();
    final double sigma = 0.36;

    final double kappa = 1.0; // mean reversion speed
    final double theta = sigma * sigma; // reversion level
    final double vol0 = theta; // start level
    final double omega = 0.001; // vol-of-vol
    final double rho = -0.3; // correlation

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, T);

    final int n = 21;
    final double deltaMoneyness = 0.1;
    final double alpha = -0.5;
    final double tol = 1e-9;

    final double[][] strikeNprice = pricer.price(FORWARD, DF, true, heston, n, deltaMoneyness, alpha, tol, sigma);

    for (int i = 0; i < n; i++) {
      final double k = strikeNprice[i][0];
      final double price = strikeNprice[i][1];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      //System.out.println(k + "\t" + impVol);
      assertEquals(sigma, impVol, 1e-3);
    }
  }
}
