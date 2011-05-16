/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 *  Pricing and sensitivities of a CMS coupon by discounting (no convexity adjustment).
 */
public class CouponCMSDiscountingMethod {

  /**
   * Compute the present value of a CMS coupon by discounting (no convexity adjustment).
   * @param cmsCoupon The CMS coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The coupon price.
   */
  public double presentValue(final CouponCMS cmsCoupon, final YieldCurveBundle curves) {
    Validate.notNull(cmsCoupon);
    Validate.notNull(curves);
    ParRateCalculator parRate = ParRateCalculator.getInstance();
    double swapRate = parRate.visitFixedCouponSwap(cmsCoupon.getUnderlyingSwap(), curves);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(cmsCoupon.getFundingCurveName());
    double paymentDiscountFactor = fundingCurve.getDiscountFactor(cmsCoupon.getPaymentTime());
    double pv = swapRate * cmsCoupon.getPaymentYearFraction() * cmsCoupon.getNotional() * paymentDiscountFactor;
    return pv;
  }

  /**
   * Compute the present value sensitivity to the yield curves of a CMS coupon by discounting (no convexity adjustment).
   * @param cmsCoupon The CMS coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated. 
   * @return The present value curve sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(final CouponCMS cmsCoupon, final YieldCurveBundle curves) {
    Validate.notNull(cmsCoupon);
    Validate.notNull(curves);
    ParRateCalculator parRateCal = ParRateCalculator.getInstance();
    double swapRate = parRateCal.visitFixedCouponSwap(cmsCoupon.getUnderlyingSwap(), curves);
    String fundingCurveName = cmsCoupon.getFundingCurveName();
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fundingCurveName);
    double paymentTime = cmsCoupon.getPaymentTime();
    double paymentDiscountFactor = fundingCurve.getDiscountFactor(paymentTime);
    ParRateCurveSensitivityCalculator parRateSensCal = ParRateCurveSensitivityCalculator.getInstance();
    PresentValueSensitivity swapRateSens = new PresentValueSensitivity(parRateSensCal.visit(cmsCoupon.getUnderlyingSwap(), curves));
    PresentValueSensitivity payDFSens = new PresentValueSensitivity(PresentValueSensitivityCalculator.discountFactorSensitivity(fundingCurveName, fundingCurve, paymentTime));
    PresentValueSensitivity result = swapRateSens.multiply(paymentDiscountFactor);
    result = result.add(payDFSens.multiply(swapRate));
    result = result.multiply(cmsCoupon.getNotional() * cmsCoupon.getPaymentYearFraction());
    return result;

  }
}
