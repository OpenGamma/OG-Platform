/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class HestonFitterTest {

  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final double SIGMA = 0.36;

  private static final double KAPPA = 1.4; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = 1.5 * THETA; // start level
  private static final double OMEGA = 0.25; // vol-of-vol
  private static final double RH0 = -0.7; // correlation
  private static final double EPS = 1e-6;

  private static final int N = 7;
  private static final double[] strikes;
  private static final double[] vols;
  private static final double[] errors;

  static {
    CharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RH0, T);
    FourierPricer pricer = new FourierPricer();
    strikes = new double[N];
    vols = new double[N];
    errors = new double[N];
    for (int i = 0; i < N; i++) {
      errors[i] = 0.001;
      strikes[i] = 0.01 + 0.01 * i;
      double price = pricer.price(FORWARD, strikes[i], 1.0, true, heston, -0.5, 1e-9, SIGMA);
      vols[i] = BlackImpliedVolFormula.impliedVol(price, FORWARD, strikes[i], 1.0, T, true);
    }
  }

  @Test
  public void testExactFit() {
    HestonFitter fitter = new HestonFitter();
    double[] temp = new double[] {1.0, 0.04, 0.04, 0.2, 0.0 };
    LeastSquareResults results = fitter.solve(FORWARD, T, strikes, vols, errors, temp, null);
    assertEquals(0.0, results.getChiSq(), 1e-4);

  }
}
