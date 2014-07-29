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
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.method.AnnuityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to compute the quantities related to swaps (annuity, PVBP, coupon equivalent).
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod}
 */
@Deprecated
public class SwapFixedCouponDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final SwapFixedCouponDiscountingMethod INSTANCE = new SwapFixedCouponDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwapFixedCouponDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  protected SwapFixedCouponDiscountingMethod() {
  }

  /**
   * The methods.
   */
  protected static final AnnuityDiscountingMethod METHOD_ANNUITY = AnnuityDiscountingMethod.getInstance();

  /**
   * Computes the conventional cash annuity of a swap. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward rate.
   * @return The cash annuity.
   */
  public double getAnnuityCash(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double forward) {
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
  public double getAnnuityCashDerivative(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double forward) {
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
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final YieldAndDiscountCurve discountingCurve) {
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
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final YieldCurveBundle curves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final Calendar calendar,
      final YieldAndDiscountCurve discountingCurve) {
    ArgumentChecker.notNull(fixedCouponSwap, "swap");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(discountingCurve, "discounting curve");
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    if (annuityFixed.getNumberOfPayments() == 0) {
      return 0;
    }
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate(), calendar)
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()) * discountingCurve.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param discountingCurve The discount curve.
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final YieldAndDiscountCurve discountingCurve) {
    ArgumentChecker.notNull(fixedCouponSwap, "swap");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(discountingCurve, "discounting curve");
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    if (annuityFixed.getNumberOfPayments() == 0) {
      return 0;
    }
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
   * @param calendar The calendar, not null
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final Calendar calendar,
      final YieldCurveBundle curves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    return presentValueBasisPoint(fixedCouponSwap, dayCount, calendar, discountingCurve);
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final YieldCurveBundle curves) {
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
  public List<DoublesPair> presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final YieldAndDiscountCurve discountingCurve) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public List<DoublesPair> presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final YieldAndDiscountCurve discountingCurve) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public List<DoublesPair> presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final YieldAndDiscountCurve discountingCurve) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public InterestRateCurveSensitivity presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
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
  public double couponEquivalent(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double pvbp, final YieldCurveBundle curves) {
    return METHOD_ANNUITY.presentValuePositiveNotional(fixedCouponSwap.getFixedLeg(), curves).getAmount() / pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final YieldCurveBundle curves) {
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
  public double couponEquivalent(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final Calendar calendar, final YieldCurveBundle curves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, dayCount, calendar, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }

}
