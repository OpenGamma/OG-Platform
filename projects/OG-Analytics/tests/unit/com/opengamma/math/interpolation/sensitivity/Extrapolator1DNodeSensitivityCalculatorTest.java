/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.FiniteDifferenceInterpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.FlatExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.LinearExtrapolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.NaturalCubicSplineInterpolator1DNodeSensitivityCalculator;

/**
 * 
 */
public class Extrapolator1DNodeSensitivityCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final NaturalCubicSplineInterpolator1D INTERPOLATOR = new NaturalCubicSplineInterpolator1D();
  private static final NaturalCubicSplineInterpolator1DNodeSensitivityCalculator CALCULATOR = new NaturalCubicSplineInterpolator1DNodeSensitivityCalculator();
  private static final FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> FD_CALCULATOR = new FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(
      INTERPOLATOR);
  private static final FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> FLAT_CALCULATOR = new FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>();
  private static final LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> LINEAR_CALCULATOR = new LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(
      CALCULATOR);
  private static final LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle> LINEAR_FD_CALCULATOR = new LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineDataBundle>(
      FD_CALCULATOR);
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
    //TODO 
    DATA = new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(t, r));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    new LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    LINEAR_CALCULATOR.calculate(null, 102);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    FLAT_CALCULATOR.calculate(null, 105);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithinRange1() {
    LINEAR_CALCULATOR.calculate(DATA, 20.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithinRange2() {
    FLAT_CALCULATOR.calculate(DATA, 20);
  }

  @Test
  public void testSensitivities() {
    double[] sensitivityFD, sensitivity;
    double tUp, tDown;
    for (int i = 0; i < 100; i++) {
      tUp = RANDOM.nextDouble() * 10 + 30;
      tDown = -RANDOM.nextDouble() * 10;
      sensitivityFD = LINEAR_FD_CALCULATOR.calculate(DATA, tUp);
      sensitivity = LINEAR_CALCULATOR.calculate(DATA, tUp);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivityFD[j], sensitivity[j], EPS);
      }
      sensitivityFD = LINEAR_FD_CALCULATOR.calculate(DATA, tDown);
      sensitivity = LINEAR_CALCULATOR.calculate(DATA, tDown);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivityFD[j], sensitivity[j], EPS);
      }
    }
  }

}
