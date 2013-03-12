/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.model.volatility.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.PriceSurface;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.StrikeType;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DupireLocalVolatilityCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(DupireLocalVolatilityCalculator.class);
  private final double _eps;

  public DupireLocalVolatilityCalculator() {
    _eps = 1e-3;
  }

  public DupireLocalVolatilityCalculator(final double h) {
    _eps = h;
  }

  /**
   * Classic Dupire local volatility formula
   * 
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
          s_logger.info("Negative variance; returning 0");
          return 0.;
        }
        return Math.sqrt(var);
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", priceSurface, spot, r, q), "getSurface"),
            "getFunction");
      }

    };

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(locVol)) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", priceSurface, spot, r, q);
      }
    };
  }

  /**
   * REVIEW if we need this Get the absolute (i.e. normal instantaneous) local vol surface
   * 
   * @param impliedVolatilitySurface BlackVolatilitySurface
   * @param spot value of underlying
   * @param rate interest rate
   * @return local vol surface
   */
  public AbsoluteLocalVolatilitySurface getAbsoluteLocalVolatilitySurface(final BlackVolatilitySurfaceStrike impliedVolatilitySurface, final double spot,
      final double rate) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double s = x[1];

        final double vol = impliedVolatilitySurface.getVolatility(t, s);

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
            s_logger.info("negative variance; returning 0");
            return 0.;
          }
        }
        return s * Math.sqrt(var);
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getAbsoluteLocalVolatilitSurface", impliedVolatilitySurface, spot,
            rate), "getSurface"), "getFunction");
      }

    };

    return new AbsoluteLocalVolatilitySurface(FunctionalDoublesSurface.from(locVol)) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getAbsoluteLocalVolatilitSurface", impliedVolatilitySurface, spot, rate);
      }
    };
  }

  /**
   * Classic Dupire local volatility formula in terms of the Black Volatility surface (parameterised by strike)
   * 
   * @param impliedVolatilitySurface Black Volatility surface (parameterised by strike)
   * @param spot Level of underlying
   * @param drift The risk free rate minus The dividend yield (r-q), or the difference between the domestic and foreign risk free rates in FX
   * @return A Local Volatility surface parameterised by expiry and strike
   * @deprecated Don't use
   */
  @Deprecated
  public LocalVolatilitySurfaceStrike getLocalVolatility(final BlackVolatilitySurfaceStrike impliedVolatilitySurface, final double spot, final double drift) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double s = x[1];

        final double vol = impliedVolatilitySurface.getVolatility(t, s);

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
            s_logger.error("Negative variance; returning 0");
            var = 0.0;
          }
        }
        return Math.sqrt(var);
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", impliedVolatilitySurface, spot, drift),
            "getSurface"), "getFunction");
      }

    };

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(locVol)) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", impliedVolatilitySurface, spot, drift);
      }
    };
  }

  //TODO replace this
  public <T extends StrikeType> LocalVolatilitySurface<?> getLocalVolatilitySurface(final BlackVolatilitySurface<T> impliedVolatilitySurface,
      final ForwardCurve forwardCurve) {
    if (impliedVolatilitySurface instanceof BlackVolatilitySurfaceStrike) {
      return getLocalVolatility((BlackVolatilitySurfaceStrike) impliedVolatilitySurface, forwardCurve);
    } else if (impliedVolatilitySurface instanceof BlackVolatilitySurfaceMoneyness) {
      return getLocalVolatility((BlackVolatilitySurfaceMoneyness) impliedVolatilitySurface);
    }
    throw new IllegalArgumentException();
  }

  /**
   * Get the local volatility in the case where the option price is a function of the forward price
   * 
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
          if (var < 0.0) {
            s_logger.info("Negative variance; returning 0");
            var = 0.0;
          }
        }
        return Math.sqrt(var);
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", impliedVolatilitySurface, forwardCurve),
            "getSurface"), "getFunction");
      }

    };

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(locVol)) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", impliedVolatilitySurface, forwardCurve);
      }
    };
  }

  /**
   * Get the <b>pure</b> local volatility surface (i.e. if the pure stock $x$ follows the SDE $\frac{dx}{x} = \sigma(t,x) dW$ then $\sigma(t,x)$ is the pure local volatility
   * <p>
   * See White, R (2012) Equity Variance Swap with Dividends
   * 
   * @param pureImpliedVolatilitySurface The pure implied volatility surface - i.e. the volatility that put into the Black formula will give the price of an option on the pure stock
   * @return pure local volatility surface
   */
  public PureLocalVolatilitySurface getLocalVolatility(final PureImpliedVolatilitySurface pureImpliedVolatilitySurface) {
    final Surface<Double, Double, Double> lv = getLocalVolatility(pureImpliedVolatilitySurface.getSurface());
    return new PureLocalVolatilitySurface(lv);
  }

  /**
   * Get the local volatility surface (parameterised by expiry and moneyness = strike/forward) from a Black volatility surface (also parameterised by expiry and moneyness). <b>Note</b> this is the
   * cleanest method as is does not require any knowledge of instantaneous rates (i.e. r & q). If the Black volatility surface is parameterised by strike and/or the local volatility surface is
   * required to be parameterised by strike use can use the converters BlackVolatilitySurfaceConverter and/or LocalVolatilitySurfaceConverter
   * 
   * @param impliedVolatilitySurface Black volatility surface (parameterised by expiry and moneyness)
   * @return local volatility surface (parameterised by expiry and moneyness)
   */
  public LocalVolatilitySurfaceMoneyness getLocalVolatility(final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface) {
    final Surface<Double, Double, Double> lv = getLocalVolatility(impliedVolatilitySurface.getSurface());
    return new LocalVolatilitySurfaceMoneyness(lv, impliedVolatilitySurface.getForwardCurve());
  }

  public LocalVolatilitySurfaceMoneyness getLocalVolatilityDebug(final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface) {

    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        ArgumentChecker.isTrue(t >= 0, "negative t");
        ArgumentChecker.isTrue(m >= 0, "negative m");

        final double vol = impliedVolatilitySurface.getVolatilityForMoneyness(t, m);

        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, m, vol);
        final double divM = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, m, vol, 1.0);
        final double divM2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, m, vol, 1.0);

        final double bTheta = -BlackFormulaRepository.driftlessTheta(1.0, m, t, vol);
        final double vega = BlackFormulaRepository.vega(1.0, m, t, vol);
        final double theta = bTheta + vega * divT;
        final double dg = BlackFormulaRepository.dualGamma(1.0, m, t, vol);
        final double vanna = BlackFormulaRepository.dualVanna(1.0, m, t, vol);
        final double vomma = BlackFormulaRepository.vomma(1.0, m, t, vol);
        final double dens = dg + 2 * vanna * divM + +vomma * divM * divM + vega * divM2;
        return Math.sqrt(2 * theta / dens) / m;
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatilityDebug", impliedVolatilitySurface),
            "getSurface"), "getFunction");
      }

    };

    return new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(locVol), impliedVolatilitySurface.getForwardCurve()) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatilityDebug", impliedVolatilitySurface);
      }
    };
  }

  /**
   * Get the theta surface - the rate of change of an option with respect to the time-to-expiry (<b>Note</b> this is the negative of the normal definition as change of an option with respect to
   * (calendar) time)
   * 
   * @param impliedVolatilitySurface Black volatility surface (parameterised by expiry and moneyness)
   * @return Theta surface (parameterised by moneyness)
   */
  public Surface<Double, Double, Double> getTheta(final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface) {

    final Function<Double, Double> theta = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        ArgumentChecker.isTrue(t >= 0, "negative t");
        ArgumentChecker.isTrue(m >= 0, "negative m");

        final double vol = impliedVolatilitySurface.getVolatilityForMoneyness(t, m);

        final double divT = getFirstTimeDev(impliedVolatilitySurface.getSurface(), t, m, vol);

        final double bTheta = -BlackFormulaRepository.driftlessTheta(1.0, m, t, vol);
        final double vega = BlackFormulaRepository.vega(1.0, m, t, vol);
        return bTheta + vega * divT;
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getTheta", impliedVolatilitySurface), "getFunction");
      }

    };

    return new FunctionalDoublesSurface(theta) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getTheta", impliedVolatilitySurface);
      }
    };
  }

  /**
   * Get the transition density surface - each time slice through this surface is the Probably Density Function (PDF), in the risk neutral measure, for the underlying at that time (parameterised by
   * moneyness)
   * 
   * @param impliedVolatilitySurface Black volatility surface (parameterised by expiry and moneyness)
   * @return The transition density surface
   */
  public Surface<Double, Double, Double> getDensity(final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface) {

    final Function<Double, Double> density = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        ArgumentChecker.isTrue(t >= 0, "negative t");
        ArgumentChecker.isTrue(m >= 0, "negative m");

        final double vol = impliedVolatilitySurface.getVolatilityForMoneyness(t, m);

        final double divM = getFirstStrikeDev(impliedVolatilitySurface.getSurface(), t, m, vol, 1.0);
        final double divM2 = getSecondStrikeDev(impliedVolatilitySurface.getSurface(), t, m, vol, 1.0);

        final double dg = BlackFormulaRepository.dualGamma(1.0, m, t, vol);
        final double vanna = BlackFormulaRepository.dualVanna(1.0, m, t, vol);
        final double vega = BlackFormulaRepository.vega(1.0, m, t, vol);
        final double vomma = BlackFormulaRepository.vomma(1.0, m, t, vol);
        final double dens = dg + 2 * vanna * divM + +vomma * divM * divM + vega * divM2;
        return dens;
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getDensity", impliedVolatilitySurface), "getFunction");
      }

    };

    return new FunctionalDoublesSurface(density) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getDensity", impliedVolatilitySurface);
      }
    };
  }

  public Surface<Double, Double, Double> getLocalVolatility(final Surface<Double, Double, Double> surf) {
    final Function<Double, Double> locVol = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        ArgumentChecker.isTrue(t >= 0, "negative t");
        ArgumentChecker.isTrue(m >= 0, "negative m");

        final double vol = surf.getZValue(t, m);

        final double divT = getFirstTimeDev(surf, t, m, vol);
        double var;
        if (m == 0) {
          var = vol * vol + 2 * vol * t * (divT);
        } else {
          final double divM = getFirstStrikeDev(surf, t, m, vol, 1.0);
          final double divM2 = getSecondStrikeDev(surf, t, m, vol, 1.0);
          final double h1 = (-Math.log(m) + (vol * vol / 2) * t) / vol;
          final double h2 = h1 - vol * t;
          var = (vol * vol + 2 * vol * t * divT) / (1 + 2 * h1 * m * divM + m * m * (h1 * h2 * divM * divM + t * vol * divM2));
          if (var < 0.0) {
            s_logger.info("Negative variance; returning 0");
            var = 0.0;
          }
        }
        return Math.sqrt(var);
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", surf), "getFunction");
      }

    };

    return new FunctionalDoublesSurface(locVol) {
      public Object writeReplace() {
        return new InvokedSerializedForm(DupireLocalVolatilityCalculator.this, "getLocalVolatility", surf);
      }
    };
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
    final double res = (up + down - 2 * cent) / eps / eps;
    return res;
  }

}
