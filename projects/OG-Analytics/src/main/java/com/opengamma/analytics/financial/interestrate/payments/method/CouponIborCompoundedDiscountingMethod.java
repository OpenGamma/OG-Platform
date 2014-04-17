/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor compounded coupon.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingDiscountingMethod}
 */
@Deprecated
public final class CouponIborCompoundedDiscountingMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborCompoundedDiscountingMethod INSTANCE = new CouponIborCompoundedDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborCompoundedDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborCompoundedDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor compounded coupon by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponIborCompounding coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double notionalAccrued = coupon.getNotionalAccrued();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      final double ratioForward = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTimes()[loopsub]) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTimes()[loopsub]);
      notionalAccrued *= ratioForward;
    }
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double pv = (notionalAccrued - coupon.getNotional()) * df;
    return CurrencyAmount.of(coupon.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponIborCompounding, "CouponIborCompounded");
    return presentValue((CouponIborCompounding) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor compounded coupon by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponIborCompounding coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double notionalAccrued = coupon.getNotionalAccrued();
    final double[] ratioForward = new double[nbSubPeriod];
    final double[] dfStart = new double[nbSubPeriod];
    final double[] dfEnd = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      dfStart[loopsub] = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTimes()[loopsub]);
      dfEnd[loopsub] = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTimes()[loopsub]);
      ratioForward[loopsub] = dfStart[loopsub] / dfEnd[loopsub];
      notionalAccrued *= ratioForward[loopsub];
    }
    final double dfPayment = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfPaymentBar = (notionalAccrued - coupon.getNotional()) * pvBar;
    final double notionalAccruedBar = dfPayment * pvBar;
    final double[] ratioForwardBar = new double[nbSubPeriod];
    final double[] dfStartBar = new double[nbSubPeriod];
    final double[] dfEndBar = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      ratioForwardBar[loopsub] = notionalAccrued / ratioForward[loopsub] * notionalAccruedBar;
      dfEndBar[loopsub] = -dfStart[loopsub] / (dfEnd[loopsub] * dfEnd[loopsub]) * ratioForwardBar[loopsub];
      dfStartBar[loopsub] = ratioForwardBar[loopsub] / dfEnd[loopsub];
    }
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * dfPayment * dfPaymentBar));
    result = result.plus(coupon.getFundingCurveName(), listDiscounting);
    final List<DoublesPair> listForward = new ArrayList<>();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      listForward.add(DoublesPair.of(coupon.getFixingPeriodStartTimes()[loopsub], -coupon.getFixingPeriodStartTimes()[loopsub] * dfStart[loopsub] * dfStartBar[loopsub]));
      listForward.add(DoublesPair.of(coupon.getFixingPeriodEndTimes()[loopsub], -coupon.getFixingPeriodEndTimes()[loopsub] * dfEnd[loopsub] * dfEndBar[loopsub]));
    }
    result = result.plus(coupon.getForwardCurveName(), listForward);
    return result;
  }

}
