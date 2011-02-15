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
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class FFTPricerTest {

  private static final double FORWARD = 1;
  private static final double T = 1 / 52.0;
  private static final double DF = 0.96;
  private static final double MU = 0.07;
  private static final double SIGMA = 0.2;

  private static final CharacteristicExponent CEF = new GaussianCharacteristicExponent(-0.5 * SIGMA * SIGMA, SIGMA, T);

  @Test
  public void test() {
    boolean isCall = true;
    int nStrikes = 21;
    double deltaMoneyness = 0.01;
    double alpha = -0.5;
    double tol = 1e-8;

    FFTPricer pricer = new FFTPricer();
    double[][] strikeNprice = null;
    for (int i = 0; i < 1000; i++) {
      strikeNprice = pricer.price(FORWARD, DF, isCall, CEF, nStrikes, deltaMoneyness, alpha, tol, SIGMA);
    }
    // strikeNprice = pricer.price(FORWARD, T, DF, true, CEF, 51, -0.5, 0.1, 16383, 2000);

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
      System.out.println(k + "\t" + price + "\t" + impVol);
      //assertEquals(SIGMA, impVol, 1e-6);

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
