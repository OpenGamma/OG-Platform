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
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
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
  private static final EquityVarianceSwapForwardPurePDE VAR_SWAP_FWD_PDE_CALCULATOR = new EquityVarianceSwapForwardPurePDE();
  private static final EquityVarianceSwapBackwardsPurePDE VAR_SWAP_BKW_PDE_CALCULATOR = new EquityVarianceSwapBackwardsPurePDE();

  /**
   * 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param expiries The expiries of option strips
   * @param strikes The strikes in each option strips 
   * @param otmPrices The <b>out-of-the-money</b> option prices 
   * @return The <b>annualised</b> variance 
   */
  public double priceFromOTMPrices(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
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

    final double t = swap.getTimeToSettlement();
    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    double res = (swap.correctForDividends() ? ev[0] : ev[1]) / t;
    return res;
  }

  /**
   * 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The <b>annualised</b> variance 
   */
  public double priceFromImpliedVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    final double t = swap.getTimeToSettlement();
    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    double res = (swap.correctForDividends() ? ev[0] : ev[1]) / t;
    return res;
  }

  /**
   * 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The <b>annualised</b> variance 
   */
  public double priceFromImpliedVols2(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    PureLocalVolatilitySurface plv = dupire.getLocalVolatility(pureSurf);
    final double t = swap.getTimeToSettlement();
    double[] ev = VAR_SWAP_BKW_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plv);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    double res = (swap.correctForDividends() ? ev[0] : ev[1]) / t;
    return res;
  }

  /**
   * Here we tread the market implied volatilities as invariant to the spot (sticky-strike), and price the variance swap twice with the spot bumped up and down. The
   * Pricing itself involves finding pure implied volatilities, from these an interpolated implied volatility surface and finally the expected variance via static replication. 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double deltaWithStickyStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    //here we assume the market implied volatilities are invariant to a change of spot 
    final double eps = 1e-5;
    double up = priceFromImpliedVols(swap, (1 + eps) * spot, discountCurve, dividends, marketVols);
    double down = priceFromImpliedVols(swap, (1 - eps) * spot, discountCurve, dividends, marketVols);
    double ssDelta = (up - down) / 2 / eps;
    return ssDelta;
  }

  /**
   * Here we tread the pure implied volatilities as invariant to the spot (sticky-pure strike which is similar to sticky delta), and price the variance swap twice with the spot bumped up and down. 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double deltaWithStickyPureStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    double up = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 + eps), discountCurve, dividends, t, pureSurf)[index];
    double down = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 - eps), discountCurve, dividends, t, pureSurf)[index];

    double ssDelta = (up - down) / 2 / eps / t;
    return ssDelta;
  }

  /**
   * Here we tread the local volatility surface  as invariant to the spot and price the variance swap twice with the spot bumped up and down. 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double deltaWithStickyLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    LocalVolatilitySurfaceStrike lv = VolatilitySurfaceConverter.convertLocalVolSurface(plv, divCurves);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    //up - here we assume that the local vol surface is invariant to a change of spot and form a new pure local vol surface corresponding to the bumped spot 
    final double sUp = spot * (1 + eps);
    final EquityDividendsCurvesBundle divCurvesUp = new EquityDividendsCurvesBundle(sUp, discountCurve, dividends);
    final PureLocalVolatilitySurface plvUp = VolatilitySurfaceConverter.convertLocalVolSurface(lv, divCurvesUp);
    final double up = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(sUp, discountCurve, dividends, t, plvUp)[index];
    //down 
    final double sDown = spot * (1 - eps);
    final EquityDividendsCurvesBundle divCurvesDown = new EquityDividendsCurvesBundle(sDown, discountCurve, dividends);
    final PureLocalVolatilitySurface plvDown = VolatilitySurfaceConverter.convertLocalVolSurface(lv, divCurvesDown);
    final double down = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(sDown, discountCurve, dividends, t, plvDown)[index];

    double delta = (up - down) / 2 / eps / t;
    return delta;
  }

  /**
   * Here we tread the market implied volatilities as invariant to the spot (sticky-strike), and price the variance swap twice with the spot bumped up and down. The
   * Pricing itself involves finding pure implied volatilities, from these an interpolated implied volatility surface and finally the expected variance via static replication. 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The gamma of the variance swap under a sticky-strike assumption <b>scaled by spot^2</b>
   */
  public double gammaWithStickyStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    //here we assume the market implied volatilities are invariant to a change of spot 
    final double eps = 1e-5;
    double up = priceFromImpliedVols(swap, (1 + eps) * spot, discountCurve, dividends, marketVols);
    double mid = priceFromImpliedVols(swap, spot, discountCurve, dividends, marketVols);
    double down = priceFromImpliedVols(swap, (1 - eps) * spot, discountCurve, dividends, marketVols);
    double gamma = (up + down - 2 * mid) / eps / eps;
    return gamma;
  }

  /**
   * Here we tread the pure implied volatilities as invariant to the spot (sticky-pure strike which is similar to sticky delta), and price the variance swap twice with the spot bumped up and down. 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double gammaWithStickyPureStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    double up = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 + eps), discountCurve, dividends, t, pureSurf)[index];
    double mid = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf)[index];
    double down = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 - eps), discountCurve, dividends, t, pureSurf)[index];

    double gamma = (up + down - 2 * mid) / eps / eps / t;
    return gamma;
  }

  /**
   * Here we tread the local volatility surface  as invariant to the spot and price the variance swap twice with the spot bumped up and down. 
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double gammaWithStickyLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    LocalVolatilitySurfaceStrike lv = VolatilitySurfaceConverter.convertLocalVolSurface(plv, divCurves);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    final double mid = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plv)[index];
    //up - here we assume that the local vol surface is invariant to a change of spot and form a new pure local vol surface corresponding to the bumped spot 
    final double sUp = spot * (1 + eps);
    final EquityDividendsCurvesBundle divCurvesUp = new EquityDividendsCurvesBundle(sUp, discountCurve, dividends);
    final PureLocalVolatilitySurface plvUp = VolatilitySurfaceConverter.convertLocalVolSurface(lv, divCurvesUp);
    final double up = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(sUp, discountCurve, dividends, t, plvUp)[index];
    //down 
    final double sDown = spot * (1 - eps);
    final EquityDividendsCurvesBundle divCurvesDown = new EquityDividendsCurvesBundle(sDown, discountCurve, dividends);
    final PureLocalVolatilitySurface plvDown = VolatilitySurfaceConverter.convertLocalVolSurface(lv, divCurvesDown);
    final double down = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(sDown, discountCurve, dividends, t, plvDown)[index];

    double gamma = (up + down - 2 * mid) / eps / eps / t;
    return gamma;
  }

  /**
   * Here the vega is taken as the sensitivity of the <b>square-root</b> of the annualised Expected Variance (EV) (Note: this is not the same as the expected volatility)
   * to a parallel shift of the implied volatility surface.  
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The vega
   */
  public double vegaImpVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureImpliedVolatilitySurface piv = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final BlackVolatilitySurfaceStrike iv = VolatilitySurfaceConverter.convertImpliedVolSurface(piv, divCurves);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    //up
    final BlackVolatilitySurfaceStrike ivUp = new BlackVolatilitySurfaceStrike(flooredShiftSurface(iv.getSurface(), eps));
    final double up = Math.sqrt(VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, ivUp)[index] / t);
    //down
    final BlackVolatilitySurfaceStrike ivDown = new BlackVolatilitySurfaceStrike(flooredShiftSurface(iv.getSurface(), -eps));
    final double down = Math.sqrt(VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, ivDown)[index] / t);
    double vega = (up - down) / 2 / eps;
    return vega;
  }

  /**
   * Here the vega is taken as the sensitivity of the <b>square-root</b> of the annualised Expected Variance (EV) (Note: this is not the same as the expected volatility)
   * to a parallel shift of the local volatility surface.  
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The vega
   */
  public double vegaLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final LocalVolatilitySurfaceStrike lv = VolatilitySurfaceConverter.convertLocalVolSurface(plv, divCurves);
    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    //up
    final LocalVolatilitySurfaceStrike lvUp = new LocalVolatilitySurfaceStrike(flooredShiftSurface(lv.getSurface(), eps));
    final PureLocalVolatilitySurface plvUp = VolatilitySurfaceConverter.convertLocalVolSurface(lvUp, divCurves);
    final double up = Math.sqrt(VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plvUp)[index] / t);
    //down
    final LocalVolatilitySurfaceStrike lvDown = new LocalVolatilitySurfaceStrike(flooredShiftSurface(lv.getSurface(), -eps));
    final PureLocalVolatilitySurface plvDown = VolatilitySurfaceConverter.convertLocalVolSurface(lvDown, divCurves);
    final double down = Math.sqrt(VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plvDown)[index] / t);
    double vega = (up - down) / 2 / eps;
    return vega;
  }

  /**
   * Here the vega is taken as the sensitivity of the <b>square-root</b> of the annualised Expected Variance (EV) (Note: this is not the same as the expected volatility)
   * to a parallel shift of the <b>pure</b> implied volatility surface.  
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The vega
   */
  public double vegaPureImpVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureImpliedVolatilitySurface piv = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    //up
    final PureImpliedVolatilitySurface ivUp = new PureImpliedVolatilitySurface(flooredShiftSurface(piv.getSurface(), eps));
    final double up = Math.sqrt(VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, ivUp)[index] / t);
    //down
    final PureImpliedVolatilitySurface ivDown = new PureImpliedVolatilitySurface(flooredShiftSurface(piv.getSurface(), -eps));
    final double down = Math.sqrt(VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, ivDown)[index] / t);
    double vega = (up - down) / 2 / eps;
    return vega;
  }

  /**
   * Here the vega is taken as the sensitivity of the <b>square-root</b> of the annualised Expected Variance (EV) (Note: this is not the same as the expected volatility)
   * to a parallel shift of the <b>pure</b> local volatility surface.  
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities 
   * @return The vega
   */
  public double vegaPureLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    //up
    final PureLocalVolatilitySurface plvUp = new PureLocalVolatilitySurface(flooredShiftSurface(plv.getSurface(), eps));
    final double up = Math.sqrt(VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plvUp)[index] / t);
    //down
    final PureLocalVolatilitySurface plvDown = new PureLocalVolatilitySurface(flooredShiftSurface(plv.getSurface(), -eps));
    final double down = Math.sqrt(VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plvDown)[index] / t);
    double vega = (up - down) / 2 / eps;
    return vega;
  }

  //shift a surface flooring the result at zero
  private Surface<Double, Double, Double> flooredShiftSurface(final Surface<Double, Double, Double> from, final double amount) {
    Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... x) {
        double temp = from.getZValue(x[0], x[1]) + amount;
        return Math.max(0.0, temp);
      }
    };
    return FunctionalDoublesSurface.from(surf);
  }

  /**
   * Compute a delta from the market implied volatilities by first computing a pure implied volatility surface, then treating this as an invariant while the spot is moved
   * @param swap The Variance swap 
   * @param spot The spot value of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends 
   * @param marketVols The market option prices expressed as implied volatilities 
   * @return The delta
   */
  public double deltaFromImpliedVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;

    //this surface is assumed invariant to change in the spot 
    PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] evUp = VAR_SWAP_CALCULATOR.expectedVariance((1 + eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    double[] evDown = VAR_SWAP_CALCULATOR.expectedVariance((1 - eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);

    double res = swap.correctForDividends() ? (evUp[0] - evDown[0]) / spot / eps : (evUp[1] - evDown[1]) / spot / eps;
    return res;
  }

  public double priceFromLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    PureLocalVolatilitySurface pureSurf = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    //PDEUtilityTools.printSurface("pure local vol", pureSurf.getSurface(), 0, 2, 0.3, 3);

    double[] ev = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    double res = swap.correctForDividends() ? ev[0] : ev[1];
    return res;
  }

  public double deltaFromLocalVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;

    //this surface is assumed invariant to change in the spot 
    PureLocalVolatilitySurface pureSurf = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);

    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] evUp = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 + eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    double[] evDown = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 - eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);

    double res = swap.correctForDividends() ? (evUp[0] - evDown[0]) / spot / eps : (evUp[1] - evDown[1]) / spot / eps;
    return res;
  }

  public double deltaFromLocalVols2(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;

    EquityDividendsCurvesBundle div = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    PureLocalVolatilitySurface pureSurf = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    //this surface is assumed invariant to change in the spot
    LocalVolatilitySurfaceStrike lv = convertLV(pureSurf, div);

    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    //up
    EquityDividendsCurvesBundle divUp = new EquityDividendsCurvesBundle((1 + eps) * spot, discountCurve, dividends);
    PureLocalVolatilitySurface plvUp = convertLV(lv, divUp);
    double[] evUp = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 + eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), plvUp);
    //down
    EquityDividendsCurvesBundle divDown = new EquityDividendsCurvesBundle((1 - eps) * spot, discountCurve, dividends);
    PureLocalVolatilitySurface plvDown = convertLV(lv, divDown);
    double[] evDown = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 - eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), plvDown);
    double res = swap.correctForDividends() ? (evUp[0] - evDown[0]) / spot / eps : (evUp[1] - evDown[1]) / spot / eps;
    return res;
  }

  /**
   * Compute sensitivity of EV to the dividends. Here the "market" implied volatility surface is assumed to be invariant to a change of dividends 
   * @param swap The equity swap 
   * @param spot The spot value of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends 
   * @param marketVols The market option prices expressed as implied volatilities 
   * @return Array of arrays containing dividend sensitivity. For n dividends, there are n rows, each containing two elements: the sensitivity to alpha and beta
   */
  public double[][] dividendSensitivityWithStickyImpliedVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;
    final double t = swap.getTimeToObsEnd();
    final int index = swap.correctForDividends() ? 0 : 1;
    PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    double base = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf)[index];

    final int n = dividends.getNumberOfDividends();

    double[][] res = new double[n][2];
    for (int i = 0; i < n; i++) {
      //bump alpha
      if (dividends.getAlpha(i) > eps / (1 - eps)) {
        //up
        AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) * (1 + eps) + eps, i);
        PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, daUp, marketVols);
        double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pvUp);
        //down
        AffineDividends daDown = dividends.withAlpha(dividends.getAlpha(i) * (1 - eps) - eps, i);
        PureImpliedVolatilitySurface pvDown = getPureImpliedVolFromMarket(spot, discountCurve, daDown, marketVols);
        double[] aDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daDown, t, pvDown);
        res[i][0] = spot * (aUp[index] - aDown[index]) / 2 / eps / (1 + dividends.getAlpha(i));
      } else {
        //forward difference for zero (or very near zero) alpha
        AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) + eps, i);
        PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, daUp, marketVols);
        double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pvUp);
        res[i][0] = spot * (aUp[index] - base) / eps;
      }

      //bump beta    
      if (dividends.getBeta(i) > eps) {
        AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, dbUp, marketVols);
        double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pvUp);
        AffineDividends dbDown = dividends.withBeta(dividends.getBeta(i) - eps, i);
        PureImpliedVolatilitySurface pvDown = getPureImpliedVolFromMarket(spot, discountCurve, dbDown, marketVols);
        double[] bDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbDown, swap.getTimeToObsEnd(), pvDown);
        res[i][1] = (bUp[index] - bDown[index]) / 2 / eps;
      } else {
        //forward difference for zero (or near zero) beta       
        AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, dbUp, marketVols);
        double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pvUp);
        res[i][1] = (bUp[index] - base) / eps;
      }
    }
    return res;
  }

  /**
   * Compute sensitivity of EV to the dividends. Here the pure volatility surface is assumed to be invariant to a change of dividends 
   * @param swap The equity swap 
   * @param spot The spot value of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends 
   * @param marketVols The market option prices expressed as implied volatilities 
   * @return Array of arrays containing dividend sensitivity. For n dividends, there are n rows, each containing two elements: the sensitivity to alpha and beta
   */
  public double[][] dividendSensitivityWithStickyPureVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;
    final double t = swap.getTimeToObsEnd();
    final int index = swap.correctForDividends() ? 0 : 1;
    PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    double base = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf)[index];

    final int n = dividends.getNumberOfDividends();

    double[][] res = new double[n][2];
    for (int i = 0; i < n; i++) {
      //bump alpha
      if (dividends.getAlpha(i) > eps / (1 - eps)) {
        //up
        AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) * (1 + eps) + eps, i);
        double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pureSurf);
        //down
        AffineDividends daDown = dividends.withAlpha(dividends.getAlpha(i) * (1 - eps) - eps, i);
        double[] aDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daDown, t, pureSurf);
        res[i][0] = spot * (aUp[index] - aDown[index]) / 2 / eps / (1 + dividends.getAlpha(i));
      } else {
        //forward difference for zero (or very near zero) alpha
        AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) + eps, i);
        double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pureSurf);
        res[i][0] = spot * (aUp[index] - base) / eps;
      }

      //bump beta    
      if (dividends.getBeta(i) > eps) {
        AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pureSurf);
        AffineDividends dbDown = dividends.withBeta(dividends.getBeta(i) - eps, i);
        double[] bDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbDown, swap.getTimeToObsEnd(), pureSurf);
        res[i][1] = (bUp[index] - bDown[index]) / 2 / eps;
      } else {
        //forward difference for zero (or near zero) beta       
        AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pureSurf);
        res[i][1] = (bUp[index] - base) / eps;
      }
    }

    return res;
  }

  private LocalVolatilitySurfaceStrike convertLV(final PureLocalVolatilitySurface from, final EquityDividendsCurvesBundle divs) {
    Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        double f = divs.getF(t);
        double d = divs.getD(t);
        if (s <= d) {
          return 0.0;
        }
        double x = (s - d) / (f - d);
        return s / (s - d) * from.getVolatility(t, x);
      }
    };
    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(func));
  }

  private PureLocalVolatilitySurface convertLV(final LocalVolatilitySurfaceStrike from, final EquityDividendsCurvesBundle divs) {
    Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double f = divs.getF(t);
        double d = divs.getD(t);

        double s = (f - d) * x + d;
        return s / (s - d) * from.getVolatility(t, s);
      }
    };
    return new PureLocalVolatilitySurface(FunctionalDoublesSurface.from(func));
  }

  /**
   * Compute the "bucked vega" of a equity variance swap - the sensitivity of the square-root of the expected variance (since this has the same scale as the implied volatilities)
   *  to the market implied volatilities. This is done by bumping each market implied volatility in turn, and computing the sensitivity by finite difference 
     * @param swap The details of the equality variance swap
     * @param spot current level of the underlying 
     * @param discountCurve The discount curve 
     * @param dividends The assumed dividends
     * @param marketVols the market implied volatilities 
   * @return bucked vega
   */
  public double[][] buckedVega(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;
    final int nExp = marketVols.getNumExpiries();
    double[][] res = new double[nExp][];
    for (int i = 0; i < nExp; i++) {
      final int nStrikes = marketVols.getStrikes()[i].length;
      res[i] = new double[nStrikes];
      for (int j = 0; j < nStrikes; j++) {
        SmileSurfaceDataBundle upVols = marketVols.withBumpedPoint(i, j, eps);
        double pUp = Math.sqrt(priceFromImpliedVols(swap, spot, discountCurve, dividends, upVols));
        SmileSurfaceDataBundle downVols = marketVols.withBumpedPoint(i, j, -eps);
        double pDown = Math.sqrt(priceFromImpliedVols(swap, spot, discountCurve, dividends, downVols));
        res[i][j] = (pUp - pDown) / 2 / eps;
      }
    }
    return res;
  }

  private PureLocalVolatilitySurface getPureLocalVolFromMarket(final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final DupireLocalVolatilityCalculator dCal = new DupireLocalVolatilityCalculator();
    PureImpliedVolatilitySurface piv = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    return dCal.getLocalVolatility(piv);
  }

  /**
   * Convert each market implied volatility to an implied volatility of an option on the 'pure' stock, the the VolatilitySurfaceInterpolator to construct a smooth
   * pure implied volatility surface 
   * @param spot The spot value of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends 
   * @param marketVols The market option prices expressed as implied volatilities 
   * @return pure implied volatility surface 
   */
  private PureImpliedVolatilitySurface getPureImpliedVolFromMarket(final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);

    //convert the real option prices to prices of options on pure stock, then find the implied volatility of these options
    final double[][] strikes = marketVols.getStrikes();
    final double[][] vols = marketVols.getVolatilities();
    final int nExp = marketVols.getNumExpiries();
    double[][] x = new double[nExp][];
    double[][] pVols = new double[nExp][];
    for (int i = 0; i < nExp; i++) {
      double t = marketVols.getExpiries()[i];
      double f = divCurves.getF(t);
      double d = divCurves.getD(t);
      final int n = strikes[i].length;
      x[i] = new double[n];
      pVols[i] = new double[n];
      for (int j = 0; j < n; j++) {
        double temp = strikes[i][j] - d;
        ArgumentChecker.isTrue(temp >= 0, "strike of {} at expiry {} is less than the discounts value of future cash dividends {}. Either remove this option or change the dividend assumption",
            strikes[i][j], t, d);
        x[i][j] = temp / (f - d);
        pVols[i][j] = volToPureVol(strikes[i][j], f, d, t, vols[i][j]);
      }
    }

    //fit an implied volatility surface to the pure implied vols (as the forward is 1.0, the BlackVolatilitySurfaceMoneyness is numerically identical to the PureImpliedVolatilitySurface
    SmileSurfaceDataBundle data = new StandardSmileSurfaceDataBundle(new ForwardCurve(1.0), marketVols.getExpiries(), x, pVols);
    BlackVolatilitySurfaceMoneyness surf = SURFACE_INTERPOLATOR.getVolatilitySurface(data);
    PureImpliedVolatilitySurface pureSurf = new PureImpliedVolatilitySurface(surf.getSurface()); //TODO have a direct fitter for PureImpliedVolatilitySurface
    return pureSurf;
  }

  /**
   * Convert the market out-the-money price to the implied volatility of an option on the 'pure' stock
    @param df The discount factor
   * @param k The Strike
   * @param f The forward 
   * @param d The discounted future cash dividends 
   * @param t The time-to-expiry
   * @param p The market out-the-money price
   * @return The implied volatility of an option on the 'pure' stock
   */
  private double priceToPureVol(final double df, final double k, final double f, final double d, final double t, final double p) {
    final boolean isCall = k >= f;
    final double pp = p / (f - d) / df;
    final double x = (k - d) / (f - d);
    return BlackFormulaRepository.impliedVolatility(pp, 1.0, x, t, isCall);
  }

  /**
   * Convert the market implied volatility to the implied volatility of an option on the 'pure' stock
   * @param k The Strike
   * @param f The forward 
   * @param d The discounted future cash dividends 
   * @param t The time-to-expiry
   * @param vol The market implied volatility
   * @return The implied volatility of an option on the 'pure' stock
   */
  private double volToPureVol(final double k, final double f, final double d, final double t, final double vol) {
    //with no cash dividends both implied volatilities are the same 
    if (d == 0) {
      return vol;
    }
    final boolean isCall = k >= f;
    final double p = BlackFormulaRepository.price(f, k, t, vol, isCall);
    final double pp = p / (f - d);
    final double x = (k - d) / (f - d);
    return BlackFormulaRepository.impliedVolatility(pp, 1.0, x, t, vol);
  }

}
