/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.surface.ParameterizedVolatilitySurfaceProvider;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.ParameterizedSurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Set up a {@link VolatilitySurfaceProvider} that is quadratic in time-to-expiry (t) and strike (k) 
 * (so-called practitioners Black-Scholes, with 6 parameters), then convert this to a {@link DiscreteVolatilityFunctionProvider}
 * and obtain a {@link DiscreteVolatilityFunction} at randomly chosen expiry-strike points. Check the volatilities and
 * their sensitivity to the model parameters are correct. 
 */
@Test(groups = TestGroup.UNIT)
public class DiscreteVolatilityFunctionProviderFromVolSurfaceTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ParameterizedSurface SURFACE;
  private static final VolatilitySurfaceProvider VOL_SURF_PRO;

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

    VOL_SURF_PRO = new ParameterizedVolatilitySurfaceProvider(SURFACE);
  }

  @Test
  public void test() {
    final DiscreteVolatilityFunctionProvider discPro = new DiscreteVolatilityFunctionProviderFromVolSurface(VOL_SURF_PRO);

    final int nSamples = 35;
    final DoublesPair[] points = new DoublesPair[nSamples];
    for (int i = 0; i < nSamples; i++) {
      final double t = RANDOM.nextDouble() * 20.0;
      final double k = RANDOM.nextDouble() * 0.15;
      points[i] = DoublesPair.of(t, k);
    }

    final DiscreteVolatilityFunction func = discPro.from(points);

    //parameters of the vol surface 
    final DoubleMatrix1D a = new DoubleMatrix1D(0.3, -0.02, -0.4, 0.003, 15.6, -0.05);
    final DoubleMatrix1D vols = func.evaluate(a);
    final Function1D<DoublesPair, Double> volFunc = SURFACE.asFunctionOfArguments(a);
    for (int i = 0; i < nSamples; i++) {
      assertEquals(volFunc.evaluate(points[i]), vols.getEntry(i), 1e-15);
    }

    final DoubleMatrix2D jac = func.calculateJacobian(a);
    final DoubleMatrix2D fdJac = func.calculateJacobianViaFD(a);
    AssertMatrix.assertEqualsMatrix(fdJac, jac, 1e-11);

    assertEquals(SURFACE.getNumberOfParameters(), func.getLengthOfDomain());
    assertEquals(nSamples, func.getLengthOfRange());
  }

}
