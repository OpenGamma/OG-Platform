/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class ForexSmileDeltaSurfaceDataBundle extends SmileSurfaceDataBundle {
  private static final Interpolator1D EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private final double[] _forwards;
  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[][] _vols;
  private final ForwardCurve _forwardCurve;
  private final int _nExpiries;
  private final boolean _isCallData;

  public ForexSmileDeltaSurfaceDataBundle(final double[] forwards, final double[] expiries, final double[] deltas, final double[] atms, final double[][] riskReversals,
      final double[][] strangle, final boolean isCallData) {
    Validate.notNull(deltas, "null delta");
    Validate.notNull(forwards, "null forwards");
    Validate.notNull(expiries, "null expiries");
    Validate.notNull(atms, "null atms");
    Validate.notNull(riskReversals, "null rr");
    Validate.notNull(strangle, "null bf");
    _nExpiries = expiries.length;
    Validate.isTrue(_nExpiries == forwards.length, "forwards wrong length");
    Validate.isTrue(_nExpiries == atms.length, "atms wrong length");
    final int n = deltas.length;
    Validate.isTrue(n > 0, "need at lest one delta");
    Validate.isTrue(n == riskReversals.length, "wrong number of rr sets");
    Validate.isTrue(n == strangle.length, "wrong number of bf sets");
    for (int i = 0; i < n; i++) {
      Validate.isTrue(_nExpiries == riskReversals[i].length, "wrong number of rr");
      Validate.isTrue(_nExpiries == strangle[i].length, "wrong number of bf");
    }
    _forwards = forwards;
    _expiries = expiries;
    _forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(_expiries, _forwards, EXTRAPOLATOR));
    _strikes = new double[_nExpiries][];
    _vols = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      final SmileDeltaParameter cal = new SmileDeltaParameter(_expiries[i], atms[i], deltas,
          new double[] {riskReversals[0][i], riskReversals[1][i] }, new double[] {strangle[0][i], strangle[1][i] });
      _strikes[i] = cal.getStrike(_forwards[i]);
      _vols[i] = cal.getVolatility();
    }
    _isCallData = isCallData;
    checkVolatilities(expiries, _vols);
  }

  public ForexSmileDeltaSurfaceDataBundle(final ForwardCurve forwardCurve, final double[] expiries, final double[] deltas, final double[] atms, final double[][] riskReversals,
      final double[][] strangle, final boolean isCallData) {
    Validate.notNull(deltas, "null delta");
    Validate.notNull(forwardCurve, "null forward curve");
    Validate.notNull(expiries, "null expiries");
    Validate.notNull(atms, "null atms");
    Validate.notNull(riskReversals, "null rr");
    Validate.notNull(strangle, "null bf");
    _nExpiries = expiries.length;
    Validate.isTrue(_nExpiries == atms.length, "atms wrong length");
    final int n = deltas.length;
    Validate.isTrue(n > 0, "need at lest one delta");
    Validate.isTrue(n == riskReversals.length, "wrong number of rr sets");
    Validate.isTrue(n == strangle.length, "wrong number of bf sets");
    for (int i = 0; i < n; i++) {
      Validate.isTrue(_nExpiries == riskReversals[i].length, "wrong number of rr");
      Validate.isTrue(_nExpiries == strangle[i].length, "wrong number of bf");
    }
    _forwards = new double[_nExpiries];
    _expiries = expiries;
    _forwardCurve = forwardCurve;
    _strikes = new double[_nExpiries][];
    _vols = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      _forwards[i] = forwardCurve.getForward(_expiries[i]);
      final SmileDeltaParameter cal = new SmileDeltaParameter(_expiries[i], atms[i], deltas,
          new double[] {riskReversals[0][i], riskReversals[1][i] }, new double[] {strangle[0][i], strangle[1][i] });
      _strikes[i] = cal.getStrike(_forwards[i]);
      _vols[i] = cal.getVolatility();
    }
    _isCallData = isCallData;
    checkVolatilities(expiries, _vols);
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
    return _vols;
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
    final double[][] strikes = getStrikes();
    final int nStrikes = strikes[expiryIndex].length;
    Validate.isTrue(strikeIndex >= 0 && strikeIndex < nStrikes, "strike index out of range");
    final double[][] vols = new double[nStrikes][];
    for (int i = 0; i < _nExpiries; i++) {
      System.arraycopy(_vols[i], 0, vols[i], 0, nStrikes);
    }
    vols[expiryIndex][strikeIndex] += amount;
    return new StandardSmileSurfaceDataBundle(getForwardCurve(), getExpiries(), getStrikes(), vols, isCallData());
  }
}
