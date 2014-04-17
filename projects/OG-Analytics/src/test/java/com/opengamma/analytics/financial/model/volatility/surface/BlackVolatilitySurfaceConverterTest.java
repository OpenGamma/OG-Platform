/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackVolatilitySurfaceConverterTest {

  private static final BlackVolatilitySurfaceDelta DELTA_SURFACE;
  private static final BlackVolatilitySurfaceStrike STRIKE_SURFACE;
  private static final ForwardCurve FORWARD_CURVE;
  private static final double SPOT = 167;
  private static final double DRIFT = 0.03;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
    Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... td) {
        double delta = td[1];
        return 0.2 + 2.0 * FunctionUtils.square(delta - 0.4);
      }
    };
    DELTA_SURFACE = new BlackVolatilitySurfaceDelta(FunctionalDoublesSurface.from(func), FORWARD_CURVE);

    Function<Double, Double> func2 = new Function<Double, Double>() {
      final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];

        double alpha = 0.3 * Math.exp(-0.3 * t) + 0.2;
        double beta = 1.0;
        double rho = -0.4;
        double nu = 0.6;
        return sabr.getVolatility(FORWARD_CURVE.getForward(t), k, t, alpha, beta, rho, nu);
      }
    };

    STRIKE_SURFACE = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(func2));

  }

  @Test
  public void testValues() {
    for (int t = 0; t < 10; t++) {
      assertEquals(0.2, DELTA_SURFACE.getVolatilityForDelta(t, 0.4), 1e-6);
    }
  }

  @Test
  public void roundTripTest() {
    deltaToStrikeToDelta(DELTA_SURFACE);
  }

  private void deltaToStrikeToDelta(final BlackVolatilitySurfaceDelta originalDeltaSurface) {
    final BlackVolatilitySurfaceStrike strikeSurface = BlackVolatilitySurfaceConverter.toStrikeSurface(originalDeltaSurface);
    BlackVolatilitySurfaceDelta newDeltaSurface = BlackVolatilitySurfaceConverter.toDeltaSurface(strikeSurface, originalDeltaSurface.getForwardCurve());
    for (int i = 0; i < 10; i++) {
      double t = Math.exp(i / 4.0) - 0.95;
      for (int j = 0; j < 21; j++) {
        double delta = 0.01 + 0.98 * j / 20.;
        double vol1 = originalDeltaSurface.getVolatilityForDelta(t, delta);
        double vol2 = newDeltaSurface.getVolatilityForDelta(t, delta);
        //System.out.println(t+"\t"+delta+"\t"+originalDeltaSurface.getVolatilityForDelta(t, delta));
        assertEquals(vol1, vol2, 1e-9);
      }
    }
  }

  /**
   * The strike surface is from the Hagan SABR formula which is well known to exhibit arbitrage (that is negative prices of butterflies or equivalently a negative implied density
   * at some strike)  for extreme strikes (the problem gets worse at time-to-expiry is increased).<p>
   * The round trip of strike to delta to strike surface CANNOT work at a strike where there is an arbitrage in the original surface.
   */
  @Test
  public void roundTripTest2() {

    BlackVolatilitySurfaceDelta deltaSurface = BlackVolatilitySurfaceConverter.toDeltaSurface(STRIKE_SURFACE, FORWARD_CURVE);
    BlackVolatilitySurfaceStrike strikeSurface = BlackVolatilitySurfaceConverter.toStrikeSurface(deltaSurface);

    //    double k = 1000;
    //    double vol = STRIKE_SURFACE.getVolatility(t, k);
    //    double delta = BlackFormulaRepository.delta(f, k, t, vol, true);
    //    System.out.println(vol + "\t" + delta);
    //    double vol2 = deltaSurface.getVolatilityForDelta(t, delta);
    //    System.out.println("vol from delta surface " + vol2);
    //    double k2 = BlackVolatilitySurfaceConverter.strikeForDelta(delta, STRIKE_SURFACE, FORWARD_CURVE, t);
    //    System.out.println("strike for delta " + k2);
    //    double k3 = BlackVolatilitySurfaceConverter.strikeForDelta(1e-4, STRIKE_SURFACE, FORWARD_CURVE, t);
    //    System.out.println("strike for delta (debug)" + k3);
    //    double delta2 = BlackVolatilitySurfaceConverter.deltaForStrike(k, deltaSurface, FORWARD_CURVE, t);
    //    System.out.println("delta for strike " + delta2);

    //    double expiry = 8.53;
    //    double fwd = FORWARD_CURVE.getForward(expiry);
    //
    //    for (int i = 0; i < 101; i++) {
    //      double x = 0.0 + 5.0 * i / 100.;
    //      double k = fwd * Math.exp(x);
    //      double vol = STRIKE_SURFACE.getVolatility(expiry, k);
    //
    //      double d = BlackVolatilitySurfaceConverter.deltaForStrike(k, deltaSurface, expiry);
    //      double vol2 = deltaSurface.getVolatilityForDelta(expiry, d);
    //      double price = BlackFormulaRepository.price(fwd, k, expiry, vol, true);
    //      System.out.println(x + "\t" + k + "\t" + vol + "\t" + d + "\t" + vol2 + "\t" + price);
    //    }
    //
    //    System.out.println();
    //    for (int i = 0; i < 101; i++) {
    //      double d = 0.001 + 0.998 * i / 100.;
    //
    //      double vol = deltaSurface.getVolatilityForDelta(t, d);
    //      double k = BlackVolatilitySurfaceConverter.strikeForDelta(d, STRIKE_SURFACE, FORWARD_CURVE, t);
    //      double vol2 = STRIKE_SURFACE.getVolatility(t, k);
    //      System.out.println(d + "\t" + vol + "\t" + k + "\t" + vol2);
    //
    //    }
    //
    //    double vol = strikeSurface.getVolatility(1.7682818284590456, 823.6246577301929);
    //

    // double debug = strikeSurface.getVolatility(6.44, 3.7);

    for (int i = 0; i < 10; i++) {
      double t = Math.exp(i / 4.0) - 0.95;
      double rootT = Math.sqrt(t);
      // double fwd = FORWARD_CURVE.getForward(t);
      for (int j = 0; j < 10; j++) {
        double k = SPOT * Math.exp(0.3 * rootT * (-4.5 + 6.0 * j / 9.));
        double vol1 = STRIKE_SURFACE.getVolatility(t, k);
        double vol2 = strikeSurface.getVolatility(t, k);

        //   System.out.println(t + "\t" + k + "\t" + vol1 + "\t" + vol2);

        //        double delta1 = BlackFormulaRepository.delta(fwd, k, t, vol1, true);
        //        double delta2 = BlackVolatilitySurfaceConverter.deltaForStrike(k, deltaSurface, t);
        //        double k2 = BlackVolatilitySurfaceConverter.strikeForDelta(delta1, STRIKE_SURFACE, FORWARD_CURVE, t);
        //
        //        System.out.println(delta1 + "\t" + delta2 + "\t" + k2);
        assertEquals(vol1, vol2, 1e-12);
      }
    }
  }
}
