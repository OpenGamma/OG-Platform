/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * If we sample an interpolated curve at a fix set of points (the sample points), then this can be viewed as a
 * vector to vector mapping. This provides that map and the associated Jacobian 
 */
public class InterpolatedCurveVectorFunction extends VectorFunction {
  private final double[] _samplePoints;
  private final Interpolator1D _interpolator;
  private final double[] _knots;

  /**
   * 
   * @param samplePoints position where the (interpolated) curve is sampled 
   * @param interpolator The interpolator 
   * @param knots knots of the interpolated curve 
   */
  public InterpolatedCurveVectorFunction(final double[] samplePoints, final Interpolator1D interpolator, final double[] knots) {

    ArgumentChecker.notEmpty(samplePoints, "samplePoints");
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notEmpty(knots, "knots");
    _samplePoints = samplePoints;
    _interpolator = interpolator;
    _knots = knots;
  }

  @Override
  public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
    final Interpolator1DDataBundle db = _interpolator.getDataBundle(_knots, x.getData());
    final int n = _samplePoints.length;
    final int nKnots = _knots.length;
    final DoubleMatrix2D res = new DoubleMatrix2D(n, nKnots);
    final double[][] data = res.getData(); //direct access to matrix data
    for (int i = 0; i < n; i++) {
      data[i] = _interpolator.getNodeSensitivitiesForValue(db, _samplePoints[i]);
    }
    return res;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    final Interpolator1DDataBundle db = _interpolator.getDataBundle(_knots, x.getData());
    final int n = _samplePoints.length;
    final DoubleMatrix1D res = new DoubleMatrix1D(n);
    final double[] data = res.getData(); //direct access to vector data
    for (int i = 0; i < n; i++) {
      data[i] = _interpolator.interpolate(db, _samplePoints[i]);
    }
    return res;
  }

  @Override
  public int getSizeOfDomain() {
    return _knots.length;
  }

  @Override
  public int getSizeOfRange() {
    return _samplePoints.length;
  }

}
