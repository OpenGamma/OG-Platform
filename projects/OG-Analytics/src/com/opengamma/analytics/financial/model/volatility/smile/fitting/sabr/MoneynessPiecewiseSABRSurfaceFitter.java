/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import static com.opengamma.analytics.math.FunctionUtils.square;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.Moneyness;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MoneynessPiecewiseSABRSurfaceFitter implements PiecewiseSABRSurfaceFitter1<Moneyness> {
  private static final PiecewiseSABRFitter FITTER = new PiecewiseSABRFitter();
  private static final Interpolator1D EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private final boolean _useLogTime;
  private final boolean _useIntegratedVariance;
  private final boolean _useLogValue;

  /**
   * @param useLogTime The x-axis is the log of time
   * @param useIntegratedVariance the y-points are integrated variance (rather than volatility)
   * @param useLogValue The y-axis values (whether they be variance or integrated variance) are logged
   */
  public MoneynessPiecewiseSABRSurfaceFitter(final boolean useLogTime, final boolean useIntegratedVariance, final boolean useLogValue) {
    _useLogTime = useLogTime;
    _useIntegratedVariance = useIntegratedVariance;
    _useLogValue = useLogValue;
  }

  /**
   * For a given expiry and strike, perform an interpolation between either the volatility or integrated variances
   *  of points with the same modified log-moneyness (d = Math.log(forward / k) / Math.sqrt(1 + lambda * t)) on the fitted smiles.
   *  The interpolation is a natural cubic spline using the four nearest points.
   *  There is no guarantees a monotonically increasing integrated variance
   * (hence no calendar arbitrage and a real positive local volatility), but using log time to better space out the x-points
   * help.
   * @param data The surface data
   * @return Implied volatility surface parameterised by time and moneyness
   */
  @Override
  public BlackVolatilitySurfaceMoneyness getVolatilitySurface(final SmileSurfaceDataBundle data) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.getExpiries().length >= 4, "Need at least four expiries; have {}", data.getExpiries().length);
    final double[] expiries = data.getExpiries();
    final double[] forwards = data.getForwards();
    final double[][] strikes = data.getStrikes();
    final double[][] impliedVols = data.getVolatilities();
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final int nExpiries = expiries.length;
    //TODO move this out of here - need a way to bump a point on the surface without having to re-fit unaffected slices
    @SuppressWarnings("unchecked")
    final Function1D<Double, Double>[] fitters = new Function1D[nExpiries];
    final double[] logExpiries = new double[nExpiries];
    for (int i = 0; i < nExpiries; i++) {
      fitters[i] = FITTER.getVolatilityFunction(forwards[i], strikes[i], expiries[i], impliedVols[i]);
      logExpiries[i] = Math.log(expiries[i]);
    }

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        //For time less than the first expiry, linearly extrapolate the variance
        if (t <= expiries[0]) {
          final double k1 = forwards[0] * m;
          final double k2 = forwards[1] * m;
          final double var1 = square(fitters[0].evaluate(k1));
          final double var2 = square(fitters[1].evaluate(k2));
          final double dt = expiries[1] - expiries[0];
          final double var = ((expiries[1] - t) * var1 + (t - expiries[0]) * var2) / dt;
          if (var >= 0.0) {
            return Math.sqrt(var);
          }
          return Math.sqrt(var1);
        }

        final int index = SurfaceArrayUtils.getLowerBoundIndex(expiries, t);

        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index == nExpiries - 2) {
          lower = index - 2;
        } else if (index == nExpiries - 1) {
          lower = index - 3;
        } else {
          lower = index - 1;
        }
        double[] xData;
        double x;
        if (_useLogTime) {
          xData = Arrays.copyOfRange(logExpiries, lower, lower + 4);
          x = Math.log(t);
        } else {
          xData = Arrays.copyOfRange(expiries, lower, lower + 4);
          x = t;
        }

        final double[] yData = new double[4];

        for (int i = 0; i < 4; i++) {
          final double time = expiries[lower + i];
          final double k = forwards[lower + i] * Math.pow(m, Math.sqrt(time / t));
          double temp = square(fitters[lower + i].evaluate(k));

          if (_useIntegratedVariance) {
            temp *= time;
          }
          if (_useLogValue) {
            temp = Math.log(temp);
          }
          yData[i] = temp;
        }

        final Interpolator1DDataBundle db = EXTRAPOLATOR.getDataBundle(xData, yData);

        double tRes = EXTRAPOLATOR.interpolate(db, x);
        if (_useLogValue) {
          tRes = Math.exp(tRes);
        }
        if (_useIntegratedVariance) {
          tRes /= t;
        }

        return Math.sqrt(tRes);
      }

      //        final double t = tm[0];
      //        final double m = tm[1];
      //        final double d = -Math.log(m) / Math.sqrt(1 + _lambda * t);
      //        if (t <= expiries[0]) {
      //          final double k1 = forwards[0] * Math.exp(-d * Math.sqrt(1 + _lambda * expiries[0]));
      //          return fitters[0].evaluate(k1);
      //        }
      //
      //        final int index = SurfaceArrayUtils.getLowerBoundIndex(expiries, t);
      //
      //        //TODO this logic doesn't always work
      //        int lower;
      //        if (index == 0) {
      //          lower = 0;
      //        } else if (index == nExpiries - 2) {
      //          lower = index - 2;
      //        } else if (index == nExpiries - 1) {
      //          lower = index - 3;
      //        } else {
      //          lower = index - 1;
      //        }
      //        final double[] times = Arrays.copyOfRange(expiries, lower, lower + 4);
      //        double[] xs = new double[4];
      //        double x = 0;
      //        if (_useLogTime) {
      //          for (int i = 0; i < 4; i++) {
      //            xs[i] = Math.log(times[i]);
      //            x = Math.log(t);
      //          }
      //        } else {
      //          xs = times;
      //          x = t;
      //        }
      //
      //        final double[] strikes = new double[4];
      //        final double[] vols = new double[4];
      //        final double[] intVar = new double[4];
      //        double[] y = null;
      //
      //        for (int i = 0; i < 4; i++) {
      //          strikes[i] = forwards[lower + i] * Math.exp(-d * Math.sqrt(1 + _lambda * times[i]));
      //          vols[i] = fitters[lower + i].evaluate(strikes[i]);
      //
      //          intVar[i] = vols[i] * vols[i] * times[i];
      //          if (_useIntegratedVar) {
      //            y = intVar;
      //          } else {
      //            y = vols;
      //          }
      //        }
      //
      //        final Interpolator1DDataBundle db = EXTRAPOLATOR.getDataBundle(xs, y);
      //        double sigma;
      //
      //        final double res = EXTRAPOLATOR.interpolate(db, x);
      //        if (_useIntegratedVar) {
      //          Validate.isTrue(res >= 0.0, "Negative integrated variance");
      //          sigma = Math.sqrt(res / t);
      //        } else {
      //          sigma = res;
      //        }
      //        return sigma;
      //        }

      public Object writeReplace() {
        return new InvokedSerializedForm(MoneynessPiecewiseSABRSurfaceFitter.this, "getVolatilitySurface", data);
      }
    };

    return new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(surFunc), forwardCurve);
  }

  public boolean useLogTime() {
    return _useLogTime;
  }

  public boolean useIntegratedVariance() {
    return _useIntegratedVariance;
  }

  public boolean useLogValue() {
    return _useLogValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_useIntegratedVariance ? 1231 : 1237);
    result = prime * result + (_useLogTime ? 1231 : 1237);
    result = prime * result + (_useLogValue ? 1231 : 1237);
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
    final MoneynessPiecewiseSABRSurfaceFitter other = (MoneynessPiecewiseSABRSurfaceFitter) obj;
    if (_useIntegratedVariance != other._useIntegratedVariance) {
      return false;
    }
    if (_useLogTime != other._useLogTime) {
      return false;
    }
    if (_useLogValue != other._useLogValue) {
      return false;
    }
    return true;
  }

}
