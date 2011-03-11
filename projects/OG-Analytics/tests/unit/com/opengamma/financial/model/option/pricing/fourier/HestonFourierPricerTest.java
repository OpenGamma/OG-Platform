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
public class HestonFourierPricerTest {
  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  @Test
  public void testLowVolOfVol() {
    final double sigma = 0.36;

    final double kappa = 1.0; // mean reversion speed
    final double theta = sigma * sigma; // reversion level
    final double vol0 = theta; // start level
    final double omega = 0.001; // vol-of-vol
    final double rho = -0.3; // correlation

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();

    for (int i = 0; i < 21; i++) {
      final double k = 0.2 + 3.0 * i / 20.0;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k * FORWARD, T, true);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      final double price = pricer.price(data, option, heston, -0.5, 1e-6);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      assertEquals(sigma, impVol, 1e-3);
    }
  }

  @Test
  public void testHestonCE() {

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

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
  public void testIntegrandHeston() {

    final double alpha = 0.75;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation
    final double t = 1.0;// / 52.0;
    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(heston, alpha, true);
    final BlackFunctionData data = new BlackFunctionData(1, 1, 0.5);
    //    for (int i = 0; i < 201; i++) {
    //      final double x = -0. + i * 80. / 200.0;
    //      final EuropeanVanillaOption option = new EuropeanVanillaOption(2, t, true);
    //      final ComplexNumber res = intergrand.getIntegrand(x);
    //      System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    //    }

  }

  @Test
  public void testEuropeanCallFTHeston() {

    final double alpha = 0.75;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation
    final double t = 1 / 52.0;
    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final EuropeanCallFourierTransform integrand = new EuropeanCallFourierTransform(heston);

    //        for (int i = 0; i < 201; i++) {
    //          double x = -0. + i * 20. / 200.0;
    //          ComplexNumber res = intergrand.evaluate(new ComplexNumber(x, -(1 - 0.6)));
    //          System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    //        }

  }

  @Test
  public void testHeston() {
    final double alpha = 0.75;

    // parameters from the paper Not-so-complex logarithms in the Heston model
    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double t = 1 / 12.0;

    final CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(1, 1, 0);
    for (int i = 0; i < 11; i++) {
      final double k = 0.5 + 1.0 * i / 10.0;

      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final double price = pricer.price(data, option, heston, alpha, 1e-8);
      BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      //  System.out.println(k + "\t" + impVol);
    }
  }

}
