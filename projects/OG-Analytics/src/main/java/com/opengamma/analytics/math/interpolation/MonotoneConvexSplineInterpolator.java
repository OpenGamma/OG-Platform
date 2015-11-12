/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Monotone Convex Interpolation based on 
 * P. S. Hagan, G. West, "interpolation Methods for Curve Construction"
 * Applied Mathematical Finance, Vol. 13, No. 2, 89–129, June 2006
 * 
 * Given a data set (time) and (spot rates)*(time), {t_i, r_i*t_i}, derive forward rate curve, f(t)=\frac{\partial r(t) t}{\partial t}, by "interpolateFwds" method 
 * or derive a curve of (spot rates) * (time), r(t) * t, by "interpolate" method by applying the interpolation to forward rates f_i estimated from spot rates r_i
 * When we apply this spline to interest rates, DO INCLUDE THE TRIVIAL POINT (t_0,r_0*t_0) = (0,0). In this case r_0 = 0 is automatically assumed. 
 * Note that f(t_i) = (original) f_i does NOT necessarily hold due to forward modification for ensuring positivity of the curve
 */
public class MonotoneConvexSplineInterpolator extends PiecewisePolynomialInterpolator {

  private double[] _time;
  private double[] _spotRates;

  /**
   * 
   */
  public MonotoneConvexSplineInterpolator() {
    _time = null;
    _spotRates = null;
  }

