/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing utility methods for pure volatility surfaces
 */
public class VolatilitySurfaceConverter {

  /**
   * Converts a Black volatility surface (parameterised by strike) to a pure implied volatility surface.
   * @param volSurface The Black volatility surface, not null
   * @param divCurves Bundle containing a discounting curve, forward curve and dividends data, not null
   * @return A pure implied surface
   */
  public static PureImpliedVolatilitySurface convertImpliedVolSurface(final BlackVolatilitySurfaceStrike volSurface, final EquityDividendsCurvesBundle divCurves) {
    ArgumentChecker.notNull(volSurface, "volatility surface");
    ArgumentChecker.notNull(divCurves, "curves and dividend data");
    final Function<Double, Double> impVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = divCurves.getF(t);
        final double d = divCurves.getD(t);

        final boolean isCall = x > 1.0;
        final double k = (f - d) * x + d;
        final double vol = volSurface.getVolatility(t, k);
        final double price = BlackFormulaRepository.price(f, k, t, vol, isCall);
        if (price < 0.0) {
          return 0.0;
        }
        final double vol2 = BlackFormulaRepository.impliedVolatility(price / (f - d), 1.0, x, t, isCall);
        return vol2;
      }
    };
    return new PureImpliedVolatilitySurface(FunctionalDoublesSurface.from(impVol));
  }

  /**
   * Converts a pure implied volatility surface to a Black volatility surface parameterised by strike.
   * @param pureVolSurface The pure volatility surface, not null
   * @param divCurves Bundle containing a discounting curve, forward curve and dividends data, not null
   * @return A Black volatility surface parameterised by strike
   */
  public static BlackVolatilitySurfaceStrike convertImpliedVolSurface(final PureImpliedVolatilitySurface pureVolSurface, final EquityDividendsCurvesBundle divCurves) {
    ArgumentChecker.notNull(pureVolSurface, "pure volatility surface");
    ArgumentChecker.notNull(divCurves, "curves and dividend data");
    final Function<Double, Double> impVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double f = divCurves.getF(t);
        final double d = divCurves.getD(t);
        if (k < d) {
          return 0.0;
        }
        final boolean isCall = k > f;
        final double x = (k - d) / (f - d);
        final double vol = pureVolSurface.getVolatility(t, x);
        final double price = (f - d) * BlackFormulaRepository.price(1.0, x, t, vol, isCall);
        if (price < 0.0) {
          return 0.0;
        }
        final double vol2 = BlackFormulaRepository.impliedVolatility(price, f, k, t, isCall);
        return vol2;
      }
    };
    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(impVol));
  }

  /**
   * Converts a local volatility surface (parameterised by strike) to a pure implied volatility surface.
   * @param volSurface The local volatility surface, not null
   * @param divCurves Bundle containing a discounting curve, forward curve and dividends data, not null
   * @return A Black volatility surface parameterised by strike
   */
  public static PureLocalVolatilitySurface convertLocalVolSurface(final LocalVolatilitySurfaceStrike volSurface, final EquityDividendsCurvesBundle divCurves) {
    ArgumentChecker.notNull(volSurface, "volatility surface");
    ArgumentChecker.notNull(divCurves, "curves and dividend data");
    final Function<Double, Double> pureLocalVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double d = divCurves.getD(t);
        final double f = divCurves.getF(t);
        final double s = (f - d) * x + d;
        return volSurface.getVolatility(t, s) * s / (s - d);
      }
    };
    return new PureLocalVolatilitySurface(FunctionalDoublesSurface.from(pureLocalVol));
  }

  /**
   * Converts a pure local volatility surface to a local volatility surface.
   * @param pureVolSurface The pure local volatility surface, not null
   * @param divCurves Bundle containing a discounting curve, forward curve and dividends data, not null
   * @return A local volatility surface
   */
  public static LocalVolatilitySurfaceStrike convertLocalVolSurface(final PureLocalVolatilitySurface pureVolSurface, final EquityDividendsCurvesBundle divCurves) {
    ArgumentChecker.notNull(pureVolSurface, "volatility surface");
    ArgumentChecker.notNull(divCurves, "curves and dividend data");
    final Function<Double, Double> localVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        final double t = ts[0];
        final double s = ts[1];
        final double d = divCurves.getD(t);
        if (s < d) {
          return 0.0;
        }
        final double f = divCurves.getF(t);
        final double x = (s - d) / (f - d);
        return pureVolSurface.getVolatility(t, x) * (s - d) / s;
      }
    };
    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(localVol));
  }

}
