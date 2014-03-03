/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and its sensitivities for OIS coupons.
 */
public final class CouponONSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponONSpreadDiscountingMethod INSTANCE = new CouponONSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponONSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponONSpreadDiscountingMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponONSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Market");
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor()
        * multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = (coupon.getNotionalAccrued() * ratio + coupon.getSpreadAmount() - coupon.getNotional()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the present value.
   * @param coupon the coupon.
   * @param multicurve the multi-curve provider.
   * @param forwardRateProvider the forward rate provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(
      final CouponONSpread coupon,
      final MulticurveProviderInterface multicurve,
      final ForwardRateProvider<IndexON> forwardRateProvider) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Market");
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor()
        * forwardRateProvider.getRate(multicurve, coupon, coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = (coupon.getNotionalAccrued() * ratio + coupon.getSpreadAmount() - coupon.getNotional()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a OIS coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponONSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double forward = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor() * forward;
    // Backward sweep
    final double pvBar = 1.0;
    final double ratioBar = coupon.getNotionalAccrued() * df * pvBar;
    final double forwardBar = coupon.getFixingPeriodAccrualFactor() * ratioBar;
    final double dfBar = (coupon.getNotionalAccrued() * ratio + coupon.getSpreadAmount() - coupon.getNotional()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

  /**
   * Computes the par rate, i.e. the fair rate for the remaining period. Does not take the spread into account.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The par rate.
   */
  public double parRate(final CouponONSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    return multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
  }

  /**
   * Computes the par rate sensitivity to the curve rates. Does not take the spread into account.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity parRateCurveSensitivity(final CouponONSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    // Backward sweep.
    final double forwardBar = 1.0;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.ofForward(mapFwd));
    return result;
  }

}
