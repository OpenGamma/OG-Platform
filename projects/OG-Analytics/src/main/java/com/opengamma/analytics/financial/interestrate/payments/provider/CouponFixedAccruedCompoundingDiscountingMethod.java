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

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for fixed accrued compounding coupon.
 */
public final class CouponFixedAccruedCompoundingDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponFixedAccruedCompoundingDiscountingMethod INSTANCE = new CouponFixedAccruedCompoundingDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponFixedAccruedCompoundingDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedAccruedCompoundingDiscountingMethod() {
  }

  /**
   * Compute the present value of a Fixed coupon by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponFixedAccruedCompounding coupon, final MulticurveProviderInterface multicurves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(multicurves, "multicurve");
    final double df = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double value = coupon.getAmount() * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), value);
  }

  /**
   * Computes the present value of the fixed coupon with positive notional (abs(notional) is used) by discounting.
   * @param coupon The coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public CurrencyAmount presentValuePositiveNotional(final CouponFixedAccruedCompounding coupon, final MulticurveProviderInterface multicurves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(multicurves, "multicurve");
    return CurrencyAmount.of(coupon.getCurrency(), Math.signum(coupon.getNotional()) * presentValue(coupon, multicurves).getAmount(coupon.getCurrency()));
  }

  /**
   * Computes the present value curve sensitivity of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param multicurve The multi-curve provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponFixedAccruedCompounding cpn, final MulticurveProviderInterface multicurve) {
    final double time = cpn.getPaymentTime();
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final DoublesPair s = DoublesPair.of(time, -time * cpn.getAmount() * multicurve.getDiscountFactor(cpn.getCurrency(), time));
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    mapDsc.put(multicurve.getName(cpn.getCurrency()), list);
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    result = result.plus(cpn.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    return result;
  }

}
