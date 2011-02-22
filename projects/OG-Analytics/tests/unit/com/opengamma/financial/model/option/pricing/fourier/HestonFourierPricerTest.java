/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class HestonFourierPricerTest {

  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;

  @Test
  public void testLowVolOfVol() {
    FourierPricer pricer = new FourierPricer();
    double sigma = 0.36;

    double kappa = 1.0; // mean reversion speed
    double theta = sigma * sigma; // reversion level
    double vol0 = theta; // start level
    double omega = 0.001; // vol-of-vol
    double rho = -0.3; // correlation

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, T);

    for (int i = 0; i < 21; i++) {
      double k = 0.2 + 3.0 * i / 20.0;
      double price = pricer.price(FORWARD, k * FORWARD, DF, true, heston, -0.5, 1e-6);
      double impVol = BlackImpliedVolFormula.impliedVol(price, FORWARD, k * FORWARD, DF, T, true);
      // System.out.println(k + "\t" + impVol);
      assertEquals(sigma, impVol, 1e-3);
    }
  }

  @Test
  public void testHestonCE() {

    double kappa = 1.0; // mean reversion speed
    double theta = 0.16; // reversion level
    double vol0 = theta; // start level
    double omega = 2; // vol-of-vol
    double rho = -0.8; // correlation

    // final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    // for (int j = 0; j < 101; j++) {
    // double y = -3. + j * 6. / 100.0;
    // System.out.print("\t" + y);
    // }
    // System.out.print("\n");
    //
    // for (int i = 0; i < 101; i++) {
    // double x = -3. + i * 6. / 100.0;
    // System.out.print(x + "\t");
    // for (int j = 0; j < 101; j++) {
    // double y = -3. + j * 6. / 100.0;
    // ComplexNumber res = heston.evaluate(new ComplexNumber(x, y), 0.25);
    // System.out.print(res.getReal() + "\t");
    // }
    // System.out.print("\n");
    // }
  }

  @Test
  public void testIntergrad_Heston() {

    double alpha = 0.75;

    double kappa = 1.0; // mean reversion speed
    double theta = 0.16; // reversion level
    double vol0 = theta; // start level
    double omega = 2; // vol-of-vol
    double rho = -0.8; // correlation
    double t = 1.0;// / 52.0;
    // final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    // EuropeanPriceIntegrand intergrand = new EuropeanPriceIntegrand(heston, alpha, 1, 2, t, true, 0.5);
    //
    // for (int i = 0; i < 201; i++) {
    // double x = -0. + i * 80. / 200.0;
    // ComplexNumber res = intergrand.getIntegrand(x);
    // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    // }

  }

  @Test
  public void testEuropeanCallFT_Heston() {

    double alpha = 0.75;

    double kappa = 1.0; // mean reversion speed
    double theta = 0.16; // reversion level
    double vol0 = theta; // start level
    double omega = 2; // vol-of-vol
    double rho = -0.8; // correlation
    double t = 1 / 52.0;
    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, t);
    EuropeanCallFT intergrand = new EuropeanCallFT(heston);

//        for (int i = 0; i < 201; i++) {
//          double x = -0. + i * 20. / 200.0;
//          ComplexNumber res = intergrand.evaluate(new ComplexNumber(x, -(1 - 0.6)));
//          System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
//        }

  }

  @Test
  public void test_Heston() {
    double alpha = 0.75;

    FourierPricer pricer = new FourierPricer();

    // parameters from the paper Not-so-complex logarithms in the Heston model
    double kappa = 1.0; // mean reversion speed
    double theta = 0.16; // reversion level
    double vol0 = theta; // start level
    double omega = 2; // vol-of-vol
    double rho = -0.8; // correlation

    double t = 1 / 12.0;

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, t);

    for (int i = 0; i < 11; i++) {
      double k = 0.5 + 1.0 * i / 10.0;

      double price = pricer.price(1, k, 1, true, heston, -0.5, 1e-8);
      double impVol = 0;

      impVol = BlackImpliedVolFormula.impliedVol(price, 1, k, 1, t, true);

     //  System.out.println(k + "\t" + impVol);
    }
  }

}
