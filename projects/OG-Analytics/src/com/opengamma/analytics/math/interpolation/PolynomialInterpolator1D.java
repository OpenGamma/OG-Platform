/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.analysis.interpolation.NevilleInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.util.ArgumentChecker;

/**
 * Interpolates between data points using a polynomial. The method used is
 * Neville's algorithm.
 */
public class PolynomialInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final NevilleInterpolator _interpolator = new NevilleInterpolator();
  private final int _degree;
  private final int _offset;

  public PolynomialInterpolator1D(final int degree) {
    ArgumentChecker.notNegativeOrZero(degree, "degree");
    _degree = degree;
    _offset = 0;
  }

  public PolynomialInterpolator1D(final int degree, final int offset) {
    ArgumentChecker.notNegativeOrZero(degree, "degree");
    ArgumentChecker.notNegative(offset, "offset");
    if (offset >= degree) {
      throw new IllegalArgumentException("Offset cannot be greater than the degree");
    }
    _degree = degree;
    _offset = offset;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    final int n = data.size();
    final double[] keys = data.getKeys();
    final double[] values = data.getValues();
    if (n <= _degree) {
      throw new MathException("Need at least " + (_degree + 1) + " data points to perform polynomial interpolation of degree " + _degree);
    }
    if (data.getLowerBoundIndex(value) == n - 1) {
      return values[n - 1];
    }
    final int lower = data.getLowerBoundIndex(value);
    final int lowerBound = lower - _offset;
    final int upperBound = _degree + 1 + lowerBound;
    if (lowerBound < 0) {
      throw new MathException("Could not get lower bound: index " + lowerBound + " must be greater than or equal to zero");
    }
    if (upperBound > n + 1) {
      throw new MathException("Could not get upper bound: index " + upperBound + " must be less than or equal to " + (n + 1));
    }
    final double[] x = Arrays.copyOfRange(keys, lowerBound, upperBound);
    final double[] y = Arrays.copyOfRange(values, lowerBound, upperBound);
    try {
      final PolynomialFunctionLagrangeForm lagrange = _interpolator.interpolate(x, y);
      return CommonsMathWrapper.unwrap(lagrange).evaluate(value);
    } catch (final org.apache.commons.math.MathException e) {
      throw new MathException(e);
    }
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PolynomialInterpolator1D other = (PolynomialInterpolator1D) obj;
    if (_degree != other._degree) {
      return false;
    }
    if (_offset != other._offset) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _degree;
    result = prime * result + _offset;
    return result;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    return getFiniteDifferenceSensitivities(data, value);
  }

}
