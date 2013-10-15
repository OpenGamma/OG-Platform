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

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and its sensitivities for OIS coupons.
 * @deprecated {@link PricingMethod} is deprecated.
 */
@Deprecated
public final class CouponONDiscountingMethod implements PricingMethod {

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
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponON coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double ratio = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double value = (coupon.getNotionalAccrued() * ratio - coupon.getNotional()) * df;
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponON, "Coupon OIS");
    return presentValue((CouponON) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a OIS coupon by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value curve sensitivities.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponON coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double dfRatioStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    final double dfRatioEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    final double ratio = dfRatioStart / dfRatioEnd;
    // Backward sweep
    final double pvBar = 1.0;
    final double ratioBar = coupon.getNotionalAccrued() * df * pvBar;
    final double dfRatioEndBar = -dfRatioStart / (dfRatioEnd * dfRatioEnd) * ratioBar;
    final double dfRatioStartBar = 1.0 / dfRatioEnd * ratioBar;
    final double dfBar = (coupon.getNotionalAccrued() * ratio - coupon.getNotional()) * pvBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    resultMapDsc.put(coupon.getFundingCurveName(), listDiscounting);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMapDsc);
    final Map<String, List<DoublesPair>> resultMapFwd = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfRatioStart * dfRatioStartBar));
    listForward.add(DoublesPair.of(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfRatioEnd * dfRatioEndBar));
    resultMapFwd.put(coupon.getForwardCurveName(), listForward);
    result = result.plus(new InterestRateCurveSensitivity(resultMapFwd));
    return result;
  }

  /**
   * Computes the par rate, i.e. the fair rate for the remaining period.
   * @param coupon The coupon.
   * @param curves The curves.
   * @return The par rate.
   */
  public double parRate(final CouponON coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / coupon.getFixingPeriodAccrualFactor();
    return forward;
  }

  /**
   * Computes the par rate sensitivity to the curve rates.
   * @param coupon The coupon.
   * @param curves The curves.
   * @return The sensitivities.
   */
  public InterestRateCurveSensitivity parRateCurveSensitivity(final CouponON coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    // Backward sweep.
    final double forwardBar = 1.0;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / coupon.getFixingPeriodAccrualFactor() * forwardBar;
    final double dfForwardStartBar = 1.0 / (coupon.getFixingPeriodAccrualFactor() * dfForwardEnd) * forwardBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(coupon.getForwardCurveName(), listForward);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

}
