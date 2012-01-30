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
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class DupireLocalVolatilityCalculator {
  private final double _eps;
  //  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public DupireLocalVolatilityCalculator() {
    _eps = 1e-3;
  }

  public DupireLocalVolatilityCalculator(final double tol) {
    _eps = tol;
  }

  /**
   * Classic Dupire local volatility formula
   * @param priceSurface present value (i.e. discounted) value of options on underlying at various expiries and strikes
   * @param spot The current value of the underlying
   * @param r The risk free rate (or domestic rate in FX)
   * @param q The dividend yield (or foreign rate in FX)
   * @return The local volatility surface
   */
  public LocalVolatilitySurfaceStrike getLocalVolatility(final PriceSurface priceSurface, final double spot, final double r, final double q) {

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

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(locVol));
  }

  /**
   * REVIEW if we need this
   * Get the absolute (i.e. normal instantaneous) local vol surface
   * @param impliedVolatilitySurface BlackVolatilitySurface
   * @param spot value of underlying
   * @param rate interest rate
   * @return local vol surface
   */
  public AbsoluteLocalVolatilitySurface getAbsoluteLocalVolatilitySurface(final BlackVolatilitySurfaceStrike impliedVolatilitySurface, final double spot, final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double s = x[1];

        final double vol = impliedVolatilitySurface.getVolatility(t, s);

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
   * Classic Dupire local volatility formula in terms of the Black Volatility surface (parameterised by strike)
   * @param impliedVolatilitySurface Black Volatility surface (parameterised by strike)
   * @param spot Level of underlying
   * @param drift The risk free rate minus The dividend yield (r-q), or the difference between the domestic and foreign risk free rates in FX
   * @return A Local Volatility surface parameterised by expiry and strike
   */
  public LocalVolatilitySurfaceStrike getLocalVolatility(final BlackVolatilitySurfaceStrike impliedVolatilitySurface, final double spot, final double drift) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double s = x[1];

        final double vol = impliedVolatilitySurface.getVolatility(t, s);

        //  double rootT = Math.sqrt(t);
        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, s, vol);
        double var;
        if (s == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          final double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          final double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, s, vol, spot);
          final double h1 = (Math.log(spot / s) + (drift + vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * (divT + drift * s * divK)) / (1 + 2 * h1 * s * divK + s * s * (h1 * h2 * divK * divK + t * vol * divK2));
          if (var < 0.0) {
            var = 0.0;
            //TODO log error
          }
        }
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(locVol));
  }

  /**
   * Get the local volatility in the case where the option price is a function of the forward price
   * @param impliedVolatilitySurface The Black implied volatility surface
   * @param forwardCurve Curve of forward prices
   * @return The local volatility
   */
  public LocalVolatilitySurfaceStrike getLocalVolatility(final BlackVolatilitySurfaceStrike impliedVolatilitySurface, final ForwardCurve forwardCurve) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double forward = forwardCurve.getForward(t);
        final double drift = forwardCurve.getDrift(t);

        final double vol = impliedVolatilitySurface.getVolatility(t, k);
        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, k, vol);
        double var;
        if (k == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          final double divK = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, forward);
          final double divK2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, k, vol, forward);
          final double h1 = (Math.log(forward / k) + (vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * (divT + k * drift * divK)) / (1 + 2 * h1 * k * divK + k * k * (h1 * h2 * divK * divK + t * vol * divK2));
          //  System.out.println(t+"\t"+vol+"\t"+divT+"\t"+divK+"\t"+divK2+"\t"+drift);
          if (var < 0.0) {
            // throw new MathException("negative variance");
            var = 0.0;
            //TODO log error
          }
        }
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(locVol));
  }

  /**
   * Get the local volatility surface (parameterised by expiry and moneyness = strike/forward) from a Black volatility surface (also parameterised by expiry and moneyness).
   * <b>Note</b> this is the cleanest method as is does not require any knowledge of instantaneous rates (i.e. r & q). If the Black volatility surface is parameterised by strike and/or the
   * local volatility surface is required to be parameterised by strike use can use the converters BlackVolatilitySurfaceConverter and/or LocalVolatilitySurfaceConverter
   * @param impliedVolatilitySurface Black volatility surface (parameterised by expiry and moneyness)
   * @return local volatility surface (parameterised by expiry and moneyness)
   */
  public LocalVolatilitySurfaceMoneyness getLocalVolatility(final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        Validate.isTrue(t >= 0, "negative t");
        Validate.isTrue(m >= 0, "negative m");

        final double vol = impliedVolatilitySurface.getVolatilityForMoneyness(t, m);

        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, m, vol);
        double var;
        if (m == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          final double divM = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, m, vol, 1.0);
          final double divM2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, m, vol, 1.0);
          final double h1 = (-Math.log(m) + (vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * divT) / (1 + 2 * h1 * m * divM + m * m * (h1 * h2 * divM * divM + t * vol * divM2));
          if (var < 0.0) {
            //throw new MathException("negative variance");
            var = 0.0;
            //TODO log error
          }
        }
        return Math.sqrt(var);
      }
    };

    return new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(locVol), impliedVolatilitySurface.getForwardCurve());
  }

  private double getFirstTimeDev(final Surface<Double, Double, Double> surface, final double t, final double k, final double mid) {
    if (t <= _eps) {
      final double up = surface.getZValue(t + _eps, k);
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
