/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * For a set of knot points, x, and values, y, interpolate on the product xy using the supplied base interpolator.
 * Financial applications include interpolating on the product of the zero rates and time (equivalent to interpolation
 * on the log of the discount factors) and interpolating on the total variance (sigma-squared multiplied by time).
 * <p>
 * Knot values are supplied as y (i.e. <b>not</b> multiplied by x) and results from the interpolate method also also not Multiplied by x - i.e. the details of actually interpolating on xy are hidden
 * from the user.
 * <p>
 * One limitation is that is you supply a knot pair {0,y_0} the value y_0 will not necessarily be recovered at x=0 (the value returned is equal to the gradient of the underlying (i.e. base)
 * interpolated function at x=0)
 */
public class ProductInterpolator1D extends Interpolator1D {

  private static final long serialVersionUID = 1L;
  private static final double SMALL = 1e-15;
  private final Interpolator1D _base;
  private final boolean _addZeroValue;

  /**
   * Set up the interpolator. Internally the interpolation is on x-xy (where x are the knot positions and y are the values
   * - with xy the product) using the base interpolator
   * @param base the interpolator used to interpolate on x-xy
   */
  public ProductInterpolator1D(Interpolator1D base) {
    this(base, false);
  }

  /**
   * Set up the interpolator. Internally the interpolation is on x-xy (where x are the knot positions and y are the values
   * - with xy the product) using the base interpolator
   * @param base the interpolator used to interpolate on x-xy
   * @param addZeroValue If true added and extra knot at x=0 (and xy=0) to be used by the base interpolator
   */
  public ProductInterpolator1D(Interpolator1D base, boolean addZeroValue) {
    _base = ArgumentChecker.notNull(base, "base");
    _addZeroValue = addZeroValue;
  }

  @Override
  public Double interpolate(Interpolator1DDataBundle data, Double value) {
    ArgumentChecker.isTrue(value >= 0.0, "Negative value not allowed for this interpolator");
    // for x close to zero the xy (base) function can be expanded as bx + cx^2 +.... (there is no constant term since xy
    // must equal 0 for x = 0) so have y = b + cx + ...., hence y(0) = b - the gradient of the base function at x = 0
    if (value < SMALL) {
      return _base.firstDerivative(data, value);
    }
    double xy = _base.interpolate(data, value);
    return xy / value;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    ArgumentChecker.isTrue(value >= 0.0, "Negative value not allowed for this interpolator");
    if (value < SMALL) {
      // There is currently not implementation of the curves gradients sensitively to the knot points, so this is
      // the best we can do
      return getNodeSensitivitiesForValue(data, SMALL);
    }
    double[] baseSense = _base.getNodeSensitivitiesForValue(data, value);
    double[] x = data.getKeys();
    int n = data.size();
    double[] sense = new double[n];
    double invX = 1.0 / value;
    for (int i = 0; i < n; i++) {
      sense[i] = invX * x[i] * baseSense[i];
    }
    return sense;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(double[] x, double[] y) {
    // use ArrayInterpolator1DDataBundle to sort x and y
    ArrayInterpolator1DDataBundle bundle = new ArrayInterpolator1DDataBundle(x, y);
    return getDataBundleFromBaseDataBundle(bundle);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(double[] x, double[] y) {
    ArrayInterpolator1DDataBundle bundle = new ArrayInterpolator1DDataBundle(x, y, true);
    return getDataBundleFromBaseDataBundle(bundle);
  }

  private Interpolator1DDataBundle getDataBundleFromBaseDataBundle(ArrayInterpolator1DDataBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    double[] x = bundle.getKeys();
    double[] y = bundle.getValues();
    int n = bundle.size();
    if (_addZeroValue) {
      double[] xy = new double[n + 1];
      double[] xMod = new double[n + 1];
      System.arraycopy(x, 0, xMod, 1, n);
      for (int i = 0; i < n; i++) {
        xy[i + 1] = x[i] * y[i];
      }
      return _base.getDataBundleFromSortedArrays(xMod, xy);
    } else {
      double[] xy = new double[n];
      for (int i = 0; i < n; i++) {
        xy[i] = x[i] * y[i];
      }
      return _base.getDataBundleFromSortedArrays(x, xy);
    }
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.isTrue(value >= 0.0, "Negative value not allowed for this interpolator");
    if (value > SMALL) {
      double p1 = _base.firstDerivative(data, value);
      double p2 = interpolate(data, value);
      return (p1 - p2) / value;
    }
    // Gradient at zero equals the second derivative of the base function at zero
    double eps = 1e-6 * (data.lastKey() - data.firstKey());
    Function1D<Double, Double> gradFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return _base.firstDerivative(data, x);
      }
    };
    Function1D<Double, Boolean> domain = new Function1D<Double, Boolean>() {
      @Override
      public Boolean evaluate(Double x) {
        return x >= data.firstKey() && x <= data.lastKey();
      }
    };
    ScalarFirstOrderDifferentiator diff = new ScalarFirstOrderDifferentiator(FiniteDifferenceType.CENTRAL, eps);
    Function1D<Double, Double> secondDivFunc = diff.differentiate(gradFunc, domain);
    return secondDivFunc.evaluate(value);
  }
}
