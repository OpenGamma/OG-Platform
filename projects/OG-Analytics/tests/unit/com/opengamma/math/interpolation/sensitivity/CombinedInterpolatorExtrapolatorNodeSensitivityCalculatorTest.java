/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.junit.Assert.assertArrayEquals;
import org.testng.annotations.Test;
import org.testng.Assert;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorTest {
  private static final LinearInterpolator1DNodeSensitivityCalculator LINEAR = new LinearInterpolator1DNodeSensitivityCalculator();
  private static final FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> LEFT = new FlatExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>();
  private static final LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> RIGHT = new LinearExtrapolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>(LINEAR);
  private static final double[] X;
  private static final double[] Y;
  private static final Interpolator1DDataBundle DATA;
  private static final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle> COMBINED1 = new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(
      LINEAR);
  private static final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle> COMBINED2 = new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(
      LINEAR, LEFT);
  private static final CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle> COMBINED3 = new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(
      LINEAR, LEFT, RIGHT);
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 3 * x + 11;
    }

  };

  static {
    final int n = 10;
    X = new double[n];
    Y = new double[n];
    for (int i = 0; i < n; i++) {
      X[i] = i;
      Y[i] = F.evaluate(X[i]);
    }
    DATA = new ArrayInterpolator1DDataBundle(X, Y);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator1() {
    new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator2() {
    new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(null, LEFT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator3() {
    new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(null, LEFT, RIGHT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExtrapolator() {
    new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(LINEAR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLeftExtrapolator() {
    new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(LINEAR, null, RIGHT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRightExtrapolator() {
    new CombinedInterpolatorExtrapolatorNodeSensitivityCalculator<Interpolator1DDataBundle>(LINEAR, LEFT, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    COMBINED1.calculate(null, 2.3);
  }

  @Test
  public void testInterpolatorOnly() {
    final double x = 6.7;
    assertArrayEquals(COMBINED1.calculate(DATA, x), LINEAR.calculate(DATA, x), 1e-15);
    try {
      COMBINED1.calculate(DATA, x - 100);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
    }
    try {
      COMBINED1.calculate(DATA, x + 100);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testOneExtrapolator() {
    final double x = 3.6;
    assertArrayEquals(COMBINED2.calculate(DATA, x), LINEAR.calculate(DATA, x), 1e-15);
    assertArrayEquals(COMBINED2.calculate(DATA, x - 100), LEFT.calculate(DATA, x - 100), 1e-15);
    assertArrayEquals(COMBINED2.calculate(DATA, x + 100), LEFT.calculate(DATA, x + 100), 1e-15);
  }

  @Test
  public void testTwoExtrapolators() {
    final double x = 3.6;
    assertArrayEquals(COMBINED3.calculate(DATA, x), LINEAR.calculate(DATA, x), 1e-15);
    assertArrayEquals(COMBINED3.calculate(DATA, x - 100), LEFT.calculate(DATA, x - 100), 1e-15);
    assertArrayEquals(COMBINED3.calculate(DATA, x + 100), RIGHT.calculate(DATA, x + 100), 1e-5);
  }
}
