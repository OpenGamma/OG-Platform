/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import java.util.Arrays;

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
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
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

    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    double res = swap.correctForDividends() ? ev[0] : ev[1];
    return res;
  }

  public double priceFromImpliedVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    //price the variance swap by static replication of the log-payoff and dividend correction terms 
    double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    double res = swap.correctForDividends() ? ev[0] : ev[1];
    return res;
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
   * Compute the pure implied volatility (from the market implied vol and the dividends), then hold this surface constant while adjusting the dividends 
   * @param swap The equity swap 
   * @param spot The spot value of the underlying 
   * @param discountCurve The discount curve 
   * @param dividends The assumed dividends 
   * @param marketVols The market option prices expressed as implied volatilities 
   * @return Array of arrays containing dividend sensitivity. For n dividends, there are n rows, each containing two elements: the sensitivity to alpha and beta
   */
  public double[][] dividendSensitivity2(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    double base = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, swap.getTimeToObsEnd(), pureSurf)[index];

    final int n = dividends.getNumberOfDividends();

    double[] alpha = Arrays.copyOf(dividends.getAlpha(), n);
    double[] beta = Arrays.copyOf(dividends.getBeta(), n);
    double[][] res = new double[n][2];
    for (int i = 0; i < n; i++) {
      //bump alpha
      double tAlpha = alpha[i];
      if (tAlpha > eps / (1 - eps)) {
        alpha[i] = tAlpha + (1 + tAlpha) * eps;
        AffineDividends daUp = new AffineDividends(dividends.getTau(), alpha, dividends.getBeta());
        double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, swap.getTimeToObsEnd(), pureSurf);
        alpha[i] = tAlpha - (1 + tAlpha) * eps;
        AffineDividends daDown = new AffineDividends(dividends.getTau(), alpha, dividends.getBeta());
        double[] aDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daDown, swap.getTimeToObsEnd(), pureSurf);
        res[i][0] = (aUp[index] - aDown[index]) / 2 / eps / (1 + dividends.getAlpha(i));
      } else {
        //forward difference for zero (or very near zero) alpha
        alpha[i] = tAlpha + eps;
        AffineDividends daUp = new AffineDividends(dividends.getTau(), alpha, dividends.getBeta());
        double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, swap.getTimeToObsEnd(), pureSurf);
        res[i][0] = (aUp[index] - base) / eps;
      }
      alpha[i] = tAlpha;
      //bump beta
      double tBeta = beta[i];
      if (tBeta > eps) {
        beta[i] = tBeta + eps;
        AffineDividends dbUp = new AffineDividends(dividends.getTau(), dividends.getAlpha(), beta);
        double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pureSurf);
        beta[i] = tBeta - eps;
        AffineDividends dbDown = new AffineDividends(dividends.getTau(), dividends.getAlpha(), beta);
        double[] bDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbDown, swap.getTimeToObsEnd(), pureSurf);
        res[i][1] = (bUp[index] - bDown[index]) / 2 / eps;
      } else {
        //forward difference for zero (or near zero) beta
        beta[i] = tBeta + eps;
        AffineDividends dbUp = new AffineDividends(dividends.getTau(), dividends.getAlpha(), beta);
        double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pureSurf);
        res[i][1] = (bUp[index] - base) / eps;
      }
      beta[i] = tBeta;
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
        double pUp = priceFromImpliedVols(swap, spot, discountCurve, dividends, upVols);
        SmileSurfaceDataBundle downVols = marketVols.withBumpedPoint(i, j, -eps);
        double pDown = priceFromImpliedVols(swap, spot, discountCurve, dividends, downVols);
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
