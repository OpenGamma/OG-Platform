/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFloating;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborInArrearsSmileModelCapGenericReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborInArrearsSmileModelReplicationMethod;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This class wraps {@link CapletStripper} and {@link GeneralSmileInterpolator}. 
 * Given multi-curves and market caps, derive caplet volatility surface. 
 * Then the volatility surface is used to compute pv of target in-arrear products
 */
public class CouponInArrearsCalculation {
  private final CapletStrippingResult _capletStrippingResult;
  private final MulticurveProviderInterface _curves;
  private final InterpolatedDoublesSurface _surface;

  private final double _time;

  /**
   * Constructor
   * @param stripper The caplet stripper
   * @param caps The market caps
   * @param mrkPrices The market cap prices/volatilities
   * @param type PRICE or VOL
   * @param errors The error values
   * @param guess The guess parameters
   * @param curves The multi curve
   */
  public CouponInArrearsCalculation(CapletStripper stripper, List<CapFloor> caps, double[] mrkPrices,
      MarketDataType type, double[] errors, DoubleMatrix1D guess, MulticurveProviderInterface curves) {

    // perform caplet stripping
    long t0 = System.nanoTime();
    _capletStrippingResult = stripper.solve(mrkPrices, type, errors, guess);
    long t1 = System.nanoTime();
    _time = (t1 - t0) * 1e-9;
    _curves = curves;

    // The stripper works with discrete caplets. Need to construct a continuous surface in order to sample at any expiry-strike
    // For strippers that work with a volatility surface (e.g. the smile based strippers), we have thrown away information
    // which would be useful here
    DoublesPair[] expStrikes = _capletStrippingResult.getPricer().getExpiryStrikeArray();
    DoubleMatrix1D vols = _capletStrippingResult.getCapletVols();
    CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(interpolator, interpolator);

    _surface = new InterpolatedDoublesSurface(expStrikes, vols.getData(), interpolator2D);

  }

  /**
   * Computes non-corrected price
   * @param option caplet as option
   * @return The caplet price
   */
  public double simpleOptionPrice(SimpleOptionData option) {
    return BlackFormulaRepository.price(option, _surface.getZValue(option.getTimeToExpiry(), option.getStrike()));
  }

  /**
   * Computes non-corrected price
   * @param caplet The in-arrears caplet 
   * @return The caplet price
   */
  public double simpleCapletPrice(CapFloorIbor caplet) {
    // Construct a "standard" CapFloorIbor whose paymentTime is set to be fixingPeriodEndTime
    CapFloorIbor capStandard = new CapFloorIbor(caplet.getCurrency(), caplet.getFixingPeriodEndTime(),
        caplet.getPaymentYearFraction(), caplet.getNotional(), caplet.getFixingTime(), caplet.getIndex(),
        caplet.getFixingPeriodStartTime(), caplet.getFixingPeriodEndTime(), caplet.getFixingAccrualFactor(),
        caplet.getStrike(), caplet.isCap());
    SimpleOptionData option = CapFloorDecomposer.toOption(capStandard, _curves);
    return simpleOptionPrice(option);
  }

  /**
   * Compute present value
   * @param couponFloating The coupon being priced, must be {@link CapFloorIbor} or {@link CouponIbor}
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(CouponFloating couponFloating, final GeneralSmileInterpolator interpolator) {
    double expiry = couponFloating.getFixingTime();

    // Pick up relevant caplet strikes and vols
    double[] sampleStrikes = _capletStrippingResult.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

    // compute pv 
    if (couponFloating instanceof CapFloorIbor) {
      CapFloorIbor caplet = (CapFloorIbor) couponFloating;

      // construct a interpolated/extrapolated smile
      double forward = _curves.getSimplyCompoundForwardRate(caplet.getIndex(), caplet.getFixingPeriodStartTime(),
          caplet.getFixingPeriodEndTime(), caplet.getFixingAccrualFactor());
      InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
          expiry, sampleVols);

      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod inArrearsCal = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunction);
      return inArrearsCal.presentValue(caplet, _curves);
    }
    if (couponFloating instanceof CouponIbor) {
      CouponIbor couponIbor = (CouponIbor) couponFloating;

      // construct a interpolated/extrapolated smile
      double forward = _curves.getSimplyCompoundForwardRate(couponIbor.getIndex(),
          couponIbor.getFixingPeriodStartTime(), couponIbor.getFixingPeriodEndTime(),
          couponIbor.getFixingAccrualFactor());
      InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
          expiry, sampleVols);

      CouponIborInArrearsSmileModelReplicationMethod inArrearsCal = new CouponIborInArrearsSmileModelReplicationMethod(
          smileFunction);
      return inArrearsCal.presentValue(couponIbor, _curves);
    }
    throw new IllegalArgumentException("couponFloating should be CapFloorIbor or CouponIbor");
  }

  /**
   * Gets the time.
   * @return the time
   */
  public double getTime() {
    return _time;
  }

  /**
   * Gets the chi-square
   * @return chi-square
   */
  public double getChiSq() {
    return _capletStrippingResult.getChiSqr();
  }
}
