/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ShapePreservingCubicSplineInterpolatorTest {
  private static final double EPS = 1e-10;
  private static final double INF = 1. / 0.;

  /**
   * data points interpolated by linear function
   */
  @Test
  public void linearTest() {

    final double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    final double[] yValues = new double[] {2., 4., 6., 8., 10., 12. };

    final double[][] coefExp = new double[][] { {0., 0., 2., 2., },
        {0., 0., 2., 8. / 3. },
        {0., 0., 2., 10. / 3. },
        {0., 0., 2., 4. },
        {0., 0., 2., 14. / 3. },
        {0., 0., 2., 16. / 3. },
        {0., 0., 2., 6. },
        {0., 0., 2., 20. / 3. },
        {0., 0., 2., 22. / 3. },
        {0., 0., 2., 8. },
        {0., 0., 2., 26. / 3. },
        {0., 0., 2., 28. / 3. },
        {0., 0., 2., 10. },
        {0., 0., 2., 32. / 3. },
        {0., 0., 2., 34. / 3. } };

    final double[] knotsExp = new double[] {0., 1. / 3., 2. / 3., 1., 4. / 3., 5. / 3., 2., 7. / 3., 8. / 3., 3., 10. / 3., 11. / 3., 4., 13. / 3., 14. / 3., 5. };
    final int orderExp = 4;
    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), orderExp);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 0; i < intLengthExp; ++i) {
      for (int j = 0; j < orderExp; ++j) {
        final double ref = coefExp[i][j] == 0. ? 1. : Math.abs(coefExp[i][j]);
        assertEquals(result.getCoefMatrix().getData()[i][j], coefExp[i][j], ref * EPS);
      }
    }
    for (int j = 0; j < intLengthExp + 1; ++j) {
      final double ref = knotsExp[j] == 0. ? 1. : Math.abs(knotsExp[j]);
      assertEquals(result.getKnots().getData()[j], knotsExp[j], ref * EPS);
    }

    double[] xKeys = new double[nData - 1];
    double[] valuesExp = new double[nData - 1];
    for (int i = 0; i < nData - 1; ++i) {
      xKeys[i] = 0.5 * (xValues[i + 1] + xValues[i]);
      valuesExp[i] = 0.5 * (yValues[i + 1] + yValues[i]);
    }

    double[] values = intp.interpolate(xValues, yValues, xKeys).getData();

    for (int i = 0; i < nData - 1; ++i) {
      final double ref = valuesExp[i] == 0. ? 1. : Math.abs(valuesExp[i]);
      assertEquals(values[i], valuesExp[i], ref * EPS);
    }

  }

  /**
   * Positions of extra knots are modified 
   */
  @Test
  public void correctedExtraKnotsTest() {
    final double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    final double[] yValues = new double[] {5.117767385717404, 6.448193771622548, 2.2821942943281783, 6.26865829460428, 8.66539745601466, 0.4684081305693999 };

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    {
      final double ref = Math.abs(1. + 1. / 3. * Math.pow(0.8, 117));
      assertEquals(result.getKnots().getData()[4], 1. + 1. / 3. * Math.pow(0.8, 117), EPS * ref);
    }

  }

  /**
   * zeroBetaTests below are for checking all the branches in double sweep method
   */
  @Test
  public void zeroBeta1Test() {

    final double[] xValues = new double[] {0., 1., 2., 3., 4., 5., 6., 7., 7.5, 8., 8.3, 8.4 };
    final double[] yValues = new double[] {1., 2., 3., 4., 5., 1.41, 1.43324, 1.43, 1.4333, 1.42, 1.420006, 1.42001 };

    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), 4);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 3; i < intLengthExp - 1; i += 3) {
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
    }
    int[] i = new int[] {1, 2 };
    for (int ii : i) {
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 1][2]), 1);
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 2][2]), 1);
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii + 1][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 2][2]), 1);
    }
  }

  /**
   * 
   */
  @Test
  public void zeroBeta2Test() {

    final double[] xValues = new double[] {0., 1., 2., 3., 4., 5., 6., 7., 7.5, 8., 8.3, 8.4 };
    final double[] yValues = new double[] {1., 2., 3., 4., 5., 6.41, 6.43324, 6.44, 6.4333, 6.42, 6.420006, 6.42001 };

    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), 4);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 3; i < intLengthExp - 1; i += 3) {
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
    }
    int[] i = new int[] {1, 2, 3, 4 };
    for (int ii : i) {
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 1][2]), 1);
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 2][2]), 1);
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii + 1][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 2][2]), 1);
    }
  }

  /**
   * 
   */
  @Test
  public void zeroBeta3Test() {

    final double[] xValues = new double[] {3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13., 14., 15., 16., 17., 18., 19., 20. };
    final double[] yValues = new double[] {1.42, 1.42, 1.43, 1.42, 1., 0., -1., -2., -3., 2.42, 2.42, 2.43, 2.42, 2., 0., -1., -2., -3. };

    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    final double[] beta = new double[] {1.0, 1.0, -1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 1.0, -1.0, 1.0, -1.0, -1.0, -1.0, 1.0, 0.0, 0.0, 0.0 };

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), 4);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 3; i < intLengthExp - 1; i += 3) {
      if (beta[i / 3] != 0) {
        assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
            Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
        assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
            Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
            1);
        assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
            Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
      }
    }
    int[] i = new int[] {5, 6, 13, 14 };
    for (int ii : i) {
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 1][2]), 1);
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 2][2]), 1);
      assertEquals(Math.signum(result.getCoefMatrix().getData()[3 * ii + 1][2]), Math.signum(result.getCoefMatrix().getData()[3 * ii + 2][2]), 1);
    }
  }

  /**
   * 
   */
  @Test
  public void zeroBeta4Test() {

    final double[] xValues = new double[] {0., 1., 1.1, 1.3, 3., 4., 4.1, 4.6, 4.8, 5., 6.2, 7.9 };
    final double[] yValues = new double[] {0.9008311501090895, 0.1719634598183083, 0.1010309520578011, 0.10504721080659263, 0.17540666458174503, 0.2093234479937922, 0.8881947659143393,
        0.34762162959831977, 0.3329817591903894, 0.5182377743435055, 0.183164167705688, 0.047878939120233 };

    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), 4);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 1; i < 6; i += 3) {
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
    }
  }

  /**
   * 
   */
  @Test
  public void zeroBeta5Test() {

    final double[] xValues = new double[] {3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 12.1 };
    final double[] yValues = new double[] {1.42, 1.421, 1.422, 1.421, 1., 0., 6.41, 6.43324, 6.44, 6.4333, 6.42 };

    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), 4);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 3; i < intLengthExp - 1; i += 3) {
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
    }
  }

  /**
   * 
   */
  @Test
  public void zeroBeta6Test() {

    final double[] xValues = new double[] {3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 12.1 };
    final double[] yValues = new double[] {-1.42, -1.421, -1.422, -1.421, -1., 0., -6.41, -6.43324, -6.44, -6.4333, -6.42 };

    final int nData = xValues.length;
    final int intLengthExp = 3 * (nData - 1);

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    PiecewisePolynomialResult result = intp.interpolate(xValues, yValues);

    assertEquals(result.getDimensions(), 1);
    assertEquals(result.getOrder(), 4);
    assertEquals(result.getNumberOfIntervals(), intLengthExp);

    for (int i = 3; i < intLengthExp - 1; i += 3) {
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i - 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i - 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          1);
      assertEquals(Math.signum(Math.abs(result.getCoefMatrix().getData()[i + 1][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i + 1][1]),
          Math.signum(Math.abs(result.getCoefMatrix().getData()[i][1]) < 1.e-15 ? 0. : result.getCoefMatrix().getData()[i][1]), 1);
    }
  }

  /**
   * Due to non-uniqueness of first derivatives, spline is not found in some cases 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noSplineFoundTest() {
    final double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    final double[] yValues = new double[] {6.826093986047667, 2.0898823357582286, 0.9283831909337348, 0.7977927420474962, 3.5944356762557206, 6.620380982226143 };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noSplineFoundTauTest() {
    final double[] xValues = new double[] {0., 1., 1.1, 1.3, 3., 4., 4.1, 4.6, 4.8, 5., 6.2, 7.9 };
    final double[] yValues = new double[] {0.026006379073901575, 0.10550492102081444, 0.6794576424972392, 0.921863379969563, 0.28648620217835274, 0.7199803557365164, 0.22626060774706713,
        0.22624762260968234, 0.579582487626177, 0.22299042180552542, 0.09527581704572874, 0.021179942182509737 };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noSplineFoundEtaTest() {
    final double[] xValues = new double[] {0., 1., 1.1, 1.3, 3., 4., 4.1, 4.6, 4.8, 5., 6.2, 7.9 };
    final double[] yValues = new double[] {0.1876319060272671, 0.9614212044716063, 0.6816857638943187, 0.37675837990239536, 0.5844983876486328, 0.6434299586348089, 0.41471703104897784,
        0.4148555495946201, 0.40730984287499683, 0.1149629409860089, 0.2922931033679792, 0.18170530592245404 };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * Multi-dimensional yData are not supported 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void multiDimTest() {
    final double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    final double[][] yValues = new double[][] { {0., 1., 2., 3., 4., 5. }, {1., 2., 3., 4., 5., 6. } };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullXvaluesTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    xValues = null;
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYvaluesTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    yValues = null;
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanXvaluesTest() {
    double[] xValues = new double[] {0., 1., 2., Double.NaN, 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanYvaluesTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., Double.NaN };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infXvaluesTest() {
    double[] xValues = new double[] {0., 1., 2., INF, 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infYvaluesTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., INF };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lengthMismatchTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4. };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lengthShortTest() {
    double[] xValues = new double[] {5., 6. };
    double[] yValues = new double[] {4., 2. };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sameXvaluesTest() {
    double[] xValues = new double[] {0., 1., 1. };
    double[] yValues = new double[] {0., 1., 2. };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullKeyTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] xKeys = new double[] {0., 1., 2., 3., 4., 5. };
    xKeys = null;
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues, xKeys);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanKeyTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] xKeys = new double[] {0., 1., 2., 3., 4., Double.NaN };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues, xKeys);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infKeyTest() {
    double[] xValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] yValues = new double[] {0., 1., 2., 3., 4., 5. };
    double[] xKeys = new double[] {0., 1., 2., 3., INF, 5. };
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    intp.interpolate(xValues, yValues, xKeys);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notReconnectedTest() {
    double[] xValues = new double[] {1., 2., 2.0000000000001, 4. };
    double[] yValues = new double[] {2., 3.e10, 4.e-5, 5.e11 };

    PiecewisePolynomialInterpolator interpPos = new ShapePreservingCubicSplineInterpolator();
    interpPos.interpolate(xValues, yValues);
    //    System.out.println(interpPos.interpolate(xValues, yValues, xValues[1] * (1. - EPS)));
    //    System.out.println(interpPos.interpolate(xValues, yValues, xValues[1] * (1.)));
    //    System.out.println(interpPos.interpolate(xValues, yValues, xValues[1] * (1. + .00000000001)));
    //    System.out.println(interpPos.interpolate(xValues, yValues, xValues[2]));
  }

  /**
   * Tests below are for debugging
   */
  @Test
      (enabled = false)
      public void printTest() {

    final double[] xValues = new double[] {0., 1., 1.1, 1.3, 3., 4., 4.1, 4.6, 4.8, 5., 6.2, 7.9 };
    //    final double[] yValues = new double[] {-0.017368531235435615, -0.0700802853761212, -0.05408456435017034, -0.05913192596181248, -0.08417269025678796, -0.09519817187545818, -0.08392176115961013, -0.0273334463566804, -0.027814969452007822, -0.07487636330653216, -0.08932053011417443, -0.06799921678834817 };
    final int nData = xValues.length;
    double[] yValues = new double[nData];
    Random randObj = new Random();

    //    boolean done = false;
    //    while (done == false) {
    //      try {
    //        int k = 0;
    //        while (k < 10000) {
    //
    //          for (int i = 0; i < nData; ++i) {
    //            yValues[i] = randObj.nextDouble();
    //          }
    //          System.out.println(new DoubleMatrix1D(yValues));
    //          ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();
    //
    //          intp.interpolate(xValues, yValues);
    //          System.out.println("\n");
    //          ++k;
    //        }
    //      } catch (IllegalArgumentException e) {
    //        if (e.getMessage() == "Spline is not found!") {
    //          done = true;
    //        }
    //      }
    //    }

    int ctr = 0;
    int ctr2 = 0;
    int n = 0;
    int k = 0;
    while (n < 1000) {
      try {
        k = 0;
        while (k < 10000) {

          for (int i = 0; i < nData; ++i) {
            yValues[i] = randObj.nextDouble();
          }
          System.out.println(new DoubleMatrix1D(yValues));
          ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

          intp.interpolate(xValues, yValues);
          System.out.println("\n");
          ++k;
        }
      } catch (IllegalArgumentException e) {
        if (e.getMessage() == "Spline is not found" | e.getMessage() == "Local monotonicity can not be preserved") {
          ctr2 += (k + 1);
          ++ctr;
        }
      }
      ++n;
    }
    System.out.println(ctr + " / " + ctr2);

    //    final int nPts = 301;
    //    double[] keys = new double[nPts];
    //    for (int i = 0; i < nPts; ++i) {
    //      keys[i] = -1. + 11. / (nPts - 1) * i;
    //    }
    //
    //    double[] values = intp.interpolate(xValues, yValues, keys).getData();

    //    System.out.println(intp.interpolate(xValues, yValues).getCoefMatrix());
    //    System.out.println(intp.interpolate(xValues, yValues).getKnots());
    //
    //    for (int i = 0; i < nPts; ++i) {
    //      System.out.println(keys[i] + "\t" + values[i]);
    //    }

  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void print2Test() {

    final double[] xValues = new double[] {0., 1., 1.1, 1.3, 3., 4., 4.1, 4.6, 4.8, 5., 6.2, 7.9 };
    //    final double[] yValues = new double[] {-0.012571907543470618, -0.04883756776089532, -0.014388941042652703, -0.05064825621670973, -0.051504458856219196, -0.05365750284016134, -0.07570239700636491,
    //        -0.09682884295882602, -0.046370358425074934, -0.0890250059480754, -0.037570713349090526, -0.09150855513318415 };
    final double[] yValues = new double[] {0.46608273840991754, 0.8312159840478093, 0.9194772023433536, 0.6757561802041987, 0.6796484240935459, 0.30926871248752386, 0.10127356457226167,
        0.37084482298919885, 0.4707389784307331, 0.45361468489333356, 0.9307438159899785, 0.3902599731656107 };

    System.out.println(new DoubleMatrix1D(yValues));
    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    intp.interpolate(xValues, yValues);
    System.out.println("\n");

    //    final int nPts = 301;
    //    double[] keys = new double[nPts];
    //    for (int i = 0; i < nPts; ++i) {
    //      keys[i] = -1. + 11. / (nPts - 1) * i;
    //    }
    //
    //    double[] values = intp.interpolate(xValues, yValues, keys).getData();

    //    System.out.println(intp.interpolate(xValues, yValues).getCoefMatrix());
    //    System.out.println(intp.interpolate(xValues, yValues).getKnots());
    //
    //    for (int i = 0; i < nPts; ++i) {
    //      System.out.println(keys[i] + "\t" + values[i]);
    //    }

  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void print3Test() {

    final double[] xValues = new double[] {0., 0.1, 2., 3., 4., 9., 20., 30. };
    final double[] yValues = new double[] {0., 6., 5., 5., 5., 6.5, 6., 6. };

    ShapePreservingCubicSplineInterpolator intp = new ShapePreservingCubicSplineInterpolator();

    intp.interpolate(xValues, yValues);

    final int nPts = 301;
    double[] keys = new double[nPts];
    for (int i = 0; i < nPts; ++i) {
      keys[i] = 0.01 + 30. / (nPts - 1) * i;
    }

    double[] values = intp.interpolate(xValues, yValues, keys).getData();

    System.out.println(intp.interpolate(xValues, yValues).getCoefMatrix());
    System.out.println(intp.interpolate(xValues, yValues).getKnots());

    for (int i = 0; i < nPts; ++i) {
      System.out.println(keys[i] + "\t" + values[i]);
    }

  }

}
