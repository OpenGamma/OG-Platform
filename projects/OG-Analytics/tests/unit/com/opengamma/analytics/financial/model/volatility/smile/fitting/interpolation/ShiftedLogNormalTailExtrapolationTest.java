/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.MathException;

/**
 * 
 */
public class ShiftedLogNormalTailExtrapolationTest {

  @Test
  public void test() {
    //    double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(1.34, 3.477, 0.01917, -0.00336, 0.18413);
    //    assertEquals(0.18348337357940317, vol, 1e-9);

    double fwd = 0.05;
    double t = 1 / 52.;
    double mu = 0.05;
    double theta = 0.3;
    for (int i = 0; i < 201; i++) {
      double k = fwd * (0.2 + 0.7 * i / 200.);

      double c = Math.log(k / fwd);
      double a = t / 2;
      double b = (-c + mu - theta * theta * t / 2) / theta;
      double arg = b * b - 4 * a * c;
      if (arg < 0) {
        throw new MathException("cannot solve for sigma");
      }
      double root = Math.sqrt(arg);
      double sigma1 = (-b + root) / 2 / a;
      double sigma2 = (-b - root) / 2 / a;

      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(fwd, k, t, mu, theta);
      double price = ShiftedLogNormalTailExtrapolation.price(fwd, k, t, false, mu, theta);
      //   System.out.println(k + "\t" + price + "\t" + vol + "\t" + sigma1);
    }

    double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(fwd, 0.2 * fwd, t, mu, theta);

  }

  @Test
  public void testUpperTail() {
    //    double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(1.34, 3.477, 0.01917, -0.00336, 0.18413);
    //    assertEquals(0.18348337357940317, vol, 1e-9);

    double fwd = 0.05;
    double t = 1 / 52.;
    double mu = -0.05;
    double theta = 0.3;
    for (int i = 0; i < 201; i++) {
      double k = fwd * (1.3 + 5.0 * i / 200.);

      double c = Math.log(k / fwd);
      double a = t / 2;
      double b = (-c + mu - theta * theta * t / 2) / theta;
      double arg = b * b - 4 * a * c;
      if (arg < 0) {
        throw new MathException("cannot solve for sigma");
      }
      double root = Math.sqrt(arg);
      double sigma1 = (-b + root) / 2 / a;
      double sigma2 = (-b - root) / 2 / a;

      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(fwd, k, t, mu, theta);
      double price = ShiftedLogNormalTailExtrapolation.price(fwd, k, t, true, mu, theta);
      //  System.out.println(k + "\t" + price + "\t" + vol + "\t" + sigma2);
    }

    double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(fwd, 0.2 * fwd, t, mu, theta);

  }
}
