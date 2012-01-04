/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.surface.StringValue;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Methods related to fixed coupons.
 */
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
  public CurrencyAmount presentValue(CouponFixed cpn, YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(cpn);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(cpn.getFundingCurveName());
    double pv = cpn.getAmount() * fundingCurve.getDiscountFactor(cpn.getPaymentTime());
    return CurrencyAmount.of(cpn.getCurrency(), pv);
  }

  /**
   * Computes the present value curve sensitivity of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(CouponFixed cpn, YieldCurveBundle curves) {
    final String curveName = cpn.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = cpn.getPaymentTime();
    final DoublesPair s = new DoublesPair(time, -time * cpn.getAmount() * discountingCurve.getDiscountFactor(time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, list);
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Compute the the present value curve sensitivity of a fixed coupon by discounting to a parallel curve movement.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public StringValue presentValueParallelCurveSensitivity(CouponFixed cpn, YieldCurveBundle curves) {
    final String curveName = cpn.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = cpn.getPaymentTime();
    double sensitivity = -time * cpn.getAmount() * discountingCurve.getDiscountFactor(time);
    return StringValue.from(curveName, sensitivity);
  }

}
