/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderFromVolSurface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class BasisSpineVolatilitySurfaceProviderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test
  public void test() {
    final BasisSplineVolatilitySurfaceProvider vsPro = new BasisSplineVolatilitySurfaceProvider(0.0, 0.15, 10, 1, 0.0, 10.0, 7, 1);
    assertEquals(10 * 7, vsPro.getNumModelParameters());

    final DoubleMatrix1D w = new DoubleMatrix1D(70, 0.3);

    final VolatilitySurface vs = vsPro.getVolSurface(w);
    final int nSamples = 10;
    for (int i = 0; i < nSamples; i++) {
      final double t = 10.0 * RANDOM.nextDouble();
      final double k = 0.15 * RANDOM.nextDouble();
      assertEquals(0.3, vs.getVolatility(t, k), 1e-15);
    }
  }

  @Test
  public void sensitivityTest() {
    final VolatilitySurfaceProvider vsPro = new BasisSplineVolatilitySurfaceProvider(0.0, 0.15, 10, 3, 0.0, 10.0, 7, 2);

    final DoubleMatrix1D w = new DoubleMatrix1D(vsPro.getNumModelParameters());
    for (int i = 0; i < w.getNumberOfElements(); i++) {
      w.getData()[i] = RANDOM.nextDouble();
    }

    final VolatilitySurface volSurf = vsPro.getVolSurface(w);
    final Surface<Double, Double, DoubleMatrix1D> senseSurf = vsPro.getParameterSensitivitySurface(w);
    final Surface<Double, Double, Pair<Double, DoubleMatrix1D>> volAndSenseSurf = vsPro.getVolAndParameterSensitivitySurface(w);

    final int nSamples = 20;
    final DoublesPair[] points = new DoublesPair[nSamples];
    for (int i = 0; i < nSamples; i++) {
      final double t = 10.0 * RANDOM.nextDouble();
      final double k = 0.15 * RANDOM.nextDouble();
      points[i] = DoublesPair.of(t, k);

      final double vol = volSurf.getVolatility(points[i]);
      final DoubleMatrix1D sense = senseSurf.getZValue(points[i]);
      final Pair<Double, DoubleMatrix1D> volAndSense = volAndSenseSurf.getZValue(points[i]);
      assertEquals(vol, volAndSense.getFirst(), 1e-15);
      AssertMatrix.assertEqualsVectors(sense, volAndSense.getSecond(), 1e-15);
    }

    //create a DiscreteVolatilityFunctionProvider in order to compute the Jacobian for a (random) set the points 
    final DiscreteVolatilityFunctionProvider dvfp = new DiscreteVolatilityFunctionProviderFromVolSurface(vsPro);
    final DiscreteVolatilityFunction func = dvfp.from(points);
    final DoubleMatrix2D jac = func.calculateJacobian(w);
    final DoubleMatrix2D jacFD = func.calculateJacobianViaFD(w);
    AssertMatrix.assertEqualsMatrix(jacFD, jac, 1e-10);
  }

  @Test
  public void altConstructorTest() {
    final Function1D<double[], Double> flat = new Function1D<double[], Double>() {

      @Override
      public Double evaluate(final double[] x) {
        return 1.0;
      }
    };

    final List<Function1D<double[], Double>> functions = new ArrayList<>();
    functions.add(flat);
    final VolatilitySurfaceProvider vsPro = new BasisSplineVolatilitySurfaceProvider(functions);
    assertEquals(1, vsPro.getNumModelParameters());
    final VolatilitySurface vs = vsPro.getVolSurface(new DoubleMatrix1D(0.35));
    assertEquals(0.35, vs.getVolatility(4.5, 0.23));
  }
}
