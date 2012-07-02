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

/**
 * 
 */
public class VolatilitySurfaceConverter {

  public static PureImpliedVolatilitySurface convertImpliedVolSurface(final BlackVolatilitySurfaceStrike volSurface, final EquityDividendsCurvesBundle divCurves) {
    Function<Double, Double> impVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double f = divCurves.getF(t);
        double d = divCurves.getD(t);

        boolean isCall = x > 1.0;
        double k = (f - d) * x + d;
        double vol = volSurface.getVolatility(t, k);
        double price = BlackFormulaRepository.price(f, k, t, vol, isCall);
        if (price < 0.0) {
          return 0.0;
        }
        double vol2 = BlackFormulaRepository.impliedVolatility(price / (f - d), 1.0, x, t, isCall);
        return vol2;
      }
    };
    return new PureImpliedVolatilitySurface(FunctionalDoublesSurface.from(impVol));
  }

  public static BlackVolatilitySurfaceStrike convertImpliedVolSurface(final PureImpliedVolatilitySurface pureVolSurface, final EquityDividendsCurvesBundle divCurves) {
    Function<Double, Double> impVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];
        double f = divCurves.getF(t);
        double d = divCurves.getD(t);
        if (k < d) {
          return 0.0;
        }
        boolean isCall = k > f;
        double x = (k - d) / (f - d);
        double vol = pureVolSurface.getVolatility(t, x);
        double price = (f - d) * BlackFormulaRepository.price(1.0, x, t, vol, isCall);
        if (price < 0.0) {
          return 0.0;
        }
        double vol2 = BlackFormulaRepository.impliedVolatility(price, f, k, t, isCall);
        return vol2;
      }
    };
    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(impVol));
  }

  public static PureLocalVolatilitySurface convertLocalVolSurface(final LocalVolatilitySurfaceStrike volSurface, final EquityDividendsCurvesBundle divCurves) {
    Function<Double, Double> pureLocalVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double d = divCurves.getD(t);
        double f = divCurves.getF(t);
        double s = (f - d) * x + d;
        return volSurface.getVolatility(t, s) * s / (s - d);
      }
    };
    return new PureLocalVolatilitySurface(FunctionalDoublesSurface.from(pureLocalVol));
  }

  public static LocalVolatilitySurfaceStrike convertLocalVolSurface(final PureLocalVolatilitySurface pureVolSurface, final EquityDividendsCurvesBundle divCurves) {
    Function<Double, Double> localVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        double d = divCurves.getD(t);
        if (s < d) {
          return 0.0;
        }
        double f = divCurves.getF(t);
        double x = (s - d) / (f - d);
        return pureVolSurface.getVolatility(t, x) * (s - d) / s;
      }
    };
    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(localVol));
  }

}
