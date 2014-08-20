/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.ParameterizedSurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Set up a {@link ParameterizedSurface} that is quadratic in time-to-expiry (t) and strike (k)  (so-called practitioners
 * Black-Scholes, with 6 parameters), and use this to make a {@link VolatilitySurfaceProvider}. <P>
 * A {@link ParameterizedSurface} and a {@link VolatilitySurfaceProvider} are conceptually similar things in that
 * they both give a volatility and its sensitivity at a particular (expiry-strike) point dependent on same parameters.
 */
@Test(groups = TestGroup.UNIT)
public class ParameterizedVolatilitySurfaceProviderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  private static final ParameterizedSurface SURFACE;

  static {
    SURFACE = new ParameterizedSurface() {

      @Override
      public int getNumberOfParameters() {
        return 6;
      }

      @Override
      public Double evaluate(final DoublesPair tk, final DoubleMatrix1D parameters) {
        final double[] a = parameters.getData();
        final double t = tk.first;
        final double k = tk.second;
        return a[0] + a[1] * t + a[2] * k + a[3] * t * t + a[4] * k * k + a[5] * k * t;
      }

      @Override
      public Function1D<DoublesPair, DoubleMatrix1D> getZParameterSensitivity(final DoubleMatrix1D params) {
        return new Function1D<DoublesPair, DoubleMatrix1D>() {

          @Override
          public DoubleMatrix1D evaluate(final DoublesPair tk) {
            final double t = tk.first;
            final double k = tk.second;
            return new DoubleMatrix1D(1.0, t, k, t * t, k * k, k * t);
          }
        };

      }
    };
  }

  @Test
  public void test() {

    //parameters of the vol surface 
    final DoubleMatrix1D a = new DoubleMatrix1D(0.3, -0.02, -0.4, 0.003, 15.6, -0.05);
    final VolatilitySurfaceProvider pro = new ParameterizedVolatilitySurfaceProvider(SURFACE);
    final VolatilitySurface vs = pro.getVolSurface(a);
    final Function1D<DoublesPair, Double> vf = SURFACE.asFunctionOfArguments(a);
    assertEquals(SURFACE.getNumberOfParameters(), pro.getNumModelParameters());

    //check values
    final int nSamples = 35;
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      final DoublesPair point = DoublesPair.of(t, k);
      final double v1 = vf.evaluate(point);
      final double v2 = vs.getVolatility(point);
      assertEquals(v1, v2, 1e-14);
    }

    //check sensitivities 
    final Function1D<DoublesPair, DoubleMatrix1D> senseFunc = SURFACE.getZParameterSensitivity(a);
    final Surface<Double, Double, DoubleMatrix1D> senseSurf = pro.getParameterSensitivitySurface(a);
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      final DoublesPair point = DoublesPair.of(t, k);
      final DoubleMatrix1D s1 = senseFunc.evaluate(point);
      final DoubleMatrix1D s2 = senseSurf.getZValue(point);
      AssertMatrix.assertEqualsVectors(s1, s2, 1e-13);
    }

    final Surface<Double, Double, Pair<Double, DoubleMatrix1D>> vsSurf = pro.getVolAndParameterSensitivitySurface(a);
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      final DoublesPair point = DoublesPair.of(t, k);
      final Double vol = vs.getVolatility(point);
      final DoubleMatrix1D sense = senseSurf.getZValue(point);
      final Pair<Double, DoubleMatrix1D> volAndSense = vsSurf.getZValue(point);

      assertEquals(vol, volAndSense.getFirst(), 1e-14);
      AssertMatrix.assertEqualsVectors(sense, volAndSense.getSecond(), 1e-13);
    }

  }

}
