/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterpolatedVectorFunctionProvider extends DoublesVectorFunctionProvider {
  private final Interpolator1D _interpolator;
  private final double[] _knots;

  public InterpolatedVectorFunctionProvider(final Interpolator1D interpolator, final double[] knots) {
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notEmpty(knots, "knots");
    final int n = knots.length;

    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(knots[i] > knots[i - 1], "knot points must be strictly ascending");
    }
    _interpolator = interpolator;
    _knots = knots;
  }

  @Override
  public VectorFunction from(final double[] x) {
    return new InterpolatedCurveVectorFunction(x, _interpolator, _knots);
  }

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  public double[] getKnots() {
    return _knots;
  }

}
