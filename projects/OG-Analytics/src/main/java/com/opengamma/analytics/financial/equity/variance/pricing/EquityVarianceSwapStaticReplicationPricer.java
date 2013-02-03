/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
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
public final class EquityVarianceSwapStaticReplicationPricer {
  /** Prices using static replication */
  private static final EquityVarianceSwapStaticReplication VAR_SWAP_CALCULATOR = new EquityVarianceSwapStaticReplication();

  /** The smile interpolator to use */
  private final VolatilitySurfaceInterpolator _surfaceInterpolator;

  /**
   * Builder class for this pricer.
   * <p>
   * The following default values are supplied:
   * <ul>
   * <li> Smile interpolator = spline
   * <li> Time interpolator = natural cubic spline with linear extrapolation
   * <li> Use log time = true
   * <li> Use integrated variance = true
   * <li> Use log value = true
   * </ul>
   */
  public static final class Builder {
    /** The smile interpolator to use */
    private final GeneralSmileInterpolator _smileInterpolator;
    /** The time interpolator to use */
    private final Interpolator1D _timeInterpolator;
    /** Use log time */
    private final boolean _useLogTime;
    /** Use integrated variance */
    private final boolean _useIntegratedVariance;
    /** Use log value */
    private final boolean _useLogValue;

    /* package */Builder() {
      this(new SmileInterpolatorSpline(), CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR),
          true, true, true);
    }

    /* package */Builder(final GeneralSmileInterpolator smileInterpolator, final Interpolator1D timeInterpolator, final boolean useLogTime, final boolean useIntegratedVariance,
        final boolean useLogValue) {
      ArgumentChecker.notNull(smileInterpolator, "smile interpolator");
      ArgumentChecker.notNull(timeInterpolator, "time interpolator");
      _smileInterpolator = smileInterpolator;
      _timeInterpolator = timeInterpolator;
      _useLogTime = useLogTime;
      _useIntegratedVariance = useIntegratedVariance;
      _useLogValue = useLogValue;
    }

    /**
     * @param smileInterpolator The smile interpolator, not null
     * @return a new Builder with this smile interpolator
     */
    public Builder withSmileInterpolator(final GeneralSmileInterpolator smileInterpolator) {
      return new Builder(smileInterpolator, _timeInterpolator, _useLogTime, _useIntegratedVariance, _useLogValue);
    }

    /**
     * @param timeInterpolator The time interpolator, not null
     * @return a new Builder with this time interpolator
     */
    public Builder timeInterpolator(final Interpolator1D timeInterpolator) {
      return new Builder(_smileInterpolator, timeInterpolator, _useLogTime, _useIntegratedVariance, _useLogValue);
    }

    /**
     * @param useLogTime true if log time is to be used
     * @return a new Builder with the log time parameter set to true
     */
    public Builder useLogTime(final boolean useLogTime) {
      return new Builder(_smileInterpolator, _timeInterpolator, useLogTime, _useIntegratedVariance, _useLogValue);
    }

    /**
     * @param useIntegratedVariance true if integrated variance is to be used
     * @return a new Builder with the integrated variance parameter set to true
     */
    public Builder useIntegratedVariance(final boolean useIntegratedVariance) {
      return new Builder(_smileInterpolator, _timeInterpolator, _useLogTime, useIntegratedVariance, _useLogValue);
    }

    /**
     * @param useLogValue true if log values are to be used
     * @return a new Builder with the log value parameter set to true
     */
    public Builder useLogValue(final boolean useLogValue) {
      return new Builder(_smileInterpolator, _timeInterpolator, _useLogTime, _useIntegratedVariance, useLogValue);
    }

    /* package */GeneralSmileInterpolator getSmileInterpolator() {
      return _smileInterpolator;
    }

    /* package */Interpolator1D getTimeInterpolator() {
      return _timeInterpolator;
    }

    /* package */boolean useLogTime() {
      return _useLogTime;
    }

    /* package */boolean useIntegratedVariance() {
      return _useIntegratedVariance;
    }

