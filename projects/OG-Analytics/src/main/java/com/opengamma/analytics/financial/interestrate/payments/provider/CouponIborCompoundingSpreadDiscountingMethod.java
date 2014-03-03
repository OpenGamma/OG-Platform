/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor compounding coupon with spread.
 * The definition of "Compounding" is available in the ISDA document:
 * Reference: Alternative compounding methods for over-the-counter derivative transactions (2009)
 */
public final class CouponIborCompoundingSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborCompoundingSpreadDiscountingMethod INSTANCE = new CouponIborCompoundingSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborCompoundingSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborCompoundingSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor compounded coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborCompoundingSpread coupon, final MulticurveProviderInterface multicurve) {
    return presentValue(coupon, multicurve, IborForwardRateProvider.getInstance());
  }

  /**
   * Compute the present value of a Ibor compounded coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @param forwardRateProvider The forward rate provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(
      final CouponIborCompoundingSpread coupon,
      final MulticurveProviderInterface multicurve,
      final ForwardRateProvider<IborIndex> forwardRateProvider) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(forwardRateProvider, "forwardRateProvider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double notionalAccrued = coupon.getNotionalAccrued();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      final double forward = forwardRateProvider.getRate(
          multicurve,
          coupon,
          coupon.getFixingPeriodStartTimes()[loopsub],
          coupon.getFixingPeriodEndTimes()[loopsub],
          coupon.getFixingPeriodAccrualFactors()[loopsub]);
      final double investFactor = 1.0 + coupon.getPaymentAccrualFactors()[loopsub] * (forward + coupon.getSpread());
      notionalAccrued *= investFactor;
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = (notionalAccrued - coupon.getNotional()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor compounded coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborCompoundingSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double notionalAccrued = coupon.getNotionalAccrued();
    final double[] forward = new double[nbSubPeriod];
    final double[] investFactor = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      forward[loopsub] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTimes()[loopsub], coupon.getFixingPeriodEndTimes()[loopsub],
          coupon.getFixingPeriodAccrualFactors()[loopsub]);
      investFactor[loopsub] = 1.0 + coupon.getPaymentAccrualFactors()[loopsub] * (forward[loopsub] + coupon.getSpread());
      notionalAccrued *= investFactor[loopsub];
    }
    final double dfPayment = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfPaymentBar = (notionalAccrued - coupon.getNotional()) * pvBar;
    final double notionalAccruedBar = dfPayment * pvBar;
    final double[] investFactorBar = new double[nbSubPeriod];
    final double[] forwardBar = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      investFactorBar[loopsub] = notionalAccrued / investFactor[loopsub] * notionalAccruedBar;
      forwardBar[loopsub] = coupon.getPaymentAccrualFactors()[loopsub] * investFactorBar[loopsub];
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * dfPayment * dfPaymentBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTimes()[loopsub], coupon.getFixingPeriodEndTimes()[loopsub], coupon.getFixingPeriodAccrualFactors()[loopsub],
          forwardBar[loopsub]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }
}
