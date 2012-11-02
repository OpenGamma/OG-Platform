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

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.method.PricingProviderMethod;
import com.opengamma.analytics.financial.provider.sensitivity.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with gearing factor and spread.
 */
public final class CouponIborGearingDiscountingProviderMethod implements PricingProviderMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborGearingDiscountingProviderMethod INSTANCE = new CouponIborGearingDiscountingProviderMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborGearingDiscountingProviderMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborGearingDiscountingProviderMethod() {
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
    final double forward = multicurves.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final MulticurveProviderInterface multicurve) {
    Validate.isTrue(instrument instanceof CouponIborGearing, "Coupon Ibor Gearing");
    return presentValue((CouponIborGearing) instrument, multicurve);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon with gearing and spread by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket presentValueCurveSensitivity(final CouponIborGearing coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Curves");
    final double forward = multicurves.getForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor());
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getFactor() * df * pvBar;
    final double dfBar = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurves.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    final List<ForwardSensitivity> listForward = new ArrayList<ForwardSensitivity>();
    listForward.add(new ForwardSensitivity(coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(), coupon.getFixingAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyCurveSensitivityMarket result = MultipleCurrencyCurveSensitivityMarket.of(coupon.getCurrency(), CurveSensitivityMarket.ofYieldDiscountingAndForward(mapDsc, mapFwd));
    return result;
  }

}
