/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborInArrearsSmileModelCapGenericReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborInArrearsSmileModelReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This class wraps {@link CapletStripper} and {@link GeneralSmileInterpolator}. 
 * Given multi-curves and market caps, derive caplet volatility surface. 
 * Then the volatility surface is used to compute pv of target in-arrear product
 */
public class CouponInArrearsCalculation {
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();

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
   * Compute present value of in-arrears caplet
   * @param caplet The caplet being priced
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(CapFloorIbor caplet, final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(caplet, "caplet");
    ArgumentChecker.notNull(interpolator, "interpolator");

    double expiry = caplet.getFixingTime();

    // Pick up relevant caplet strikes and vols
    double[] sampleStrikes = _capletStrippingResult.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

    // construct a interpolated/extrapolated smile
    double forward = caplet.accept(PRC, _curves);
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
        expiry, sampleVols);

    // compute pv 
    CapFloorIborInArrearsSmileModelCapGenericReplicationMethod inArrearsCal = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
        smileFunction);
    return inArrearsCal.presentValue(caplet, _curves);
  }

  /**
   * Compute present value of coupon in arrears
   * @param couponIbor The coupon being priced
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(CouponIbor couponIbor, final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(couponIbor, "couponIbor");
    ArgumentChecker.notNull(interpolator, "interpolator");

    double expiry = couponIbor.getFixingTime();

    // Pick up relevant caplet strikes and vols
    double[] sampleStrikes = _capletStrippingResult.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

      // construct a interpolated/extrapolated smile
    double forward = _curves.getSimplyCompoundForwardRate(couponIbor.getIndex(),
          couponIbor.getFixingPeriodStartTime(), couponIbor.getFixingPeriodEndTime(),
          couponIbor.getFixingAccrualFactor());
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes,
          expiry, sampleVols);

    // compute pv 
    CouponIborInArrearsSmileModelReplicationMethod inArrearsCal = new CouponIborInArrearsSmileModelReplicationMethod(
          smileFunction);
    return inArrearsCal.presentValue(couponIbor, _curves);
  }

  /**
   * Compute present value of in-arrears swap
   * @param swap The swap being priced, assuming 
   * @param interpolator The smile interpolator and extrapolator
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(Swap<? extends Payment, ? extends Payment> swap,
      final GeneralSmileInterpolator interpolator) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(interpolator, "interpolator");

    MultipleCurrencyAmount firstPV;
    MultipleCurrencyAmount secondPV;

    if (swap.getFirstLeg().getNthPayment(0) instanceof CouponIbor) {
      firstPV = presentValue((CouponIbor) swap.getFirstLeg().getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getFirstLeg().getNumberOfPayments(); j++) {
        firstPV.plus(presentValue((CouponIbor) swap.getFirstLeg().getNthPayment(j),
            interpolator));
      }
    } else {
      firstPV = swap.getFirstLeg().accept(PVDC, _curves);
    }

    if (swap.getSecondLeg().getNthPayment(0) instanceof CouponIbor) {
      secondPV = presentValue((CouponIbor) swap.getSecondLeg().getNthPayment(0), interpolator);
      for (int j = 1; j < swap.getSecondLeg().getNumberOfPayments(); j++) {
        secondPV.plus(presentValue((CouponIbor) swap.getSecondLeg().getNthPayment(j),
            interpolator));
      }
    } else {
      secondPV = swap.getSecondLeg().accept(PVDC, _curves);
    }

    return firstPV.plus(secondPV);
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
