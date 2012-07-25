/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.equity.variance.derivative.EquityVarianceSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EquityVarianceSwapPricer {

  private static final GeneralSmileInterpolator SMILE_INTERPOLATOR = new SmileInterpolatorSpline();
  //private static final GeneralSmileInterpolator SMILE_INTERPOLATOR = new SmileInterpolatorSABR();
  private static final Interpolator1D TIME_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final boolean USE_LOG_TIME = true;
  private static final boolean USE_INTEGRATED_VARIANCE = true;
  private static final boolean USE_LOG_VALUE = true;
  private static final VolatilitySurfaceInterpolator SURFACE_INTERPOLATOR = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, TIME_INTERPOLATOR, USE_LOG_TIME,
      USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  private static final EquityVarianceSwapStaticReplication VAR_SWAP_CALCULATOR = new EquityVarianceSwapStaticReplication();

  public double price(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      double[] expiries, double[][] strikes, double[][] otmPrices) {

    final int nExp = expiries.length;
    ArgumentChecker.isTrue(strikes.length == nExp, "number of strike strips ({}) not equal to number of expiries({})", strikes.length, nExp);
    ArgumentChecker.isTrue(otmPrices.length == nExp, "number of price strips ({}) not equal to number of expiries({})", strikes.length, nExp);
    for (int i = 0; i < nExp; i++) {
      ArgumentChecker.isTrue(strikes[i].length == otmPrices[i].length, "number of prices and strikes in strip #{} (expiry = {}) do not match. {} prices and {} strikes", i, expiries[i],
          otmPrices[i].length,
          strikes[i].length);
    }

    //convert the real option prices to prices of options on pure stock, then find the implied volatility of these options
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    double[][] x = new double[nExp][];
    double[][] vols = new double[nExp][];
    for (int i = 0; i < nExp; i++) {
      double t = expiries[i];
      double f = divCurves.getF(t);
      double d = divCurves.getD(t);
      double p = discountCurve.getDiscountFactor(t);
      final int n = strikes[i].length;
      x[i] = new double[n];
      vols[i] = new double[n];
      for (int j = 0; j < n; j++) {
        boolean isCall = strikes[i][j] >= f;
        double temp = strikes[i][j] - d;
        ArgumentChecker.isTrue(temp >= 0, "strike of {} at expiry {} is less than the discounts value of future cash dividends {}. Either remove this option or change the dividend assumption",
            strikes[i][j], t, d);
        x[i][j] = temp / (f - d);
        double purePrice = otmPrices[i][j] / p / (f - d);
        vols[i][j] = BlackFormulaRepository.impliedVolatility(purePrice, 1.0, x[i][j], t, isCall);
      }
    }

    //fit an implied volatility surface to the pure implied vols (as the forward is 1.0, the BlackVolatilitySurfaceMoneyness is numerically identical to the PureImpliedVolatilitySurface
    SmileSurfaceDataBundle data = new StandardSmileSurfaceDataBundle(new ForwardCurve(1.0), expiries, x, vols);
    BlackVolatilitySurfaceMoneyness surf = SURFACE_INTERPOLATOR.getVolatilitySurface(data);
    PureImpliedVolatilitySurface pureSurf = new PureImpliedVolatilitySurface(surf.getSurface()); //TODO have a direct fitter for PureImpliedVolatilitySurface

    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    double res = swap.correctForDividends() ? ev[0] : ev[1];
    return res;
  }
}
