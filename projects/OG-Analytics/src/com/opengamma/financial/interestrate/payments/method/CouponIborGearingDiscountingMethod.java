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

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor coupon with gearing factor and spread.
 */
public class CouponIborGearingDiscountingMethod implements PricingMethod {

  /**
   * Compute the present value of a Ibor coupon with gearing factor and spread by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    double forward = (forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime()) - 1) / coupon.getFixingAccrualFactor();
    double value = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    return CurrencyAmount.of(coupon.getCurrency(), value);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponIborGearing, "Forward rate agreement");
    return presentValue((CouponIborGearing) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor coupon with gearing and spread by discounting.
   * @param coupon The coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(curves, "Curves");
    YieldAndDiscountCurve forwardCurve = curves.getCurve(coupon.getForwardCurveName());
    YieldAndDiscountCurve discountingCurve = curves.getCurve(coupon.getFundingCurveName());
    double df = discountingCurve.getDiscountFactor(coupon.getPaymentTime());
    double dfForwardStart = forwardCurve.getDiscountFactor(coupon.getFixingPeriodStartTime());
    double dfForwardEnd = forwardCurve.getDiscountFactor(coupon.getFixingPeriodEndTime());
    double forward = (dfForwardStart / dfForwardEnd - 1.0) / coupon.getFixingAccrualFactor();
    // Backward sweep
    double pvBar = 1.0;
    double forwardBar = coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getFactor() * df * pvBar;
    double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / coupon.getFixingAccrualFactor() * forwardBar;
    double dfForwardStartBar = 1.0 / (coupon.getFixingAccrualFactor() * dfForwardEnd) * forwardBar;
    double dfBar = (coupon.getNotional() * coupon.getPaymentYearFraction() * (coupon.getFactor() * forward) + coupon.getSpreadAmount()) * pvBar;
    Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    resultMap.put(coupon.getFundingCurveName(), listDiscounting);
    List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(coupon.getFixingPeriodStartTime(), -coupon.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(new DoublesPair(coupon.getFixingPeriodEndTime(), -coupon.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(coupon.getForwardCurveName(), listForward);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    return result;
  }

}
