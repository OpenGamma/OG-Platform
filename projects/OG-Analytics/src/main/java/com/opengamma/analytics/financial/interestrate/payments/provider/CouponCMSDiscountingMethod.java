/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Pricing and sensitivities of a CMS coupon by discounting (no convexity adjustment).
 */
public final class CouponCMSDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponCMSDiscountingMethod INSTANCE = new CouponCMSDiscountingMethod();

  /**
   * Private constructor.
   */
  private CouponCMSDiscountingMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponCMSDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The par rate calculator.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * Compute the present value of a CMS coupon by discounting (no convexity adjustment).
   * @param coupon The CMS coupon.
   * @param multicurves The multi-curve provider.
   * @return The coupon price.
   */
  public MultipleCurrencyAmount presentValue(final CouponCMS coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double swapRate = PRDC.visitFixedCouponSwap(coupon.getUnderlyingSwap(), multicurves);
    final double paymentDiscountFactor = multicurves.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = swapRate * coupon.getPaymentYearFraction() * coupon.getNotional() * paymentDiscountFactor;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to the yield curves of a CMS coupon by discounting (no convexity adjustment).
   * @param coupon The CMS coupon.
   * @param multicurves The multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponCMS coupon, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final Currency ccy = coupon.getCurrency();
    final double swapRate = PRDC.visitFixedCouponSwap(coupon.getUnderlyingSwap(), multicurves);
    final double paymentTime = coupon.getPaymentTime();
    final double paymentDiscountFactor = multicurves.getDiscountFactor(coupon.getCurrency(), paymentTime);
    final double paymentDiscountFactorBar = swapRate * coupon.getPaymentYearFraction() * coupon.getNotional();
    final MulticurveSensitivity swapRateDp = coupon.getUnderlyingSwap().accept(PRCSDC, multicurves);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(paymentTime, -paymentTime * paymentDiscountFactor * paymentDiscountFactorBar));
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    resultMapDsc.put(multicurves.getName(coupon.getCurrency()), list);
    final MulticurveSensitivity dfDp = MulticurveSensitivity.ofYieldDiscounting(resultMapDsc);
    return MultipleCurrencyMulticurveSensitivity.of(
        ccy,
        swapRateDp.multipliedBy(coupon.getPaymentYearFraction() * coupon.getNotional() * paymentDiscountFactor).plus(
            dfDp.multipliedBy(swapRate * coupon.getPaymentYearFraction() * coupon.getNotional())));
  }

}
