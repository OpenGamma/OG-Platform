/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.method.AnnuityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponONCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to compute the quantities related to swaps (annuity, PVBP, coupon equivalent).
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod}
 */
@Deprecated
public class SwapFixedCompoundingONCompoundingDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod INSTANCE = new SwapFixedCompoundingONCompoundingDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwapFixedCompoundingONCompoundingDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  protected SwapFixedCompoundingONCompoundingDiscountingMethod() {
  }

  /**
   * The methods.
   */
  protected static final AnnuityDiscountingMethod METHOD_ANNUITY = AnnuityDiscountingMethod.getInstance();
  private static final CouponONCompoundedDiscountingMethod METHOD_COUPON_ON_CMP = CouponONCompoundedDiscountingMethod.getInstance();

  /**
   * REVIEW: MH 26-Sep-13: Is "cash annuity" used with couponFixedAccruedCompounding swaps?
   * Computes the conventional cash annuity of a swap. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward rate.
   * @return The cash annuity.
   */
  public double getAnnuityCash(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final double forward) {
    final Annuity<? extends Payment> annuity = fixedCouponSwap.getFirstLeg();
    final int nbFixedPeriod = annuity.getPayments().length;
    final int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFirstLeg().getNthPayment(0).getPaymentYearFraction());
    final double notional = Math.abs(fixedCouponSwap.getFirstLeg().getNthPayment(0).getNotional());
    final double annuityCash = 1.0 / forward * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    return annuityCash;
  }

  /**
   * Computes the derivative of cash annuity with respect to the forward. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward.
   * @return The cash annuity derivative.
   */
  public double getAnnuityCashDerivative(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final double forward) {
    final int nbFixedPeriod = fixedCouponSwap.getFirstLeg().getPayments().length;
    final int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFirstLeg().getNthPayment(0).getPaymentYearFraction());
    final double notional = Math.abs(fixedCouponSwap.getFirstLeg().getNthPayment(0).getNotional());
    double annuityCashDerivative = -1.0 / (forward * forward) * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    annuityCashDerivative += 1.0 / (forward * nbFixedPaymentYear) * nbFixedPeriod * Math.pow(1 + forward / nbFixedPaymentYear, -nbFixedPeriod - 1.0) * notional;
    return annuityCashDerivative;
  }

  /**
   * Computes the forward rate of the swaps with one fixed payment.
   * @param fixedCouponSwap The underlying swap. Should have one fixed payment.
   * @param curves The yield curve bundle.
   * @return The forward rate.
   */
  public double forward(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(fixedCouponSwap, "Swap");
    ArgumentChecker.isTrue(fixedCouponSwap.getFirstLeg().getNumberOfPayments() == 1, "Swap should have one fixed payment");
    ArgumentChecker.isTrue(fixedCouponSwap.getSecondLeg().getNumberOfPayments() == 1, "Swap should have one floating payment");
    CouponFixedAccruedCompounding cpnFixed = fixedCouponSwap.getFirstLeg().getNthPayment(0);
    CurrencyAmount pvLegFloating = METHOD_COUPON_ON_CMP.presentValue(fixedCouponSwap.getSecondLeg().getNthPayment(0), curves);
    double dfPay = curves.getCurve(cpnFixed.getFundingCurveName()).getDiscountFactor(cpnFixed.getPaymentTime());
    double rate = Math.pow(-pvLegFloating.getAmount() / (dfPay * cpnFixed.getNotional()), 1.0d / cpnFixed.getPaymentYearFraction()) - 1.0d;
    return rate;
  }

  /**
   * Computes the "modified forward", i.e. the quantity F such that the swap with amount $N(F + 1)$ has a pv of 0.
   * The modified forward is also equal to $(1+forward)^\delta - 1$.
   * @param fixedCouponSwap The underlying swap. Should have one fixed payment.
   * @param curves The yield curve bundle.
   * @return The modified forward rate.
   */
  public double forwardModified(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(fixedCouponSwap, "Swap");
    ArgumentChecker.isTrue(fixedCouponSwap.getFirstLeg().getNumberOfPayments() == 1, "Swap should have one fixed payment");
    ArgumentChecker.isTrue(fixedCouponSwap.getSecondLeg().getNumberOfPayments() == 1, "Swap should have one floating payment");
    CouponFixedAccruedCompounding cpnFixed = fixedCouponSwap.getFirstLeg().getNthPayment(0);
    CurrencyAmount pvLegFloating = METHOD_COUPON_ON_CMP.presentValue(fixedCouponSwap.getSecondLeg().getNthPayment(0), curves);
    double dfPay = curves.getCurve(cpnFixed.getFundingCurveName()).getDiscountFactor(cpnFixed.getPaymentTime());
    double rate = -pvLegFloating.getAmount() / (dfPay * cpnFixed.getNotional()) - 1.0d;
    return rate;
  }

  /**
   * Computes the "modified forward", i.e. the quantity F such that the swap with amount $N(F + 1)$ has a pv of 0.
   * The modified forward is also equal to $(1+forward)^\delta - 1$.
   * @param fixedCouponSwap The underlying swap. Should have one fixed payment.
   * @param curves The yield curve bundle.
   * @return The modified forward rate.
   */
  public InterestRateCurveSensitivity forwardModifiedCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(fixedCouponSwap, "Swap");
    ArgumentChecker.isTrue(fixedCouponSwap.getFirstLeg().getNumberOfPayments() == 1, "Swap should have one fixed payment");
    ArgumentChecker.isTrue(fixedCouponSwap.getSecondLeg().getNumberOfPayments() == 1, "Swap should have one floating payment");
    CouponFixedAccruedCompounding cpnFixed = fixedCouponSwap.getFirstLeg().getNthPayment(0);
    CurrencyAmount pvLegFloating = METHOD_COUPON_ON_CMP.presentValue(fixedCouponSwap.getSecondLeg().getNthPayment(0), curves);
    double dfPay = curves.getCurve(cpnFixed.getFundingCurveName()).getDiscountFactor(cpnFixed.getPaymentTime());
    //    double rate = -pvLegFloating.getAmount() / (dfPay * cpnFixed.getNotional()) - 1.0d;
    // Backward sweep
    double rateBar = 1.0;
    double pvFloatingBar = -1.0d / (dfPay * cpnFixed.getNotional()) * rateBar;
    double dfBar = pvLegFloating.getAmount() / (dfPay * dfPay * cpnFixed.getNotional()) * rateBar;
    final double dfDr = -cpnFixed.getPaymentTime() * dfPay;
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cpnFixed.getPaymentTime(), dfDr * dfBar));
    final Map<String, List<DoublesPair>> dfMap = new HashMap<>();
    dfMap.put(cpnFixed.getFundingCurveName(), list);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(dfMap);
    InterestRateCurveSensitivity pvLegFloatingDr = METHOD_COUPON_ON_CMP.presentValueCurveSensitivity(fixedCouponSwap.getSecondLeg().getNthPayment(0), curves);
    result = result.plus(pvLegFloatingDr.multipliedBy(pvFloatingBar));
    return result;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap.
   * @param fixedCouponSwap The underlying swap.
   * @param discountingCurve The discount curve.
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldAndDiscountCurve discountingCurve) {
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
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
  public double presentValueBasisPoint(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldCurveBundle curves) {
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    return presentValueBasisPoint(fixedCouponSwap, discountingCurve);
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar, not null
   * @param discountingCurve The discount curve.
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final DayCount dayCount, final Calendar calendar,
      final YieldAndDiscountCurve discountingCurve) {
    ArgumentChecker.notNull(fixedCouponSwap, "swap");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(discountingCurve, "discounting curve");
    final Annuity<? extends Payment> annuityFixed = fixedCouponSwap.getFirstLeg();
    if (annuityFixed.getNumberOfPayments() == 0) {
      return 0;
    }
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      final CouponFixedAccruedCompounding coupon = (CouponFixedAccruedCompounding) annuityFixed.getNthPayment(loopcpn);
      pvbp += dayCount.getDayCountFraction(coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), calendar)
          * Math.abs(coupon.getNotional()) * discountingCurve.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar, not null
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final DayCount dayCount, final Calendar calendar,
      final YieldCurveBundle curves) {
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    return presentValueBasisPoint(fixedCouponSwap, dayCount, calendar, discountingCurve);
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param discountingCurve The discounting curve.
   * @return The sensitivity.
   */
  public List<DoublesPair> presentValueBasisPointCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldAndDiscountCurve discountingCurve) {
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, -time * discountingCurve.getDiscountFactor(time) * annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction()
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
  public InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    result.put(annuityFixed.getNthPayment(0).getFundingCurveName(), presentValueBasisPointCurveSensitivity(fixedCouponSwap, discountingCurve));
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param discountingCurve The discounting curve.
   * @return The sensitivity.
   */
  public List<DoublesPair> presentValueBasisPointCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final YieldAndDiscountCurve discountingCurve) {
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, -time * discountingCurve.getDiscountFactor(time)
          * dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate(), calendar)
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    return list;
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param discountingCurve The discounting curve.
   * @return The sensitivity.
   */
  public List<DoublesPair> presentValueBasisPointCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final DayCount dayCount,
      final YieldAndDiscountCurve discountingCurve) {
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, -time * discountingCurve.getDiscountFactor(time)
          * dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate())
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    return list;
  }

  /**
   * Compute the sensitivity of the PVBP to a curve bundle.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    result.put(annuityFixed.getNthPayment(0).getFundingCurveName(), presentValueBasisPointCurveSensitivity(fixedCouponSwap, dayCount, calendar, discountingCurve));
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Compute the sensitivity of the PVBP to a curve bundle.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap,
      final DayCount dayCount,
      final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = fixedCouponSwap.getFirstLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    result.put(annuityFixed.getNthPayment(0).getFundingCurveName(), presentValueBasisPointCurveSensitivity(fixedCouponSwap, dayCount, discountingCurve));
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final double pvbp, final YieldCurveBundle curves) {
    return METHOD_ANNUITY.presentValuePositiveNotional(fixedCouponSwap.getFirstLeg(), curves).getAmount() / pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final YieldCurveBundle curves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final YieldCurveBundle curves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, dayCount, calendar, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }

}
