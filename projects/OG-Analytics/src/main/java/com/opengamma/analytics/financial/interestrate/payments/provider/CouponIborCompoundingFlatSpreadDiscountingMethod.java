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

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor compounding coupon with spread and compounding type "Flat Compounding".
 * The definition of "Flat Compounding" is available in the ISDA document:
 * Reference: Alternative compounding methods for over-the-counter derivative transactions (2009)
 */
public final class CouponIborCompoundingFlatSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborCompoundingFlatSpreadDiscountingMethod INSTANCE = new CouponIborCompoundingFlatSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborCompoundingFlatSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborCompoundingFlatSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor compounded coupon with compounding type "Flat Compounding" by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double cpaAccumulated = coupon.getCompoundingPeriodAmountAccumulated();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      final double forward = multicurve.getForwardRate(coupon.getIndex(), coupon.getFixingSubperiodsStartTimes()[loopsub], coupon.getFixingSubperiodsEndTimes()[loopsub],
          coupon.getFixingSubperiodsAccrualFactors()[loopsub]);
      cpaAccumulated += cpaAccumulated * forward * coupon.getSubperiodsAccrualFactors()[loopsub]; // Additional Compounding Period Amount
      cpaAccumulated += coupon.getNotional() * (forward + coupon.getSpread()) * coupon.getSubperiodsAccrualFactors()[loopsub]; // Basic Compounding Period Amount
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = cpaAccumulated * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor compounded coupon with compounding type "Flat Compounding" by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double[] cpa = new double[nbSubPeriod + 1];
    double[] cpaAccumulated = new double[nbSubPeriod + 1];
    double[] forward = new double[nbSubPeriod];
    cpa[0] = coupon.getCompoundingPeriodAmountAccumulated();
    cpaAccumulated[0] = coupon.getCompoundingPeriodAmountAccumulated();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      forward[loopsub] = multicurve.getForwardRate(coupon.getIndex(), coupon.getFixingSubperiodsStartTimes()[loopsub], coupon.getFixingSubperiodsEndTimes()[loopsub],
          coupon.getFixingSubperiodsAccrualFactors()[loopsub]);
      cpa[loopsub + 1] += coupon.getNotional() * (forward[loopsub] + coupon.getSpread()) * coupon.getSubperiodsAccrualFactors()[loopsub]; // Basic Compounding Period Amount
      cpa[loopsub + 1] += cpaAccumulated[loopsub] * forward[loopsub] * coupon.getSubperiodsAccrualFactors()[loopsub]; // Additional Compounding Period Amount
      cpaAccumulated[loopsub + 1] = cpaAccumulated[loopsub] + cpa[loopsub + 1];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    //    final double pv = cpaAccumulated[nbSubPeriod + 1] * df;
    // Backward sweep
    final double pvBar = 1.0;
    final double dfBar = cpaAccumulated[nbSubPeriod] * pvBar;
    final double[] cpaAccumulatedBar = new double[nbSubPeriod + 1];
    cpaAccumulatedBar[nbSubPeriod] = df * pvBar;
    final double[] cpaBar = new double[nbSubPeriod + 1];
    final double[] forwardBar = new double[nbSubPeriod];
    for (int loopsub = nbSubPeriod - 1; loopsub >= 0; loopsub--) {
      cpaAccumulatedBar[loopsub] = cpaAccumulatedBar[loopsub + 1];
      cpaBar[loopsub + 1] += cpaAccumulatedBar[loopsub + 1];
      cpaAccumulatedBar[loopsub] += forward[loopsub] * coupon.getSubperiodsAccrualFactors()[loopsub] * cpaBar[loopsub + 1];
      forwardBar[loopsub] += cpaAccumulated[loopsub] * coupon.getSubperiodsAccrualFactors()[loopsub] * cpaBar[loopsub + 1];
      forwardBar[loopsub] += coupon.getNotional() * coupon.getSubperiodsAccrualFactors()[loopsub] * cpaBar[loopsub + 1];
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      listForward.add(new ForwardSensitivity(coupon.getFixingSubperiodsStartTimes()[loopsub], coupon.getFixingSubperiodsEndTimes()[loopsub], coupon.getFixingSubperiodsAccrualFactors()[loopsub],
          forwardBar[loopsub]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

}