    /* package */boolean useLogValue() {
      return _useLogValue;
    }

    /**
     * @return The pricer instance
     */
    @SuppressWarnings("synthetic-access")
    public EquityVarianceSwapStaticReplicationPricer create() {
      return new EquityVarianceSwapStaticReplicationPricer(this);
    }
  }

  /**
   * Provides a builder that can construct a pricer with values other than the defaults
   * @return The builder
   */
  public static Builder builder() {
    return new Builder();
  }

  private EquityVarianceSwapStaticReplicationPricer(final Builder builder) {
    _surfaceInterpolator = new VolatilitySurfaceInterpolator(builder.getSmileInterpolator(), builder.getTimeInterpolator(), builder.useLogTime(),
        builder.useIntegratedVariance(), builder.useLogValue());
  }

  /**
   * Calculates the price of an equity variance swap from OTM option prices. The surface used is a pure implied volatility surface.
   * @param swap The details of the equity variance swap, not null
   * @param spot Current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param expiries The strips of option expiries, not null
   * @param strikes The strikes for each option strip, not null. Must have the same number of strips as expiries.
   * @param otmPrices The <b>out-of-the-money</b> option prices, not null. Must have the same number of strips as expiries and values as strikes.
   * @return The <b>annualised</b> variance
   */
  public double priceFromOTMPrices(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final double[] expiries, final double[][] strikes, final double[][] otmPrices) {
    ArgumentChecker.notNull(swap, "swap");
    final PureImpliedVolatilitySurface pureSurf = EquityVolatilityToPureVolatilitySurfaceConverter.getConvertedSurface(spot, discountCurve, dividends, expiries, strikes,
        otmPrices, _surfaceInterpolator);

    final double t = swap.getTimeToSettlement();
    //price the variance swap by static replication of the log-payoff and dividend correction terms
    final double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    final double res = (swap.correctForDividends() ? ev[0] : ev[1]) / t;
    return res;
  }

  /**
   * Calculates the delta of a variance swap.
   * <p>
   * The market implied volatilities are treated as invariant to the spot (sticky-strike), and price the variance swap twice with the spot bumped up and down. The
   * pricing itself involves finding pure implied volatilities, then interpolated implied volatility surface and finally the expected variance via static replication.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double deltaWithStickyStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");
    //here we assume the market implied volatilities are invariant to a change of spot
    final double eps = 1e-5;
    final double up = priceFromImpliedVols(swap, (1 + eps) * spot, discountCurve, dividends, marketVols);
    final double down = priceFromImpliedVols(swap, (1 - eps) * spot, discountCurve, dividends, marketVols);
    final double ssDelta = (up - down) / 2 / eps;
    return ssDelta;
  }

  /**
   * Compute the "bucketed vega" of a equity variance swap - the sensitivity of the square-root of the expected variance (since this has the same scale as the implied volatilities)
   * to the market implied volatilities. This is done by bumping each market implied volatility in turn, and computing the sensitivity by finite difference
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying
   * @param discountCurve The discount curve
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities
   * @return bucked vega
   */
  public double[][] bucketedVega(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final double eps = 1e-5;
    final int nExp = marketVols.getNumExpiries();
    final double[][] res = new double[nExp][];
    for (int i = 0; i < nExp; i++) {
      final int nStrikes = marketVols.getStrikes()[i].length;
      res[i] = new double[nStrikes];
      for (int j = 0; j < nStrikes; j++) {
        final SmileSurfaceDataBundle upVols = marketVols.withBumpedPoint(i, j, eps);
        final double pUp = Math.sqrt(priceFromImpliedVols(swap, spot, discountCurve, dividends, upVols));
        final SmileSurfaceDataBundle downVols = marketVols.withBumpedPoint(i, j, -eps);
        final double pDown = Math.sqrt(priceFromImpliedVols(swap, spot, discountCurve, dividends, downVols));
        res[i][j] = (pUp - pDown) / 2 / eps;
      }
    }
    return res;
  }

  /**
   * Calculates the gamma of a variance swap.
   * <p>
   * The market implied volatilities are treated as invariant to the spot (sticky-strike), and price the variance swap twice with the spot bumped up and down. The
   * pricing itself involves finding pure implied volatilities, then an interpolated implied volatility surface and finally the expected variance via static replication.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The gamma of the variance swap under a sticky-strike assumption <b>scaled by spot^2</b>
   */
  public double gammaWithStickyStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");
    //here we assume the market implied volatilities are invariant to a change of spot
    final double eps = 1e-5;
    final double up = priceFromImpliedVols(swap, (1 + eps) * spot, discountCurve, dividends, marketVols);
    final double mid = priceFromImpliedVols(swap, spot, discountCurve, dividends, marketVols);
    final double down = priceFromImpliedVols(swap, (1 - eps) * spot, discountCurve, dividends, marketVols);
    final double gamma = (up + down - 2 * mid) / eps / eps;
    return gamma;
  }

  /**
   * Calculates the price of an equity variance swap from implied volatilities. The surface used is a pure implied volatility surface.
   * @param swap The details of the equity variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The <b>annualised</b> variance
   */
  public double priceFromImpliedVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    final double t = swap.getTimeToSettlement();
    //price the variance swap by static replication of the log-payoff and dividend correction terms
    final double[] ev = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    final double res = (swap.correctForDividends() ? ev[0] : ev[1]) / t;
    return res;
  }

  /**
   * Calculates the delta of a variance swap using a pure implied volatility surface.
   * <p>
   * The (pure) implied volatilities are treated as invariant to the spot (sticky-pure strike which is similar to sticky delta),
   * The variance swap is priced twice with the spot bumped up and down.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double deltaWithStickyPureStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    final double up = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 + eps), discountCurve, dividends, t, pureSurf)[index];
    final double down = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 - eps), discountCurve, dividends, t, pureSurf)[index];

    final double ssDelta = (up - down) / 2 / eps / t;
    return ssDelta;
  }

  /**
   * Calculates the gamma of a variance swap using a pure implied volatility surface.
   * <p>
   * The (pure) implied volatilities as invariant to the spot (sticky-pure strike which is similar to sticky delta).
   * The variance swap is priced three times; spot bumped up, down and left unchanged.
   * @param swap The details of the equality variance swap
   * @param spot current level of the underlying
   * @param discountCurve The discount curve
   * @param dividends The assumed dividends
   * @param marketVols the market implied volatilities
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double gammaWithStickyPureStrike(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    final double up = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 + eps), discountCurve, dividends, t, pureSurf)[index];
    final double mid = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf)[index];
    final double down = VAR_SWAP_CALCULATOR.expectedVariance(spot * (1 - eps), discountCurve, dividends, t, pureSurf)[index];

    final double gamma = (up + down - 2 * mid) / eps / eps / t;
    return gamma;
  }

  /**
   * Calculates the vega of a variance swap to a pure implied volatility surface.
   * <p>
   * The vega is taken as the sensitivity to the <b>square-root</b> of the annualised expected variance (EV) (n.b. this is not the same as the expected volatility)
   * to a parallel shift of the implied volatility surface.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The vega
   */
  public double vegaImpVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

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
    final double vega = (up - down) / 2 / eps;
    return vega;
  }

  /**
   * Calculates the vega of a variance swap to a pure implied volatility surface.
   * <p>
   * The vega is taken as the sensitivity of the <b>square-root</b> of the annualised expected variance (EV) (n.b. this is not the same as the expected volatility)
   * to a parallel shift of the <b>pure</b> implied volatility surface.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The vega
   */
  public double vegaPureImpVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

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
    final double vega = (up - down) / 2 / eps;
    return vega;
  }

  /**
   * Compute a delta from the market implied volatilities by first computing a pure implied volatility surface, then treating this as an invariant while the spot is moved.
   * @param swap The variance swap, not null
   * @param spot The spot value of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols The market option prices expressed as implied volatilities, not null
   * @return The delta
   */
  public double deltaFromImpliedVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market vols");
    final double eps = 1e-5;

    //this surface is assumed invariant to change in the spot
    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);

    //price the variance swap by static replication of the log-payoff and dividend correction terms
    final double[] evUp = VAR_SWAP_CALCULATOR.expectedVariance((1 + eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    final double[] evDown = VAR_SWAP_CALCULATOR.expectedVariance((1 - eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);

    final double res = swap.correctForDividends() ? (evUp[0] - evDown[0]) / spot / eps : (evUp[1] - evDown[1]) / spot / eps;
    return res;
  }

  /**
   * Compute sensitivity of an equity variance swap to the dividends. Here the pure volatility surface is assumed to be invariant to a change of dividends
   * @param swap The equity swap, not null
   * @param spot The spot value of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols The market option prices expressed as implied volatilities, not null
   * @return Array of arrays containing dividend sensitivity. For n dividends, there are n rows, each containing two elements: the sensitivity to alpha and beta
   */
  public double[][] dividendSensitivityWithStickyPureVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve,
      final AffineDividends dividends, final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");
    final double eps = 1e-5;
    final double t = swap.getTimeToObsEnd();
    final int index = swap.correctForDividends() ? 0 : 1;
    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    final double base = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf)[index];

    final int n = dividends.getNumberOfDividends();

    final double[][] res = new double[n][2];
    for (int i = 0; i < n; i++) {
      //bump alpha
      if (dividends.getAlpha(i) > eps / (1 - eps)) {
        //up
        final AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) * (1 + eps) + eps, i);
        final double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pureSurf);
        //down
        final AffineDividends daDown = dividends.withAlpha(dividends.getAlpha(i) * (1 - eps) - eps, i);
        final double[] aDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daDown, t, pureSurf);
        res[i][0] = spot * (aUp[index] - aDown[index]) / 2 / eps / (1 + dividends.getAlpha(i));
      } else {
        //forward difference for zero (or very near zero) alpha
        final AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) + eps, i);
        final double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pureSurf);
        res[i][0] = spot * (aUp[index] - base) / eps;
      }

      //bump beta
      if (dividends.getBeta(i) > eps) {
        final AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        final double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pureSurf);
        final AffineDividends dbDown = dividends.withBeta(dividends.getBeta(i) - eps, i);
        final double[] bDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbDown, swap.getTimeToObsEnd(), pureSurf);
        res[i][1] = (bUp[index] - bDown[index]) / 2 / eps;
      } else {
        //forward difference for zero (or near zero) beta
        final AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        final double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pureSurf);
        res[i][1] = (bUp[index] - base) / eps;
      }
    }

    return res;
  }

  /**
   * Compute sensitivity of an equity variance swap to the dividends. The "market" implied volatility surface is assumed to be invariant to a change of dividends
   * @param swap The equity swap, not null
   * @param spot The spot value of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols The market option prices expressed as implied volatilities, not null
   * @return Array of arrays containing dividend sensitivity. For n dividends, there are n rows, each containing two elements: the sensitivity to alpha and beta
   */
  public double[][] dividendSensitivityWithStickyImpliedVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve,
      final AffineDividends dividends, final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market vols");
    final double eps = 1e-5;
    final double t = swap.getTimeToObsEnd();
    final int index = swap.correctForDividends() ? 0 : 1;
    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    final double base = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, pureSurf)[index];

    final int n = dividends.getNumberOfDividends();

    final double[][] res = new double[n][2];
    for (int i = 0; i < n; i++) {
      //bump alpha
      if (dividends.getAlpha(i) > eps / (1 - eps)) {
        //up
        final AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) * (1 + eps) + eps, i);
        final PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, daUp, marketVols);
        final double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pvUp);
        //down
        final AffineDividends daDown = dividends.withAlpha(dividends.getAlpha(i) * (1 - eps) - eps, i);
        final PureImpliedVolatilitySurface pvDown = getPureImpliedVolFromMarket(spot, discountCurve, daDown, marketVols);
        final double[] aDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daDown, t, pvDown);
        res[i][0] = spot * (aUp[index] - aDown[index]) / 2 / eps / (1 + dividends.getAlpha(i));
      } else {
        //forward difference for zero (or very near zero) alpha
        final AffineDividends daUp = dividends.withAlpha(dividends.getAlpha(i) + eps, i);
        final PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, daUp, marketVols);
        final double[] aUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, daUp, t, pvUp);
        res[i][0] = spot * (aUp[index] - base) / eps;
      }

      //bump beta
      if (dividends.getBeta(i) > eps) {
        final AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        final PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, dbUp, marketVols);
        final double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pvUp);
        final AffineDividends dbDown = dividends.withBeta(dividends.getBeta(i) - eps, i);
        final PureImpliedVolatilitySurface pvDown = getPureImpliedVolFromMarket(spot, discountCurve, dbDown, marketVols);
        final double[] bDown = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbDown, swap.getTimeToObsEnd(), pvDown);
        res[i][1] = (bUp[index] - bDown[index]) / 2 / eps;
      } else {
        //forward difference for zero (or near zero) beta
        final AffineDividends dbUp = dividends.withBeta(dividends.getBeta(i) + eps, i);
        final PureImpliedVolatilitySurface pvUp = getPureImpliedVolFromMarket(spot, discountCurve, dbUp, marketVols);
        final double[] bUp = VAR_SWAP_CALCULATOR.expectedVariance(spot, discountCurve, dbUp, swap.getTimeToObsEnd(), pvUp);
        res[i][1] = (bUp[index] - base) / eps;
      }
    }
    return res;
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
  private PureImpliedVolatilitySurface getPureImpliedVolFromMarket(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);

    //convert the real option prices to prices of options on pure stock, then find the implied volatility of these options
    final double[][] strikes = marketVols.getStrikes();
    final double[][] vols = marketVols.getVolatilities();
    final int nExp = marketVols.getNumExpiries();
    final double[][] x = new double[nExp][];
    final double[][] pVols = new double[nExp][];
    for (int i = 0; i < nExp; i++) {
      final double t = marketVols.getExpiries()[i];
      final double f = divCurves.getF(t);
      final double d = divCurves.getD(t);
      final int n = strikes[i].length;
      x[i] = new double[n];
      pVols[i] = new double[n];
      for (int j = 0; j < n; j++) {
        final double temp = strikes[i][j] - d;
        ArgumentChecker.isTrue(temp >= 0,
            "strike of {} at expiry {} is less than the discounts value of future cash dividends {}. Either remove this option or change the dividend assumption",
            strikes[i][j], t, d);
        x[i][j] = temp / (f - d);
        pVols[i][j] = volToPureVol(strikes[i][j], f, d, t, vols[i][j]);
      }
    }

    //fit an implied volatility surface to the pure implied vols (as the forward is 1.0, the BlackVolatilitySurfaceMoneyness is numerically identical to the PureImpliedVolatilitySurface
    final SmileSurfaceDataBundle data = new StandardSmileSurfaceDataBundle(new ForwardCurve(1.0), marketVols.getExpiries(), x, pVols);
    final BlackVolatilitySurfaceMoneyness surf = _surfaceInterpolator.getVolatilitySurface(data);
    final PureImpliedVolatilitySurface pureSurf = new PureImpliedVolatilitySurface(surf.getSurface()); //TODO have a direct fitter for PureImpliedVolatilitySurface
    return pureSurf;
  }

  //shift a surface flooring the result at zero
  private static Surface<Double, Double, Double> flooredShiftSurface(final Surface<Double, Double, Double> from, final double amount) {
    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double temp = from.getZValue(x[0], x[1]) + amount;
        return Math.max(0.0, temp);
      }
    };
    return FunctionalDoublesSurface.from(surf);
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
  private static double volToPureVol(final double k, final double f, final double d, final double t, final double vol) {
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
