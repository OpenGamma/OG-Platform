/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Methods related to fixed coupons.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod}
 */
@Deprecated
public final class CouponFixedDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponFixedDiscountingMethod INSTANCE = new CouponFixedDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponFixedDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedDiscountingMethod() {
  }

  /**
   * Computes the present value of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponFixed cpn, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(cpn);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(cpn.getFundingCurveName());
    final double pv = cpn.getAmount() * fundingCurve.getDiscountFactor(cpn.getPaymentTime());
    return CurrencyAmount.of(cpn.getCurrency(), pv);
  }

  /**
   * Computes the present value of the fixed coupon with positive notional (abs(notional) is used) by discounting.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValuePositiveNotional(final CouponFixed cpn, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(cpn);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(cpn.getFundingCurveName());
    final double pv = cpn.getPaymentYearFraction() * Math.abs(cpn.getNotional()) * cpn.getFixedRate() * fundingCurve.getDiscountFactor(cpn.getPaymentTime());
    return CurrencyAmount.of(cpn.getCurrency(), pv);
  }

  /**
   * Computes the present value curve sensitivity of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponFixed cpn, final YieldCurveBundle curves) {
    final String curveName = cpn.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = cpn.getPaymentTime();
    final DoublesPair s = DoublesPair.of(time, -time * cpn.getAmount() * discountingCurve.getDiscountFactor(time));
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(curveName, list);
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Compute the the present value curve sensitivity of a fixed coupon by discounting to a parallel curve movement.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public StringAmount presentValueParallelCurveSensitivity(final CouponFixed cpn, final YieldCurveBundle curves) {
    final String curveName = cpn.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = cpn.getPaymentTime();
    final double sensitivity = -time * cpn.getAmount() * discountingCurve.getDiscountFactor(time);
    return StringAmount.from(curveName, sensitivity);
  }

}
