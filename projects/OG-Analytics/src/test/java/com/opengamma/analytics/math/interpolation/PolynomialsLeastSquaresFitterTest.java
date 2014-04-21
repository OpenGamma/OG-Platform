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

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PolynomialsLeastSquaresFitterTest {
  private static final double EPS = 1e-14;
  private static final Random randObj = new Random();

  private final Function1D<double[], Double> _meanCal = new MeanCalculator();
  private final Function1D<double[], Double> _stdCal = new SampleStandardDeviationCalculator();

  /**
   * Checks coefficients of polynomial f(x) are recovered and residuals, { y_i -f(x_i) }, are accurate
   */
  @Test
  public void PolynomialFunctionRecoverTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();
    final double[] coeff = new double[] {3.4, 5.6, 1., -4. };

    DoubleFunction1D func = new RealPolynomialFunction1D(coeff);

    final int degree = coeff.length - 1;

    final int nPts = 7;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = -5. + 10 * i / (nPts - 1);
      yValues[i] = func.evaluate(xValues[i]);
    }

    double[] yValuesNorm = new double[nPts];

    final double mean = _meanCal.evaluate(xValues);
    final double std = _stdCal.evaluate(xValues);
    final double ratio = mean / std;

    for (int i = 0; i < nPts; ++i) {
      final double tmp = xValues[i] / std - ratio;
      yValuesNorm[i] = func.evaluate(tmp);
    }

    /**
     * Tests for regress(..)
     */

    LeastSquaresRegressionResult result = regObj.regress(xValues, yValues, degree);

    double[] coeffResult = result.getBetas();

    for (int i = 0; i < degree + 1; ++i) {
      assertEquals(coeff[i], coeffResult[i], EPS * Math.abs(coeff[i]));
    }

    final double[] residuals = result.getResiduals();
    func = new RealPolynomialFunction1D(coeffResult);
    double[] yValuesFit = new double[nPts];
    for (int i = 0; i < nPts; ++i) {
      yValuesFit[i] = func.evaluate(xValues[i]);
    }

    for (int i = 0; i < nPts; ++i) {
      assertEquals(Math.abs(yValuesFit[i] - yValues[i]), 0., Math.abs(yValues[i]) * EPS);
    }

    for (int i = 0; i < nPts; ++i) {
      assertEquals(Math.abs(yValuesFit[i] - yValues[i]), Math.abs(residuals[i]), Math.abs(yValues[i]) * EPS);
    }

    double sum = 0.;
    for (int i = 0; i < nPts; ++i) {
      sum += residuals[i] * residuals[i];
    }
    sum = Math.sqrt(sum);

    /**
     * Tests for regressVerbose(.., false)
     */

    PolynomialsLeastSquaresFitterResult resultVer = regObj.regressVerbose(xValues, yValues, degree, false);
    coeffResult = resultVer.getCoeff();
    func = new RealPolynomialFunction1D(coeffResult);
    for (int i = 0; i < nPts; ++i) {
      yValuesFit[i] = func.evaluate(xValues[i]);
    }

    assertEquals(nPts - (degree + 1), resultVer.getDof(), 0);
    for (int i = 0; i < degree + 1; ++i) {
      assertEquals(coeff[i], coeffResult[i], EPS * Math.abs(coeff[i]));
    }

    for (int i = 0; i < nPts; ++i) {
      assertEquals(Math.abs(yValuesFit[i] - yValues[i]), 0., Math.abs(yValues[i]) * EPS);
    }

    assertEquals(sum, resultVer.getDiffNorm(), EPS);

    /**
     * Tests for regressVerbose(.., true)
     */

    PolynomialsLeastSquaresFitterResult resultNorm = regObj.regressVerbose(xValues, yValuesNorm, degree, true);

    coeffResult = resultNorm.getCoeff();
    final double[] meanAndStd = resultNorm.getMeanAndStd();

    assertEquals(nPts - (degree + 1), resultNorm.getDof(), 0);
    assertEquals(mean, meanAndStd[0], EPS);
    assertEquals(std, meanAndStd[1], EPS);
    for (int i = 0; i < degree + 1; ++i) {
      assertEquals(coeff[i], coeffResult[i], EPS * Math.abs(coeff[i]));
    }

    func = new RealPolynomialFunction1D(coeffResult);
    for (int i = 0; i < nPts; ++i) {
      final double tmp = xValues[i] / std - ratio;
      yValuesFit[i] = func.evaluate(tmp);
    }

    for (int i = 0; i < nPts; ++i) {
      assertEquals(Math.abs(yValuesFit[i] - yValuesNorm[i]), 0., Math.abs(yValuesNorm[i]) * EPS);
    }

    sum = 0.;
    for (int i = 0; i < nPts; ++i) {
      sum += (yValuesFit[i] - yValuesNorm[i]) * (yValuesFit[i] - yValuesNorm[i]);
    }
    sum = Math.sqrt(sum);

    assertEquals(sum, resultNorm.getDiffNorm(), EPS);

  }

  /**
   * 
   */
  @Test
  public void RmatrixTest() {

    final PolynomialsLeastSquaresFitter regObj1 = new PolynomialsLeastSquaresFitter();
    final double[] xValues = new double[] {-1., 0, 1. };
    final double[] yValues = new double[] {1., 0, 1. };
    final double[][] rMatrix = new double[][] { {-Math.sqrt(3.), 0., -2. / Math.sqrt(3.) }, {0., -Math.sqrt(2.), 0. }, {0., 0., -Math.sqrt(2. / 3.) } };

    final int degree = 2;

    PolynomialsLeastSquaresFitterResult resultVer = regObj1.regressVerbose(xValues, yValues, degree, false);
    double[][] rMatResult = resultVer.getRMat().getData();

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        assertEquals(rMatrix[i][j], rMatResult[i][j], EPS);
      }
    }

    final PolynomialsLeastSquaresFitter regObj2 = new PolynomialsLeastSquaresFitter();
    PolynomialsLeastSquaresFitterResult resultNorm = regObj2.regressVerbose(xValues, yValues, degree, true);
    rMatResult = resultNorm.getRMat().getData();

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        assertEquals(rMatrix[i][j], rMatResult[i][j], EPS);
      }
    }

  }

  /**
   * An error is thrown if rescaling of xValues is NOT used and we try to access data, mean and standard deviation 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NormalisationErrorTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    PolynomialsLeastSquaresFitterResult result = regObj.regressVerbose(xValues, yValues, degree, false);
    result.getMeanAndStd();

  }

  /**
   * Number of data points should be larger than (degree + 1) of a polynomial
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void DataShortTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 6;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void DataShortVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 6;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void DataShortVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 6;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * Degree of polynomial must be positive 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinusDegreeTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = -4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinusDegreeVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = -4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinusDegreeVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = -4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1 };

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * xValues length should be the same as yValues length
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void WrongDataLengthTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1, 2 };

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void WrongDataLengthVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1, 2 };

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void WrongDataLengthVerboseTureTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 5, 6 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 1, 2 };

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * An error is thrown if too many repeated data are found
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void RepeatDataTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 1, 1 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 2 };

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void RepeatDataVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 1, 1 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 2 };

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void RepeatDataVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1, 2, 3, 1, 1 };
    final double[] yValues = new double[] {1, 2, 3, 4, 2, 2 };

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ExtremeValueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1e-307, 2e-307, 3e18, 4 };
    final double[] yValues = new double[] {1, 2, 3, 4, 5 };

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ExtremeValueVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1e-307, 2e-307, 3e18, 4 };
    final double[] yValues = new double[] {1, 2, 3, 4, 5 };

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ExtremeValueVerboseTrueAlphaTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final double[] xValues = new double[] {0, 1e-307, 2e-307, 3e-307, 4 };
    final double[] yValues = new double[] {1, 2, 3, 4, 5 };

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NullTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    xValues = null;
    yValues = null;

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NullVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    xValues = null;
    yValues = null;

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NullVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    xValues = null;
    yValues = null;

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void InfinityTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    final double zero = 0.;

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = 1. / zero;
      yValues[i] = 1. / zero;
    }

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void InfinityVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    final double zero = 0.;

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = 1. / zero;
      yValues[i] = 1. / zero;
    }

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void InfinityVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    final double zero = 0.;

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = 1. / zero;
      yValues[i] = 1. / zero;
    }

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NaNTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = Double.NaN;
      yValues[i] = Double.NaN;
    }

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NaNVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = Double.NaN;
      yValues[i] = Double.NaN;
    }

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void NaNVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    final int nPts = 5;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = Double.NaN;
      yValues[i] = Double.NaN;
    }

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void LargeNumberTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    double[] xValues = new double[] {1, 2, 3, 4e2, 5, 6, 7 };
    double[] yValues = new double[] {1, 2, 3, 4, 5, 6, 7 };

    regObj.regress(xValues, yValues, degree);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void LargeNumberVerboseFalseTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 4;

    double[] xValues = new double[] {1, 2, 3, 4e2, 5, 6, 7 };
    double[] yValues = new double[] {1, 2, 3, 4, 5, 6, 7 };

    regObj.regressVerbose(xValues, yValues, degree, false);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void LargeNumberVerboseTrueTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    final int degree = 6;

    double[] xValues = new double[] {1, 2, 3, 4e17, 5, 6, 7 };
    double[] yValues = new double[] {1, 2, 3, 4, 5, 6, 7 };

    regObj.regressVerbose(xValues, yValues, degree, true);

  }

  /**
   *  Print tests below are for debugging
   */
  @Test(enabled = false)
  public void PolynomialFunctionFitPrintTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    // final double[] coeff = new double[] {5. * (randObj.nextDouble() + .5), 5. * (randObj.nextDouble() - .5), 5. * (randObj.nextDouble() - 5.), 5. * (randObj.nextDouble() - .5) };
    final double[] coeff = new double[] {-(randObj.nextDouble() + 1.), (randObj.nextDouble() + 1.), -(randObj.nextDouble() + 1.), (randObj.nextDouble() + 1.) };
    final DoubleFunction1D func = new RealPolynomialFunction1D(coeff);

    final int degree = coeff.length - 1;

    final int nPts = 7;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = -5. + 10 * i / (nPts - 1);
      //xValues[i] = 3. * (randObj.nextDouble() + .5);
      yValues[i] = func.evaluate(xValues[i]);
    }

    PolynomialsLeastSquaresFitterResult result = regObj.regressVerbose(xValues, yValues, degree, false);

    final double[] coeffResult = result.getCoeff();

    System.out.println("xValues");
    for (int i = 0; i < nPts; ++i) {
      System.out.print(xValues[i] + ",");
    }

    System.out.println("\n");
    System.out.println("yValues");
    for (int i = 0; i < nPts; ++i) {
      System.out.print(yValues[i] + ",");
    }

    System.out.println("\n");

    System.out.println("true coeffs");
    for (int i = 0; i < degree + 1; ++i) {
      System.out.print(coeff[degree - i] + ",");
    }

    System.out.println("\n");
    System.out.println("fit coeffs");
    for (int i = 0; i < degree + 1; ++i) {
      System.out.print(coeffResult[degree - i] + ",");
    }

    System.out.println("\n");

    System.out.println("fit norm");
    System.out.println(result.getDiffNorm());

    System.out.println("\n");

    System.out.println("fit dof");
    System.out.println(result.getDof());

    final DoubleMatrix2D rMatrix = result.getRMat();
    final double[][] rMatrixDoub = rMatrix.getData();

    for (int i = 0; i < degree + 1; ++i) {
      for (int j = 0; j < degree + 1; ++j) {
        System.out.print(rMatrixDoub[i][j] + "\t");
      }
      System.out.print("\n");
    }

  }

  /**
   * 
   */
  @Test(enabled = false)
  public void PolynomialFunctionFitPrintTest2() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    // final double[] coeff = new double[] {5. * (randObj.nextDouble() + .5), 5. * (randObj.nextDouble() - .5), 5. * (randObj.nextDouble() - 5.), 5. * (randObj.nextDouble() - .5) };
    final double[] coeff = new double[] {-1.9564385860928322, 1.968428061753627, -1.8042487762604558, 1.1347030699838965, 2.1347030699838965 };
    final DoubleFunction1D func = new RealPolynomialFunction1D(coeff);

    final int degree = coeff.length - 1;

    final int nPts = 7;
    double[] xValues = new double[] {-2.0, -1.0, -.5, 0.0, 1.0, 2.0, 3.0 };
    final double mean = 0.357142857142857;
    final double std = 1.749149453169685;

    double[] xValuesNom = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      //  xValues[i] = -5. + 10 * i / (nPts - 1);
      //xValues[i] = 3. * (randObj.nextDouble() + .5);
      xValuesNom[i] = xValues[i] / std - mean / std;
      yValues[i] = func.evaluate(xValuesNom[i]);
    }

    PolynomialsLeastSquaresFitterResult resultNom = regObj.regressVerbose(xValues, yValues, degree, true);
    PolynomialsLeastSquaresFitterResult result = regObj.regressVerbose(xValuesNom, yValues, degree, false);

    final double[] coeffResultNom = resultNom.getCoeff();
    final double[] coeffResult = result.getCoeff();

    System.out.println("true coeffs");
    for (int i = 0; i < degree + 1; ++i) {
      assertEquals(coeffResultNom[i], coeffResult[i], EPS * Math.abs(coeff[i]));
    }

    System.out.println("xValues");
    for (int i = 0; i < nPts; ++i) {
      System.out.print(xValues[i] + ",");
    }

    System.out.println("\n");
    System.out.println("yValues");
    for (int i = 0; i < nPts; ++i) {
      System.out.print(yValues[i] + ",");
    }

    System.out.println("\n");

    System.out.println("true coeffs");
    for (int i = 0; i < degree + 1; ++i) {
      System.out.print(coeff[degree - i] + ",");
    }

    System.out.println("\n");
    System.out.println("fit coeffs");
    for (int i = 0; i < degree + 1; ++i) {
      System.out.print(coeffResult[degree - i] + ",");
    }

    System.out.println("\n");

    System.out.println("fit norm");
    System.out.println(result.getDiffNorm());

    System.out.println("\n");

    System.out.println("fit dof");
    System.out.println(result.getDof());

    final DoubleMatrix2D rMatrix = result.getRMat();
    final double[][] rMatrixDoub = rMatrix.getData();

    for (int i = 0; i < degree + 1; ++i) {
      for (int j = 0; j < degree + 1; ++j) {
        System.out.print(rMatrixDoub[i][j] + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");
    System.out.println("Norm of rMatrix");
    System.out.println(OG_ALGEBRA.getNorm2(rMatrix));

    System.out.println("\n");

  }

  /**
   * 
   */
  @Test
      (enabled = false)
      public void GeneralTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();

    //  final int degree = 4;

    //    double eps = 1e-8;
    //    double[] xValues = new double[] {-2, 0, 5, 5. * (1. + eps), 7, 8 };
    //    double[] yValues = new double[] {1, 2, 3, 5, 7, 8 };

    //    double eps = 1e-8;
    //    double[] xValues = new double[] {-2, 0, 5, 6., 7, 8 };
    //    double[] yValues = new double[] {1, 2, 3, 5, 5 * (1. + eps), 8 };

    //    double[] xValues = new double[] {-2, 0, 5, 4, 5, 8 };
    //    double[] yValues =new double[]  {1, 1, 1, 1, 1, 1 };

    //    double[] xValues =  new double[] {-2, 0, 5, 4, 2, 8 };
    //    double[] yValues = new double[] {1, -1, 10, 11 / 1e-12, -2., 12 };

    //    double[] xValues =new double[]  {-2, 0, 5, 4, 4, 6 };
    //    double[] yValues = new double[] {1, -1, 10, +0, -0., 0.0000003574827931 };

    //    double[] xValues = new double[] {-2, 0, 5, 3, 4, 6 };
    //    double[] yValues = new double[] {1, -1, 10, -0, -1e8, 1e8 };

    //    double[] xValues = new double[] {0, 1e-307, 2e-307, 3e17, 4 };
    //    double[] yValues = new double[] {1, 2, 3, 4, 5 };

    //    double[] xValues = new double[] {1, 2, 3, 4, 5, 6, 7 };
    //    double[] yValues = new double[] {1, 2, 3, 4, 5, 6, 7 };

    final int degree = 6;

    double[] xValues = new double[] {1, 2, 3, 4e16, 5, 6, 7 };
    double[] yValues = new double[] {1, 2, 3, 4, 5, 6, 7 };

    //    final int degree = 4;
    //    double[] xValues = new double[] {0, 1e-307, 2e-307, 3e17, 4 };
    //    double[] yValues = new double[] {1, 2, 3, 4, 5 };

    PolynomialsLeastSquaresFitterResult result = regObj.regressVerbose(xValues, yValues, degree, true);

    final double[] coeffResult = result.getCoeff();
    final double res = result.getDiffNorm();

    for (int i = 0; i < degree + 1; ++i) {
      System.out.println(coeffResult[i]);
    }

    System.out.println(res);

  }

  /**
   * 
   */
  @Test(enabled = false)
  public void RandomDataFitPrintTest() {

    final PolynomialsLeastSquaresFitter regObj = new PolynomialsLeastSquaresFitter();
    final int degree = 6;

    final int nPts = 7;
    double[] xValues = new double[nPts];
    double[] yValues = new double[nPts];

    for (int i = 0; i < nPts; ++i) {
      xValues[i] = 10. * (randObj.nextDouble() - .5);
      yValues[i] = 10. * (randObj.nextDouble() - .5);
      System.out.println(xValues[i] + "\t" + yValues[i]);
    }

    System.out.println("\n");

    LeastSquaresRegressionResult result = regObj.regress(xValues, yValues, degree);
    final double[] coeffResult = result.getBetas();

    final DoubleFunction1D func = new RealPolynomialFunction1D(coeffResult);

    for (int i = 0; i < 100; ++i) {
      final double k = -5. + 10. * i / 100.;
      System.out.println(k + "\t" + func.evaluate(k));
    }

    System.out.println("\n");
    final double[] resResult = result.getResiduals();
    double resSumSqHalf = 0.;

    for (int i = 0; i < nPts; ++i) {
      resSumSqHalf += 0.5 * resResult[i] * resResult[i];
      System.out.println(resResult[i]);
    }

    System.out.println("\n");

    System.out.println("chisq: " + "\t" + resSumSqHalf);

  }
}
