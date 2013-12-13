/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ShiftedLogNormalTailExtrapolationTest {
  private static final ShiftedLogNormalTailExtrapolationFitter FITTER = new ShiftedLogNormalTailExtrapolationFitter();
  private static final double FORWARD = 0.04;
  private static final double EXPIRY = 2.5;
  private static final double[][] LEFT_STRIKES = {
      {0.0057, 0.0061 },
      {0.02, 0.025 },
      {0.02, 0.021 },
      {0.015, 0.035 } };
  private static final double[][] RIGHT_STRIKES = {
      {0.041, 0.0415 },
      {0.045, 0.09 },
      {0.055, 0.065 } };
  private static final double[][] LEFT_VOLS = {
      {0.7366, 0.7277 },
      {0.35, 0.3 },
      {0.4, 0.41 },
      {0.31, 0.15 } };
  private static final double[][] RIGHT_VOLS = {
      {0.35, 0.348 },
      {0.4, 0.42 },
      {0.31, 0.37 } };
  private static final double[] LEFT_DD = {0.1, 0.1, 0.2, 0.1 };
  private static final double[] RIGHT_DD = {-0.4, -0.4, -0.3 };
  private static final double[] LEFT_DV_DK;
  private static final double[] RIGHT_DV_DK;
  private static final double[][] LEFT_PRICES;
  private static final double[][] RIGHT_PRICES;

  static {
    final int nl = LEFT_STRIKES.length;
    LEFT_PRICES = new double[nl][2];
    LEFT_DV_DK = new double[nl];
    for (int i = 0; i < nl; i++) {
      LEFT_DV_DK[i] = (LEFT_DD[i] - BlackFormulaRepository.dualDelta(FORWARD, LEFT_STRIKES[i][0], EXPIRY, LEFT_VOLS[i][0], false)) /
          BlackFormulaRepository.vega(FORWARD, LEFT_STRIKES[i][0], EXPIRY, LEFT_VOLS[i][0]);
      for (int j = 0; j < 2; j++) {
        LEFT_PRICES[i][j] = BlackFormulaRepository.price(FORWARD, LEFT_STRIKES[i][j], EXPIRY, LEFT_VOLS[i][j], false);
      }
    }
    final int nr = RIGHT_STRIKES.length;
    RIGHT_PRICES = new double[nl][2];
    RIGHT_DV_DK = new double[nr];
    for (int i = 0; i < nr; i++) {
      RIGHT_DV_DK[i] = (RIGHT_DD[i] - BlackFormulaRepository.dualDelta(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, RIGHT_VOLS[i][0], true)) /
          BlackFormulaRepository.vega(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, RIGHT_VOLS[i][0]);
      for (int j = 0; j < 2; j++) {
        RIGHT_PRICES[i][j] = BlackFormulaRepository.price(FORWARD, RIGHT_STRIKES[i][j], EXPIRY, RIGHT_VOLS[i][j], true);
      }
    }
  }

  @Test
  public void leftTailVolTest() {
    final int nl = LEFT_STRIKES.length;
    for (int i = 0; i < nl; i++) {
      double[] res = FITTER.fitTwoVolatilities(FORWARD, LEFT_STRIKES[i], LEFT_VOLS[i], EXPIRY);
      assertEquals(2, res.length);
      for (int j = 0; j < 2; j++) {
        double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, LEFT_STRIKES[i][j], EXPIRY, res[0], res[1]);
        assertEquals(LEFT_VOLS[i][j], vol, 1e-9);
      }
      for (int j = 0; j < 5; j++) {
        double k = FORWARD * (j / 10.);
        double vol0 = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, k, EXPIRY, res[0], res[1]);
        assertTrue(vol0 > 0.0 && !Double.isInfinite(vol0) && !Double.isNaN(vol0));
      }
    }
  }

  @Test
  public void leftTailPriceTest() {
    final int nl = LEFT_STRIKES.length;
    for (int i = 0; i < nl; i++) {
      double[] res = FITTER.fitTwoPrices(FORWARD, LEFT_STRIKES[i], LEFT_PRICES[i], EXPIRY, false);
      assertEquals(2, res.length);
      for (int j = 0; j < 2; j++) {
        double price = ShiftedLogNormalTailExtrapolation.price(FORWARD, LEFT_STRIKES[i][j], EXPIRY, false, res[0], res[1]);
        assertEquals(LEFT_PRICES[i][j], price, 1e-9);
      }
      double pOld = ShiftedLogNormalTailExtrapolation.price(FORWARD, 0.0, EXPIRY, false, res[0], res[1]);
      assertEquals(0.0, pOld, 0.0);
      for (int j = 1; j < 5; j++) {
        double k = FORWARD * (j / 10.);
        double price = ShiftedLogNormalTailExtrapolation.price(FORWARD, k, EXPIRY, false, res[0], res[1]);
        assertTrue(price > pOld);
        pOld = price;
      }
    }
  }

  @Test
  public void leftTailPriceGradTest() {
    final boolean isCall = false;
    final int n = LEFT_STRIKES.length;
    for (int i = 0; i < n; i++) {
      double[] res = FITTER.fitPriceAndGrad(FORWARD, LEFT_STRIKES[i][0], LEFT_PRICES[i][0], LEFT_DD[i], EXPIRY, isCall);
      assertEquals(2, res.length);
      double price = ShiftedLogNormalTailExtrapolation.price(FORWARD, LEFT_STRIKES[i][0], EXPIRY, isCall, res[0], res[1]);
      assertEquals(LEFT_PRICES[i][0], price, 1e-9);
      double dd = ShiftedLogNormalTailExtrapolation.dualDelta(FORWARD, LEFT_STRIKES[i][0], EXPIRY, isCall, res[0], res[1]);
      assertEquals(LEFT_DD[i], dd, 1e-9);
    }
  }

  @Test
  public void leftTailVolGradTest() {
    final int n = LEFT_STRIKES.length;
    for (int i = 0; i < n; i++) {
      double[] res = FITTER.fitVolatilityAndGrad(FORWARD, LEFT_STRIKES[i][0], LEFT_VOLS[i][0], LEFT_DV_DK[i], EXPIRY);
      assertEquals(2, res.length);
      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, LEFT_STRIKES[i][0], EXPIRY, res[0], res[1]);
      assertEquals(LEFT_VOLS[i][0], vol, 1e-8);
      double dd = ShiftedLogNormalTailExtrapolation.dVdK(FORWARD, LEFT_STRIKES[i][0], EXPIRY, res[0], res[1], vol);
      assertEquals(LEFT_DV_DK[i], dd, 1e-6);
    }
  }
  
  @Test
  public void leftTailVolGradZeroTest() {
    final int n = LEFT_STRIKES.length;
    final double volGrad00 = 0.0;
    for (int i = 0; i < n; i++) {
      double[] res = FITTER.fitVolatilityAndGrad(FORWARD, LEFT_STRIKES[i][0], LEFT_VOLS[i][0], volGrad00, EXPIRY);
      assertEquals(2, res.length);
      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, LEFT_STRIKES[i][0], EXPIRY, res[0], res[1]);
      assertEquals(LEFT_VOLS[i][0], vol, 1e-8);
    }
  }
  
  @Test
  public void leftTailVolMaxGradTest() {
    int i = 2;
    final double strike = LEFT_STRIKES[i][0];
    final double volInput = LEFT_VOLS[i][0];

    final boolean isCall = strike >= FORWARD;
    final double blackDD = BlackFormulaRepository.dualDelta(FORWARD, strike, EXPIRY, volInput, isCall);
    final double blackVega = BlackFormulaRepository.vega(FORWARD, strike, EXPIRY, volInput);

    final double maxGrad = (isCall ? -blackDD : 1 - blackDD) / blackVega;

    double[] res = FITTER.fitVolatilityAndGrad(FORWARD, strike, volInput, maxGrad-1e-8, EXPIRY);
    double volOutput = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, LEFT_STRIKES[i][0], EXPIRY, res[0], res[1]);
    assertEquals(volInput, volOutput, 1e-8);
  }
  
  @Test(enabled = false)
  public void leftTailVolGradComparison() {
    final int i = 2;
    double[] shiftedParams = FITTER.fitVolatilityAndGrad(FORWARD, LEFT_STRIKES[i][0], LEFT_VOLS[i][0], LEFT_DV_DK[i], EXPIRY);
    double[] shiftedParamsZero = FITTER.fitVolatilityAndGrad(FORWARD, LEFT_STRIKES[i][0], LEFT_VOLS[i][0], 0.0, EXPIRY);
    
    final int nSteps = 200;
    final double kMax = LEFT_STRIKES[i][0];
    final double sizeSteps = kMax / nSteps;
    double k = sizeSteps;
    
    System.out.println("Strike" + "," + "DualDelta" + "," + "Flat");
    for (int j = 0; j < nSteps; j++) {
      
      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, k, EXPIRY, shiftedParams[0], shiftedParams[1]);
      double volZero = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, k, EXPIRY, shiftedParamsZero[0], shiftedParamsZero[1]);
      System.out.println(k + "," + vol + "," + volZero);
      k += sizeSteps;
    }
  }
  
  
  
  @Test
  public void rightTailVolTest() {
    final int nl = RIGHT_STRIKES.length;
    for (int i = 0; i < nl; i++) {
      double[] res = FITTER.fitTwoVolatilities(FORWARD, RIGHT_STRIKES[i], RIGHT_VOLS[i], EXPIRY);
      assertEquals(2, res.length);
      for (int j = 0; j < 2; j++) {
        double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, RIGHT_STRIKES[i][j], EXPIRY, res[0], res[1]);
        assertEquals(RIGHT_VOLS[i][j], vol, 1e-9);
      }
      for (int j = 1; j < 6; j++) {
        double k = 2 * FORWARD * j;
        double vol0 = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, k, EXPIRY, res[0], res[1]);
        assertTrue(vol0 > 0.0 && !Double.isInfinite(vol0) && !Double.isNaN(vol0));
      }
    }
  }

  @Test
  public void rightTailPriceTest() {
    final int nl = RIGHT_STRIKES.length;
    for (int i = 0; i < nl; i++) {
      double[] res = FITTER.fitTwoPrices(FORWARD, RIGHT_STRIKES[i], RIGHT_PRICES[i], EXPIRY, true);
      assertEquals(2, res.length);
      for (int j = 0; j < 2; j++) {
        double price = ShiftedLogNormalTailExtrapolation.price(FORWARD, RIGHT_STRIKES[i][j], EXPIRY, true, res[0], res[1]);
        assertEquals(RIGHT_PRICES[i][j], price, 1e-9);
      }
      double pOld = ShiftedLogNormalTailExtrapolation.price(FORWARD, 2.0 * FORWARD, EXPIRY, true, res[0], res[1]);
      for (int j = 2; j < 6; j++) {
        double k = 2 * FORWARD * j;
        double price = ShiftedLogNormalTailExtrapolation.price(FORWARD, k, EXPIRY, true, res[0], res[1]);
        assertTrue(price < pOld);
        pOld = price;
      }
    }
  }

  @Test
  public void rightTailPriceGradTest() {
    final boolean isCall = true;
    final int nr = RIGHT_STRIKES.length;
    for (int i = 0; i < nr; i++) {
      double[] res = FITTER.fitPriceAndGrad(FORWARD, RIGHT_STRIKES[i][0], RIGHT_PRICES[i][0], RIGHT_DD[i], EXPIRY, isCall);
      assertEquals(2, res.length);
      double price = ShiftedLogNormalTailExtrapolation.price(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, isCall, res[0], res[1]);
      assertEquals(RIGHT_PRICES[i][0], price, 1e-9);
      double dd = ShiftedLogNormalTailExtrapolation.dualDelta(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, isCall, res[0], res[1]);
      assertEquals(RIGHT_DD[i], dd, 1e-7);
    }
  }

  @Test
  public void rightTailVolGradTest() {
    final int n = RIGHT_STRIKES.length;
    for (int i = 0; i < n; i++) {
      double[] res = FITTER.fitVolatilityAndGrad(FORWARD, RIGHT_STRIKES[i][0], RIGHT_VOLS[i][0], RIGHT_DV_DK[i], EXPIRY);
      assertEquals(2, res.length);
      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, res[0], res[1]);
      assertEquals(RIGHT_VOLS[i][0], vol, 1e-7);
      double dd = ShiftedLogNormalTailExtrapolation.dVdK(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, res[0], res[1], vol);
      assertEquals(RIGHT_DV_DK[i], dd, 1e-6);
    }
  }

  @Test
  public void rightTailVolGradZeroTest() {
    final int n = RIGHT_STRIKES.length;
    for (int i = 0; i < n; i++) {
      double[] res = FITTER.fitVolatilityAndGrad(FORWARD, RIGHT_STRIKES[i][0], RIGHT_VOLS[i][0], 0.0, EXPIRY);
      assertEquals(2, res.length);
      double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, RIGHT_STRIKES[i][0], EXPIRY, res[0], res[1]);
      assertEquals(RIGHT_VOLS[i][0], vol, 1e-7);
    }
  }
  
  @Test
      (enabled = false)
      public void leftTailVolPrint() {
    final int nl = LEFT_STRIKES.length;
    for (int i = 0; i < nl; i++) {
      //  double[] res = FITTER.fitTwoVolatilities(FORWARD, LEFT_STRIKES[i], LEFT_VOLS[i], EXPIARY);
      double[] res = FITTER.fitVolatilityAndGrad(FORWARD, LEFT_STRIKES[i][0], LEFT_VOLS[i][0], LEFT_DV_DK[i], EXPIRY);
      assertEquals(2, res.length);
      System.out.println("fit:\t" + res[0] + "\t" + res[1]);
      for (int j = 0; j < 101; j++) {
        double d = -5.0 + 4.8 * j / 100.;
        double k = FORWARD * Math.exp(d * Math.sqrt(EXPIRY));
        double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, k, EXPIRY, res[0], res[1]);
        System.out.println(k + "\t" + vol);
      }
      System.out.print("\n");
    }
  }

  @Test
      (enabled = false)
      public void rightTailVolPrint() {
    System.out.println("ShiftedLogNormalTailExtrapolationTest");
    final int n = RIGHT_STRIKES.length;
    for (int i = 0; i < n; i++) {
      double[] res = FITTER.fitTwoVolatilities(FORWARD, RIGHT_STRIKES[i], RIGHT_VOLS[i], EXPIRY);
      assertEquals(2, res.length);
      System.out.println("fit:\t" + res[0] + "\t" + res[1]);
      for (int j = 0; j < 101; j++) {
        double d = 1.0 + 4.5 * j / 100.;
        double k = FORWARD * Math.exp(d * Math.sqrt(EXPIRY));
        double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, k, EXPIRY, res[0], res[1]);
        System.out.println(k + "\t" + vol);
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void printSeachSurfaceTest() {
    System.out.println("ShiftedLogNormalTailExtrapolationTest");
    Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double mu = x[0];
        double sigma = x[1];
        double vol1 = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, LEFT_STRIKES[1][0], EXPIRY, mu, sigma) - LEFT_VOLS[1][0];
        double vol2 = ShiftedLogNormalTailExtrapolation.impliedVolatility(FORWARD, LEFT_STRIKES[1][1], EXPIRY, mu, sigma) - LEFT_VOLS[1][1];
        return vol1 * vol1 + vol2 * vol2;
      }
    };
    FunctionalDoublesSurface surf = FunctionalDoublesSurface.from(func);
    PDEUtilityTools.printSurface("debug", surf, -0.7, -0.0, 0.01, 0.4, 200, 200);
  }

  @Test(enabled = false)
  public void debugTest() {
    double eps = 1e-300;
    double f = 1.0;
    double t = 2. / 52.;
    //    double price = 0.0005;
    //    double dd = -0.04;
    double k = 1.24;

    //    double mu = 0.1;
    //    double theta = 0.4;

    //    double p = ShiftedLogNormalTailExtrapolation.price(f, k, t, true, mu, theta);
    //    double dd = ShiftedLogNormalTailExtrapolation.dualDelta(f, k, t, true, mu, theta);
    //    System.out.println(p + "\t" + dd);

    for (int i = 0; i < 100; i++) {
      double mu = 0.172 + 0.0005 * i / 100.;
      for (int j = 0; j < 100; j++) {
        double theta = 0.125 + 0.001 * j / 100.;
        double p = Math.max(eps, ShiftedLogNormalTailExtrapolation.price(f, k, t, true, mu, theta));
        double dd = Math.min(-eps, ShiftedLogNormalTailExtrapolation.dualDelta(f, k, t, true, mu, theta));
        System.out.println(mu + "\t" + theta + "\t" + p + "\t" + dd);
      }
    }

    //    ShiftedLogNormalTailExtrapolationFitter fitter = new ShiftedLogNormalTailExtrapolationFitter();
    //    double[] res = fitter.fitPriceAndGrad(f, k, price, dd, t, true);
    //    System.out.println(res[0] + "\t" + res[1]);
  }

  @Test
      (enabled = false)
      public void debugTest2() {
    final ParameterLimitsTransform trans = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    ShiftedLogNormalTailExtrapolationFitter fitter = new ShiftedLogNormalTailExtrapolationFitter();
    double f = 1.0;
    double t = 2. / 52.;
    double k = 1.1;

    double mu = 0.133333;
    double theta = 0.16;
    double p = ShiftedLogNormalTailExtrapolation.price(f, k, t, true, mu, theta);
    double dd = ShiftedLogNormalTailExtrapolation.dualDelta(f, k, t, true, mu, theta);
    System.out.println("price and DD " + p + "\t" + dd);
    System.out.println("trans " + trans.inverseTransform(-2.2521684610628063));

    double[] res = fitter.fitPriceAndGrad(f, k, p, dd, t, true);
    assertEquals("mu ", mu, res[0], 1e-8);
    assertEquals("theta", theta, res[1], 1e-8);
  }

  @Test
  public void roundTripTest() {
    ShiftedLogNormalTailExtrapolationFitter fitter = new ShiftedLogNormalTailExtrapolationFitter();
    double f = 1.0;
    double t = 2. / 52.;
    for (int i = 0; i < 10; i++) {
      double mu = 0.0 + 0.2 * i / 9.;
      for (int j = 0; j < 10; j++) {
        double theta = 0.1 + 0.3 * j / 10.;
        for (int k = 0; k < 10; k++) {
          double strike = 1.1 + 0.1 * k;
          double p = ShiftedLogNormalTailExtrapolation.price(f, strike, t, true, mu, theta);
          double dd = ShiftedLogNormalTailExtrapolation.dualDelta(f, strike, t, true, mu, theta);
          //  System.out.println(" ShiftedLogNormalTailExtrapolationTest " + mu + "\t" + theta + "\t" + strike + "\t" + p + "\t" + dd);
          double[] res = fitter.fitPriceAndGrad(f, strike, p, dd, t, true);
          assertEquals("mu (k = " + strike + ", mu = " + mu + ", theta = " + theta + ")", mu, res[0], 1e-7);
          assertEquals("theta (k = " + strike + ", mu = " + mu + ", theta = " + theta + ")", theta, res[1], 1e-7);
        }
      }
    }
  }

  @Test(enabled = false)
  public void failingTest() {
    double f = 1332.6440427977093;
    double k = 500.0;
    double t = 0.06557377049180328;
    double vol = 1.173565;
    double volGrad = -0.002302809210959822 / 1.1;
    ShiftedLogNormalTailExtrapolationFitter fitter = new ShiftedLogNormalTailExtrapolationFitter();

    double[] res = fitter.fitVolatilityAndGrad(f, k, vol, volGrad, t);
    System.out.println(res[0] + "\t" + res[1]);
  }

  @Test
      (enabled = false)
      public void printSeachSurfaceTest2() {
    final double f = 1332.6440427977093;
    final double k = 500.0;
    final double t = 0.06557377049180328;
    final double vol = 1.173565;
    final double volGrad = -0.002302809210959822 / 1.0;
    final double p = BlackFormulaRepository.price(f, k, t, vol, false);
    final double dd = BlackFormulaRepository.dualDelta(f, k, t, vol, false) + BlackFormulaRepository.vega(f, k, t, vol) * volGrad;

    System.out.println("ShiftedLogNormalTailExtrapolationTest");
    Function<Double, Double> func = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double mu = x[0];
        double sigma = x[1];
        //  double sigma = 0.8 * mu + s;
        double p1 = Math.log(ShiftedLogNormalTailExtrapolation.price(f, k, t, false, mu, sigma) / p);
        double p2 = Math.log(ShiftedLogNormalTailExtrapolation.dualDelta(f, k, t, false, mu, sigma) / dd);
        double temp = p1 * p1 + p2 * p2;
        return Double.isInfinite(temp) ? 1e6 : temp;
      }
    };
    FunctionalDoublesSurface surf = FunctionalDoublesSurface.from(func);
    PDEUtilityTools.printSurface("debug", surf, 250, 300, 70, 90.0, 200, 200);

    double mu = 0.1;
    double sigma = 0.1;

    double r1 = ShiftedLogNormalTailExtrapolation.impliedVolatility(f, k, t, mu, sigma);
    double r2 = ShiftedLogNormalTailExtrapolation.dVdK(f, k, t, mu, sigma);
    System.out.println(r1 + "\t" + r2);
  }

}
