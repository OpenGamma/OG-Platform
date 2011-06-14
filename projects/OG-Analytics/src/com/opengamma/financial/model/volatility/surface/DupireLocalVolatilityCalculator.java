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
   * @param price The price surface
   * @param impliedVolatilitySurface The implied volatility surface
   * @param spot The spot
   * @param rate The interest rate
   * @param t The time
   * @param k The strike
   */
  @Deprecated
  public void debug(final PriceSurface price, final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate, final double t, final double k) {

    final double cdT = getFirstTimeDev(price.getSurface(), t, k);
    final double cdK = getFirstStrikeDev(price.getSurface(), t, k);
    final double cdKK = getSecondStrikeDev(price.getSurface(), t, k);

    final double sigmadT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k);
    final double sigmadK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k);
    final double sigmadKK = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k);

    final double vol = impliedVolatilitySurface.getVolatility(t, k);
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

  public LocalVolatilitySurface getLocalVolatility(final PriceSurface price, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];

        final double divT = getFirstTimeDev(price.getSurface(), t, k);
        final double divK = getFirstStrikeDev(price.getSurface(), t, k);
        final double divK2 = getSecondStrikeDev(price.getSurface(), t, k);

        final double var = 2. * (divT + rate * k * divK) / (k * k * divK2);
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  public LocalVolatilitySurface getLocalVolatility(final BlackVolatilitySurface impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final double vol = impliedVolatilitySurface.getVolatility(t, k);
        if (t == 0) {
          return vol;
        }
        final double rootT = Math.sqrt(t);
        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k);
        final double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k);
        final double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k);
        final double d1 = (Math.log(spot / k) + (rate + vol * vol / 2) * t) / vol / rootT;
        final double d2 = d1 - vol * rootT;
        final double var = (vol * vol + 2 * vol * t * (divT + rate * k * divK)) / (1 + 2 * d1 * k * rootT * divK + k * k * t * (d1 * d2 * divK * divK + vol * divK2));
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurface(FunctionalDoublesSurface.from(locVol));
  }

  private double getFirstTimeDev(final Surface<Double, Double, Double> surface, final double t, final double k) {
    final double up = surface.getZValue(t + _eps, k);
    final double down = surface.getZValue(t - _eps, k);
    return (up - down) / 2. / _eps;
  }

  private double getFirstStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k) {
    final double up = surface.getZValue(t, k + _eps);
    final double down = surface.getZValue(t, k - _eps);
    return (up - down) / 2. / _eps;
  }

  private double getSecondStrikeDev(final Surface<Double, Double, Double> surface, final double t, final double k) {
    final double up = surface.getZValue(t, k + _eps);
    final double cent = surface.getZValue(t, k);
    final double down = surface.getZValue(t, k - _eps);
    return (up + down - 2 * cent) / _eps / _eps;
  }

}
