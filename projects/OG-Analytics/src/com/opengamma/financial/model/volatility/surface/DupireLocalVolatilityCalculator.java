/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

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
   */
  @Deprecated
  public void debug(final PriceSurface priceSurface, final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate, final double t, final double k) {

    double vol = impliedVolatilitySurface.getVolatility(t, k);
    double price = priceSurface.getPrice(t, k);
    double cdT = getFirstTimeDev(priceSurface.getSurface(), t, k, price);
    double cdK = getFirstStrikeDev(priceSurface.getSurface(), t, k, price, spot);
    double cdKK = getSecondStrikeDev(priceSurface.getSurface(), t, k, price, spot);

    double sigmadT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k, vol);
    double sigmadK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, spot);
    double sigmadKK = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, spot);

    double d1 = (Math.log(spot / k) + (rate + vol * vol / 2) * t) / vol / Math.sqrt(t);
    double d2 = d1 - vol * Math.sqrt(t);
    double nd2 = NORMAL.getCDF(d2);
    double nPrimed2 = NORMAL.getPDF(d2);
    double df = Math.exp(-rate * t);
    double delta = -df * nd2;
    double gamma = df * nPrimed2 / k / vol / Math.sqrt(t);
    double vega = Math.sqrt(t) * k * df * nPrimed2;
    double vanna = df * d1 * nPrimed2 / vol;
    double vomma = df * k * nPrimed2 * Math.sqrt(t) * d1 * d2 / vol;
    double theta = df * k * (rate * nd2 + nPrimed2 * vol / 2 / Math.sqrt(t));

    double cdTStar = theta + vega * sigmadT;
    double cdKStar = delta + vega * sigmadK;
    double cdKKStar = gamma + 2 * vanna * sigmadK + +vomma * sigmadK * sigmadK + vega * sigmadKK;

    Validate.isTrue(Math.abs((cdT - cdTStar) / cdT) < 1e-5, "theta");
    Validate.isTrue(Math.abs((cdK - cdKStar) / cdK) < 1e-5, "delta");
    Validate.isTrue(Math.abs((cdKK - cdKKStar) / cdKK) < 1e-5, "gamma");

  }

  public LocalVolatilitySurface getLocalVolatility(final PriceSurface priceSurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double t = x[0];
        double k = x[1];

        double price = priceSurface.getPrice(t, k);
        double divT = getFirstTimeDev(priceSurface.getSurface(), t, k, price);
        double divK = getFirstStrikeDev(priceSurface.getSurface(), t, k, price, spot);
        double divK2 = getSecondStrikeDev(priceSurface.getSurface(), t, k, price, spot);

        double var = 2. * (divT + rate * k * divK) / (k * k * divK2);
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  public AbsoluteLocalVolatilitySurface getAbsoluteLocalVolatilitySurface(final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double t = x[0];
        double s = x[1];

        double vol = impliedVolatilitySurface.getVolatility(t, s);
        if (t == 0 && s == spot) {
          return vol;
        }
        //  double rootT = Math.sqrt(t);
        double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, s, vol);
        double var;
        if (s == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          double d1 = (Math.log(spot / s) + (rate + vol * vol / 2) * t) / vol;
          double d2 = d1 - vol * t;
          var = (vol * vol + 2 * vol * t * (divT + rate * s * divK)) / (1 + 2 * d1 * s * divK + s * s * (d1 * d2 * divK * divK + t * vol * divK2));
          if (var < 0.0) {
            var = 0.0;
            //TODO log error
          }
        }
        return s * Math.sqrt(var);
      }
    };

    return new AbsoluteLocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  public LocalVolatilitySurface getLocalVolatility(final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double t = x[0];
        double s = x[1];

        double vol = impliedVolatilitySurface.getVolatility(t, s);
        if (t == 0 && s == spot) {
          return vol;
        }
        //  double rootT = Math.sqrt(t);
        double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, s, vol);
        double var;
        if (s == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          double d1 = (Math.log(spot / s) + (rate + vol * vol / 2) * t) / vol;
          double d2 = d1 - vol * t;
          var = (vol * vol + 2 * vol * t * (divT + rate * s * divK)) / (1 + 2 * d1 * s * divK + s * s * (d1 * d2 * divK * divK + t * vol * divK2));
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

  private double getFirstTimeDev(final Surface<Double, Double, Double> surface, final double t, final double k, final double mid) {
    if (t == 0) {
      double up = surface.getZValue(_eps, k);
      return (up - mid) / _eps;
    }
    double up = surface.getZValue(t + _eps, k);
    double down = surface.getZValue(t - _eps, k);
    return (up - down) / 2. / _eps;
  }

  private double getFirstStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k, final double mid, final double spot) {
    final double eps = spot * _eps;
    if (k <= eps) {
      double up = surface.getZValue(t, k + eps);
      return (up - mid) / eps;
    }
    double up = surface.getZValue(t, k + eps);
    double down = surface.getZValue(t, k - eps);
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
    double up = surface.getZValue(t, k + eps + offset);
    double down = surface.getZValue(t, k - eps + offset);
    return (up + down - 2 * cent) / eps / eps;
  }

}
