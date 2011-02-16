/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class TestTimeChange {

  private static final double FORWARD = 1.0;
  private static final double T = 3.0;
  private static final double DF = 0.93;
  private static final double SIGMA = 0.2;

  private static final double KAPPA = 0.7; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = THETA; // start level
  private static final double OMEGA = 0.2; // vol-of-vol
  private static final double RH0 = -0.0; // correlation

  private static double ALPHA = -0.5;

  private static final double MAX_LOG_MONEYNESS = 0.1;
  private static final double EPS = 1e-6;

  private static final CharacteristicExponent HESTON = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RH0, T);
  private static final CharacteristicExponent CIR = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA, THETA / VOL0, OMEGA / Math.sqrt(VOL0), T);
  private static final CharacteristicExponent NORMAL = new GaussianCharacteristicExponent(-VOL0 / 2, Math.sqrt(VOL0), 1.0);
  private static final CharacteristicExponent NORMAL_CIR = new TimeChangedCharacteristicExponent(NORMAL, CIR);

  private static final FFTPricer FFT_PRICER = new FFTPricer();

  @Test
  public void testAgainstHeston() {
    int n = 21;

    double[][] heston_strikeNprice = FFT_PRICER.price(FORWARD, DF, true, HESTON, n, MAX_LOG_MONEYNESS, ALPHA, 0.01 * EPS, SIGMA);
    double[][] normalCIR_strikeNprice = FFT_PRICER.price(FORWARD, DF, true, NORMAL_CIR, n, MAX_LOG_MONEYNESS, ALPHA, 0.01 * EPS, SIGMA);

    for (int i = 0; i < n; i++) {
      assertEquals(heston_strikeNprice[i][0], normalCIR_strikeNprice[i][0], 1e-12);//should have the same strikes
      assertEquals(heston_strikeNprice[i][1], normalCIR_strikeNprice[i][1], 1e-12);//should have the same price
    }

  }

  @Test
  public void testCE() {

    for (int i = 0; i < 101; i++) {
      double x = 10.0 * i / 100.0;
      ComplexNumber z = new ComplexNumber(x, -(1 + ALPHA));
      ComplexNumber res1 = NORMAL_CIR.evaluate(z);
      ComplexNumber res2 = NORMAL.evaluate(z);
      ComplexNumber res3 = HESTON.evaluate(z);
      // System.out.println(x + "\t" + res1.getReal() + "\t" + +res2.getReal() + "\t" + res3.getReal() + "\t" + res1.getImaginary() + "\t" +
      //    res2.getImaginary() + "\t" + res3.getImaginary());
    }

  }

}
