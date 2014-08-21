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

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class BasisSplineVolatilityTermStructureProviderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test
  public void test() {
    //this is just linear interpolation 
    final BasisSplineVolatilityTermStructureProvider pro = new BasisSplineVolatilityTermStructureProvider(0.0, 10.0, 11, 1);
    assertEquals(11, pro.getNumModelParameters());

    final DoubleMatrix1D w = new DoubleMatrix1D(0.3, 0.35, 0.4, 0.2, 0.6, 0.6, 0.7, 0.14, 0.26, 0.4, 0.41);
    final VolatilitySurface volSurf = pro.getVolSurface(w);

    //check values at knots 
    final int n = w.getNumberOfElements();
    for (int i = 0; i < n; i++) {
      double vol = volSurf.getVolatility(i, 0.05); //Arbitrary strike 
      assertEquals(w.getEntry(i), vol, 1e-15);
    }
    //between knots
    assertEquals(0.375, volSurf.getVolatility(1.5, 0.07), 1e-15);
    assertEquals(0.65, volSurf.getVolatility(5.5, 0.01), 1e-15);

    //check sensitivity 
    final Surface<Double, Double, DoubleMatrix1D> senseSurf = pro.getParameterSensitivitySurface(w);
    for (int i = 0; i < n; i++) {
      final DoubleMatrix1D sense = senseSurf.getZValue((double) i, 0.05); //Arbitrary strike 
      final DoubleMatrix1D expected = new DoubleMatrix1D(n);
      expected.getData()[i] = 1.0;
      AssertMatrix.assertEqualsVectors(expected, sense, 1e-14);
    }

    //check value and sense 
    final Surface<Double, Double, Pair<Double, DoubleMatrix1D>> valueAndSenseSurf = pro.getVolAndParameterSensitivitySurface(w);
    final int nSamples = 20;
    for (int i = 0; i < nSamples; i++) {
      final double t = 10.0 * RANDOM.nextDouble();
      final double k = 0.3 * RANDOM.nextDouble();
      final DoublesPair tk = DoublesPair.of(t, k);
      final Double v = volSurf.getVolatility(tk);
      final DoubleMatrix1D sense = senseSurf.getZValue(tk);
      final Pair<Double, DoubleMatrix1D> vAndSense = valueAndSenseSurf.getZValue(tk);
      assertEquals(v, vAndSense.getFirst(), 1e-15);
      AssertMatrix.assertEqualsVectors(sense, vAndSense.getSecond(), 1e-15);
    }

  }

  /**
   * represent a vol surface as w0 + w1*exp(-0.2*t)
   */
  @Test
  public void altConstructorTest() {
    final Function1D<Double, Double> f1 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return 1.0;
      }
    };

    final Function1D<Double, Double> f2 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return Math.exp(-0.2 * x);
      }
    };

    final List<Function1D<Double, Double>> functions = new ArrayList<>();
    functions.add(f1);
    functions.add(f2);

    final BasisSplineVolatilityTermStructureProvider pro = new BasisSplineVolatilityTermStructureProvider(functions);
    final DoubleMatrix1D w = new DoubleMatrix1D(0.2, 0.7);
    final VolatilitySurface volSurface = pro.getVolSurface(w);
    final double t = 1.3;
    assertEquals(w.getEntry(0) * f1.evaluate(t) + w.getEntry(1) * f2.evaluate(t), volSurface.getVolatility(t, 0.05));
  }
}
