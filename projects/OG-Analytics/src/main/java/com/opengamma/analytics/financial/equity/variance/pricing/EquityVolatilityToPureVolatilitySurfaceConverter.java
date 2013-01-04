/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts an volatility surface where the prices (and so the implied volatilities) include information about dividends to a pure implied volatility
 * surface, where the effect of dividends has been removed.
 */
public class EquityVolatilityToPureVolatilitySurfaceConverter {

  /**
   * @param spot The spot, greater than zero
   * @param discountCurve The discount curve, not null
   * @param dividends The dividends, not null
   * @param expiries The expiries, not null
   * @param strikes The strikes, not null. Must have the same number of strikes as expiries.
   * @param otmPrices The out of the money prices, not null. Must have the same number of price strips as expiries, with each price strip being
   * the same length as the strikes.
   * @param surfaceInterpolator The volatility surface interpolator, not null
   * @return A pure implied volatility surface
   */
  public static PureImpliedVolatilitySurface getConvertedSurface(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final double[] expiries, final double[][] strikes, final double[][] otmPrices, final VolatilitySurfaceInterpolator surfaceInterpolator) {
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(expiries, "expiries");
    final int nExp = expiries.length;
    ArgumentChecker.isTrue(strikes.length == nExp, "number of strike strips ({}) not equal to number of expiries({})", strikes.length, nExp);
    ArgumentChecker.isTrue(otmPrices.length == nExp, "number of price strips ({}) not equal to number of expiries({})", strikes.length, nExp);
    for (int i = 0; i < nExp; i++) {
      ArgumentChecker.isTrue(strikes[i].length == otmPrices[i].length, "number of prices and strikes in strip #{} (expiry = {}) do not match. {} prices and {} strikes",
          i, expiries[i], otmPrices[i].length, strikes[i].length);
    }

    //convert the real option prices to prices of options on pure stock, then find the implied volatility of these options
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final double[][] x = new double[nExp][];
    final double[][] vols = new double[nExp][];
    for (int i = 0; i < nExp; i++) {
      final double t = expiries[i];
      final double f = divCurves.getF(t);
      final double d = divCurves.getD(t);
      final double p = discountCurve.getDiscountFactor(t);
      final int n = strikes[i].length;
      x[i] = new double[n];
      vols[i] = new double[n];
      for (int j = 0; j < n; j++) {

        final boolean isCall = strikes[i][j] >= f;
        final double temp = strikes[i][j] - d;
        ArgumentChecker.isTrue(temp >= 0,
            "strike of {} at expiry {} is less than the discounted value of future cash dividends {}. Either remove this option or change the dividend assumption",
            strikes[i][j], t, d);
        x[i][j] = temp / (f - d);
        final double purePrice = otmPrices[i][j] / p / (f - d);
        vols[i][j] = BlackFormulaRepository.impliedVolatility(purePrice, 1.0, x[i][j], t, isCall);
      }
    }

    //fit an implied volatility surface to the pure implied vols (as the forward is 1.0, the BlackVolatilitySurfaceMoneyness is numerically identical to the PureImpliedVolatilitySurface
    final SmileSurfaceDataBundle data = new StandardSmileSurfaceDataBundle(new ForwardCurve(1.0), expiries, x, vols);
    final BlackVolatilitySurfaceMoneyness surf = surfaceInterpolator.getVolatilitySurface(data);
    return new PureImpliedVolatilitySurface(surf.getSurface()); //TODO have a direct fitter for PureImpliedVolatilitySurface
  }
}
