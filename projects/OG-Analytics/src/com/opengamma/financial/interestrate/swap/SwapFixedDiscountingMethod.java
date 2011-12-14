/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.method.AnnuityDiscountingMethod;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to compute the quantities related to swaps (annuity, PVBP, coupon equivalent).
 */
//TODO: REVIEW 12-6-2011 too many static methods
public class SwapFixedDiscountingMethod {

  /**
   * The methods.
   */
  private static final AnnuityDiscountingMethod METHOD_ANNUITY = AnnuityDiscountingMethod.getInstance();

  /**
   * Computes the conventional cash annuity of a swap. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward rate.
   * @return The cash annuity.
   */
  public static double getAnnuityCash(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final double forward) {
    final int nbFixedPeriod = fixedCouponSwap.getFixedLeg().getPayments().length;
    final int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double notional = Math.abs(fixedCouponSwap.getFixedLeg().getNthPayment(0).getNotional());
    final double annuityCash = 1.0 / forward * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    return annuityCash;
  }

  /**
   * Computes the derivative of cash annuity with respect to the forward. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward.
   * @return The cash annuity derivative.
   */
  public static double getAnnuityCashDerivative(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final double forward) {
    final int nbFixedPeriod = fixedCouponSwap.getFixedLeg().getPayments().length;
    final int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double notional = Math.abs(fixedCouponSwap.getFixedLeg().getNthPayment(0).getNotional());
    double annuityCashDerivative = -1.0 / (forward * forward) * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    annuityCashDerivative += 1.0 / (forward * nbFixedPaymentYear) * nbFixedPeriod * Math.pow(1 + forward / nbFixedPaymentYear, -nbFixedPeriod - 1.0) * notional;
    return annuityCashDerivative;

  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap.
   * @param fixedCouponSwap The underlying swap.
   * @param discountingCurve The discount curve.
   * @return The physical annuity.
   */
  public static double presentValueBasisPoint(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final YieldAndDiscountCurve discountingCurve) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction() * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional())
          * discountingCurve.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap.
   * @param fixedCouponSwap The underlying swap.
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The physical annuity.
   */
  public static double presentValueBasisPoint(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final YieldCurveBundle curves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    return presentValueBasisPoint(fixedCouponSwap, discountingCurve);
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param discountingCurve The discount curve.
   * @return The physical annuity.
   */
  public static double presentValueBasisPoint(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final DayCount dayCount, final YieldAndDiscountCurve discountingCurve) {
    Validate.notNull(fixedCouponSwap, "swap");
    Validate.notNull(dayCount, "day count");
    Validate.notNull(discountingCurve, "discounting curve");
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate())
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()) * discountingCurve.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The physical annuity.
   */
  public static double presentValueBasisPoint(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final DayCount dayCount, final YieldCurveBundle curves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    return presentValueBasisPoint(fixedCouponSwap, dayCount, discountingCurve);
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param discountingCurve The discounting curve.
   * @return The sensitivity.
   */
  public static List<DoublesPair> presentValueBasisPointCurveSensitivity(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final YieldAndDiscountCurve discountingCurve) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double time;
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = new DoublesPair(time, -time * discountingCurve.getDiscountFactor(time) * annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction()
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    return list;
  }

  /**
   * Compute the sensitivity of the PVBP to a curve bundle.
   * @param fixedCouponSwap The swap.
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The sensitivity.
   */
  public static InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    result.put(annuityFixed.getNthPayment(0).getFundingCurveName(), presentValueBasisPointCurveSensitivity(fixedCouponSwap, discountingCurve));
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final double pvbp, final YieldCurveBundle curves) {
    return Math.abs(METHOD_ANNUITY.presentValue(fixedCouponSwap.getFixedLeg(), curves).getAmount()) / pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final YieldCurveBundle curves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(final FixedCouponSwap<? extends Payment> fixedCouponSwap, final DayCount dayCount, final YieldCurveBundle curves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, dayCount, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }
}
