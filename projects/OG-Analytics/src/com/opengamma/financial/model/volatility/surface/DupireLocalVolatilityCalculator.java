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
  private final double _eps = 1e-5;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * @deprecated As its name suggests this is purely for debugging and will be removed
   */
  @Deprecated
  public void debug(final PriceSurface price, final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate, final double t, final double k) {

    double cdT = getFirstTimeDev(price.getSurface(), t, k);
    double cdK = getFirstStrikeDev(price.getSurface(), t, k);
    double cdKK = getSecondStrikeDev(price.getSurface(), t, k);

    double sigmadT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k);
    double sigmadK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k);
    double sigmadKK = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k);

    double vol = impliedVolatilitySurface.getVolatility(t, k);
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

  public LocalVolatilitySurface getLocalVolatility(final PriceSurface price, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double t = x[0];
        double k = x[1];

        double divT = getFirstTimeDev(price.getSurface(), t, k);
        double divK = getFirstStrikeDev(price.getSurface(), t, k);
        double divK2 = getSecondStrikeDev(price.getSurface(), t, k);

        double var = 2. * (divT + rate * k * divK) / (k * k * divK2);
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  public LocalVolatilitySurface getLocalVolatility(final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        double t = x[0];
        double k = x[1];
        double vol = impliedVolatilitySurface.getVolatility(t, k);
        if (t == 0) {
          return vol;
        }
        double rootT = Math.sqrt(t);
        double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k);
        double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k);
        double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k);
        double d1 = (Math.log(spot / k) + (rate + vol * vol / 2) * t) / vol / rootT;
        double d2 = d1 - vol * rootT;
        double var = (vol * vol + 2 * vol * t * (divT + rate * k * divK)) / (1 + 2 * d1 * k * rootT * divK + k * k * t * (d1 * d2 * divK * divK + vol * divK2));
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  private double getFirstTimeDev(final Surface<Double, Double, Double> surface, final double t, final double k) {
    double up = surface.getZValue(t + _eps, k);
    double down = surface.getZValue(t - _eps, k);
    return (up - down) / 2. / _eps;
  }

  private double getFirstStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k) {
    double up = surface.getZValue(t, k + _eps);
    double down = surface.getZValue(t, k - _eps);
    return (up - down) / 2. / _eps;
  }

  private double getSecondStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k) {
    double up = surface.getZValue(t, k + _eps);
    double cent = surface.getZValue(t, k);
    double down = surface.getZValue(t, k - _eps);
    return (up + down - 2 * cent) / _eps / _eps;
  }

}
