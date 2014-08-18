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
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
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
public class InterpolatedVolatilityTermStructureProviderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double[] KNOTS = new double[] {0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0 };
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory
      .getInterpolator(Interpolator1DFactory.CLAMPED_CUBIC_NONNEGATIVE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final double[] KNOT_VAULES = new double[] {0.3, 0.35, 0.4, 0.36, 0.31, 0.25, 0.2 };

  private static final VolatilitySurfaceProvider SURF_PRO = new InterpolatedVolatilityTermStructureProvider(KNOTS, INTERPOLATOR);

  /**
   * Check the vols from the surface against a curve build with the same data 
   */
  @Test
  public void valueTest() {
    assertEquals(KNOTS.length, SURF_PRO.getNumModelParameters());
    final VolatilitySurface vs = SURF_PRO.getVolSurface(new DoubleMatrix1D(KNOT_VAULES));
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(KNOTS, KNOT_VAULES, INTERPOLATOR, true);
    for (int i = 0; i < 20; i++) {
      final double t = 12.0 * i / (19.0);
      final double k = RANDOM.nextDouble();
      final double vol1 = curve.getYValue(t);
      final double vol2 = vs.getVolatility(t, k);
      assertEquals(vol1, vol2, 1e-15);
    }
  }

  /**
   * Check the combined method getVolAndParameterSensitivitySurface agrees with getVolSurface and getParameterSensitivitySurface
   */
  @Test
  public void valueAndSenseTest() {
    final VolatilitySurface vs = SURF_PRO.getVolSurface(new DoubleMatrix1D(KNOT_VAULES));
    final Surface<Double, Double, DoubleMatrix1D> senseSurf = SURF_PRO.getParameterSensitivitySurface(new DoubleMatrix1D(KNOT_VAULES));
    final Surface<Double, Double, Pair<Double, DoubleMatrix1D>> valAndSenseSurf = SURF_PRO.getVolAndParameterSensitivitySurface(new DoubleMatrix1D(KNOT_VAULES));
    for (int i = 0; i < 20; i++) {
      final double t = 12.0 * i / (19.0);
      final double k = RANDOM.nextDouble();
      final double vol = vs.getVolatility(t, k);
      final DoubleMatrix1D sense = senseSurf.getZValue(t, k);
      final Pair<Double, DoubleMatrix1D> vAndSense = valAndSenseSurf.getZValue(t, k);
      assertEquals(vol, vAndSense.getFirst());
      AssertMatrix.assertEqualsVectors(sense, vAndSense.getSecond(), 1e-15);
    }
  }

  /**
   * Check the sensitivity to the model parameters at a set of sample points - this uses {@link DiscreteVolatilityFunctionProviderFromVolSurface}
   * to handle the finite-difference calculation of the Jacobian 
   */
  @Test
  public void sensitivityTest() {
    final DiscreteVolatilityFunctionProvider discPro = new DiscreteVolatilityFunctionProviderFromVolSurface(SURF_PRO);
    final int nSamples = 30;
    final List<DoublesPair> points = new ArrayList<>(nSamples);
    for (int i = 0; i < nSamples; i++) {
      final double t = 15.0 * i / (nSamples - 1.0);
      final double k = RANDOM.nextDouble();
      points.add(DoublesPair.of(t, k));
    }
    final DiscreteVolatilityFunction func = discPro.from(points);
    final DoubleMatrix1D x = new DoubleMatrix1D(KNOT_VAULES);
    final DoubleMatrix2D jac = func.calculateJacobian(x);
    final DoubleMatrix2D fdJac = func.calculateJacobianViaFD(x);
    AssertMatrix.assertEqualsMatrix(fdJac, jac, 2e-4);
  }

}
