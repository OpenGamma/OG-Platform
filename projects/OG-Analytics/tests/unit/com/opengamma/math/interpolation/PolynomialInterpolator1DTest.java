/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import java.util.TreeMap;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class PolynomialInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR_NO_OFFSET = new PolynomialInterpolator1D(3);
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR_WITH_OFFSET = new PolynomialInterpolator1D(3, 2);
  private static final Interpolator1DDataBundle MODEL = INTERPOLATOR_NO_OFFSET.getDataBundle(new double[] {1, 2, 3, 4, 5}, new double[] {6, 7, 8, 9, 10});
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIllegalDegree1() {
    new PolynomialInterpolator1D(0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIllegalDegree2() {
    new PolynomialInterpolator1D(0, 3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOffset() {
    new PolynomialInterpolator1D(3, -4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLargeOffset() {
    new PolynomialInterpolator1D(3, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR_WITH_OFFSET.interpolate(null, 3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR_WITH_OFFSET.interpolate(MODEL, null);
  }

  @Test(expectedExceptions = MathException.class)
  public void testInsufficientData() {
    INTERPOLATOR_WITH_OFFSET.interpolate(INTERPOLATOR_WITH_OFFSET.getDataBundle(new double[] {1, 2, 3}, new double[] {4, 5, 6}), 1.5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRange() {
    INTERPOLATOR_NO_OFFSET.interpolate(MODEL, 0.);
  }

  @Test(expectedExceptions = MathException.class)
  public void testOutOfRangeWithOffset() {
    INTERPOLATOR_WITH_OFFSET.interpolate(MODEL, 2.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighOutOfRange() {
    INTERPOLATOR_NO_OFFSET.interpolate(MODEL, 10.);
  }

  @Test
  public void testDataBundleType1() {
    assertEquals(INTERPOLATOR_NO_OFFSET.getDataBundle(new double[] {1, 2, 3}, new double[] {1, 2, 3}).getClass(), ArrayInterpolator1DDataBundle.class);
  }

  @Test
  public void testDataBundleType2() {
    assertEquals(INTERPOLATOR_NO_OFFSET.getDataBundleFromSortedArrays(new double[] {1, 2, 3}, new double[] {1, 2, 3}).getClass(), ArrayInterpolator1DDataBundle.class);
  }

  @Test
  public void testHashCodeAndEquals() {
    assertEquals(INTERPOLATOR_NO_OFFSET, new PolynomialInterpolator1D(3));
    assertEquals(INTERPOLATOR_NO_OFFSET.hashCode(), new PolynomialInterpolator1D(3).hashCode());
    assertFalse(INTERPOLATOR_NO_OFFSET.equals(new PolynomialInterpolator1D(5)));
    assertEquals(INTERPOLATOR_WITH_OFFSET, new PolynomialInterpolator1D(3, 2));
    assertEquals(INTERPOLATOR_WITH_OFFSET.hashCode(), new PolynomialInterpolator1D(3, 2).hashCode());
    assertFalse(INTERPOLATOR_WITH_OFFSET.equals(new PolynomialInterpolator1D(5, 2)));
    assertFalse(INTERPOLATOR_WITH_OFFSET.equals(new PolynomialInterpolator1D(3, 1)));
  }

  @Test
  public void testInterpolation() {
    final Function1D<Double, Double> quadratic = new RealPolynomialFunction1D(new double[] {-4., 3., 1.});
    final Function1D<Double, Double> quartic = new RealPolynomialFunction1D(new double[] {-4., 3., 1., 1., 1.});
    final TreeMap<Double, Double> quadraticMap = new TreeMap<Double, Double>();
    final TreeMap<Double, Double> quarticMap = new TreeMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = i / 10.;
      quadraticMap.put(x, quadratic.evaluate(x));
      quarticMap.put(x, quartic.evaluate(x));
    }
    x = 0.35;
    Interpolator1D<Interpolator1DDataBundle> quadraticInterpolator = new PolynomialInterpolator1D(2);
    Interpolator1D<Interpolator1DDataBundle> quarticInterpolator = new PolynomialInterpolator1D(4);
    final Interpolator1DDataBundle quadraticData = quadraticInterpolator.getDataBundle(quadraticMap);
    final Interpolator1DDataBundle quarticData = quarticInterpolator.getDataBundle(quarticMap);
    Double quadraticResult = quadraticInterpolator.interpolate(quadraticData, x);
    Double quarticResult = quarticInterpolator.interpolate(quarticData, x);
    assertEquals(quadraticResult, quadratic.evaluate(x), EPS);
    assertEquals(quarticResult, quartic.evaluate(x), EPS);
    quadraticInterpolator = new PolynomialInterpolator1D(2, 1);
    quadraticResult = quadraticInterpolator.interpolate(quadraticData, x);
    quarticInterpolator = new PolynomialInterpolator1D(4, 1);
    quarticResult = quarticInterpolator.interpolate(quarticData, x);
    assertEquals(quadraticResult, quadratic.evaluate(x), EPS);
    assertEquals(quarticResult, quartic.evaluate(x), EPS);
  }

}
