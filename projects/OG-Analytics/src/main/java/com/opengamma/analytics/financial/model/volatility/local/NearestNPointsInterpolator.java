/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import java.util.Arrays;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
/* package */class NearestNPointsInterpolator extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final Interpolator1D _interpolator;
  private final int _n;
  private final boolean _oddN;
  private final int _pointsBelow;

  public NearestNPointsInterpolator(final Interpolator1D interpolator, final int n) {
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notNegativeOrZero(n, "number of points");
    _interpolator = interpolator;
    _n = n;
    _oddN = n % 2 == 0;
    _pointsBelow = _oddN ? n / 2 : n / 2 - 1;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    final int indexBelow = data.getLowerBoundIndex(value);
    if (indexBelow < 0) {
      throw new IllegalArgumentException("Cannot extrapolate");
    }
    int lowerIndex = indexBelow - _pointsBelow;
    int upperIndex = lowerIndex + _n;
    if (lowerIndex < 0) {
      upperIndex -= lowerIndex;
      lowerIndex = 0;
    }
    final int numberOfDataPoints = data.size();
    if (upperIndex >= numberOfDataPoints) {
      final int diff = numberOfDataPoints - upperIndex;
      upperIndex -= diff;
      lowerIndex -= diff;
    }
    final double[] x = Arrays.copyOfRange(data.getKeys(), lowerIndex, upperIndex + 1);
    final double[] y = Arrays.copyOfRange(data.getValues(), lowerIndex, upperIndex + 1);
    final Interpolator1DDataBundle truncatedData = getDataBundleFromSortedArrays(x, y);
    return _interpolator.interpolate(truncatedData, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    ArgumentChecker.isTrue(x.length >= _n, "insufficient number of points");
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

}
