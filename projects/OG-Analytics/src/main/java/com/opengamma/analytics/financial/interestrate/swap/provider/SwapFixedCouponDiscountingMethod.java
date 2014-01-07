/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.provider.AnnuityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to compute the quantities related to swaps (annuity, PVBP, coupon equivalent).
 */
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
   * Computes the second derivative of cash annuity with respect to the forward. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward.
   * @return The cash annuity second derivative.
   */
  public double getAnnuityCashSecondDerivative(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double forward) {
    final int nbFixedPeriod = fixedCouponSwap.getFixedLeg().getPayments().length;
    final int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double notional = Math.abs(fixedCouponSwap.getFixedLeg().getNthPayment(0).getNotional());
    double annuityCashDerivative = 2.0 / (forward * forward * forward) * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    annuityCashDerivative -= 2.0 / (forward * forward * nbFixedPaymentYear) * nbFixedPeriod * Math.pow(1 + forward / nbFixedPaymentYear, -nbFixedPeriod - 1.0) * notional;
    annuityCashDerivative -= 1.0 / (forward * nbFixedPaymentYear * nbFixedPaymentYear) * nbFixedPeriod * (nbFixedPeriod + 1.) * Math.pow(1 + forward / nbFixedPaymentYear, -nbFixedPeriod - 2.0) *
        notional;
    return annuityCashDerivative;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap.
   * @param fixedCouponSwap The underlying swap.
   * @param multicurves The multi-curves provider.
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final MulticurveProviderInterface multicurves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction() * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional())
          * multicurves.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getCurrency(), annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param multicurves The multi-curves provider.
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fixedCouponSwap, "swap");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate())
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional())
          * multicurves.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getCurrency(), annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap modified by a day count.
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param multicurves The multi-curves provider.
   * @return The physical annuity.
   */
  public double presentValueBasisPoint(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final Calendar calendar,
      final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(fixedCouponSwap, "swap");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate(), calendar)
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional())
          * multicurves.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getCurrency(), annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   */
  public MulticurveSensitivity presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final MulticurveProviderInterface multicurves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final Currency ccy = annuityFixed.getCurrency();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, -time * multicurves.getDiscountFactor(ccy, time) * annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction()
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(multicurves.getName(annuityFixed.getCurrency()), list);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(mapDsc);
    return result;
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   */
  public MulticurveSensitivity presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final MulticurveProviderInterface multicurves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final Currency ccy = annuityFixed.getCurrency();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, -time * multicurves.getDiscountFactor(ccy, time)
          * dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate(), calendar)
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(multicurves.getName(annuityFixed.getCurrency()), list);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(mapDsc);
    return result;
  }

  /**
   * Compute the second order sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   */
  public MulticurveSensitivity presentValueBasisPointSecondOrderCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final Calendar calendar, final MulticurveProviderInterface multicurves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final Currency ccy = annuityFixed.getCurrency();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, time * time * multicurves.getDiscountFactor(ccy, time)
          * dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate(), calendar)
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(multicurves.getName(annuityFixed.getCurrency()), list);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(mapDsc);
    return result;
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   */
  public MulticurveSensitivity presentValueBasisPointCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final MulticurveProviderInterface multicurves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final Currency ccy = annuityFixed.getCurrency();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, -time * multicurves.getDiscountFactor(ccy, time)
          * dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate())
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(multicurves.getName(annuityFixed.getCurrency()), list);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(mapDsc);
    return result;
  }

  /**
   * Compute the sensitivity of the PVBP to the discounting curve.
   * @param fixedCouponSwap The swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param multicurves The multi-curves provider.
   * @return The sensitivity.
   */
  public MulticurveSensitivity presentValueBasisPointSecondOrderCurveSensitivity(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount,
      final MulticurveProviderInterface multicurves) {
    final AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    final Currency ccy = annuityFixed.getCurrency();
    double time;
    final List<DoublesPair> list = new ArrayList<>();
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      time = annuityFixed.getNthPayment(loopcpn).getPaymentTime();
      final DoublesPair s = DoublesPair.of(time, time * time * multicurves.getDiscountFactor(ccy, time)
          * dayCount.getDayCountFraction(annuityFixed.getNthPayment(loopcpn).getAccrualStartDate(), annuityFixed.getNthPayment(loopcpn).getAccrualEndDate())
          * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional()));
      list.add(s);
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(multicurves.getName(annuityFixed.getCurrency()), list);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(mapDsc);
    return result;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param multicurves The multi-curves provider.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double pvbp, final MulticurveProviderInterface multicurves) {
    return METHOD_ANNUITY.presentValuePositiveNotional(fixedCouponSwap.getFixedLeg(), multicurves).getAmount() / pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param multicurves The multi-curves provider.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final MulticurveProviderInterface multicurves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, multicurves);
    return couponEquivalent(fixedCouponSwap, pvbp, multicurves);
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param dayCount Day count convention for the PVBP modification.
   * @param calendar The calendar
   * @param multicurves The multi-curves provider.
   * @return The coupon equivalent.
   */
  public double couponEquivalent(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final DayCount dayCount, final Calendar calendar,
      final MulticurveProviderInterface multicurves) {
    final double pvbp = presentValueBasisPoint(fixedCouponSwap, dayCount, calendar, multicurves);
    return couponEquivalent(fixedCouponSwap, pvbp, multicurves);
  }

  // TODO: par rate ?

}
