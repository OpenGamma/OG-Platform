/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor compounding coupon with spread and compounding type "Compounding treating spread as simple interest".
 * The definition of "Compounding treating spread as simple interest" is available in the ISDA document:
 * Reference (cash-flow description): Alternative compounding methods for over-the-counter derivative transactions (2009)
 * Reference (oricing method): Compounded Swaps in Multi-Curve Framework, OpenGamma documentation n. 19, version 1.1, August 2012.
 */
public final class CouponIborCompoundingSimpleSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborCompoundingSimpleSpreadDiscountingMethod INSTANCE = new CouponIborCompoundingSimpleSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborCompoundingSimpleSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborCompoundingSimpleSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor compounded coupon with compounding type "Compounding treating spread as simple interest" by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborCompoundingSimpleSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurve, "multicurve");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double cpa = coupon.getCompoundingPeriodAmountAccumulated();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      final double forward = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTimes()[loopsub],
          coupon.getFixingPeriodEndTimes()[loopsub], coupon.getFixingPeriodAccrualFactors()[loopsub]);
      cpa *= 1.0d + forward * coupon.getPaymentPeriodAccrualFactors()[loopsub];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = (cpa - coupon.getNotional() + coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getSpread()) * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor compounded coupon with compounding type "Flat Compounding" by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborCompoundingSimpleSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurve, "multicurve");
    int nbSubPeriod = coupon.getFixingTimes().length;
    double cpa = coupon.getCompoundingPeriodAmountAccumulated();
    double[] investFactor = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      final double forward = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTimes()[loopsub],
          coupon.getFixingPeriodEndTimes()[loopsub], coupon.getFixingPeriodAccrualFactors()[loopsub]);
      investFactor[loopsub] = 1.0d + forward * coupon.getPaymentPeriodAccrualFactors()[loopsub];
      cpa *= investFactor[loopsub];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    double pvBar = 1.0;
    double dfBar = (cpa - coupon.getNotional() + coupon.getNotional() * coupon.getPaymentYearFraction() * coupon.getSpread()) * pvBar;
    double cpaBar = df * pvBar;
    final double[] forwardBar = new double[nbSubPeriod];
    for (int loopsub = nbSubPeriod - 1; loopsub >= 0; loopsub--) {
      forwardBar[loopsub] = cpa / investFactor[loopsub] * coupon.getPaymentPeriodAccrualFactors()[loopsub] * cpaBar;
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTimes()[loopsub], coupon.getFixingPeriodEndTimes()[loopsub],
          coupon.getFixingPeriodAccrualFactors()[loopsub],
          forwardBar[loopsub]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

}
