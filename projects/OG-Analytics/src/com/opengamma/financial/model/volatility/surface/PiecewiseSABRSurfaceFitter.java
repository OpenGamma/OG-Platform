/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.volatility.smile.fitting.PiecewiseSABRFitter;
import com.opengamma.math.MathException;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.MonotonicIncreasingInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class PiecewiseSABRSurfaceFitter {

  private static Interpolator1D EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private final ForwardCurve _forwardCurve;
  private final double[] _forwards;
  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[] _atmVols;
  private final double[][] _vols;
  private final int _nExpiries;

  private final PiecewiseSABRFitter[] _fitters;

  public PiecewiseSABRSurfaceFitter(final double[] deltas, final double[] forwards, final double[] expiries,
      final double[] atms, final double[][] riskReversals, final double[][] strangle) {
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
    _atmVols = atms;
    for (int i = 0; i < _nExpiries; i++) {
      SmileDeltaParameter cal = new SmileDeltaParameter(_expiries[i], atms[i], deltas,
          new double[] {riskReversals[0][i], riskReversals[1][i] }, new double[] {strangle[0][i], strangle[1][i] });
      _strikes[i] = cal.getStrike(_forwards[i]);
      _vols[i] = cal.getVolatility();
    }

    checkVols();

    //fit each time slice with piecewise SABR
    _fitters = new PiecewiseSABRFitter[_nExpiries];
    for (int i = 0; i < _nExpiries; i++) {
      _fitters[i] = new PiecewiseSABRFitter(_forwards[i], _strikes[i], _expiries[i], _vols[i]);
    }

    checkMoneyness();
  }

  private void checkVols() {
    final int n = _vols[0].length;
    for (int i = 0; i < n; i++) {
      double[] intVar = new double[_nExpiries];
      for (int j = 0; j < _nExpiries; j++) {
        double vol = _vols[j][i];
        intVar[j] = vol * vol * _expiries[j];
        if (j > 0) {
          Validate.isTrue(intVar[j] >= intVar[j - 1], "integrated variance not increasing");
        }
      }
    }
  }

  private void checkMoneyness() {
    double xMin = 2 * Math.log(_strikes[0][0] / _forwards[0]) / Math.sqrt(_expiries[0]);
    double xMax = 2 * Math.log(_strikes[0][_strikes[0].length - 1] / _forwards[0]) / Math.sqrt(_expiries[0]);
    for (int i = 0; i < 101; i++) {
      double x = xMin + (xMax - xMin) * i / 100;
      double[] intVar = new double[_nExpiries];
      for (int j = 0; j < _nExpiries; j++) {
        double k = _forwards[j] * Math.exp(Math.sqrt(_expiries[0]) * x);
        double vol = _fitters[j].getVol(k);
        intVar[j] = vol * vol * _expiries[j];
        if (j > 0) {
          Validate.isTrue(intVar[j] >= intVar[j - 1], "trouble in x space");
        }
      }
    }
  }

  private PiecewiseSABRSurfaceFitter(final PiecewiseSABRSurfaceFitter from) {
    _nExpiries = from._nExpiries;
    _forwards = Arrays.copyOf(from._forwards, from._nExpiries);
    _expiries = Arrays.copyOf(from._expiries, from._nExpiries);
    _forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(_expiries, _forwards, EXTRAPOLATOR));
    _atmVols = Arrays.copyOf(from._atmVols, from._nExpiries);
    _strikes = new double[_nExpiries][];
    _vols = new double[_nExpiries][];
    _fitters = new PiecewiseSABRFitter[_nExpiries];
    for (int i = 0; i < _nExpiries; i++) {
      _strikes[i] = Arrays.copyOf(from._strikes[i], from._nExpiries);
      _vols[i] = Arrays.copyOf(from._vols[i], from._nExpiries);
      _fitters[i] = from._fitters[i]; //shallow copy of fitters
    }
  }

  public BlackVolatilitySurface getImpliedVolatilitySurface() {
    return getSurfaceMoneyness();
  }

  public PiecewiseSABRSurfaceFitter withBumpedPoint(final int expiryIndex, final int strikeIndex, final double amount) {
    Validate.isTrue(expiryIndex >= 0 && expiryIndex < _nExpiries, "expiry index out of range");
    final int nStrikes = _strikes[expiryIndex].length;
    Validate.isTrue(strikeIndex >= 0 && strikeIndex < nStrikes, "strike index out of range");

    double[] vols = new double[nStrikes];
    System.arraycopy(_vols[expiryIndex], 0, vols, 0, nStrikes);
    vols[strikeIndex] += amount;
    PiecewiseSABRFitter fitters = new PiecewiseSABRFitter(_forwards[expiryIndex], _strikes[expiryIndex], _expiries[expiryIndex], vols);

    PiecewiseSABRSurfaceFitter res = new PiecewiseSABRSurfaceFitter(this);
    res._fitters[expiryIndex] = fitters;
    res._vols[expiryIndex] = vols;

    return res;
  }

  private BlackVolatilitySurface getSurfaceLinear() {
    Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];
        if (t <= _expiries[0]) { //linear extrapolation in sigma
          double sigma1 = _fitters[0].getVol(k);
          double sigma2 = _fitters[1].getVol(k);
          double dt = _expiries[1] - _expiries[0];
          return ((_expiries[1] - t) * sigma1 + (t - _expiries[0]) * sigma2) / dt;
        }
        if (t >= _expiries[_nExpiries - 1]) { //flat extrapolation
          return _fitters[_nExpiries - 1].getVol(k);
        }

        int index = getLowerBoundIndex(t);
        double[] sample = new double[2];
        double[] times = new double[2];
        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index >= _nExpiries - 1) {
          lower = index - 1;
        } else {
          lower = index;
        }
        for (int i = 0; i < 2; i++) {
          double vol = _fitters[i + lower].getVol(k);
          sample[i] = vol * vol * _expiries[i + lower]; //interpolate the variance
          if (i > 0) {
            Validate.isTrue(sample[i] >= sample[i - 1], "variance must increase");
          }
        }
        times = Arrays.copyOfRange(_expiries, lower, lower + 2);

        double dt = times[1] - times[0];
        double var = ((times[1] - t) * sample[0] + (t - times[0]) * sample[1]) / dt;

        // double var = INTERPOLATOR_1D.interpolate(db, t);
        if (var >= 0) {
          return Math.sqrt(var / t);
        } else {
          throw new MathException("negative var " + var);
        }
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(surFunc));
  }

  private double interpolatedATMVol(final double t) {

    if (t <= _expiries[0]) { //
      return _atmVols[0];
    }
    if (t >= _expiries[_nExpiries - 1]) {
      return _atmVols[_nExpiries - 1];
    }

    int index = getLowerBoundIndex(t);

    double[] sample = new double[2];
    double[] times = new double[2];
    int lower;
    if (index == 0) {
      lower = 0;
    } else if (index >= _nExpiries - 1) {
      lower = index - 1;
    } else {
      lower = index;
    }
    for (int i = 0; i < 2; i++) {
      double vol = _atmVols[i + lower]; //TODO this can be done in constructor
      sample[i] = vol * vol * _expiries[i + lower]; //interpolate the variance
      if (i > 0) {
        Validate.isTrue(sample[i] >= sample[i - 1], "variance must increase");
      }
    }
    times = Arrays.copyOfRange(_expiries, lower, lower + 2);

    double dt = times[1] - times[0];
    double var = ((times[1] - t) * sample[0] + (t - times[0]) * sample[1]) / dt;

    // double var = INTERPOLATOR_1D.interpolate(db, t);
    if (var >= 0) {
      return Math.sqrt(var / t);
    } else {
      throw new MathException("negative var " + var);
    }
  }

  private double getD1(final double f, final double k, final double sigma, final double t) {
    final double sigmaRootT = sigma * Math.sqrt(t);
    return Math.log(f / k) / sigmaRootT + 0.5 * sigmaRootT;
  }

  private double getStrike(final double d1,
      final double f1, final double sigma1, final double t1) {
    return f1 * Math.exp(-d1 * sigma1 * Math.sqrt(t1) + sigma1 * sigma1 * t1);
  }

  private BlackVolatilitySurface getSurfaceDelta() {
    Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];

        final double atmVol = interpolatedATMVol(t);
        final double forward = _forwardCurve.getForward(t);

        if (t < 1e-6) {
          t = 1e-6;
        }

        final double d1 = getD1(forward, k, atmVol, t);

        if (t <= _expiries[0]) {
          double k1 = getStrike(d1, _forwards[0], _atmVols[0], _expiries[0]);
          return _fitters[0].getVol(k1);
        }
        if (t >= _expiries[_nExpiries - 1]) {
          double k1 = getStrike(d1, _forwards[_nExpiries - 1], _atmVols[_nExpiries - 1], _expiries[_nExpiries - 1]);
          return _fitters[_nExpiries - 1].getVol(k1);
        }

        int index = getLowerBoundIndex(t);

        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index >= _nExpiries - 1) {
          lower = index - 1;
        } else {
          lower = index;
        }

        final double t1 = _expiries[lower];
        final double t2 = _expiries[lower + 1];
        double k1 = getStrike(d1, _forwards[lower], _atmVols[lower], t1);
        double k2 = getStrike(d1, _forwards[lower + 1], _atmVols[lower + 1], t2);

        double vol1 = _fitters[lower].getVol(k1);
        double vol2 = _fitters[lower + 1].getVol(k2);

        double intVar1 = vol1 * vol1 * t1;
        double intVar2 = vol2 * vol2 * t2;

        Validate.isTrue(intVar2 > intVar1, "variance must increase");

        double dt = t2 - t1;
        double var = ((t2 - t) * intVar1 + (t - t1) * intVar2) / dt;

        if (var >= 0) {
          return Math.sqrt(var / t);
        } else {
          throw new MathException("negative var " + var);
        }
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(surFunc));
  }

  private BlackVolatilitySurface getSurfaceMoneyness() {
    final double eps = 1e-4;

    Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];

        final double forward = _forwardCurve.getForward(t);

        //        if (t < 1e-6) {
        //          t = 1e-6;
        //        }

        if (t <= _expiries[0]) {
          //         double k1 = _forwards[0] * Math.exp(Math.log(k / forward) * Math.sqrt((_expiries[0] + eps) / (t + eps)));
          return _fitters[0].getVol(k);
        }
        if (t >= _expiries[_nExpiries - 1]) {
          double k1 = _forwards[_nExpiries - 1] * Math.exp(Math.log(k / forward) * Math.sqrt(_expiries[_nExpiries - 1] / t));
          return _fitters[_nExpiries - 1].getVol(k1);
        }

        final double x = Math.log(k / forward) / Math.sqrt(t);

        int index = getLowerBoundIndex(t);

        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index >= _nExpiries - 1) {
          lower = index - 1;
        } else {
          lower = index;
        }

        final double t1 = _expiries[lower];
        final double t2 = _expiries[lower + 1];
        double k1 = _forwards[lower] * Math.exp(Math.sqrt(t1) * x);
        double k2 = _forwards[lower + 1] * Math.exp(Math.sqrt(t2) * x);

        double vol1 = _fitters[lower].getVol(k1);
        double vol2 = _fitters[lower + 1].getVol(k2);

        double intVar1 = vol1 * vol1 * t1;
        double intVar2 = vol2 * vol2 * t2;

        Validate.isTrue(intVar2 >= intVar1, "variance must increase");

        double dt = t2 - t1;
        double var = ((t2 - t) * intVar1 + (t - t1) * intVar2) / dt;

        if (var >= 0) {
          return Math.sqrt(var / t);
        } else {
          throw new MathException("negative var " + var);
        }
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(surFunc));
  }

  private BlackVolatilitySurface getSurfaceMoneynessMonotonic() {
    final double eps = 1e-4;
    final Interpolator1D interpolator = new MonotonicIncreasingInterpolator1D();

    Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];

        final double forward = _forwardCurve.getForward(t);

        //        if (t < 1e-6) {
        //          t = 1e-6;
        //        }

        if (t <= _expiries[0]) {
          //         double k1 = _forwards[0] * Math.exp(Math.log(k / forward) * Math.sqrt((_expiries[0] + eps) / (t + eps)));
          return _fitters[0].getVol(k);
        }
        if (t >= _expiries[_nExpiries - 1]) {
          double k1 = _forwards[_nExpiries - 1] * Math.exp(Math.log(k / forward) * Math.sqrt(_expiries[_nExpiries - 1] / t));
          return _fitters[_nExpiries - 1].getVol(k1);
        }

        final double x = Math.log(k / forward) / Math.sqrt(t);

        int index = getLowerBoundIndex(t);

        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index >= _nExpiries - 1) {
          lower = index - 1;
        } else {
          lower = index;
        }

        double[] invars = new double[_nExpiries];
        for (int i = 0; i < _nExpiries; i++) {
          double strike = _forwards[i] * Math.exp(Math.sqrt(_expiries[i]) * x);
          double vol = _fitters[lower].getVol(strike);
          invars[i] = vol * vol * _expiries[i];
          if (i > 0) {
            Validate.isTrue(invars[i] >= invars[i - 1], "variance must increase");
          }
        }

        if (t > 4 && x < 0.5 && x > -0.5) {
          System.out.println("debug");
        }

        Interpolator1DDataBundle db = interpolator.getDataBundle(_expiries, invars);
        double var = interpolator.interpolate(db, t);

        if (var >= 0) {
          return Math.sqrt(var / t);
        } else {
          throw new MathException("negative var " + var);
        }
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(surFunc));
  }

  private BlackVolatilitySurface getSurfaceDoubleQuad() {
    Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];

        final double atmVol = interpolatedATMVol(t);
        final double forward = _forwardCurve.getForward(t);

        //        if (t > 1.0) {
        //          System.out.println("debug");
        //        }

        if (t <= _expiries[0]) {
          // double k1 = getStrike(d1, _forwards[0], _atmVols[0], _expiries[0]);
          return _fitters[0].getVol(k);
        }

        final double d1 = getD1(forward, k, atmVol, t);
        if (t >= _expiries[_nExpiries - 1]) {
          double k1 = getStrike(d1, _forwards[_nExpiries - 1], _atmVols[_nExpiries - 1], _expiries[_nExpiries - 1]);
          return _fitters[_nExpiries - 1].getVol(k1);
        }

        int index = getLowerBoundIndex(t);

        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index == _nExpiries - 2) {
          lower = index - 2;
        } else if (index == _nExpiries - 1) {
          lower = index - 3;
        } else {
          lower = index - 1;
        }

        final double[] times = Arrays.copyOfRange(_expiries, lower, lower + 4);
        final double[] forwards = Arrays.copyOfRange(_forwards, lower, lower + 4);
        final double[] atm = Arrays.copyOfRange(_atmVols, lower, lower + 4);
        final double[] strikes = new double[4];
        final double[] vols = new double[4];
        // final double[] intVar = new double[4];

        for (int i = 0; i < 4; i++) {
          strikes[i] = getStrike(d1, forwards[i], atm[i], times[i]);
          vols[i] = _fitters[lower + i].getVol(strikes[i]);
          //          intVar[i] = vols[i]*vols[i]*times[i];
          //          if(i>0) {
          //            Validate.isTrue(intVar[i] > intVar[i-1], "variance must increase");
          //          }
        }

        Interpolator1DDataBundle db = EXTRAPOLATOR.getDataBundle(times, vols);
        double sigma = EXTRAPOLATOR.interpolate(db, t);
        return sigma;
      }
    };

    return new BlackVolatilitySurface(FunctionalDoublesSurface.from(surFunc));
  }

  private int getLowerBoundIndex(final double t) {
    if (t < _expiries[0]) {
      return 0;
    }
    if (t > _expiries[_nExpiries - 1]) {
      return _nExpiries - 1;
    }

    int index = Arrays.binarySearch(_expiries, t);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
  }

}
