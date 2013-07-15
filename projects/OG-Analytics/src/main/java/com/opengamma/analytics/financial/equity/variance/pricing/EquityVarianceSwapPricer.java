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
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * Prices equity variance swaps using one of three methods:
 * <ul>
 * <li> Static replication
 * <li> Forward PDE
 * <li> Backward PDE
 * </ul>
 */
public final class EquityVarianceSwapPricer {
  /** Prices using a forward PDE */
  private static final EquityVarianceSwapForwardPurePDE VAR_SWAP_FWD_PDE_CALCULATOR = new EquityVarianceSwapForwardPurePDE();
  /** Prices using a backwards PDE */
  private static final EquityVarianceSwapBackwardsPurePDE VAR_SWAP_BKW_PDE_CALCULATOR = new EquityVarianceSwapBackwardsPurePDE();
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
    public EquityVarianceSwapPricer create() {
      return new EquityVarianceSwapPricer(this);
    }
  }

  /**
   * Provides a builder that can construct a pricer with values other than the defaults
   * @return The builder
   */
  public static Builder builder() {
    return new Builder();
  }

  private EquityVarianceSwapPricer(final Builder builder) {
    _surfaceInterpolator = new VolatilitySurfaceInterpolator(builder.getSmileInterpolator(), builder.getTimeInterpolator(), builder.useLogTime(),
        builder.useIntegratedVariance(), builder.useLogValue());
  }

  /**
   * Calculates the price of an equity variance swap from implied volatilities. The surface used is a local volatility surface.
   * @param swap The details of the equity variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The <b>annualised</b> variance
   */
  public double priceFromImpliedVolsBackwardPDE(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(marketVols, "market volatilities");
    final DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    final PureImpliedVolatilitySurface pureSurf = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
    final PureLocalVolatilitySurface plv = dupire.getLocalVolatility(pureSurf);
    final double t = swap.getTimeToSettlement();
    final double[] ev = VAR_SWAP_BKW_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plv);
    //TODO while calculating both with and without div correction is go for testing, don't want it for production
    final double res = (swap.correctForDividends() ? ev[0] : ev[1]) / t;
    return res;
  }

  /**
   * Calculates the delta of a variance swap using a local volatility surface.
   * <p>
   * The local volatility surface is treated as as invariant to the spot. The variance swap is priced twice with the spot bumped up and down.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double deltaWithStickyLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

    final PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final LocalVolatilitySurfaceStrike lv = VolatilitySurfaceConverter.convertLocalVolSurface(plv, divCurves);

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

    final double delta = (up - down) / 2 / eps / t;
    return delta;
  }

  /**
   * Calculates the gamma of a variance swap using a local volatility surface.
   * <p>
   * The local volatility surface is treated as invariant to the spot. The variance swap is priced three times; spot bumped up, down and left unchanged.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The delta of the variance swap under a sticky-strike assumption <b>scaled by spot</b>
   */
  public double gammaWithStickyLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

    final PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final LocalVolatilitySurfaceStrike lv = VolatilitySurfaceConverter.convertLocalVolSurface(plv, divCurves);

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

    final double gamma = (up + down - 2 * mid) / eps / eps / t;
    return gamma;
  }

  /**
   * Calculates the vega of a variance swap to a local volatility surface.
   * <p>
   * The vega is taken as the sensitivity of the <b>square-root</b> of the annualised expected variance (EV) (n.b. this is not the same as the expected volatility)
   * to a parallel shift of the local volatility surface.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The vega
   */
  public double vegaLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

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
    final double vega = (up - down) / 2 / eps;
    return vega;
  }

  /**
   * Calculates the vega of a variance swap to a pure local volatility surface.
   * <p>
   * The vega is taken as the sensitivity of the <b>square-root</b> of the annualised expected variance (EV) (n.b. this is not the same as the expected volatility)
   * to a parallel shift of the <b>pure</b> local volatility surface.
   * @param swap The details of the equality variance swap, not null
   * @param spot current level of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols the market implied volatilities, not null
   * @return The vega
   */
  public double vegaPureLocalVol(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");

    final PureLocalVolatilitySurface plv = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double eps = 1e-5;
    final int index = swap.correctForDividends() ? 0 : 1;
    final double t = swap.getTimeToSettlement();

    //up
    final PureLocalVolatilitySurface plvUp = new PureLocalVolatilitySurface(flooredShiftSurface(plv.getSurface(), eps));
    final double up = Math.sqrt(VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plvUp)[index] / t);
    //down
    final PureLocalVolatilitySurface plvDown = new PureLocalVolatilitySurface(flooredShiftSurface(plv.getSurface(), -eps));
    final double down = Math.sqrt(VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, t, plvDown)[index] / t);
    final double vega = (up - down) / 2 / eps;
    return vega;
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
   * Computes the price of a variance swap from implied volatilities by first computing a pure implied volatility surface, then using a forward PDE to 
   * calculate the expected variance.
   * @param swap The variance swap, not null
   * @param spot The spot value of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols The market option prices expressed as implied volatilities, not null
   * @return The price
   */
  public double priceFromImpliedVolsForwardPDE(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {

    final PureLocalVolatilitySurface pureSurf = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);

    final double[] ev = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance(spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    final double res = swap.correctForDividends() ? ev[0] : ev[1];
    return res;
  }

  /**
   * Computes the delta of a variance swap from implied volatilities by first computing a pure implied volatility surface, then treating this as an invariant while the spot
   * is moved.
   * @param swap The variance swap, not null
   * @param spot The spot value of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols The market option prices expressed as implied volatilities, not null
   * @return The delta
   */
  public double deltaFromLocalVols(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    final double eps = 1e-5;

    //this surface is assumed invariant to change in the spot
    final PureLocalVolatilitySurface pureSurf = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);

    //price the variance swap by static replication of the log-payoff and dividend correction terms
    final double[] evUp = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 + eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);
    final double[] evDown = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 - eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), pureSurf);

    final double res = swap.correctForDividends() ? (evUp[0] - evDown[0]) / spot / eps : (evUp[1] - evDown[1]) / spot / eps;
    return res;
  }

  /**
   * Computes the delta of a variance swap from implied volatilities by first computing a pure implied volatility surface, then treating this as an invariant while the spot
   * is moved.
   * @param swap The variance swap, not null
   * @param spot The spot value of the underlying
   * @param discountCurve The discount curve, not null
   * @param dividends The assumed dividends, not null
   * @param marketVols The market option prices expressed as implied volatilities, not null
   * @return The delta
   */
  public double deltaFromLocalVols2(final EquityVarianceSwap swap, final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(discountCurve, "discount curve");
    ArgumentChecker.notNull(dividends, "dividends");
    ArgumentChecker.notNull(marketVols, "market volatilities");
    final double eps = 1e-5;

    final EquityDividendsCurvesBundle div = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final PureLocalVolatilitySurface pureSurf = getPureLocalVolFromMarket(spot, discountCurve, dividends, marketVols);
    //this surface is assumed invariant to change in the spot
    final LocalVolatilitySurfaceStrike lv = convertLV(pureSurf, div);

    //price the variance swap by static replication of the log-payoff and dividend correction terms
    //up
    final EquityDividendsCurvesBundle divUp = new EquityDividendsCurvesBundle((1 + eps) * spot, discountCurve, dividends);
    final PureLocalVolatilitySurface plvUp = convertLV(lv, divUp);
    final double[] evUp = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 + eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), plvUp);
    //down
    final EquityDividendsCurvesBundle divDown = new EquityDividendsCurvesBundle((1 - eps) * spot, discountCurve, dividends);
    final PureLocalVolatilitySurface plvDown = convertLV(lv, divDown);
    final double[] evDown = VAR_SWAP_FWD_PDE_CALCULATOR.expectedVariance((1 - eps) * spot, discountCurve, dividends, swap.getTimeToSettlement(), plvDown);
    final double res = swap.correctForDividends() ? (evUp[0] - evDown[0]) / spot / eps : (evUp[1] - evDown[1]) / spot / eps;
    return res;
  }

  private static LocalVolatilitySurfaceStrike convertLV(final PureLocalVolatilitySurface from, final EquityDividendsCurvesBundle divs) {
    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        final double t = ts[0];
        final double s = ts[1];
        final double f = divs.getF(t);
        final double d = divs.getD(t);
        if (s <= d) {
          return 0.0;
        }
        final double x = (s - d) / (f - d);
        return s / (s - d) * from.getVolatility(t, x);
      }
    };
    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(func));
  }

  private static PureLocalVolatilitySurface convertLV(final LocalVolatilitySurfaceStrike from, final EquityDividendsCurvesBundle divs) {
    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = divs.getF(t);
        final double d = divs.getD(t);

        final double s = (f - d) * x + d;
        return s / (s - d) * from.getVolatility(t, s);
      }
    };
    return new PureLocalVolatilitySurface(FunctionalDoublesSurface.from(func));
  }

  private PureLocalVolatilitySurface getPureLocalVolFromMarket(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final SmileSurfaceDataBundle marketVols) {
    final DupireLocalVolatilityCalculator dCal = new DupireLocalVolatilityCalculator();
    final PureImpliedVolatilitySurface piv = getPureImpliedVolFromMarket(spot, discountCurve, dividends, marketVols);
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
  @SuppressWarnings("unused")
  private static double priceToPureVol(final double df, final double k, final double f, final double d, final double t, final double p) {
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
