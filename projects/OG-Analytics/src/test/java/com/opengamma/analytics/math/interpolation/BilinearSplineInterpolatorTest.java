/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BilinearSplineInterpolatorTest {

  private static final double EPS = 1e-13;
  private static final double INF = 1. / 0.;

  /**
   * 
   */
  @Test
  public void constFunctionTest() {

    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = 3.;
      }
    }

    DoubleMatrix2D[][] coefsExp = new DoubleMatrix2D[n0Data - 1][n1Data - 1];
    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        coefsExp[i][j] = new DoubleMatrix2D(new double[][] { {0., 0. }, {0., 3. } });
      }
    }
    final int orderExp = 2;
    final int n0KnotsExp = x0Values.length;
    final int n1KnotsExp = x1Values.length;

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();

    final int n0Keys = 51;
    final int n1Keys = 41;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = -1. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -1. + 4. * i / (n1Keys - 1);
    }

    PiecewisePolynomialResult2D result = interp.interpolate(x0Values, x1Values, yValues);

    assertEquals(result.getNumberOfIntervals()[0], n0KnotsExp - 1);
    assertEquals(result.getNumberOfIntervals()[1], n1KnotsExp - 1);
    assertEquals(result.getOrder()[0], orderExp);
    assertEquals(result.getOrder()[1], orderExp);
    for (int i = 0; i < n0Data; ++i) {
      final double ref = Math.abs(x0Values[i]) == 0. ? 1. : Math.abs(x0Values[i]);
      assertEquals(result.getKnots0().getData()[i], x0Values[i], ref * EPS);
    }
    for (int i = 0; i < n1Data; ++i) {
      final double ref = Math.abs(x1Values[i]) == 0. ? 1. : Math.abs(x1Values[i]);
      assertEquals(result.getKnots1().getData()[i], x1Values[i], ref * EPS);
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
        final double ref = 3.;
        assertEquals(resValues[i][j], ref, ref * EPS);
      }
    }
    {
      final double ref = 3.;
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[2], x1Keys[1]), ref, ref * EPS);
    }
  }

  /**
   * 
   */
  @Test
  public void bilinearFunctionTest() {

    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = (x0Values[i] - 1.5) * (x1Values[j] - 0.5);
      }
    }

    DoubleMatrix2D[][] coefsExp = new DoubleMatrix2D[n0Data - 1][n1Data - 1];
    for (int i = 0; i < n0Data - 1; ++i) {
      for (int j = 0; j < n1Data - 1; ++j) {
        coefsExp[i][j] = new DoubleMatrix2D(new double[][] { {1., (-0.5 + x1Values[j]) }, {(-1.5 + x0Values[i]), (-1.5 + x0Values[i]) * (-0.5 + x1Values[j]) } });
      }
    }
    final int orderExp = 2;
    final int n0KnotsExp = x0Values.length;
    final int n1KnotsExp = x1Values.length;

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();

    final int n0Keys = 51;
    final int n1Keys = 41;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = -1. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -1. + 4. * i / (n1Keys - 1);
    }

    PiecewisePolynomialResult2D result = interp.interpolate(x0Values, x1Values, yValues);

    assertEquals(result.getNumberOfIntervals()[0], n0KnotsExp - 1);
    assertEquals(result.getNumberOfIntervals()[1], n1KnotsExp - 1);
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
        final double expVal = (x0Keys[i] - 1.5) * (x1Keys[j] - 0.5);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
      }
    }
    //    final PiecewisePolynomialFunction2D func = new PiecewisePolynomialFunction2D();
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        final double expVal = (x0Keys[i] - 1.5) * (x1Keys[j] - 0.5);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
        //        assertEquals(resValues[i][j], func.evaluate(result, x0Keys[i], x1Keys[j]), ref * EPS);
      }
    }
    {
      final double expVal = (x0Keys[1] - 1.5) * (x1Keys[2] - 0.5);
      final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[1], x1Keys[2]), expVal, ref * EPS);
    }
    {
      final double expVal = (x0Keys[23] - 1.5) * (x1Keys[20] - 0.5);
      final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[23], x1Keys[20]), expVal, ref * EPS);
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
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

    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
    interp.interpolate(x0Values, x1Values, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notKnotRevoveredTests() {
    double[] x0Values = new double[] {0., 1., 1.0000001, 3. };
    double[] x1Values = new double[] {1., 1.0000000001, 3. };
    double[][] yValues = new double[][] { {1., 3., 5. }, {2., 3.e13, 4. }, {1., 1.e-2, 1. }, {4., 3., 2. } };

    BilinearSplineInterpolator intp = new BilinearSplineInterpolator();
    intp.interpolate(x0Values, x1Values, yValues);
  }
  //  /**
  //   * 
  //   */
  //  @Test
  //  public void Test() {
  //    double[] x0Values = new double[] {0., 1., 2., 3. };
  //    double[] x1Values = new double[] {0., 1., 2. };
  //    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };
  //
  //    BilinearSplineInterpolator interp = new BilinearSplineInterpolator();
  //    interp.interpolate(x0Values, x1Values, yValues);
  //  }
}
