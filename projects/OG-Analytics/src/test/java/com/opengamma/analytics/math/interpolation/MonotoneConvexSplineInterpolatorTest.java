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
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MonotoneConvexSplineInterpolatorTest {

  private static final double EPS = 1e-13;
  private static final double INF = 1. / 0.;

  /**
   * Check introduction of new knots, modification of forward rates, and try all types of polynomials which interpolate adjacent data points 
   * 
   */
  @Test
  public void knotsControlTest1() {

    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13., 14., 15., 16., 17., 18., 19., 20., 21., 22., 23., 24., 25., 26., 27. };
    final double[] yValues = new double[] {3., 2., 2., 2., 3., 2.5, 2., 2., 3., 3., 2.5, 2., 2., 4., 5., 5., 4.9, 5., 6., 8., 3., -2., -1.5, -1., -2., -1.5, -1., };
    final int nData = xValues.length;
    double[] yValuesInput = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesInput[i] = yValues[i] * xValues[i];
    }

    final double[][] xValuesMatrix = new double[][] { {1., 2. }, {3., 4. } };
    final double[] xValuesMod = new double[] {1., 2., 3., 4. + 1.e-14, 5., 6. + 1.e-14, 7., 8., 9., 10., 11., 12., 13., 14., 15., 16., 17., 18., 19., 20., 21., 22., 23., 24., 25., 26., 27. };
    final double[][] xValuesModMatrix = new double[][] { {1., 2. }, {3., 4. + 1.e-14 } };
    final double[][] yValuesMatrix = new double[][] { {3., 2. }, {2., 2. } };

    final double[] knotsExp = new double[] {1., 2., 3., 4., 4.7, 5., 6., 6.5, 7., 8., 101. / 12., 9., 101. / 11., 10., 149. / 14., 11., 11.5, 12., 138. / 11., 13., 830. / 63., 14., 15., 602. / 39.,
        16., 16. + 2. / 3., 17., 487. / 28., 18., 19., 19. + 240. / 251., 20., 20. + 117. / (97. + 117.), 21., 21.5, 22., 22. + 1.5 / 224., 23., 23. + 62.5 / 63., 24., 24.5, 25., 25. + 1.5 / 63.5,
        26., 27. };

    final double[] modifiedFwds = new double[] {0.75, 1.5, 2., 4., 0., -2, -2, 4., 6., -5., -7., -7., 4., 24.5, 10., 4.15, 5., 13.4, 35., -194., -214., -214., 10., -52., -52., 11.5, 12.25, };
    final double[][] modifiedFwdsMatrix = new double[][] { {0.75, 1.5 }, {2., 4. } };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    PiecewisePolynomialResult resultInt = interpolator.interpolate(xValues, yValuesInput);
    DoubleMatrix1D values = interpolator.interpolate(xValues, yValuesInput, xValues);
    DoubleMatrix2D valuesMatrix = interpolator.interpolate(xValues, yValuesInput, xValuesMatrix);

    assertEquals(resultInt.getDimensions(), 1);
    assertEquals(resultInt.getNumberOfIntervals(), knotsExp.length - 1);
    assertEquals(resultInt.getOrder(), 4);

    for (int i = 0; i < knotsExp.length; ++i) {
      final double ref = knotsExp[i] == 0. ? 1. : Math.abs(knotsExp[i]);
      assertEquals(resultInt.getKnots().getData()[i], knotsExp[i], ref * EPS);
    }
    for (int i = 1; i < nData - 1; ++i) {
      final double ref = yValuesInput[i] == 0. ? 1. : Math.abs(yValuesInput[i]);
      assertEquals(values.getData()[i], yValuesInput[i], ref * EPS);
    }
    {
      final double ref = yValuesInput[1] == 0. ? 1. : Math.abs(yValuesInput[1]);
      assertEquals(interpolator.interpolate(xValues, yValuesInput, xValues[1]), yValuesInput[1], ref * EPS * 10);
    }
    for (int i = 0; i < 2; ++i) {
      for (int j = 0; j < 2; ++j) {
        final double ref = yValuesMatrix[i][j] * xValuesMatrix[i][j] == 0. ? 1. : Math.abs(yValuesMatrix[i][j] * xValuesMatrix[i][j]);
        assertEquals(valuesMatrix.getData()[i][j], yValuesMatrix[i][j] * xValuesMatrix[i][j], ref * EPS * 10);
      }
    }

    PiecewisePolynomialResult resultExt = interpolator.interpolateFwds(xValues, yValuesInput);
    DoubleMatrix1D valuesFwds = interpolator.interpolateFwds(xValues, yValuesInput, xValuesMod);
    DoubleMatrix2D valuesFwdsMatrix = interpolator.interpolateFwds(xValues, yValuesInput, xValuesModMatrix);

    assertEquals(resultExt.getDimensions(), 1);
    assertEquals(resultExt.getNumberOfIntervals(), knotsExp.length - 1);
    assertEquals(resultExt.getOrder(), 3);

    for (int i = 1; i < nData - 1; ++i) {
      final double ref = modifiedFwds[i] == 0. ? 1. : Math.abs(modifiedFwds[i]);
      assertEquals(valuesFwds.getData()[i], modifiedFwds[i], ref * EPS * 10);
    }
    for (int i = 0; i < knotsExp.length; ++i) {
      final double ref = knotsExp[i] == 0. ? 1. : Math.abs(knotsExp[i]);
      assertEquals(resultExt.getKnots().getData()[i], knotsExp[i], ref * EPS);
    }
    {
      final double ref = modifiedFwds[1] == 0. ? 1. : Math.abs(modifiedFwds[1]);
      assertEquals(interpolator.interpolateFwds(xValues, yValuesInput, xValuesMod[1]), modifiedFwds[1], ref * EPS * 10);
    }
    for (int i = 0; i < 2; ++i) {
      for (int j = 0; j < 2; ++j) {
        final double ref = modifiedFwdsMatrix[i][j] == 0. ? 1. : Math.abs(modifiedFwdsMatrix[i][j]);
        assertEquals(valuesFwdsMatrix.getData()[i][j], modifiedFwdsMatrix[i][j], ref * EPS * 10);
      }
    }
  }

  /**
   * 
   */
  @Test
  public void linearTest() {

    final double[] xValues = new double[] {2., 3., 4., 5., 6. };
    final double[] yValues = new double[] {2., 3., 4., 5., 6. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();

    final int nPts = 301;
    for (int i = 0; i < nPts; ++i) {
      final double key = 1. + 6. / (nPts - 1) * i;
      //      System.out.println(key + "\t" + interpolator.interpolate(xValues, yValues, key));
      final double ref = key == 0. ? 1. : Math.abs(key);
      assertEquals(interpolator.interpolate(xValues, yValues, key), key, ref * EPS);
    }

  }

  /**
   * yValues are constant
   */
  @Test
  public void constTest() {

    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6. };
    final double[] yValuesTmp = new double[] {1., 1., 1., 1., 1., 1. };
    final int nData = xValues.length;
    final double[] yValues = new double[nData];
    for (int i = 0; i < xValues.length; ++i) {
      yValues[i] = yValuesTmp[i] * xValues[i];
    }

    final double[][] coefMatInt = new double[][] { {0., 0., 1., 1. }, {0., 0., 1., 2. }, {0., 0., 1., 3. }, {0., 0., 1., 4. }, {0., 0., 1., 5. }, {0., 0., 1., 6. } };
    final double[][] coefMatIntFwds = new double[][] { {0., 0., 1. }, {0., 0., 1. }, {0., 0., 1. }, {0., 0., 1. }, {0., 0., 1. } };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    PiecewisePolynomialResult result = interpolator.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getNumberOfIntervals(), xValues.length - 1);
    assertEquals(result.getOrder(), 4);

    for (int i = 0; i < coefMatInt.length; ++i) {
      for (int j = 0; j < coefMatInt[0].length; ++j) {
        final double ref = coefMatInt[i][j] == 0. ? 1. : Math.abs(coefMatInt[i][j]);
        assertEquals(result.getCoefMatrix().getData()[i][j], coefMatInt[i][j], ref * EPS);
      }
    }
    for (int i = 0; i < xValues.length; ++i) {
      final double ref = xValues[i] == 0. ? 1. : Math.abs(xValues[i]);
      assertEquals(result.getKnots().getData()[i], xValues[i], ref * EPS);
    }

    result = interpolator.interpolateFwds(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getNumberOfIntervals(), xValues.length - 1);
    assertEquals(result.getOrder(), 3);

    for (int i = 0; i < coefMatIntFwds.length; ++i) {
      for (int j = 0; j < coefMatIntFwds[0].length; ++j) {
        final double ref = coefMatIntFwds[i][j] == 0. ? 1. : Math.abs(coefMatIntFwds[i][j]);
        assertEquals(result.getCoefMatrix().getData()[i][j], coefMatIntFwds[i][j], ref * EPS);
      }
    }
    for (int i = 0; i < xValues.length; ++i) {
      final double ref = xValues[i] == 0. ? 1. : Math.abs(xValues[i]);
      assertEquals(result.getKnots().getData()[i], xValues[i], ref * EPS);
    }

  }

  /**
   * Forwards should be positive curve IF discrete forwards are positive
   * Consequently, spots are also positive
   */
  @Test
  public void positiveTest() {
    final boolean print = false;
    //   System.out.println("MonotoneConvexSplineInterpolatorTest");

    final double[] xValues = new double[] {0., 0.1, 1., 2., 6., 9., 30 };
    final double[] yValues = new double[] {0., 2., 2., 2., 3., 2., 1. };
    final int nData = xValues.length;
    double[] yValuesInput = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesInput[i] = xValues[i] * yValues[i];
    }

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();

    final int nPts = 300;
    for (int i = 0; i < nPts; ++i) {
      final double key = 30. / nPts + 30. / nPts * i;
      if (print) {
        System.out.println(key + "\t" + interpolator.interpolateFwds(xValues, yValuesInput, key));
      }
      assertTrue(interpolator.interpolateFwds(xValues, yValuesInput, key) >= 0.);
    }

    if (print) {
      System.out.println("\n");
    }

    for (int i = 0; i < nPts + 100; ++i) {
      final double key = 30. / nPts + 30. / nPts * i;
      if (print) {
        System.out.println(key + "\t" + interpolator.interpolate(xValues, yValuesInput, key));
      }
      assertTrue(interpolator.interpolate(xValues, yValuesInput, key) >= 0.);
    }
  }

  /**
   * 
   */
  @Test
  public void monotonicTest() {
    final boolean print = false;
    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    //  System.out.println("MonotoneConvexSplineInterpolatorTest");

    final double[] xValues = new double[] {0., 0.3, 0.6, 1.5, 2.7, 3.4, 4.8, 5.9 };
    final int nData = xValues.length;
    final double[] yValuesTmp = new double[] {1.0, 1.2, 1.5, 2.0, 2.1, 3.0, 3.1, 3.3 };
    double[] yValues = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValues[i] = yValuesTmp[i] * xValues[i];
    }

    final int nPts = 300;
    double old = yValues[0] * xValues[0];
    for (int i = 0; i < nPts; ++i) {
      final double key = .0 + i * 5.9 / (nPts - 1);
      final double value = interpolator.interpolate(xValues, yValues, key);
      if (print) {
        System.out.println(key + "\t" + value);
      }
      assertTrue(value >= old);
      old = value;
    }
  }

  /**
   * f(t) may have discontinuity
   */
  @Test(enabled = false)
  public void discontTest() {

    final double[] xValues = new double[] {0., 0.1, 4., 10., 20., 30. };
    final double[] yValues = new double[] {0., 5., 5., 5., 5., 4.5 };
    final double[] fwdsExp = new double[] {0., 5., 5., 5., 4.25, 3.125 };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();

    final int nPts = xValues.length;
    for (int i = 1; i < nPts; ++i) {
      final double key = xValues[i];
      // System.out.println(key + "\t" + interpolator.interpolateFwds(xValues, yValues, key));
      final double ref = yValues[i] == 0. ? 1. : Math.abs(yValues[i]);
      assertEquals(interpolator.interpolateFwds(xValues, yValues, key), yValues[i], ref * EPS);
    }

    System.out.println("\n");

    for (int i = 1; i < nPts; ++i) {
      final double key = xValues[i];
      // System.out.println(key + "\t" + interpolator.interpolate(xValues, yValues, key));
      final double ref = fwdsExp[i] == 0. ? 1. : Math.abs(fwdsExp[i]);
      assertEquals(interpolator.interpolateFwds(xValues, yValues, key), fwdsExp[i], ref * EPS);
    }

  }

  /**
   * Tests below are for "interpolate"
   */
  /**
   * Multidimensional yValues are not supported
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nonSupTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[][] yValues = new double[][] { {1., 3., 2., 1. }, {1., 3., 2., 1. } };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullyTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[4];
    yValues = null;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullxTest() {
    double[] yValues = new double[] {1., 2., 3., 4. };
    double[] xValues = new double[4];
    xValues = null;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullkeyVecTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double[] key = new double[] {2., 2.5 };
    key = null;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullkeyMatTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    double[][] key = new double[][] { {2., 2.5 }, {2., 2.5 } };
    key = null;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infxTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    xValues[3] = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infyTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    yValues[3] = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanxTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    xValues[3] = Double.NaN;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanyTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    yValues[3] = Double.NaN;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infKeyTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double key = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infKeyVecTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double[] key = new double[] {2., 2.5 };
    key[1] = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infKeyMatTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    double[][] key = new double[][] { {2., 2.5 }, {2., 2.5 } };
    key[1][1] = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanKeyVecTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double[] key = new double[] {2., 2.5 };
    key[1] = Double.NaN;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanKeyMatTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    double[][] key = new double[][] { {2., 2.5 }, {2., 2.5 } };
    key[1][1] = Double.NaN;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeKeyTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double key = 1.e308;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeKeyVecTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double[] key = new double[] {2., 2.5 };
    key[1] = 1.e308;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeKeyMatTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    double[][] key = new double[][] { {2., 2.5 }, {2., 2.5 } };
    key[1][1] = 1.e308;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shortDataLengthTest() {
    final double[] xValues = new double[] {1. };
    final double[] yValues = new double[] {1. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDataLengthTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1., 2. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void repeatedDataTest() {
    final double[] xValues = new double[] {1., 2., 1., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * Tests below are for interpolateFwds
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infxFwdsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    xValues[3] = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infyFwdsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    yValues[3] = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanxFwdsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    xValues[3] = Double.NaN;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanyFwdsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };
    yValues[3] = Double.NaN;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infKeyFwdsTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double key = INF;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void largeKeyFwdsTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {1., 3., 2., 1. };
    double key = 1.e308;

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues, key);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shortDataLengthFwdsTest() {
    final double[] xValues = new double[] {1. };
    final double[] yValues = new double[] {1. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDataLengthFwdsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1., 2. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void repeatedDataFwdsTest() {
    final double[] xValues = new double[] {1., 2., 1., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroDataSpotsTest() {
    final double[] xValues = new double[] {0., 2., 1., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroDataFwdsTest() {
    final double[] xValues = new double[] {0., 2., 1., 4. };
    final double[] yValues = new double[] {1., 3., 2., 1. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    interpolator.interpolateFwds(xValues, yValues);
  }

  /**
   * Tests below for debugging
   */
  @Test(enabled = false)
  public void printTest2() {
    // final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13., 14., 15., 16., 17., 18., 19., 20., 21., 22., 23., 24., 25., 26., 27. };
    // final double[] yValues = new double[] {3., 2., 2., 2., 3., 2.5, 2., 2., 3., 3., 2.5, 2., 2., 4., 5., 5., 4.9, 5., 6., 8., 3., -2., -1.5, -1., -2., -1.5, -1., };
    // final double[] xValues = new double[] {0., 0.1, 1., 4., 9., 20., 30 };
    // final double[] yValues = new double[] {0., 8.1, 7., 4.4, 7., 4., 3. };

    final double[] xValues = new double[] {0., 1., 2., 3., 4. };
    final double[] yValues = new double[] {0., 1., 4., 9., 16. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    PiecewisePolynomialResult result = interpolator.interpolate(xValues, yValues);

    System.out.println(result.getCoefMatrix());
    System.out.println(result.getKnots());

    PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();

    final int nPts = 101;
    for (int i = 0; i < nPts; ++i) {
      final double key = 0. + 5. / (nPts - 1) * i;
      System.out.println(key + "\t" + interpolator.interpolate(xValues, yValues, key) + "\t" + func.integrate(result, 0., key));
    }

    System.out.println("\n");

    for (int i = 0; i < nPts; ++i) {
      final double key = 0. + 5. / (nPts - 1) * i;
      System.out.println(key + "\t" + func.evaluate(result, key).getData()[0]);
    }

  }

  /**
   * 
   */
  @Test(enabled = false)
  public void printTest3() {

    final double[] xValues = new double[] {2., 3., 7. / 2., 4., 5. };
    final double[] yValues = new double[] {0., 2., 9. / 4., 2., 0. };

    MonotoneConvexSplineInterpolator interpolator = new MonotoneConvexSplineInterpolator();
    PiecewisePolynomialResult result = interpolator.interpolate(xValues, yValues);

    System.out.println(result.getCoefMatrix());
    System.out.println(result.getKnots());

    result = interpolator.interpolateFwds(xValues, yValues);

    System.out.println(result.getCoefMatrix());
    System.out.println(result.getKnots());

    final int nPts = 100;
    for (int i = 0; i < nPts; ++i) {
      final double key = 1. + 6. / nPts * i;
      System.out.println(key + "\t" + interpolator.interpolate(xValues, yValues, key));
    }

    // for (int i = 0; i < nPts; ++i) {
    // final double key = 1. + 6. / nPts + 6. / nPts * i;
    // System.out.println(key + "\t" + interpolator.interpolateFwds(xValues, yValues, key));
    // }

  }

}
