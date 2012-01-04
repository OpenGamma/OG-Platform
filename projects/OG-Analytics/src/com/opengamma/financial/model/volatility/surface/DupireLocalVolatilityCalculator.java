/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class DupireLocalVolatilityCalculator {
  private final double _eps;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public DupireLocalVolatilityCalculator() {
    _eps = 1e-5;
  }

  public DupireLocalVolatilityCalculator(final double tol) {
    _eps = tol;
  }

  /**
   * @deprecated As its name suggests this is purely for debugging and will be removed
   * @param priceSurface The price surface
   * @param impliedVolatilitySurface The implied volatility surface
   * @param spot The spot value
   * @param rate The rate
   * @param t The time
   * @param k The strike
   */
  @Deprecated
  public void debug(final PriceSurface priceSurface, final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate, final double t, final double k) {

    final double vol = impliedVolatilitySurface.getVolatility(t, k);
    final double price = priceSurface.getPrice(t, k);
    final double cdT = getFirstTimeDev(priceSurface.getSurface(), t, k, price);
    final double cdK = getFirstStrikeDev(priceSurface.getSurface(), t, k, price, spot);
    final double cdKK = getSecondStrikeDev(priceSurface.getSurface(), t, k, price, spot);

    final double sigmadT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k, vol);
    final double sigmadK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, spot);
    final double sigmadKK = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, spot);

    final double d1 = (Math.log(spot / k) + (rate + vol * vol / 2) * t) / vol / Math.sqrt(t);
    final double d2 = d1 - vol * Math.sqrt(t);
    final double nd2 = NORMAL.getCDF(d2);
    final double nPrimed2 = NORMAL.getPDF(d2);
    final double df = Math.exp(-rate * t);
    final double delta = -df * nd2;
    final double gamma = df * nPrimed2 / k / vol / Math.sqrt(t);
    final double vega = Math.sqrt(t) * k * df * nPrimed2;
    final double vanna = df * d1 * nPrimed2 / vol;
    final double vomma = df * k * nPrimed2 * Math.sqrt(t) * d1 * d2 / vol;
    final double theta = df * k * (rate * nd2 + nPrimed2 * vol / 2 / Math.sqrt(t));

    final double cdTStar = theta + vega * sigmadT;
    final double cdKStar = delta + vega * sigmadK;
    final double cdKKStar = gamma + 2 * vanna * sigmadK + +vomma * sigmadK * sigmadK + vega * sigmadKK;

    Validate.isTrue(Math.abs((cdT - cdTStar) / cdT) < 1e-5, "theta");
    Validate.isTrue(Math.abs((cdK - cdKStar) / cdK) < 1e-5, "delta");
    Validate.isTrue(Math.abs((cdKK - cdKKStar) / cdKK) < 1e-5, "gamma");

  }

  /**
   * Classic Dupire local volatility formula
   * @param priceSurface present value (i.e. discounted) value of options on underlying at various expiries and strikes
   * @param spot The current value of the underlying
   * @param r The risk free rate (or domestic rate in FX)
   * @param q The dividend yield (or foreign rate in FX)
   * @return The local volatility surface
   */
  public LocalVolatilitySurface getLocalVolatility(final PriceSurface priceSurface, final double spot, final double r, final double q) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];

        final double price = priceSurface.getPrice(t, k);
        final double divT = getFirstTimeDev(priceSurface.getSurface(), t, k, price);
        final double divK = getFirstStrikeDev(priceSurface.getSurface(), t, k, price, spot);
        final double divK2 = getSecondStrikeDev(priceSurface.getSurface(), t, k, price, spot);
        final double var = 2. * (divT + q * price + (r - q) * k * divK) / (k * k * divK2);
        if (var < 0) {
          throw new MathException("Negative var in getLocalVolatility");
        }
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  public AbsoluteLocalVolatilitySurface getAbsoluteLocalVolatilitySurface(final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double s = x[1];

        final double vol = impliedVolatilitySurface.getVolatility(t, s);
        if (t == 0 && s == spot) {
          return s * vol;
        }
        //  double rootT = Math.sqrt(t);
        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, s, vol);
        double var;
        if (s == 0) {
          var = 0.0;
        } else {
          final double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          final double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          final double h1 = (Math.log(spot / s) + (rate + vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * (divT + rate * s * divK)) / (1 + 2 * h1 * s * divK + s * s * (h1 * h2 * divK * divK + t * vol * divK2));
          if (var < 0.0) {
            throw new MathException("negative variance");
            //var = 0.0;
            //TODO log error
          }
        }
        return s * Math.sqrt(var);
      }
    };

    return new AbsoluteLocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  /**
   * Local vol in terms of
   * @param impliedVolatilitySurface
   * @param spot
   * @param rate
   * @return
   * @deprecated don't use
   */
  @Deprecated
  public LocalVolatilitySurface getLocalVolatility(final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double s = x[1];

        final double vol = impliedVolatilitySurface.getVolatility(t, s);
        if (t == 0 && s == spot) {
          return vol;
        }
        //  double rootT = Math.sqrt(t);
        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, s, vol);
        double var;
        if (s == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          final double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          final double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          final double h1 = (Math.log(spot / s) + (rate + vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * (divT + rate * s * divK)) / (1 + 2 * h1 * s * divK + s * s * (h1 * h2 * divK * divK + t * vol * divK2));
          if (var < 0.0) {
            var = 0.0;
            //TODO log error
          }
        }
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  /**
   * Get the local volatility in the case where the option price is a function of the forward price
   * @param impliedVolatilitySurface The Black implied volatility surface
   * @param forwardCurve Curve of forward prices
   * @return The local volatility
   */
  public LocalVolatilitySurface getLocalVolatility(final BlackVolatilitySurface impliedVolatilitySurface, final ForwardCurve forwardCurve) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double forward = forwardCurve.getForward(t);

        final double vol = impliedVolatilitySurface.getVolatility(t, k);
        if (t == 0) {
          return vol;
        }

        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k, vol);
        double var;
        if (k == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          final double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, forward);
          final double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, forward);
          final double h1 = (Math.log(forward / k) + (vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * divT) / (1 + 2 * h1 * k * divK + k * k * (h1 * h2 * divK * divK + t * vol * divK2));
          if (var < 0.0) {
            //  throw new MathException("negative variance");
            var = 0.0;
            //TODO log error
          }
        }
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  private double getFirstTimeDev(final Surface<Double, Double, Double> surface, final double t, final double k, final double mid) {
    if (t == 0) {
      final double up = surface.getZValue(_eps, k);
      return (up - mid) / _eps;
    }
    final double up = surface.getZValue(t + _eps, k);
    final double down = surface.getZValue(t - _eps, k);
    return (up - down) / 2. / _eps;
  }

  private double getFirstStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k, final double mid, final double spot) {
    final double eps = spot * _eps;
    if (k <= eps) {
      final double up = surface.getZValue(t, k + eps);
      return (up - mid) / eps;
    }
    final double up = surface.getZValue(t, k + eps);
    final double down = surface.getZValue(t, k - eps);
    return (up - down) / 2. / eps;
  }

  private double getSecondStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k, final double mid, final double spot) {
    final double eps = spot * _eps;
    double offset = 0;
    double cent;
    if (k <= eps) {
      offset = eps - k;
      cent = surface.getZValue(t, k + offset);
    } else {
      cent = mid;
    }
    final double up = surface.getZValue(t, k + eps + offset);
    final double down = surface.getZValue(t, k - eps + offset);
    return (up + down - 2 * cent) / eps / eps;
  }

}
