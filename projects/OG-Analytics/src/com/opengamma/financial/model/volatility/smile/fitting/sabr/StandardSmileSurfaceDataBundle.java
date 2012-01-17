/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class StandardSmileSurfaceDataBundle extends SmileSurfaceDataBundle {
  private static final Interpolator1D EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private final double[] _forwards;
  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[][] _impliedVols;
  private final ForwardCurve _forwardCurve;
  private final int _nExpiries;
  private final boolean _isCallData;

  public StandardSmileSurfaceDataBundle(final double[] forwards, final double[] expiries, final double[][] strikes, final double[][] impliedVols, final boolean isCallData) {
    Validate.notNull(forwards, "null forwards");
    Validate.notNull(expiries, "null expiries");
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(impliedVols, "null impliedVols");
    _nExpiries = expiries.length;
    Validate.isTrue(_nExpiries == forwards.length, "forwards wrong length");
    Validate.isTrue(_nExpiries == strikes.length, "strikes wrong length");
    Validate.isTrue(_nExpiries == impliedVols.length, "impVols wrong length");
    _expiries = expiries;
    _forwards = forwards;
    _forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(_expiries, _forwards, EXTRAPOLATOR));
    _strikes = strikes;
    _impliedVols = impliedVols;
    _isCallData = isCallData;
    checkVolatilities(expiries, _impliedVols);
  }

  @Override
  public double[] getExpiries() {
    return _expiries;
  }

  @Override
  public double[][] getStrikes() {
    return _strikes;
  }

  @Override
  public double[][] getVolatilities() {
    return _impliedVols;
  }

  @Override
  public double[] getForwards() {
    return _forwards;
  }

  @Override
  public ForwardCurve getForwardCurve() {
    return _forwardCurve;
  }

  @Override
  public boolean isCallData() {
    return _isCallData;
  }

  @Override
  public SmileSurfaceDataBundle withBumpedPoint(final int expiryIndex, final int strikeIndex, final double amount) {
    final double[] forwards = getForwards();
    final double[] expiries = getExpiries();
    final double[][] strikes = getStrikes();
    final int nExpiries = expiries.length;
    final int nStrikes = strikes[expiryIndex].length;
    Validate.isTrue(strikeIndex >= 0 && strikeIndex < nStrikes, "strike index out of range");

    final double[][] vols = new double[nStrikes][];
    for (int i = 0; i < nExpiries; i++) {
      System.arraycopy(_impliedVols[i], 0, vols[i], 0, nStrikes);
    }
    vols[expiryIndex][strikeIndex] += amount;
    return new StandardSmileSurfaceDataBundle(forwards, expiries, strikes, vols, isCallData());
  }
}
