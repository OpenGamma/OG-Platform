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

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to compute the quantities related to swaps (annuity, PVBP, coupon equivalent).
 */
public class SwapFixedIborMethod {

  /**
   * Present value calculator used for intermediary computations.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * Computes the conventional cash annuity of a swap. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward rate.
   * @return The cash annuity.
   */
  public static double getAnnuityCash(FixedCouponSwap<? extends Payment> fixedCouponSwap, double forward) {
    int nbFixedPeriod = fixedCouponSwap.getFixedLeg().getPayments().length;
    int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    double notional = Math.abs(fixedCouponSwap.getFixedLeg().getNthPayment(0).getNotional());
    double annuityCash = 1.0 / forward * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    return annuityCash;
  }

  /**
   * Computes the derivative of cash annuity with respect to the forward. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward.
   * @return The cash annuity derivative.
   */
  public static double getAnnuityCashDerivative(FixedCouponSwap<? extends Payment> fixedCouponSwap, double forward) {
    int nbFixedPeriod = fixedCouponSwap.getFixedLeg().getPayments().length;
    int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    double notional = Math.abs(fixedCouponSwap.getFixedLeg().getNthPayment(0).getNotional());
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
  public static double presentValueBasisPoint(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldAndDiscountCurve discountingCurve) {
    AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public static double presentValueBasisPoint(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldCurveBundle curves) {
    AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    return presentValueBasisPoint(fixedCouponSwap, discountingCurve);
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param discountingCurve The discounting curve.
   * @return The sensitivity.
   */
  public static List<DoublesPair> presentValueBasisPointSensitivity(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldAndDiscountCurve discountingCurve) {
    AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double time;
    List<DoublesPair> list = new ArrayList<DoublesPair>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      DoublesPair s = new DoublesPair(time, -time * discountingCurve.getDiscountFactor(time) * annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction()
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
  public static PresentValueSensitivity presentValueBasisPointSensitivity(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldCurveBundle curves) {
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    result.put(annuityFixed.getNthPayment(0).getFundingCurveName(), presentValueBasisPointSensitivity(fixedCouponSwap, discountingCurve));
    return new PresentValueSensitivity(result);
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(FixedCouponSwap<? extends Payment> fixedCouponSwap, double pvbp, YieldCurveBundle curves) {
    return Math.abs(PVC.visit(fixedCouponSwap.getFixedLeg(), curves)) / pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldCurveBundle curves) {
    double pvbp = presentValueBasisPoint(fixedCouponSwap, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }
}
