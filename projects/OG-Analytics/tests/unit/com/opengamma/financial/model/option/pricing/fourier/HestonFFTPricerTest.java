/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;

/**
 * 
 */
public class HestonFFTPricerTest {

  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;

  @Test
  public void testLowVolOfVol() {
    FFTPricer pricer = new FFTPricer();
    double sigma = 0.36;

    double kappa = 1.0; // mean reversion speed
    double theta = sigma * sigma; // reversion level
    double vol0 = theta; // start level
    double omega = 0.001; // vol-of-vol
    double rho = -0.3; // correlation

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, T);

    int n = 21;
    double deltaMoneyness = 0.1;
    double alpha = -0.5;
    double tol = 1e-9;

    double[][] strikeNprice = pricer.price(FORWARD, DF, true, heston, n, deltaMoneyness, alpha, tol, sigma);

    for (int i = 0; i < n; i++) {
      double k = strikeNprice[i][0];
      double price = strikeNprice[i][1];
      double impVol = BlackImpliedVolFormula.impliedVol(price, FORWARD, k, DF, T, true);
      System.out.println(k + "\t" + impVol);
      assertEquals(sigma, impVol, 1e-3);
    }
  }

}
