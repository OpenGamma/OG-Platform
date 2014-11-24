/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DoubleQuadraticInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private static final WeightingFunction DEFAULT_WEIGHT_FUNCTION = WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION;

  private final WeightingFunction _weightFunction;

  public DoubleQuadraticInterpolator1D() {
    _weightFunction = DEFAULT_WEIGHT_FUNCTION;
  }

  public DoubleQuadraticInterpolator1D(final WeightingFunction weightFunction) {
    ArgumentChecker.notNull(weightFunction, "null weight function");
    _weightFunction = weightFunction;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(value, "value");
    ArgumentChecker.notNull(data, "data bundle");
    ArgumentChecker.isTrue(data instanceof Interpolator1DDoubleQuadraticDataBundle, "data bundle is of wrong type");
    final Interpolator1DDoubleQuadraticDataBundle quadraticData = (Interpolator1DDoubleQuadraticDataBundle) data;
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (low == n) {
      return yData[n];
    } else if (low == 0) {
      final RealPolynomialFunction1D quadratic = quadraticData.getQuadratic(0);
      final double x = value - xData[1];
      return quadratic.evaluate(x);
    } else if (high == n) {
      final RealPolynomialFunction1D quadratic = quadraticData.getQuadratic(n - 2);
      final double x = value - xData[n - 1];
      return quadratic.evaluate(x);
    }
    final RealPolynomialFunction1D quadratic1 = quadraticData.getQuadratic(low - 1);
    final RealPolynomialFunction1D quadratic2 = quadraticData.getQuadratic(high - 1);
    final double w = _weightFunction.getWeight((xData[high] - value) / (xData[high] - xData[low]));
    // final double w = (xData[high] - value) / (xData[high] - xData[low]);
    final double res = w * quadratic1.evaluate(value - xData[low]) + (1 - w) * quadratic2.evaluate(value - xData[high]);
    return res;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(value, "value");
    ArgumentChecker.notNull(data, "data bundle");
    ArgumentChecker.isTrue(data instanceof Interpolator1DDoubleQuadraticDataBundle, "data bundle is of wrong type");
    final Interpolator1DDoubleQuadraticDataBundle quadraticData = (Interpolator1DDoubleQuadraticDataBundle) data;
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();

    if (n == 0) {
      return 0.0; // Special case of single knot
    }
    if (low == 0 || n == 1) { //second case handles two knots 
      final RealPolynomialFunction1D quadraticFirstDerivative = quadraticData.getQuadraticFirstDerivative(0);
      final double x = value - xData[1];
      return quadraticFirstDerivative.evaluate(x);
    } else if (high >= n) {
      final RealPolynomialFunction1D quadraticFirstDerivative = quadraticData.getQuadraticFirstDerivative(n - 2);
      final double x = value - xData[n - 1];
      return quadraticFirstDerivative.evaluate(x);
    }
    final RealPolynomialFunction1D quadratic1 = quadraticData.getQuadratic(low - 1);
    final RealPolynomialFunction1D quadratic2 = quadraticData.getQuadratic(high - 1);
    final RealPolynomialFunction1D quadratic1FirstDerivative = quadraticData.getQuadraticFirstDerivative(low - 1);
    final RealPolynomialFunction1D quadratic2FirstDerivative = quadraticData.getQuadraticFirstDerivative(high - 1);
    final double w = _weightFunction.getWeight((xData[high] - value) / (xData[high] - xData[low]));
    final double res = w * quadratic1FirstDerivative.evaluate(value - xData[low]) + (1 - w) * quadratic2FirstDerivative.evaluate(value - xData[high]) +
        (quadratic2.evaluate(value - xData[high]) - quadratic1.evaluate(value - xData[low])) / (xData[high] - xData[low]);
    return res;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data instanceof Interpolator1DDoubleQuadraticDataBundle, "data bundle is of wrong type");
    final Interpolator1DDoubleQuadraticDataBundle quadraticData = (Interpolator1DDoubleQuadraticDataBundle) data;
    final int low = quadraticData.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = quadraticData.size();
    final double[] xData = data.getKeys();
    final double[] result = new double[n];
    if (low == 0) {
      final double[] temp = getQuadraticSensitivities(xData, value, 1);
      result[0] = temp[0];
      result[1] = temp[1];
      result[2] = temp[2];
      return result;
    } else if (high == n - 1) {
      final double[] temp = getQuadraticSensitivities(xData, value, n - 2);
      result[n - 3] = temp[0];
      result[n - 2] = temp[1];
      result[n - 1] = temp[2];
      return result;
    } else if (high == n) {
      result[n - 1] = 1;
      return result;
    }
    final double[] temp1 = getQuadraticSensitivities(xData, value, low);
    final double[] temp2 = getQuadraticSensitivities(xData, value, high);
    final double w = _weightFunction.getWeight((xData[high] - value) / (xData[high] - xData[low]));
    // final double w = (xData[high] - value) / (xData[high] - xData[low]);
    result[low - 1] = w * temp1[0];
    result[low] = w * temp1[1] + (1 - w) * temp2[0];
    result[high] = w * temp1[2] + (1 - w) * temp2[1];
    result[high + 1] = (1 - w) * temp2[2];
    return result;
  }

  @Override
  public Interpolator1DDoubleQuadraticDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DDoubleQuadraticDataBundle(new ArrayInterpolator1DDataBundle(x, y));
  }

  @Override
  public Interpolator1DDoubleQuadraticDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DDoubleQuadraticDataBundle(new ArrayInterpolator1DDataBundle(x, y, true));
  }

  private double[] getQuadraticSensitivities(final double[] xData, final double x, final int i) {
    final double[] res = new double[3];
    final double deltaX = x - xData[i];
    final double h1 = xData[i] - xData[i - 1];
    final double h2 = xData[i + 1] - xData[i];
    res[0] = deltaX * (deltaX - h2) / h1 / (h1 + h2);
    res[1] = 1 + deltaX * (h2 - h1 - deltaX) / h1 / h2;
    res[2] = deltaX * (h1 + deltaX) / (h1 + h2) / h2;
    return res;
  }
}
