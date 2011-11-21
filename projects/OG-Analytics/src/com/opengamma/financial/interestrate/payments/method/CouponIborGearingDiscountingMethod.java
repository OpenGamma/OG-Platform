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

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with gearing factor and spread.
 */
public final class CouponIborGearingDiscountingMethod implements PricingMethod {

  private static final CouponIborGearingDiscountingMethod INSTANCE = new CouponIborGearingDiscountingMethod();

  /**
   * @return A static instance
   */
  public static CouponIborGearingDiscountingMethod getInstance() {
    return INSTANCE;
  }

  private CouponIborGearingDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor coupon with gearing factor and spread by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double value = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * df;
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponIborGearing, "Forward rate agreement");
    return presentValue((CouponIborGearing) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon with gearing and spread by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    final double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    final double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / coupon.getFixingAccrualFactor();
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getFactor() * df * pvBar;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / coupon.getFixingAccrualFactor() * forwardBar;
    final double dfForwardStartBar = 1.0 / (coupon.getFixingAccrualFactor() * dfForwardEnd) * forwardBar;
    final double dfBar = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * pvBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    resultMap.put(coupon.getFundingCurveName(), listDiscounting);
    final List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(coupon.getForwardCurveName(), listForward);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

}
