/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class FFTPricerTest {

  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = Interpolator1DFactory.getInterpolator("DoubleQuadratic");

  private static final double FORWARD = 1;
  private static final double T = 1 / 52.0;
  private static final double DF = 0.96;
  private static final double MU = 0.07;
  private static final double SIGMA = 0.2;
  private static final FFTPricer PRICER = new FFTPricer();

  private static final CharacteristicExponent CEF = new GaussianCharacteristicExponent(-0.5 * SIGMA * SIGMA, SIGMA, T);

  @Test
  public void testLargeNumberOfStrikes() {
    boolean isCall = true;
    int n = 1024;
    int m = 100;
    double delta = 0.1;
    int nL = 550;
    int nH = 600;

    double alpha = -0.5;
    double tol = 1e-10;
    double[][] temp = PRICER.price(FORWARD, DF, isCall, CEF, nL, nH, alpha, delta, n, m);
    assertEquals(n + 1, temp.length);
  }

  @Test
  public void test() {
    boolean isCall = true;
    int nStrikes = 21;
    double deltaMoneyness = 0.01;
    double alpha = -0.5;
    double tol = 1e-10;

    double[][] strikeNprice = PRICER.price(FORWARD, DF, isCall, CEF, nStrikes, deltaMoneyness, alpha, tol, SIGMA);

    assertEquals(nStrikes, strikeNprice.length);
    double k;
    double price;
    for (int i = 0; i < nStrikes; i++) {
      price = strikeNprice[i][1];
      k = strikeNprice[i][0];

      double impVol = 0;
      try {
        impVol = BlackImpliedVolFormula.impliedVol(price, FORWARD, k, DF, T, true);
      } catch (Exception e) {
      }
      // System.out.println(k + "\t" + price + "\t" + impVol);
      assertEquals(SIGMA, impVol, 1e-5);
    }
  }

  @Test
  public void testPutsAndCalls() {
    boolean isCall = true;
    double tol = 1e-10;
    for (int i = 0; i < 5; i++) {
      double alpha = -1.6 + i * 0.5;
      for (int j = 0; j < 1; j++) {
        isCall = (j == 0);
        double[][] strikeNprice = PRICER.price(FORWARD, DF, isCall, CEF, FORWARD, FORWARD, 1, alpha, tol, 0.3);
        assertEquals(FORWARD, strikeNprice[0][0], 1e-9);
        assertEquals(SIGMA, BlackImpliedVolFormula.impliedVolNewton(strikeNprice[0][1], FORWARD, FORWARD, DF, T, isCall), 1e-7);
      }
    }
  }

  /**
   * This test that the same price is produced when the alpha, tolerance and limitSigma are changed 
   */
  @Test
  public void testStability() {
    boolean isCall = false;
    int nStrikes = 21;
    double kappa = 1.2;
    double theta = 0.1;
    double vol0 = 1.8 * theta;
    double omega = 0.4;
    double rho = -0.7;
    double t = 2.0;

    CharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, t);
    double[][] strikeNPrice = PRICER.price(FORWARD, DF, isCall, heston, 0.7 * FORWARD, 1.5 * FORWARD, nStrikes, -0.5, 1e-10, 0.3);

    int n = strikeNPrice.length;

    double[] k = new double[n];
    double[] vol = new double[n];
    for (int i = 0; i < n; i++) {
      k[i] = strikeNPrice[i][0];
      try {
        vol[i] = BlackImpliedVolFormula.impliedVolNewton(strikeNPrice[i][1], FORWARD, k[i], 1.0, t, isCall);
      } catch (Exception e) {
        vol[i] = 0.0;
      }
    }

    Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundleFromSortedArrays(k, vol);

    double[][] strikeNPrice2 = PRICER.price(FORWARD, DF, isCall, heston, 0.7 * FORWARD, 1.5 * FORWARD, nStrikes, 0.75, 1e-8, 0.2);
    int m = strikeNPrice2.length;
    for (int i = 0; i < m; i++) {
      double strike = strikeNPrice2[i][0];
      double sigma = BlackImpliedVolFormula.impliedVolNewton(strikeNPrice2[i][1], FORWARD, strike, 1.0, t, isCall);
      assertEquals(sigma, INTERPOLATOR.interpolate(dataBundle, strike), 1e-5);
    }

  }

  @Test
  public void testDirect() {
    final double alpha = 0.5;
    final EuropeanCallFT callFT = new EuropeanCallFT(CEF);
    Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        ComplexNumber z = new ComplexNumber(x, -1.0 - alpha);
        ComplexNumber u = callFT.evaluate(z);
        return u.getReal();
      }
    };

    RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D();
    double integral = integrator.integrate(f, 0.0, 1000.0) / Math.PI;

    double price = DF * FORWARD * integral;
    double impVol = 0;
    try {
      impVol = BlackImpliedVolFormula.impliedVol(price, FORWARD, FORWARD, DF, T, true);
    } catch (Exception e) {
    }
    assertEquals(SIGMA, impVol, 1e-5);
    // System.out.println(FORWARD + "\t" + price + "\t" + impVol);
  }

  @Test
  public void testEuropeanCallFT() {

    EuropeanCallFT callFT = new EuropeanCallFT(CEF);
    for (int i = 0; i < 101; i++) {
      double x = -10.0 + 20.0 * i / 100;
      ComplexNumber z = new ComplexNumber(x, -1.5);
      ComplexNumber u = callFT.evaluate(z);
      // System.out.println(x + "\t" + u.getReal() + "\t" + u.getImaginary());
    }

  }
}
