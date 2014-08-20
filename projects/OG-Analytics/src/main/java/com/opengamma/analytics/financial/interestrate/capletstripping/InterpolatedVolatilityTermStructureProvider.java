/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructureProvider;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class InterpolatedVolatilityTermStructureProvider implements VolatilityTermStructureProvider<DoubleMatrix1D> {

  private final double[] _knots;
  private final Interpolator1D _interpolator;

  public InterpolatedVolatilityTermStructureProvider(final double[] knotPoints, final Interpolator1D interpolator) {
    ArgumentChecker.notEmpty(knotPoints, "null or empty knotPoints");
    ArgumentChecker.notNull(interpolator, "null interpolator");
    final int n = knotPoints.length;

    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(knotPoints[i] > knotPoints[i - 1], "knot points must be strictly ascending");
    }
    _knots = knotPoints;
    _interpolator = interpolator;
  }

  @Override
  public VolatilityTermStructure evaluate(final DoubleMatrix1D data) {
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, data.getData(), _interpolator, true);
    return new VolatilityTermStructure() {

      @Override
      public Double getVolatility(final Double t) {
        return curve.getYValue(t);
      }
    };
  }

}