  /**
   * Determine r(t)t = \int _{xValues_0}^{x} f(s) ds  for t >= min{xValues}
   * Extrapolation by a linear function in the region t > max{xValues}. To employ this extrapolation, use interpolate methods in this class. 
   * @param xValues Data t_i
   * @param yValues Data r_i*t_i
   * @return PiecewisePolynomialResult for r(t)t
   */
  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length, " xValues length = yValues length");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yData containing Infinity");
    }

    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    for (int i = 0; i < nDataPts; ++i) {
      if (xValues[i] == 0.) {
        ArgumentChecker.isTrue(yValues[i] == 0., "r_i * t_i = 0 if t_i =0");
      }
    }

    double[] spotTmp = new double[nDataPts];
    for (int i = 0; i < nDataPts; ++i) {
      spotTmp[i] = xValues[i] == 0. ? 0. : yValues[i] / xValues[i];
    }

    _time = Arrays.copyOf(xValues, nDataPts);
    _spotRates = Arrays.copyOf(spotTmp, nDataPts);
    ParallelArrayBinarySort.parallelBinarySort(_time, _spotRates);

    final DoubleMatrix2D coefMatrix = solve(_time, _spotRates);
    final DoubleMatrix2D coefMatrixIntegrate = integration(_time, coefMatrix.getData());

    for (int i = 0; i < coefMatrixIntegrate.getNumberOfRows(); ++i) {
      for (int j = 0; j < coefMatrixIntegrate.getNumberOfColumns(); ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefMatrixIntegrate.getData()[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrixIntegrate.getData()[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(_time), coefMatrixIntegrate, coefMatrixIntegrate.getNumberOfColumns(), 1);
  }

  /**
   * Since Monotone Convex spline method introduces extra knots in some cases and the number of knots depends on yValues, 
   * this multidimensional method can not be supported 
   * @param xValues 
   * @param yValuesMatrix Multidimensional y values
   * @return Error is returned 
   */
  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[][] yValuesMatrix) {
    throw new IllegalArgumentException("Method with multidimensional yValues is not supported");
  }

  @Override
  public double interpolate(final double[] xValues, final double[] yValues, final double x) {

    final PiecewisePolynomialResult result = interpolate(xValues, yValues);
    final DoubleMatrix2D coefsMatrixIntegrate = result.getCoefMatrix();
    final int nKnots = coefsMatrixIntegrate.getNumberOfRows() + 1;
    final double[] knots = result.getKnots().getData();

    int indicator = 0;
    if (x <= knots[1]) {
      indicator = 0;
    } else {
      for (int i = 1; i < nKnots - 1; ++i) {
        if (knots[i] < x) {
          indicator = i;
        }
      }
    }

    final double[] coefs = coefsMatrixIntegrate.getRowVector(indicator, false).getData();

    final double res = getValue(coefs, x, knots[indicator]);
    ArgumentChecker.isFalse(Double.isInfinite(res), "Too large/small data values or xKey");
    ArgumentChecker.isFalse(Double.isNaN(res), "Too large/small data values or xKey");

    return res;
  }

  @Override
  public DoubleMatrix1D interpolate(final double[] xValues, final double[] yValues, final double[] x) {
    ArgumentChecker.notNull(x, "x");

    final int keyLength = x.length;
    double[] res = new double[keyLength];

    final PiecewisePolynomialResult result = interpolate(xValues, yValues);
    final DoubleMatrix2D coefsMatrixIntegrate = result.getCoefMatrix();
    final int nKnots = coefsMatrixIntegrate.getNumberOfRows() + 1;
    final double[] knots = result.getKnots().getData();

    for (int j = 0; j < keyLength; ++j) {
      int indicator = 0;
      if (x[j] <= knots[1]) {
        indicator = 0;
      } else {
        for (int i = 1; i < nKnots - 1; ++i) {
          if (knots[i] < x[j]) {
            indicator = i;
          }
        }
      }

      final double[] coefs = coefsMatrixIntegrate.getRowVector(indicator, false).getData();
      res[j] = getValue(coefs, x[j], knots[indicator]);
      ArgumentChecker.isFalse(Double.isInfinite(res[j]), "Too large/small data values or xKey");
      ArgumentChecker.isFalse(Double.isNaN(res[j]), "Too large/small data values or xKey");
    }

    return new DoubleMatrix1D(res, false);
  }

  @Override
  public DoubleMatrix2D interpolate(final double[] xValues, final double[] yValues, final double[][] xMatrix) {
    ArgumentChecker.notNull(xMatrix, "xMatrix");

    final int keyLength = xMatrix[0].length;
    final int keyDim = xMatrix.length;
    double[][] res = new double[keyDim][keyLength];

    final PiecewisePolynomialResult result = interpolate(xValues, yValues);
    final DoubleMatrix2D coefsMatrixIntegrate = result.getCoefMatrix();
    final int nKnots = coefsMatrixIntegrate.getNumberOfRows() + 1;
    final double[] knots = result.getKnots().getData();

    for (int j = 0; j < keyDim; ++j) {
      for (int k = 0; k < keyLength; ++k) {
        int indicator = 0;
        if (xMatrix[j][k] <= knots[1]) {
          indicator = 0;
        } else {
          for (int i = 1; i < nKnots - 1; ++i) {
            if (knots[i] < xMatrix[j][k]) {
              indicator = i;
            }
          }
        }

        final double[] coefs = coefsMatrixIntegrate.getRowVector(indicator, false).getData();
        res[j][k] = getValue(coefs, xMatrix[j][k], knots[indicator]);
        ArgumentChecker.isFalse(Double.isInfinite(res[j][k]), "Too large input");
        ArgumentChecker.isFalse(Double.isNaN(res[j][k]), "Too large input");
      }
    }

    return DoubleMatrix2D.noCopy(res);
  }

  /**
   * Since this interpolation method introduces new breakpoints in certain cases, {@link PiecewisePolynomialResultsWithSensitivity} is not well-defined
   * Instead the node sensitivity is computed in {@link MonotoneConvexSplineInterpolator1D} via {@link Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle}
   * @param xValues 
   * @param yValues 
   * @return NotImplementedException
   */
  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(final double[] xValues, final double[] yValues) {
    throw new NotImplementedException();
  }

  /**
   * Determine f(t) = \frac{\partial r(t) t}{\partial t}
   * @param xValues Data t_i
   * @param yValues Data r(t_i)
   * @return PiecewisePolynomialResult for f(t)
   */
  public PiecewisePolynomialResult interpolateFwds(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length, " xValues length = yValues length");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yData containing Infinity");
    }

    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    for (int i = 0; i < nDataPts; ++i) {
      if (xValues[i] == 0.) {
        ArgumentChecker.isTrue(yValues[i] == 0., "r_i * t_i = 0 if t_i =0");
      }
    }

    double[] spotTmp = new double[nDataPts];
    for (int i = 0; i < nDataPts; ++i) {
      spotTmp[i] = xValues[i] == 0. ? 0. : yValues[i] / xValues[i];
    }

    _time = Arrays.copyOf(xValues, nDataPts);
    _spotRates = Arrays.copyOf(spotTmp, nDataPts);
    ParallelArrayBinarySort.parallelBinarySort(_time, _spotRates);

    final DoubleMatrix2D coefMatrix = solve(_time, _spotRates);

    for (int i = 0; i < coefMatrix.getNumberOfRows(); ++i) {
      for (int j = 0; j < coefMatrix.getNumberOfColumns(); ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefMatrix.getData()[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrix.getData()[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(_time), coefMatrix, coefMatrix.getNumberOfColumns(), 1);
  }

  /**
   * Compute the value of f(t) = \frac{\partial r(t) t}{\partial t} at t=x
   * @param xValues Data r_i
   * @param yValues Data r(t_i)
   * @param x Key larger than min{r_i}
   * @return  f(x)
   */
  public double interpolateFwds(final double[] xValues, final double[] yValues, final double x) {
    final PiecewisePolynomialResult result = interpolateFwds(xValues, yValues);
    final DoubleMatrix2D coefsMatrix = result.getCoefMatrix();
    final int nKnots = coefsMatrix.getNumberOfRows() + 1;
    final double[] knots = result.getKnots().getData();

    int indicator = 0;
    if (x <= knots[1]) {
      indicator = 0;
    } else {
      for (int i = 1; i < nKnots - 1; ++i) {
        if (knots[i] < x) {
          indicator = i;
        }
      }
    }

    final double[] coefs = coefsMatrix.getRowVector(indicator, false).getData();

    final double res = getValue(coefs, x, knots[indicator]);
    ArgumentChecker.isFalse(Double.isInfinite(res), "Too large/small data values or xKey");
    ArgumentChecker.isFalse(Double.isNaN(res), "Too large/small data values or xKey");

    return res;
  }

  /**
   * Compute the values of f(t) 
   * @param xValues Data t_i
   * @param yValues Data r(t_i)
   * @param x Set of xKey 
   * @return r(x)
   */
  public DoubleMatrix1D interpolateFwds(final double[] xValues, final double[] yValues, final double[] x) {
    ArgumentChecker.notNull(x, "x");

    final PiecewisePolynomialResult result = interpolateFwds(xValues, yValues);
    final DoubleMatrix2D coefsMatrix = result.getCoefMatrix();
    final int nKnots = coefsMatrix.getNumberOfRows() + 1;
    final double[] knots = result.getKnots().getData();

    final int keyLength = x.length;
    double[] res = new double[keyLength];

    for (int j = 0; j < keyLength; ++j) {
      int indicator = 0;
      if (x[j] <= knots[1]) {
        indicator = 0;
      } else {
        for (int i = 1; i < nKnots - 1; ++i) {
          if (knots[i] < x[j]) {
            indicator = i;
          }
        }
      }

      final double[] coefs = coefsMatrix.getRowVector(indicator, false).getData();
      res[j] = getValue(coefs, x[j], knots[indicator]);
    }

    return new DoubleMatrix1D(res, false);
  }

  /**
   * Compute the values of f(t) 
   * @param xValues Data t_i
   * @param yValues Data r(t_i)
   * @param xMatrix Set of xKey 
   * @return r(x)
   */
  public DoubleMatrix2D interpolateFwds(final double[] xValues, final double[] yValues, final double[][] xMatrix) {
    ArgumentChecker.notNull(xMatrix, "xMatrix");

    final int keyLength = xMatrix[0].length;
    final int keyDim = xMatrix.length;
    double[][] res = new double[keyDim][keyLength];

    for (int i = 0; i < keyDim; ++i) {
      res[i] = interpolateFwds(xValues, yValues, xMatrix[i]).getData();
    }

    return new DoubleMatrix2D(res);
  }

  /**
   * Derive r(t) * t from f(t)
   * @param knots 
   * @param coefMatrix 
   * @return coefmatrix of r(t) * t
   */
  private DoubleMatrix2D integration(final double[] knots, final double[][] coefMatrix) {
    final int nCoefs = coefMatrix[0].length + 1;
    final int nKnots = knots.length;

    double[][] res = new double[nKnots][nCoefs];
    double sum = _spotRates[0] * _time[0];

    for (int i = 0; i < nKnots - 1; ++i) {
      res[i][0] = coefMatrix[i][0] / 3.;
      res[i][1] = coefMatrix[i][1] / 2.;
      res[i][2] = coefMatrix[i][2];
      res[i][3] = sum;
      sum = getValue(res[i], knots[i + 1], knots[i]);
    }
    res[nKnots - 1][0] = 0.;
    res[nKnots - 1][1] = 0.;
    res[nKnots - 1][2] = getValue(coefMatrix[nKnots - 2], knots[nKnots - 1], knots[nKnots - 2]);
    res[nKnots - 1][3] = sum;

    return new DoubleMatrix2D(res);
  }

  /**
   * @param xValues X values of data
   * @param yValues Y values of data
   * @return Coefficient matrix whose i-th row vector is {a3, a2, a1, a0} of f(x) = a3 * (x-x_i)^3 + a2 * (x-x_i)^2 +... for the i-th interval
   */
  private DoubleMatrix2D solve(final double[] time, final double[] spotRates) {

    final int nDataPts = time.length;
    final double[] discFwds = discFwdsFinder(time, spotRates);
    double[] fwds = fwdsFinder(time, discFwds);
    ArrayList<double[]> coefsList = new ArrayList<>();
    ArrayList<Double> knots = new ArrayList<>();

    for (int i = 0; i < nDataPts - 1; ++i) {
      final double gValue0 = fwds[i] - discFwds[i];
      final double gValue1 = fwds[i + 1] - discFwds[i];
      final double gDiff0 = -4. * gValue0 - 2. * gValue1;
      final double gDiff1 = 2. * gValue0 + 4. * gValue1;
      final double interval = time[i + 1] - time[i];
      final double shift = discFwds[i];

      if (Math.abs(gValue0) <= 1e-13) {
        final double[] coefs = new double[] {0., 0., shift, };
        knots.add(time[i]);
        coefsList.add(coefs);
      } else {
        if (Math.abs(gValue1) <= 1e-13) {
          final double[] coefs = new double[] {0., 0., shift, };
          knots.add(time[i]);
          coefsList.add(coefs);
        } else {
          if ((gValue0 > 0. && gValue1 > 0.) | (gValue0 < 0. && gValue1 < 0.)) {
            final double eta = gValue1 / (gValue1 + gValue0);
            final double cst0 = (gValue0 + 2. * gValue1) * gValue0 / gValue1;
            final double cst1 = (gValue1 + 2. * gValue0) * gValue1 / gValue0;
            final double newKnot = time[i] + interval * eta;
            final double[] coefs1 = new double[] {cst0 / gValue1 * (gValue0 + gValue1) / interval / interval, -2. * cst0 / interval, gValue0 + shift };
            final double[] coefs2 = new double[] {cst1 * (gValue0 + gValue1) / gValue0 / interval / interval, 0., -gValue0 * gValue1 / (gValue1 + gValue0) + shift };
            knots.add(time[i]);
            knots.add(newKnot);
            coefsList.add(coefs1);
            coefsList.add(coefs2);
          } else {
            if ((gDiff0 >= 0. && gDiff1 >= 0.) | (gDiff0 <= 0. && gDiff1 <= 0.)) {
              final double[] coefs = new double[] {(3. * gValue0 + 3. * gValue1) / interval / interval, (-4. * gValue0 - 2. * gValue1) / interval, gValue0 + shift };
              knots.add(time[i]);
              coefsList.add(coefs);
            } else {
              if ((gValue0 < 0. && gValue1 >= -2. * gValue0) | (gValue0 > 0. && gValue1 <= -2. * gValue0)) {
                final double eta = (gValue1 + 2. * gValue0) / (gValue1 - gValue0);
                final double newKnot = time[i] + interval * eta;
                final double cst = (gValue1 - gValue0) / 3. / gValue0;
                final double[] coefs1 = new double[] {0., 0., gValue0 + shift };
                final double[] coefs2 = new double[] {cst * cst * (gValue1 - gValue0) / interval / interval, 0., gValue0 + shift };
                knots.add(time[i]);
                knots.add(newKnot);
                coefsList.add(coefs1);
                coefsList.add(coefs2);
              } else {
                final double eta = 3. * gValue1 / (gValue1 - gValue0);
                final double newKnot = time[i] + interval * eta;
                final double cst = (gValue0 - gValue1) / 3. / gValue1;
                final double[] coefs1 = new double[] {cst * cst * (gValue0 - gValue1) / interval / interval, 2. * cst * (gValue0 - gValue1) / interval, gValue0 + shift };
                final double[] coefs2 = {0, 0, gValue1 + shift };
                knots.add(time[i]);
                knots.add(newKnot);
                coefsList.add(coefs1);
                coefsList.add(coefs2);
              }
            }
          }
        }
      }
    }
    knots.add(time[nDataPts - 1]);
    final int nKnots = knots.size();

    _time = new double[nKnots];
    for (int i = 0; i < nKnots; ++i) {
      _time[i] = knots.get(i);
    }

    double[][] res = new double[nKnots - 1][3];
    for (int i = 0; i < nKnots - 1; ++i) {
      res[i] = coefsList.get(i);
    }

    return new DoubleMatrix2D(res);
  }

  /**  
   * @param time 
   * @param spotRates 
   * @return Discrete forwards
   */
  private double[] discFwdsFinder(final double[] time, final double[] spotRates) {
    final int nDataPts = time.length;
    double[] res = new double[nDataPts - 1];

    for (int i = 0; i < nDataPts - 1; ++i) {
      res[i] = (spotRates[i + 1] * time[i + 1] - spotRates[i] * time[i]) / (time[i + 1] - time[i]);
    }

    return res;
  }

  /**
   * @param time
   * @param discFwds Discrete forwards 
   * @return Forwards
   */
  private double[] fwdsFinder(final double[] time, final double[] discFwds) {
    final int nDataPts = time.length;
    double[] res = new double[nDataPts];

    for (int i = 1; i < nDataPts - 1; ++i) {
      res[i] = (time[i] - time[i - 1]) * discFwds[i] / (time[i + 1] - time[i - 1]) + (time[i + 1] - time[i]) * discFwds[i - 1] / (time[i + 1] - time[i - 1]);
    }
    res[0] = 1.5 * discFwds[0] - 0.5 * res[1];
    res[nDataPts - 1] = 1.5 * discFwds[nDataPts - 2] - 0.5 * res[nDataPts - 2];

    return fwdsModifier(discFwds, res);
  }

  /**
   * Modify forwards such that positivity holds
   * @param discFwds Discrete forwards
   * @param fwds  Forwards 
   * @return Modified forwards
   */
  private double[] fwdsModifier(final double[] discFwds, final double[] fwds) {
    final int length = fwds.length;
    double[] res = new double[length];

    res[0] = boundFunc(0., fwds[0], 2. * discFwds[0]);
    for (int i = 1; i < length - 1; ++i) {
      res[i] = boundFunc(0., fwds[i], Math.min(2. * discFwds[i - 1], 2. * discFwds[i]));
    }
    res[length - 1] = boundFunc(0., fwds[length - 1], 2. * discFwds[length - 2]);

    return res;
  }

  private double boundFunc(final double a, final double b, final double c) {
    return Math.min(Math.max(a, b), c);
  }

}
