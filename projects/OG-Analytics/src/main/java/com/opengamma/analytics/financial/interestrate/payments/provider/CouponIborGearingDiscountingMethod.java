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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with gearing factor and spread.
 */
public final class CouponIborGearingDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborGearingDiscountingMethod INSTANCE = new CouponIborGearingDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborGearingDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborGearingDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor coupon with gearing factor and spread by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborGearing coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves");
    final double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon with gearing and spread by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborGearing coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Curves");
    final double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getFactor() * df * pvBar;
    final double dfBar = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurves.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

}
