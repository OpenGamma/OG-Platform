/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 *  Pricing and sensitivities of a CMS coupon by discounting (no convexity adjustment).
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class CouponCMSDiscountingMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponCMSDiscountingMethod INSTANCE = new CouponCMSDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponCMSDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponCMSDiscountingMethod() {
  }

  /**
   * Compute the present value of a CMS coupon by discounting (no convexity adjustment).
   * @param cmsCoupon The CMS coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The coupon price.
   */
  public CurrencyAmount presentValue(final CouponCMS cmsCoupon, final YieldCurveBundle curves) {
    Validate.notNull(cmsCoupon);
    Validate.notNull(curves);
    final ParRateCalculator parRate = ParRateCalculator.getInstance();
    final double swapRate = parRate.visitFixedCouponSwap(cmsCoupon.getUnderlyingSwap(), curves);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(cmsCoupon.getFundingCurveName());
    final double paymentDiscountFactor = fundingCurve.getDiscountFactor(cmsCoupon.getPaymentTime());
    final double pv = swapRate * cmsCoupon.getPaymentYearFraction() * cmsCoupon.getNotional() * paymentDiscountFactor;
    return CurrencyAmount.of(cmsCoupon.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponCMS, "Coupon CMS");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Hull-White data");
    return presentValue((CouponCMS) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Compute the present value sensitivity to the yield curves of a CMS coupon by discounting (no convexity adjustment).
   * @param cmsCoupon The CMS coupon.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final CouponCMS cmsCoupon, final YieldCurveBundle curves) {
    Validate.notNull(cmsCoupon);
    Validate.notNull(curves);
    final ParRateCalculator parRateCal = ParRateCalculator.getInstance();
    final double swapRate = parRateCal.visitFixedCouponSwap(cmsCoupon.getUnderlyingSwap(), curves);
    final String fundingCurveName = cmsCoupon.getFundingCurveName();
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fundingCurveName);
    final double paymentTime = cmsCoupon.getPaymentTime();
    final double paymentDiscountFactor = fundingCurve.getDiscountFactor(paymentTime);
    final ParRateCurveSensitivityCalculator parRateSensCal = ParRateCurveSensitivityCalculator.getInstance();
    final InterestRateCurveSensitivity swapRateSens = new InterestRateCurveSensitivity(cmsCoupon.getUnderlyingSwap().accept(parRateSensCal, curves));
    final InterestRateCurveSensitivity payDFSens = new InterestRateCurveSensitivity(PresentValueCurveSensitivityCalculator.discountFactorSensitivity(fundingCurveName, fundingCurve, paymentTime));
    InterestRateCurveSensitivity result = swapRateSens.multipliedBy(paymentDiscountFactor);
    result = result.plus(payDFSens.multipliedBy(swapRate));
    result = result.multipliedBy(cmsCoupon.getNotional() * cmsCoupon.getPaymentYearFraction());
    return result;
  }
}
