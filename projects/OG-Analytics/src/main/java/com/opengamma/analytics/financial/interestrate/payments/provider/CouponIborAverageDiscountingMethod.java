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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
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

public final class CouponIborAverageDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborAverageDiscountingMethod INSTANCE = new CouponIborAverageDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborAverageDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborAverageDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor average coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborAverage coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double forward1 = multicurves.getSimplyCompoundForwardRate(coupon.getIndex1(), coupon.getFixingPeriodStartTime1(), coupon.getFixingPeriodEndTime1(), coupon.getFixingAccrualFactor1());
    final double forward2 = multicurves.getSimplyCompoundForwardRate(coupon.getIndex2(), coupon.getFixingPeriodStartTime2(), coupon.getFixingPeriodEndTime2(), coupon.getFixingAccrualFactor2());
    final double forward = coupon.getWeight1() * forward1 + coupon.getWeight2() * forward2;
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = coupon.getNotional() * coupon.getPaymentYearFraction() * forward * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to yield for discounting curve and forward rate (in index convention) for forward curve.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborAverage coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Curves");
    final double forward1 = multicurve.getSimplyCompoundForwardRate(coupon.getIndex1(), coupon.getFixingPeriodStartTime1(), coupon.getFixingPeriodEndTime1(), coupon.getFixingAccrualFactor1());
    final double forward2 = multicurve.getSimplyCompoundForwardRate(coupon.getIndex2(), coupon.getFixingPeriodStartTime2(), coupon.getFixingPeriodEndTime2(), coupon.getFixingAccrualFactor2());
    final double forward = coupon.getWeight1() * forward1 + coupon.getWeight2() * forward2;
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * df * pvBar;
    final double dfBar = coupon.getNotional() * coupon.getPaymentYearFraction() * forward * pvBar;
    final double forward1Bar = coupon.getWeight1() * forwardBar;
    final double forward2Bar = coupon.getWeight2() * forwardBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward1 = new ArrayList<>();
    final List<ForwardSensitivity> listForward2 = new ArrayList<>();
    listForward1.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime1(), coupon.getFixingPeriodEndTime1(), coupon.getFixingAccrualFactor1(), forward1Bar));
    mapFwd.put(multicurve.getName(coupon.getIndex1()), listForward1);
    listForward2.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime2(), coupon.getFixingPeriodEndTime2(), coupon.getFixingAccrualFactor2(), forward2Bar));
    mapFwd.put(multicurve.getName(coupon.getIndex2()), listForward2);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

}
