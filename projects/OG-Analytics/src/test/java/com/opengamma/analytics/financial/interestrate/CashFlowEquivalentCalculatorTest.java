/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CashFlowEquivalentCalculatorTest {
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);

  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final int FIXED_PAYMENT_PAYMENT_BY_YEAR = 2;
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);

  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 7, 11);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  /**
   * Tests the cash-flow equivalent of a fixed coupon.
   */
  public void fixedCoupon() {
    final CouponFixed cpn = SWAP.getFixedLeg().getNthPayment(0);
    final AnnuityPaymentFixed cfe = cpn.accept(CFEC, CURVES);
    assertEquals("Fixed coupon: Number of flows", 1, cfe.getNumberOfPayments());
    assertEquals("Fixed coupon: Time", cpn.getPaymentTime(), cfe.getNthPayment(0).getPaymentTime(), 1E-5);
    assertEquals("Fixed coupon: Amount", cpn.getAmount(), cfe.getNthPayment(0).getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests the cash flow equivalent of a Ibor coupon.
   */
  public void iborCoupon() {
    final int cpnIndex = 17; // To have payment different from end fixing.
    final Payment cpn = SWAP.getSecondLeg().getNthPayment(cpnIndex);
    final CouponIbor cpnIbor = (CouponIbor) cpn;
    final AnnuityPaymentFixed cfe = cpn.accept(CFEC, CURVES);
    assertEquals("Fixed coupon: Number of flows", 2, cfe.getNumberOfPayments());
    assertEquals("Fixed coupon: Time", cpn.getPaymentTime(), cfe.getNthPayment(1).getPaymentTime(), 1E-5);
    assertEquals("Fixed coupon: Amount", -NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0) * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingAccrualFactor(), cfe.getNthPayment(1).getAmount(), 1E-2);
    assertEquals("Fixed coupon: Time", cpnIbor.getFixingPeriodStartTime(), cfe.getNthPayment(0).getPaymentTime(), 1E-5);
    final double beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) /
        CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
        * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
    assertEquals("Fixed coupon: Amount", beta * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0) * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingAccrualFactor(), cfe.getNthPayment(0).getAmount(), 1E-4);
    final double pvCpn = cpn.accept(PVC, CURVES);
    final double pvCfe = cfe.accept(PVC, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvCpn, pvCfe, 1E-5);
  }

  @Test
  /**
   * Tests the cash-flow equivalent of a fixed leg.
   */
  public void fixedLeg() {
    final AnnuityCouponFixed leg = SWAP.getFixedLeg();
    final AnnuityPaymentFixed cfe = leg.accept(CFEC, CURVES);
    assertEquals("Fixed coupon: Number of flows", FIXED_PAYMENT_PAYMENT_BY_YEAR * SWAP_TENOR_YEAR, cfe.getNumberOfPayments());
    for (int loopcf = 0; loopcf < leg.getNumberOfPayments(); loopcf++) {
      assertEquals("Fixed leg: Time", leg.getNthPayment(loopcf).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
      assertEquals("Fixed leg: Amount", leg.getNthPayment(loopcf).getAmount(), cfe.getNthPayment(loopcf).getAmount(), 1E-2);
    }
    final double pvLeg = leg.accept(PVC, CURVES);
    final double pvCfe = cfe.accept(PVC, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvLeg, pvCfe, 1E-5);
  }

  @Test
  /**
   * Tests the cash-flow equivalent of a Ibor leg.
   */
  public void iborLeg() {
    final Annuity<Coupon> leg = SWAP.getSecondLeg();
    CouponIbor cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(0);
    final AnnuityPaymentFixed cfe = leg.accept(CFEC, CURVES);
    assertEquals("Ibor leg: Number of flows", FIXED_PAYMENT_PAYMENT_BY_YEAR * SWAP_TENOR_YEAR * 2 + 1, cfe.getNumberOfPayments());
    assertEquals("Ibor leg: Time", cpnIbor.getFixingPeriodStartTime(), cfe.getNthPayment(0).getPaymentTime(), 1E-5);
    double beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
        * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
    assertEquals("Ibor leg: Amount", beta * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(0).getAmount(), 1E-4);
    CouponIbor cpnIborPrevious;
    for (int loopcf = 1; loopcf < leg.getNumberOfPayments(); loopcf++) {
      cpnIborPrevious = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf - 1);
      cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf);
      beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
          * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
      assertEquals("Ibor leg: Amount - item " + loopcf,
          (beta * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingAccrualFactor() - 1 * cpnIborPrevious.getPaymentYearFraction() / cpnIborPrevious.getFixingAccrualFactor()) * NOTIONAL
          * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(loopcf).getAmount(), 1E-4);
      assertEquals("Ibor leg: Time", leg.getNthPayment(loopcf - 1).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
    }
    assertEquals("Ibor leg: Amount", -1 * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(leg.getNumberOfPayments()).getAmount(), 1E-4);
    assertEquals("Ibor leg: Time", leg.getNthPayment(leg.getNumberOfPayments() - 1).getPaymentTime(), cfe.getNthPayment(leg.getNumberOfPayments()).getPaymentTime(), 1E-5);
    final double pvLeg = leg.accept(PVC, CURVES);
    final double pvCfe = cfe.accept(PVC, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvLeg, pvCfe, 1E-5);
  }

  @Test
  /**
   * Tests the cash-flow equivalent of a fixed-Ibor swap.
   */
  public void swapFixedIbor() {
    final Annuity<Coupon> leg = SWAP.getSecondLeg();
    CouponIbor cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(0);
    final AnnuityPaymentFixed cfe = SWAP.accept(CFEC, CURVES);
    assertEquals("Swap: Number of flows", FIXED_PAYMENT_PAYMENT_BY_YEAR * SWAP_TENOR_YEAR * 2 + 1, cfe.getNumberOfPayments());
    assertEquals("Swap: Time", cpnIbor.getFixingPeriodStartTime(), cfe.getNthPayment(0).getPaymentTime(), 1E-5);
    double beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
        * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
    assertEquals("Swap: Amount", beta * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(0).getAmount(), 1E-4);
    CouponIbor cpnIborPrevious;
    for (int loopcf = 2; loopcf < leg.getNumberOfPayments(); loopcf += 2) {
      cpnIborPrevious = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf - 1);
      cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf);
      beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
          * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
      assertEquals("Swap: Amount",
          (beta * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingAccrualFactor() - 1 * cpnIborPrevious.getPaymentYearFraction() / cpnIborPrevious.getFixingAccrualFactor()) * NOTIONAL
          * (FIXED_IS_PAYER ? 1.0 : -1.0) + SWAP.getFixedLeg().getNthPayment(loopcf / 2 - 1).getAmount(), cfe.getNthPayment(loopcf).getAmount(), 1E-4);
      assertEquals("Swap: Time", leg.getNthPayment(loopcf - 1).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
    }
    for (int loopcf = 1; loopcf < leg.getNumberOfPayments(); loopcf += 2) {
      cpnIborPrevious = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf - 1);
      cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf);
      beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
          * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
      assertEquals("Swap: Amount",
          (beta * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingAccrualFactor() - 1 * cpnIborPrevious.getPaymentYearFraction() / cpnIborPrevious.getFixingAccrualFactor()) * NOTIONAL
          * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(loopcf).getAmount(), 1E-4);
      assertEquals("Swap: Time", leg.getNthPayment(loopcf - 1).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
    }
    assertEquals("Swap: Amount", (-1 * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0)) + SWAP.getFixedLeg().getNthPayment(SWAP.getFixedLeg().getNumberOfPayments() - 1).getAmount(),
        cfe.getNthPayment(leg.getNumberOfPayments()).getAmount(), 1E-4);
    assertEquals("Swap: Time", leg.getNthPayment(leg.getNumberOfPayments() - 1).getPaymentTime(), cfe.getNthPayment(leg.getNumberOfPayments()).getPaymentTime(), 1E-5);
    final double pvSwap = SWAP.accept(PVC, CURVES);
    final double pvCfe = cfe.accept(PVC, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvSwap, pvCfe, 1E-5);
  }

}
