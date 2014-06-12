/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MonotonicityPreservingCubicSplineInterpolatorTest {

  private static final double EPS = 1e-13;
  private static final double INF = 1. / 0.;

  /**
   * 
   */
  @Test
  public void localMonotonicityIncTest() {
    final double[] xValues = new double[] {2., 3., 5., 8., 9., 13. };
    final double[] yValues = new double[] {1., 1.01, 2., 2.1, 2.2, 2.201 };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    //    System.out.println(resultPos.getCoefMatrix());

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), result.getOrder());

    final int nKeys = 111;
    double key0 = 2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 2. + 11. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] >= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = 2. + 11. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  @Test
  public void localMonotonicityClampedTest() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10. };
    final double[] yValues = new double[] {0., 10., 9.5, 2., 1.1, -2.2, -2.6, 0. };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    //    System.out.println(resultPos.getCoefMatrix());

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), result.getOrder());

    final int nKeys = 121;
    double key0 = -2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = -2. + 12. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = -2. + 11. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  @Test
  public void localMonotonicityClampedMultiTest() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10. };
    final double[][] yValues = new double[][] { {0., 10., 9.5, 2., 1.1, -2.2, -2.6, 0. }, {10., 10., 9.5, 2., 1.1, -2.2, -2.6, 10. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    //    System.out.println(resultPos.getCoefMatrix());

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), result.getOrder());

    final int nKeys = 121;
    double key0 = -2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = -2. + 12. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = -2. + 11. / (nKeys - 1) * i;
    }
    key0 = -2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = -2. + 12. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = -2. + 11. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  @Test
  public void localMonotonicityDecTest() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10. };
    final double[] yValues = new double[] {10., 9.5, 2., 1.1, -2.2, -2.6 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    //    System.out.println(resultPos.getCoefMatrix());

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), result.getOrder());

    final int nKeys = 121;
    double key0 = -2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = -2. + 12. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = -2. + 11. / (nKeys - 1) * i;
    }
  }

  /**
   * local extrema are not necessarily at data-points
   */
  @Test
  public void extremumTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8 };
    final double[][] yValues = new double[][] { {1., 1., 2., 4., 4., 2., 1., 1. }, {10., 10., 6., 4., 4., 6., 10., 10. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    //    System.out.println(resultPos.getCoefMatrix());

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), result.getOrder());

    assertTrue(function.evaluate(resultPos, 4.5).getData()[0] - function.evaluate(resultPos, 4).getData()[0] >= 0.);
    assertTrue(function.evaluate(resultPos, 4.5).getData()[0] - function.evaluate(resultPos, 5).getData()[0] >= 0.);
    assertTrue(function.evaluate(resultPos, 4.5).getData()[1] - function.evaluate(resultPos, 4).getData()[1] <= 0.);
    assertTrue(function.evaluate(resultPos, 4.5).getData()[1] - function.evaluate(resultPos, 5).getData()[1] <= 0.);

    final int nKeys = 41;
    double key0 = 1.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 1. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] >= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = 1. + 3. / (nKeys - 1) * i;
    }
    key0 = 1.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 1. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[1] + "\t" + function.evaluate(resultPos, key).getData()[1]);
      key0 = 1. + 3. / (nKeys - 1) * i;
    }
    key0 = 5.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 5. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = 5. + 3. / (nKeys - 1) * i;
    }
    key0 = 5.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 5. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] >= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[1] + "\t" + function.evaluate(resultPos, key).getData()[1]);
      key0 = 5. + 3. / (nKeys - 1) * i;
    }
  }

  /**
   * PiecewiseCubicHermiteSplineInterpolator is not modified except the first 2 and last 2 intervals
   */
  @Test
  public void localMonotonicityDec2Test() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10., 12., 14. };
    final double[] yValues = new double[] {11., 9.5, 2., 1.1, -2.2, -2.6, 2., 2. };

    PiecewisePolynomialInterpolator interp = new PiecewiseCubicHermiteSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    //    System.out.println(result.getCoefMatrix());
    //    System.out.println(resultPos.getCoefMatrix());

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), result.getOrder());

    for (int i = 2; i < resultPos.getNumberOfIntervals() - 2; ++i) {
      for (int j = 0; j < 4; ++j) {
        assertEquals(resultPos.getCoefMatrix().getData()[i][j], result.getCoefMatrix().getData()[i][j], EPS);
      }
    }

    final int nKeys = 121;
    double key0 = -2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = -2. + 12. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);
      //      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
      key0 = -2. + 11. / (nKeys - 1) * i;
    }
  }

  /*
   * Error tests
   */
  /**
   * Primary interpolation method should be cubic. 
   * Note that CubicSplineInterpolator returns a linear or quadratic function in certain situations 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lowDegreeTest() {
    final double[] xValues = new double[] {1., 2., 3. };
    final double[] yValues = new double[] {0., 0.1, 0.05 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lowDegreeMultiTest() {
    final double[] xValues = new double[] {1., 2., 3. };
    final double[][] yValues = new double[][] { {0., 0.1, 0.05 }, {0., 0.1, 1.05 } };

    PiecewisePolynomialInterpolator interp = new LinearInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dataShortTest() {
    final double[] xValues = new double[] {1., 2. };
    final double[] yValues = new double[] {0., 0.1 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dataShortMultiTest() {
    final double[] xValues = new double[] {1., 2., };
    final double[][] yValues = new double[][] { {0., 0.1 }, {0., 0.1 } };

    PiecewisePolynomialInterpolator interp = new PiecewiseCubicHermiteSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideDataTest() {
    final double[] xValues = new double[] {1., 1., 3. };
    final double[] yValues = new double[] {0., 0.1, 0.05 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideDataMultiTest() {
    final double[] xValues = new double[] {1., 2., 2. };
    final double[][] yValues = new double[][] { {2., 0., 0.1, 0.05, 2. }, {1., 0., 0.1, 1.05, 2. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void diffDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {0., 0.1, 0.05 };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void diffDataMultiTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[][] yValues = new double[][] { {2., 0., 0.1, 0.05, 2. }, {1., 0., 0.1, 1.05, 2. } };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullXdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };
    xValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };
    yValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullXdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };
    xValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };
    yValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infXdataTest() {
    double[] xValues = new double[] {1., 2., 3., INF };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infYdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0., 0.1, 0.05, 0.2, INF };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanXdataTest() {
    double[] xValues = new double[] {1., 2., 3., Double.NaN };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanYdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0., 0.1, 0.05, 0.2, Double.NaN };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infXdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., INF };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infYdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0., 0.1, 0.05, 0.2, 1. }, {0., 0., 0.1, 0.05, 0.2, INF } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanXdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., Double.NaN };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanYdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0., 0.1, 0.05, 0.2, 1.1 }, {0., 0., 0.1, 0.05, 0.2, Double.NaN } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /*
   * Tests below are for debugging
   */
  /**
   * 
   */
  @Test
      (enabled = false)
      public void print1Test() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6. };
    final double[][] yValues = new double[][] {{0.1, 1., 3., 8., 16., 18. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    System.out.println(resultPos.getCoefMatrix());

    final int nKeys = 101;
    for (int i = 0; i < nKeys; ++i) {
      final double key = 1. + 5. / (nKeys - 1) * i;
      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
    }
  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void print2Test() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6. };
    final double[][] yValues = new double[][] {{0.1, 1., 1., 20., 20., 16. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    System.out.println(resultPos.getCoefMatrix());

    final int nKeys = 101;
    for (int i = 0; i < nKeys; ++i) {
      final double key = 1. + 5. / (nKeys - 1) * i;
      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
    }
  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void print3Test() {
    final double[] xValues = new double[] {0.1, 1., 4., 9., 20., 30. };
    final double[][] yValues = new double[][] {{8.1, 7., 4.4, 7., 4., 3. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    System.out.println(resultPos.getCoefMatrix());

    final int nKeys = 101;
    for (int i = 0; i < nKeys; ++i) {
      final double key = +30. / (nKeys - 1) * i;
      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
    }
  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void print4Test() {
    final double[] xValues = new double[] {2., 3., 5., 8., 8.1, 13. };
    final double[][] yValues = new double[][] {{35., 22., 20., 25., 30., 25. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingCubicSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    System.out.println(resultPos.getCoefMatrix());

    final int nKeys = 101;
    for (int i = 0; i < nKeys; ++i) {
      final double key = 1.5 + 12. / (nKeys - 1) * i;
      System.out.println(key + "\t" + function.evaluate(result, key).getData()[0] + "\t" + function.evaluate(resultPos, key).getData()[0]);
    }
  }
}
