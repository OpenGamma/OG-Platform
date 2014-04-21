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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborSpreadDiscountingMethod}
 */
@Deprecated
public final class CouponIborSpreadDiscountingMethod implements PricingMethod {

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
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double value = (coupon.getNotional() * coupon.getPaymentYearFraction() * forward + coupon.getSpreadAmount()) * df;
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponIborSpread, "Coupon Ibor Spread");
    return presentValue((CouponIborSpread) instrument, curves);
  }

  /**
   * Computes the present value of the coupon without the spread part and with a positive notional.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value.
   */
  public CurrencyAmount presentValueNoSpreadPositiveNotional(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double value = (Math.abs(coupon.getNotional()) * coupon.getPaymentYearFraction() * forward) * df;
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / coupon.getFixingAccrualFactor();
    // final double pv = (coupon.getNotional() * coupon.getPaymentYearFraction() * forward + coupon.getSpreadAmount()) * df;
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * df * pvBar;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / coupon.getFixingAccrualFactor() * forwardBar;
    final double dfForwardStartBar = 1.0 / (coupon.getFixingAccrualFactor() * dfForwardEnd) * forwardBar;
    final double dfBar = (coupon.getNotional() * coupon.getPaymentYearFraction() * forward + coupon.getSpreadAmount()) * pvBar;
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    result = result.plus(coupon.getFundingCurveName(), listDiscounting);
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    result = result.plus(coupon.getForwardCurveName(), listForward);
    return result;
  }

  /**
   * Compute the par rate (Ibor forward) of a Ibor coupon by discounting. The par rate is the same with or without spread.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value.
   */
  public double parRate(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve curve = curves.getCurve(coupon.getForwardCurveName());
    return (curve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / curve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1.0) / coupon.getFixingAccrualFactor();
  }

  /**
   * Compute the par rate (Ibor forward) sensitivity to rates of a Ibor coupon by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The par rate curve sensitivity.
   */
  public InterestRateCurveSensitivity parRateCurveSensitivity(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    // Backward sweep
    final double parRateBar = 1.0;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / coupon.getFixingAccrualFactor() * parRateBar;
    final double dfForwardStartBar = 1.0 / (coupon.getFixingAccrualFactor() * dfForwardEnd) * parRateBar;
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    result = result.plus(coupon.getForwardCurveName(), listForward);
    return result;
  }

}
