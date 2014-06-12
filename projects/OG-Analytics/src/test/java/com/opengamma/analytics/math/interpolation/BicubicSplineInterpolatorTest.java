/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;
import static org.testng.Assert.assertEquals;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.function.PiecewisePolynomialFunction2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BicubicSplineInterpolatorTest {

  private static final double EPS = 1e-12;
  private static final double INF = 1. / 0.;

  /**
   * 
   */
  @Test
  public void linearTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = (x0Values[i] + 2.) * (x1Values[j] + 5.);
      }
    }
    //    System.out.println(new DoubleMatrix2D(yValues));

    CubicSplineInterpolator method = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator2D interp = new BicubicSplineInterpolator(new CubicSplineInterpolator[] {method, method });
    PiecewisePolynomialResult2D result = interp.interpolate(x0Values, x1Values, yValues);

    final int n0IntExp = n0Data - 1;
    final int n1IntExp = n1Data - 1;
    final int orderExp = 4;

    DoubleMatrix2D[][] coefsExp = new DoubleMatrix2D[n0Data - 1][n1Data - 1];
    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        coefsExp[i][j] = new DoubleMatrix2D(new double[][] { {0., 0., 0., 0., }, {0., 0., 0., 0., }, {0., 0., 1., (5. + x1Values[j]) },
            {0., 0., (2. + x0Values[i]), (2. + x0Values[i]) * (5. + x1Values[j]) } });
      }
    }

    assertEquals(result.getNumberOfIntervals()[0], n0IntExp);
    assertEquals(result.getNumberOfIntervals()[1], n1IntExp);
    assertEquals(result.getOrder()[0], orderExp);
    assertEquals(result.getOrder()[1], orderExp);

    final int n0Keys = 51;
    final int n1Keys = 61;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = 0. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -2. + 6. * i / (n1Keys - 1);
    }

    //    PiecewisePolynomialFunction2D func = new PiecewisePolynomialFunction2D();
    //    final double[][] values = func.evaluate(result, x0Keys, x1Keys).getData();

    for (int i = 0; i < n0Data; ++i) {
      final double ref = Math.abs(x0Values[i]) == 0. ? 1. : Math.abs(x0Values[i]);
      assertEquals(result.getKnots0().getData()[i], x0Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(0).getData()[i], x0Values[i], ref * EPS);
    }
    for (int i = 0; i < n1Data; ++i) {
      final double ref = Math.abs(x1Values[i]) == 0. ? 1. : Math.abs(x1Values[i]);
      assertEquals(result.getKnots1().getData()[i], x1Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(1).getData()[i], x1Values[i], ref * EPS);
    }
    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        for (int k = 0; k < orderExp; ++k) {
          for (int l = 0; l < orderExp; ++l) {
            final double ref = Math.abs(coefsExp[i][j].getData()[k][l]) == 0. ? 1. : Math.abs(coefsExp[i][j].getData()[k][l]);
            assertEquals(result.getCoefs()[i][j].getData()[k][l], coefsExp[i][j].getData()[k][l], ref * EPS);
          }
        }
      }
    }

    double[][] resValues = interp.interpolate(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();

    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        final double expVal = (x0Keys[i] + 2.) * (x1Keys[j] + 5.);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
      }
    }
    //    final PiecewisePolynomialFunction2D func = new PiecewisePolynomialFunction2D();
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        final double expVal = (x0Keys[i] + 2.) * (x1Keys[j] + 5.);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
        //        assertEquals(resValues[i][j], func.evaluate(result, x0Keys[i], x1Keys[j]), ref * EPS);
      }
    }
    {
      final double expVal = (x0Keys[1] + 2.) * (x1Keys[2] + 5.);
      final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[1], x1Keys[2]), expVal, ref * EPS);
    }
    {
      final double expVal = (x0Keys[23] + 2.) * (x1Keys[20] + 5.);
      final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[23], x1Keys[20]), expVal, ref * EPS);
    }

    //    for (int i = 0; i < n0Keys; ++i) {
    //      System.out.print("\t" + x0Keys[i]);
    //    }
    //    System.out.print("\n");
    //    for (int j = 0; j < n1Keys; ++j) {
    //      System.out.print(x1Keys[j]);
    //      for (int i = 0; i < n0Keys; ++i) {
    //        System.out.print("\t" + values[i][j]);
    //      }
    //      System.out.print("\n");
    //    }
    //
    //    System.out.print("\n");
  }

  /**
   * f(x0,x1) = ( x0 - 1.5)^2 * (x1  - 2.)^2
   */
  @Test
  public void quadraticTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = (x0Values[i] - 1.5) * (x0Values[i] - 1.5) * (x1Values[j] - 2.) * (x1Values[j] - 2.);
      }
    }

    CubicSplineInterpolator method = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator2D interp = new BicubicSplineInterpolator(method);
    PiecewisePolynomialResult2D result = interp.interpolate(x0Values, x1Values, yValues);

    final int n0IntExp = n0Data - 1;
    final int n1IntExp = n1Data - 1;
    final int orderExp = 4;

    final int n0Keys = 51;
    final int n1Keys = 61;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = 0. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -2. + 6. * i / (n1Keys - 1);
    }

    assertEquals(result.getNumberOfIntervals()[0], n0IntExp);
    assertEquals(result.getNumberOfIntervals()[1], n1IntExp);
    assertEquals(result.getOrder()[0], orderExp);
    assertEquals(result.getOrder()[1], orderExp);

    for (int i = 0; i < n0Data; ++i) {
      final double ref = Math.abs(x0Values[i]) == 0. ? 1. : Math.abs(x0Values[i]);
      assertEquals(result.getKnots0().getData()[i], x0Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(0).getData()[i], x0Values[i], ref * EPS);
    }
    for (int i = 0; i < n1Data; ++i) {
      final double ref = Math.abs(x1Values[i]) == 0. ? 1. : Math.abs(x1Values[i]);
      assertEquals(result.getKnots1().getData()[i], x1Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(1).getData()[i], x1Values[i], ref * EPS);
    }

    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        final double ref = Math.abs(yValues[i][j]) == 0. ? 1. : Math.abs(yValues[i][j]);
        assertEquals(result.getCoefs()[i][j].getData()[orderExp - 1][orderExp - 1], yValues[i][j], ref * EPS);
      }
    }

    double[][] resValues = interp.interpolate(x0Values, x1Values, yValues, x0Values, x1Values).getData();
    final PiecewisePolynomialFunction2D func2D = new PiecewisePolynomialFunction2D();
    double[][] resDiffX0 = func2D.differentiateX0(result, x0Values, x1Values).getData();
    double[][] resDiffX1 = func2D.differentiateX1(result, x0Values, x1Values).getData();

    final PiecewisePolynomialFunction1D func1D = new PiecewisePolynomialFunction1D();
    double[][] expDiffX0 = func1D.differentiate(method.interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Values).getData();
    double[][] expDiffX1 = func1D.differentiate(method.interpolate(x1Values, yValues), x1Values).getData();

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = expDiffX1[i][j];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resDiffX1[i][j], expVal, ref * EPS);
      }
    }
    //    System.out.println(new DoubleMatrix2D(expDiffX0));
    //    System.out.println(new DoubleMatrix2D(resDiffX0));
    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = expDiffX0[j][i];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resDiffX0[i][j], expVal, ref * EPS);
      }
    }

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = yValues[i][j];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
      }
    }

  }

  /**
   * f(x0,x1) = ( x0 - 1.)^3 * (x1  + 14./13.)^3
   */
  @Test
  public void cubicTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = (x0Values[i] - 1.) * (x0Values[i] - 1.) * (x0Values[i] - 1.) * (x1Values[j] + 14. / 13.) * (x1Values[j] + 14. / 13.) * (x1Values[j] + 14. / 13.);
      }
    }

    CubicSplineInterpolator method = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator2D interp = new BicubicSplineInterpolator(method);
    PiecewisePolynomialResult2D result = interp.interpolate(x0Values, x1Values, yValues);

    final int n0IntExp = n0Data - 1;
    final int n1IntExp = n1Data - 1;
    final int orderExp = 4;

    final int n0Keys = 51;
    final int n1Keys = 61;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = 0. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -2. + 6. * i / (n1Keys - 1);
    }

    assertEquals(result.getNumberOfIntervals()[0], n0IntExp);
    assertEquals(result.getNumberOfIntervals()[1], n1IntExp);
    assertEquals(result.getOrder()[0], orderExp);
    assertEquals(result.getOrder()[1], orderExp);

    for (int i = 0; i < n0Data; ++i) {
      final double ref = Math.abs(x0Values[i]) == 0. ? 1. : Math.abs(x0Values[i]);
      assertEquals(result.getKnots0().getData()[i], x0Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(0).getData()[i], x0Values[i], ref * EPS);
    }
    for (int i = 0; i < n1Data; ++i) {
      final double ref = Math.abs(x1Values[i]) == 0. ? 1. : Math.abs(x1Values[i]);
      assertEquals(result.getKnots1().getData()[i], x1Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(1).getData()[i], x1Values[i], ref * EPS);
    }

    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        final double ref = Math.abs(yValues[i][j]) == 0. ? 1. : Math.abs(yValues[i][j]);
        assertEquals(result.getCoefs()[i][j].getData()[orderExp - 1][orderExp - 1], yValues[i][j], ref * EPS);
      }
    }

    double[][] resValues = interp.interpolate(x0Values, x1Values, yValues, x0Values, x1Values).getData();
    final PiecewisePolynomialFunction2D func2D = new PiecewisePolynomialFunction2D();
    double[][] resDiffX0 = func2D.differentiateX0(result, x0Values, x1Values).getData();
    double[][] resDiffX1 = func2D.differentiateX1(result, x0Values, x1Values).getData();

    final PiecewisePolynomialFunction1D func1D = new PiecewisePolynomialFunction1D();
    double[][] expDiffX0 = func1D.differentiate(method.interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Values).getData();
    double[][] expDiffX1 = func1D.differentiate(method.interpolate(x1Values, yValues), x1Values).getData();

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = expDiffX1[i][j];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resDiffX1[i][j], expVal, ref * EPS);
      }
    }
    //    System.out.println(new DoubleMatrix2D(expDiffX0));
    //    System.out.println(new DoubleMatrix2D(resDiffX0));
    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = expDiffX0[j][i];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resDiffX0[i][j], expVal, ref * EPS);
      }
    }

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = yValues[i][j];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
      }
    }

  }

  /**
   * 
   */
  @Test
  public void crossDerivativeTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[][] { {1.0, -1.0, 0.0, 1.0, 0.0, }, {1.0, -1.0, 0.0, 1.0, -2.0 }, {1.0, -2.0, 0.0, -2.0, -2.0 }, {-1.0, -1.0, -2.0, -2.0, -1.0 } };

    NaturalSplineInterpolator method = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator2D interp = new BicubicSplineInterpolator(method);
    PiecewisePolynomialResult2D result = interp.interpolate(x0Values, x1Values, yValues);

    final int n0IntExp = n0Data - 1;
    final int n1IntExp = n1Data - 1;
    final int orderExp = 4;

    final int n0Keys = 51;
    final int n1Keys = 61;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = 0. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -2. + 6. * i / (n1Keys - 1);
    }

    assertEquals(result.getNumberOfIntervals()[0], n0IntExp);
    assertEquals(result.getNumberOfIntervals()[1], n1IntExp);
    assertEquals(result.getOrder()[0], orderExp);
    assertEquals(result.getOrder()[1], orderExp);

    for (int i = 0; i < n0Data; ++i) {
      final double ref = Math.abs(x0Values[i]) == 0. ? 1. : Math.abs(x0Values[i]);
      assertEquals(result.getKnots0().getData()[i], x0Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(0).getData()[i], x0Values[i], ref * EPS);
    }
    for (int i = 0; i < n1Data; ++i) {
      final double ref = Math.abs(x1Values[i]) == 0. ? 1. : Math.abs(x1Values[i]);
      assertEquals(result.getKnots1().getData()[i], x1Values[i], ref * EPS);
      assertEquals(result.getKnots2D().get(1).getData()[i], x1Values[i], ref * EPS);
    }

    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        final double ref = Math.abs(yValues[i][j]) == 0. ? 1. : Math.abs(yValues[i][j]);
        assertEquals(result.getCoefs()[i][j].getData()[orderExp - 1][orderExp - 1], yValues[i][j], ref * EPS);
      }
    }

    double[][] resValues = interp.interpolate(x0Values, x1Values, yValues, x0Values, x1Values).getData();
    final PiecewisePolynomialFunction2D func2D = new PiecewisePolynomialFunction2D();
    double[][] resDiffX0 = func2D.differentiateX0(result, x0Values, x1Values).getData();
    double[][] resDiffX1 = func2D.differentiateX1(result, x0Values, x1Values).getData();

    final PiecewisePolynomialFunction1D func1D = new PiecewisePolynomialFunction1D();
    double[][] expDiffX0 = func1D.differentiate(method.interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Values).getData();
    double[][] expDiffX1 = func1D.differentiate(method.interpolate(x1Values, yValues), x1Values).getData();

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = expDiffX1[i][j];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resDiffX1[i][j], expVal, ref * EPS);
      }
    }
    //    System.out.println(new DoubleMatrix2D(expDiffX0));
    //    System.out.println(new DoubleMatrix2D(resDiffX0));
    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = expDiffX0[j][i];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resDiffX0[i][j], expVal, ref * EPS);
      }
    }

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        final double expVal = yValues[i][j];
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
      }
    }

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullx0Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };
    x0Values = null;

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullx1Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };
    x1Values = null;

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullyTest() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };
    yValues = null;

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthx0Test() {
    double[] x0Values = new double[] {0., 1., 2. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthx1Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2., 3. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shortx0Test() {
    double[] x0Values = new double[] {1. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] {{1., 2., 4. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shortx1Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0. };
    double[][] yValues = new double[][] { {1. }, {-1. }, {2. }, {5. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infX0Test() {
    double[] x0Values = new double[] {0., 1., 2., INF };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanX0Test() {
    double[] x0Values = new double[] {0., 1., 2., Double.NaN };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infX1Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., INF };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanX1Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., Double.NaN };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infYTest() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., INF }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanYTest() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., Double.NaN } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideX0Test() {
    double[] x0Values = new double[] {0., 1., 1., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideX1Test() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 1. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notTwoMethodsTest() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    BicubicSplineInterpolator interp = new BicubicSplineInterpolator(new PiecewisePolynomialInterpolator[] {new CubicSplineInterpolator() });
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notKnotRevoveredTests() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1.e-20, 3.e-120, 5.e120 }, {2.e-20, 3.e-120, 4.e-120 }, {1.e-20, 1.e-120, 1.e-20 }, {4.e-120, 3.e-20, 2.e-20 } };

    BicubicSplineInterpolator intp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    intp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * Tests below for debugging
   */
  @Test
      (enabled = false)
      public void printTest() {
    //    double[] x0Values = new double[] {0., 1., 2., 3. };
    //    double[] x1Values = new double[] {0., 0.000000000001, 2. };
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1.e-20, 3.e-120, 5.e-20 }, {2.e-20, 3.e-120, 4.e-120 }, {1.e-20, 1.e-120, 1.e-20 }, {4.e-120, 3.e-20, 2.e-20 } };

    //    double[] x0Values = new double[] {0., 1., 2. };
    //    double[] x1Values = new double[] {0., 1., 2., 3. };
    //    double[][] yValues = new double[][] { {1., 3., 5., 7. }, {2., 3., 4., 5. }, {1., 1., 1., 1. } };

    BicubicSplineInterpolator intp = new BicubicSplineInterpolator(new CubicSplineInterpolator());
    PiecewisePolynomialResult2D result2D = intp.interpolate(x0Values, x1Values, yValues);
    System.out.println(result2D.getCoefs()[0][0]);
    System.out.println(result2D.getCoefs()[2][1]);

    final int n0Keys = 31;
    final int n1Keys = 21;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = 0. + 3. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = 0. + 2. * i / (n1Keys - 1);
    }

    //    final int n0Keys = 61;
    //    final int n1Keys = 101;
    //    double[] x0Keys = new double[n0Keys];
    //    double[] x1Keys = new double[n1Keys];
    //    for (int i = 0; i < n0Keys; ++i) {
    //      x0Keys[i] = -1. + 4. * i / (n0Keys - 1);
    //    }
    //    for (int i = 0; i < n1Keys; ++i) {
    //      x1Keys[i] = -1. + 5. * i / (n1Keys - 1);
    //    }

    PiecewisePolynomialFunction2D func = new PiecewisePolynomialFunction2D();
    final double[][] values = func.evaluate(result2D, x0Keys, x1Keys).getData();

    for (int i = 0; i < n0Keys; ++i) {
      System.out.print("\t" + x0Keys[i]);
    }
    System.out.print("\n");
    for (int j = 0; j < n1Keys; ++j) {
      System.out.print(x1Keys[j]);
      for (int i = 0; i < n0Keys; ++i) {
        System.out.print("\t" + values[i][j]);
      }
      System.out.print("\n");
    }

    System.out.print("\n");

    for (int i = 0; i < x0Values.length; ++i) {
      System.out.print("\t" + x0Values[i]);
    }
    System.out.print("\n");
    for (int j = 0; j < x1Values.length; ++j) {
      System.out.print(x1Values[j]);
      for (int i = 0; i < x0Values.length; ++i) {
        System.out.print("\t" + yValues[i][j]);
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void randomTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    final Random randObj = new Random();
    int k = 0;
    while (k < 100000) {
      for (int i = 0; i < n0Data; ++i) {
        for (int j = 0; j < n1Data; ++j) {
          yValues[i][j] = randObj.nextInt(4) - 2.;
        }
      }
      System.out.println(new DoubleMatrix2D(yValues));

      NaturalSplineInterpolator method = new NaturalSplineInterpolator();
      PiecewisePolynomialInterpolator2D interp = new BicubicSplineInterpolator(method);
      interp.interpolate(x0Values, x1Values, yValues);

      ++k;
    }
  }

}
