/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and its sensitivities for OIS coupons.
 */
public final class CouponONDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponONDiscountingMethod INSTANCE = new CouponONDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponONDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponONDiscountingMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Market");
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor()
        * multicurve.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = (coupon.getNotionalAccrued() * ratio - coupon.getNotional()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a OIS coupon by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double forward = multicurve.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
    final double ratio = 1.0 + coupon.getFixingPeriodAccrualFactor() * forward;
    // Backward sweep
    final double pvBar = 1.0;
    final double ratioBar = coupon.getNotionalAccrued() * df * pvBar;
    final double forwardBar = coupon.getFixingPeriodAccrualFactor() * ratioBar;
    final double dfBar = (coupon.getNotionalAccrued() * ratio - coupon.getNotional()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new ForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

  /**
   * Computes the par rate, i.e. the fair rate for the remaining period.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The par rate.
   */
  public double parRate(final CouponON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    return multicurve.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor());
  }

  /**
   * Computes the par rate sensitivity to the curve rates.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity parRateCurveSensitivity(final CouponON coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    // Backward sweep.
    final double forwardBar = 1.0;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new ForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.ofForward(mapFwd));
    return result;
  }

}
