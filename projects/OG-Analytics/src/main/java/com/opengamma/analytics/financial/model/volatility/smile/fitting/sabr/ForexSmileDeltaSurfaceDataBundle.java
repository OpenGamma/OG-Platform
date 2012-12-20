/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForexSmileDeltaSurfaceDataBundle extends SmileSurfaceDataBundle {
  private final double[] _forwards;
  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[][] _vols;
  private final ForwardCurve _forwardCurve;
  private final int _nExpiries;

  private final boolean _isCallData;

  public ForexSmileDeltaSurfaceDataBundle(final double[] forwards, final double[] expiries, final double[] deltas, final double[] atms, final double[][] riskReversals,
      final double[][] strangle, final boolean isCallData, final CombinedInterpolatorExtrapolator interpolator) {
    ArgumentChecker.notNull(deltas, "delta");
    ArgumentChecker.notNull(forwards, "forwards");
    ArgumentChecker.notNull(expiries, "expiries");
    ArgumentChecker.notNull(atms, "at-the-money");
    ArgumentChecker.notNull(riskReversals, "risk reversal");
    ArgumentChecker.notNull(strangle, "strangle");
    _nExpiries = expiries.length;
    ArgumentChecker.isTrue(_nExpiries == forwards.length, "forwards wrong length; have {}, need {}", forwards.length, _nExpiries);
    ArgumentChecker.isTrue(_nExpiries == atms.length, "atms wrong length; have {}, need {}", atms.length, _nExpiries);
    final int n = deltas.length;
    ArgumentChecker.isTrue(n > 0, "need at least one delta");
    ArgumentChecker.isTrue(n == riskReversals.length, "wrong number of rr sets; have {}, need {}", riskReversals.length, n);
    ArgumentChecker.isTrue(n == strangle.length, "wrong number of strangle sets; have {}, need {}", strangle.length, n);
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(_nExpiries == riskReversals[i].length, "wrong number of rr; have {}, need {}", riskReversals[i].length, _nExpiries);
      ArgumentChecker.isTrue(_nExpiries == strangle[i].length, "wrong number of strangles; have {}, need {}", strangle[i].length, _nExpiries);
    }
    _forwards = forwards;
    _expiries = expiries;
    _forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(_expiries, _forwards, interpolator));
    _strikes = new double[_nExpiries][];
    _vols = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      final double[] rr = new double[n];
      final double[] s = new double[n];
      for (int j = 0; j < n; j++) {
        rr[j] = riskReversals[j][i];
        s[j] = strangle[j][i];
      }
      final SmileDeltaParameters cal = new SmileDeltaParameters(_expiries[i], atms[i], deltas, rr, s);
      _strikes[i] = cal.getStrike(_forwards[i]);
      _vols[i] = cal.getVolatility();
    }
    _isCallData = isCallData;
    checkVolatilities(expiries, _vols);
  }

  public ForexSmileDeltaSurfaceDataBundle(final ForwardCurve forwardCurve, final double[] expiries, final double[] deltas, final double[] atms, final double[][] riskReversals,
      final double[][] strangle, final boolean isCallData) {
    ArgumentChecker.notNull(deltas, "delta");
    ArgumentChecker.notNull(forwardCurve, "forward curve");
    ArgumentChecker.notNull(expiries, "expiries");
    ArgumentChecker.notNull(atms, "atms");
    ArgumentChecker.notNull(riskReversals, "risk reversals");
    ArgumentChecker.notNull(strangle, "strangle");
    _nExpiries = expiries.length;
    ArgumentChecker.isTrue(_nExpiries == atms.length, "atms wrong length; have {}, need {}", atms.length, _nExpiries);
    final int n = deltas.length;
    ArgumentChecker.isTrue(n > 0, "need at least one delta");
    ArgumentChecker.isTrue(n == riskReversals.length, "wrong number of rr sets; have {}, need {}", riskReversals.length, n);
    ArgumentChecker.isTrue(n == strangle.length, "wrong number of strangle sets; have {}, need {}", strangle.length, n);
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(_nExpiries == riskReversals[i].length, "wrong number of rr; have {}, need {}", riskReversals[i].length, _nExpiries);
      ArgumentChecker.isTrue(_nExpiries == strangle[i].length, "wrong number of strangles; have {}, need {}", strangle[i].length, _nExpiries);
    }
    _forwards = new double[_nExpiries];
    _expiries = expiries;
    _forwardCurve = forwardCurve;
    _strikes = new double[_nExpiries][];
    _vols = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      _forwards[i] = forwardCurve.getForward(_expiries[i]);
      final double[] rr = new double[n];
      final double[] s = new double[n];
      for (int j = 0; j < n; j++) {
        rr[j] = riskReversals[j][i];
        s[j] = strangle[j][i];
      }
      final SmileDeltaParameters cal = new SmileDeltaParameters(_expiries[i], atms[i], deltas, rr, s);
      _strikes[i] = cal.getStrike(_forwards[i]);
      _vols[i] = cal.getVolatility();
    }
    _isCallData = isCallData;
    checkVolatilities(expiries, _vols);
  }

  public ForexSmileDeltaSurfaceDataBundle(final ForwardCurve forwardCurve, final double[] expiries, final double[][] strikes, final double[][] vols, final boolean isCallData) {
    ArgumentChecker.notNull(forwardCurve, "forward curve");
    ArgumentChecker.notNull(expiries, "expiries");
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(vols, "vols");
    _nExpiries = expiries.length;
    ArgumentChecker.isTrue(_nExpiries == strikes.length, "strikes wrong length; have {}, need {}", strikes.length, _nExpiries);
    ArgumentChecker.isTrue(_nExpiries == vols.length, "implied vols wrong length; have {}, need {}", vols.length, _nExpiries);
    for (int i = 0; i < _nExpiries; i++) {
      ArgumentChecker.isTrue(strikes[i].length == vols[i].length, "wrong number of volatilities; have {}, need {}", strikes[i].length, vols[i].length);
    }
    _forwardCurve = forwardCurve;
    _expiries = expiries;
    _strikes = strikes;
    _vols = vols;
    _forwards = new double[_nExpiries];
    for (int i = 0; i < _nExpiries; i++) {
      _forwards[i] = forwardCurve.getForward(expiries[i]);
    }
    _isCallData = isCallData;
  }

  @Override
  public int getNumExpiries() {
    return _nExpiries;
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
  public SmileSurfaceDataBundle withBumpedPoint(final int expiryIndex, final int strikeIndex, final double amount) {
    ArgumentChecker.isTrue(ArgumentChecker.isInRangeExcludingHigh(0, _nExpiries, expiryIndex), "Invalid index for expiry; {}", expiryIndex);
    final double[][] strikes = getStrikes();
    ArgumentChecker.isTrue(ArgumentChecker.isInRangeExcludingHigh(0, strikes[expiryIndex].length, strikeIndex), "Invalid index for strike; {}", strikeIndex);
    final int nStrikes = strikes[expiryIndex].length;
    final double[][] vols = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      vols[i] = new double[nStrikes];
      System.arraycopy(_vols[i], 0, vols[i], 0, nStrikes);
    }
    vols[expiryIndex][strikeIndex] += amount;
    return new ForexSmileDeltaSurfaceDataBundle(getForwardCurve(), getExpiries(), getStrikes(), vols, _isCallData);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _forwardCurve.hashCode();
    result = prime * result + Arrays.deepHashCode(_vols);
    result = prime * result + Arrays.deepHashCode(_strikes);
    result = prime * result + Arrays.hashCode(_expiries);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForexSmileDeltaSurfaceDataBundle other = (ForexSmileDeltaSurfaceDataBundle) obj;
    if (!ObjectUtils.equals(_forwardCurve, other._forwardCurve)) {
      return false;
    }
    if (!Arrays.equals(_expiries, other._expiries)) {
      return false;
    }
    for (int i = 0; i < _nExpiries; i++) {
      if (!Arrays.equals(_strikes[i], other._strikes[i])) {
        return false;
      }
      if (!Arrays.equals(_vols[i], other._vols[i])) {
        return false;
      }
    }

    return true;
  }
}
