/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with the discounting on futures based curves.
 * <p> Reference: My future is not convex. Henrard, M. SSRN working paper. Available at: http://ssrn.com/abstract=2053657
 */
public final class CouponIborDiscountingFuturesMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborDiscountingFuturesMethod INSTANCE = new CouponIborDiscountingFuturesMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborDiscountingFuturesMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborDiscountingFuturesMethod() {
  }

  /**
   * The Hull-White model.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  /**
   * Compute the present value of a Ibor coupon by discounting with futures based curves.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponIbor coupon, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    double ratioDfForward = dfForwardStart / dfForwardEnd;
    double convexityFactor = MODEL.futureConvexityFactor(curves.getHullWhiteParameter(), coupon.getFixingTime(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime());
    final double dfPayment = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double value = coupon.getNotional() * coupon.getPaymentYearFraction() / coupon.getFixingAccrualFactor() * dfPayment * (ratioDfForward / convexityFactor - 1);
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponIborGearing, "Forward rate agreement");
    return presentValue(instrument, curves);
  }

}
