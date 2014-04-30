/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class Extrapolator1DNodeSensitivityCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final FlatExtrapolator1D FLAT_INTERPOLATOR = new FlatExtrapolator1D();
  private static final LinearExtrapolator1D LINEAR_INTERPOLATOR = new LinearExtrapolator1D(new LinearInterpolator1D(), 1e-6);
  private static final LinearExtrapolator1D CUBIC_INTERPOLATOR = new LinearExtrapolator1D(new NaturalCubicSplineInterpolator1D(), 1e-6);
  private static final Interpolator1DCubicSplineDataBundle DATA;
  private static final double EPS = 1e-4;

  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {
    private static final double A = -0.045;
    private static final double B = 0.03;
    private static final double C = 0.3;
    private static final double D = 0.05;

    @Override
    public Double evaluate(final Double x) {
      return (A + B * x) * Math.exp(-C * x) + D;
    }
  };

  static {
    final double[] t = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0, 15.0, 17.5, 20.0, 25.0, 30.0};
    final int n = t.length;
    final double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = FUNCTION.evaluate(t[i]);
    }
    DATA = new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(t, r));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    new LinearExtrapolator1D(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    LINEAR_INTERPOLATOR.getNodeSensitivitiesForValue(null, 102.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    FLAT_INTERPOLATOR.getNodeSensitivitiesForValue(null, 105.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithinRange1() {
    LINEAR_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, 20.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithinRange2() {
    FLAT_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, 20.);
  }

  @Test
  public void testSensitivities() {
    double[] sensitivityFD, sensitivity;
    double tUp, tDown;
    for (int i = 0; i < 100; i++) {
      tUp = RANDOM.nextDouble() * 10 + 30;
      tDown = -RANDOM.nextDouble() * 10;
      sensitivityFD = LINEAR_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tUp, true);
      sensitivity = LINEAR_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tUp);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivityFD[j], sensitivity[j], EPS);
      }
      sensitivityFD = LINEAR_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tDown, true);
      sensitivity = LINEAR_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tDown);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivityFD[j], sensitivity[j], EPS);
      }
    }
    for (int i = 0; i < 100; i++) {
      tUp = RANDOM.nextDouble() * 10 + 30;
      tDown = -RANDOM.nextDouble() * 10;
      sensitivityFD = CUBIC_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tUp, true);
      sensitivity = CUBIC_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tUp);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivityFD[j], sensitivity[j], EPS);
      }
      sensitivityFD = CUBIC_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tDown, true);
      sensitivity = CUBIC_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, tDown);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivityFD[j], sensitivity[j], EPS);
      }
    }
  }
}
