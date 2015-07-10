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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon.
 */
public final class CouponIborSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborSpreadDiscountingMethod INSTANCE = new CouponIborSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor coupon with spread by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborSpread coupon, final MulticurveProviderInterface multicurves) {
    return presentValue(coupon, multicurves, IborForwardRateProvider.getInstance());
  }

  /**
   * Compute the present value of a Ibor coupon with spread using a specific forward rate provider by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @param forwardRateProvider The forward rate provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(
      final CouponIborSpread coupon,
      final MulticurveProviderInterface multicurves,
      final ForwardRateProvider<IborIndex> forwardRateProvider) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurves, "multicurves");
    ArgumentChecker.notNull(forwardRateProvider, "forwardRateProvider");
    final double forward = forwardRateProvider.getRate(
        multicurves,
        coupon,
        coupon.getFixingPeriodStartTime(),
        coupon.getFixingPeriodEndTime(),
        coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = (coupon.getNotional() * coupon.getPaymentYearFraction() * forward + coupon.getSpreadAmount()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  /**
   * Computes the present value of the coupon without the spread part and with a positive notional.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueNoSpreadPositiveNotional(final CouponIborSpread coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves");
    final double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = (Math.abs(coupon.getNotional()) * coupon.getPaymentYearFraction() * forward) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborSpread coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves");
    final double forward = multicurves.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * df * pvBar;
    final double dfBar = (coupon.getNotional() * coupon.getPaymentYearFraction() * forward + coupon.getSpreadAmount()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurves.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(coupon.getIndex()), listForward);
    MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(),
        MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

}
