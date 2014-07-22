/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * provide caplet volatilities for a single strike  where the caplet volatility term structure is described by an interpolated
 * curve 
 */
public class SingleStrikeInterpolatedCurve extends SingleStrike {
  private final Interpolator1D _interpolator;
  private final double[] _knots;
  private final int _nKnots;

  public SingleStrikeInterpolatedCurve(final List<CapFloor> caps, final MulticurveProviderInterface curves, final Interpolator1D interpolator, final double[] knots) {
    super(caps, curves);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notEmpty(knots, "knots");
    _interpolator = interpolator;
    _knots = knots;
    _nKnots = _knots.length;
  }

  @Override
  public DoubleMatrix1D getVolatilities(final DoubleMatrix1D modelParams) {

    final double[] t = getCapletExp();
    final Interpolator1DDataBundle db = _interpolator.getDataBundle(_knots, modelParams.getData());
    final int n = getnCaplets();
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = _interpolator.interpolate(db, t[i]);
    }
    return new DoubleMatrix1D(res);
  }

  @Override
  public DoubleMatrix2D getVolJacobian(final DoubleMatrix1D modelParams) {
    final double[] t = getCapletExp();
    final Interpolator1DDataBundle db = _interpolator.getDataBundle(_knots, modelParams.getData());
    final int n = getnCaplets();

    final double[][] res = new double[n][_nKnots];
    for (int i = 0; i < n; i++) {
      res[i] = _interpolator.getNodeSensitivitiesForValue(db, t[i]);
    }
    return new DoubleMatrix2D(res);
  }

}
