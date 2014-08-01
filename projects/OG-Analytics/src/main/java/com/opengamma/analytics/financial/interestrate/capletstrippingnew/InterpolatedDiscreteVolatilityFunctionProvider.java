/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterpolatedDiscreteVolatilityFunctionProvider implements DiscreteVolatilityFunctionProvider {

  private final double[] _knots;
  private final Interpolator1D _interpolator;

  public InterpolatedDiscreteVolatilityFunctionProvider(final double[] knotPoints, final Interpolator1D interpolator) {
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
  public int getNumModelParameters() {
    return _knots.length;
  }

  @Override
  public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
    ArgumentChecker.noNulls(expiryStrikePoints, "expiryStrikePoints");
    final int n = expiryStrikePoints.length;
    final int m = getNumModelParameters();

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final DoubleMatrix2D res = new DoubleMatrix2D(n, m);
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, x.getData(), _interpolator, true);
        for (int i = 0; i < n; i++) {
          res.getData()[i] = ArrayUtils.toPrimitive(curve.getYValueParameterSensitivity(expiryStrikePoints[i].first));
        }
        return res;
      }

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, x.getData(), _interpolator, true);
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          final double vol = curve.getYValue(expiryStrikePoints[i].first);
          res[i] = vol;
        }
        return new DoubleMatrix1D(res);
      }
    };
  }

}
