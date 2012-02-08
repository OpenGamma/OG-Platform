package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class CashFlowEquivalentCalculatorTest {
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);

  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final int FIXED_PAYMENT_PAYMENT_BY_YEAR = 2;
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);

  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 7, 11);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final FixedCouponSwap<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  /**
   * Tests the cash-flow equivalent of a fixed coupon.
   */
  public void fixedCoupon() {
    CouponFixed cpn = SWAP.getFixedLeg().getNthPayment(0);
    AnnuityPaymentFixed cfe = CFEC.visit(cpn, CURVES);
    assertEquals("Fixed coupon: Number of flows", 1, cfe.getNumberOfPayments());
    assertEquals("Fixed coupon: Time", cpn.getPaymentTime(), cfe.getNthPayment(0).getPaymentTime(), 1E-5);
    assertEquals("Fixed coupon: Amount", cpn.getAmount(), cfe.getNthPayment(0).getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests the cash flow equivalent of a Ibor coupon.
   */
  public void iborCoupon() {
    int cpnIndex = 17; // To have payment different from end fixing.
    Payment cpn = SWAP.getSecondLeg().getNthPayment(cpnIndex);
    CouponIbor cpnIbor = (CouponIbor) cpn;
    AnnuityPaymentFixed cfe = CFEC.visit(cpn, CURVES);
    assertEquals("Fixed coupon: Number of flows", 2, cfe.getNumberOfPayments());
    assertEquals("Fixed coupon: Time", cpn.getPaymentTime(), cfe.getNthPayment(1).getPaymentTime(), 1E-5);
    assertEquals("Fixed coupon: Amount", -NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0) * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingYearFraction(), cfe.getNthPayment(1).getAmount(), 1E-2);
    assertEquals("Fixed coupon: Time", cpnIbor.getFixingPeriodStartTime(), cfe.getNthPayment(0).getPaymentTime(), 1E-5);
    double beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
        * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
    assertEquals("Fixed coupon: Amount", beta * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0) * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingYearFraction(), cfe.getNthPayment(0).getAmount(), 1E-4);
    double pvCpn = PVC.visit(cpn, CURVES);
    double pvCfe = PVC.visit(cfe, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvCpn, pvCfe, 1E-5);
  }

  @Test
  /**
   * Tests the cash-flow equivalent of a fixed leg.
   */
  public void fixedLeg() {
    AnnuityCouponFixed leg = SWAP.getFixedLeg();
    AnnuityPaymentFixed cfe = CFEC.visit(leg, CURVES);
    assertEquals("Fixed coupon: Number of flows", FIXED_PAYMENT_PAYMENT_BY_YEAR * SWAP_TENOR_YEAR, cfe.getNumberOfPayments());
    for (int loopcf = 0; loopcf < leg.getNumberOfPayments(); loopcf++) {
      assertEquals("Fixed leg: Time", leg.getNthPayment(loopcf).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
      assertEquals("Fixed leg: Amount", leg.getNthPayment(loopcf).getAmount(), cfe.getNthPayment(loopcf).getAmount(), 1E-2);
    }
    double pvLeg = PVC.visit(leg, CURVES);
    double pvCfe = PVC.visit(cfe, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvLeg, pvCfe, 1E-5);
  }

  @Test
  /**
   * Tests the cash-flow equivalent of a Ibor leg.
   */
  public void iborLeg() {
    GenericAnnuity<Coupon> leg = SWAP.getSecondLeg();
    CouponIbor cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(0);
    AnnuityPaymentFixed cfe = CFEC.visit(leg, CURVES);
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
          (beta * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingYearFraction() - 1 * cpnIborPrevious.getPaymentYearFraction() / cpnIborPrevious.getFixingYearFraction()) * NOTIONAL
              * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(loopcf).getAmount(), 1E-4);
      assertEquals("Ibor leg: Time", leg.getNthPayment(loopcf - 1).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
    }
    assertEquals("Ibor leg: Amount", -1 * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(leg.getNumberOfPayments()).getAmount(), 1E-4);
    assertEquals("Ibor leg: Time", leg.getNthPayment(leg.getNumberOfPayments() - 1).getPaymentTime(), cfe.getNthPayment(leg.getNumberOfPayments()).getPaymentTime(), 1E-5);
    double pvLeg = PVC.visit(leg, CURVES);
    double pvCfe = PVC.visit(cfe, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvLeg, pvCfe, 1E-5);
  }

  @Test
  /**
   * Tests the cash-flow equivalent of a fixed-Ibor swap.
   */
  public void swapFixedIbor() {
    GenericAnnuity<Coupon> leg = SWAP.getSecondLeg();
    CouponIbor cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(0);
    AnnuityPaymentFixed cfe = CFEC.visit(SWAP, CURVES);
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
      assertEquals("Swap: Amount", (beta * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingYearFraction() - 1 * cpnIborPrevious.getPaymentYearFraction() / cpnIborPrevious.getFixingYearFraction())
          * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0) + SWAP.getFixedLeg().getNthPayment(loopcf / 2 - 1).getAmount(), cfe.getNthPayment(loopcf).getAmount(), 1E-4);
      assertEquals("Swap: Time", leg.getNthPayment(loopcf - 1).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
    }
    for (int loopcf = 1; loopcf < leg.getNumberOfPayments(); loopcf += 2) {
      cpnIborPrevious = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf - 1);
      cpnIbor = (CouponIbor) SWAP.getSecondLeg().getNthPayment(loopcf);
      beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodEndTime())
          * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getPaymentTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cpnIbor.getFixingPeriodStartTime());
      assertEquals("Swap: Amount", (beta * cpnIbor.getPaymentYearFraction() / cpnIbor.getFixingYearFraction() - 1 * cpnIborPrevious.getPaymentYearFraction() / cpnIborPrevious.getFixingYearFraction())
          * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0), cfe.getNthPayment(loopcf).getAmount(), 1E-4);
      assertEquals("Swap: Time", leg.getNthPayment(loopcf - 1).getPaymentTime(), cfe.getNthPayment(loopcf).getPaymentTime(), 1E-5);
    }
    assertEquals("Swap: Amount", (-1 * NOTIONAL * (FIXED_IS_PAYER ? 1.0 : -1.0)) + SWAP.getFixedLeg().getNthPayment(SWAP.getFixedLeg().getNumberOfPayments() - 1).getAmount(),
        cfe.getNthPayment(leg.getNumberOfPayments()).getAmount(), 1E-4);
    assertEquals("Swap: Time", leg.getNthPayment(leg.getNumberOfPayments() - 1).getPaymentTime(), cfe.getNthPayment(leg.getNumberOfPayments()).getPaymentTime(), 1E-5);
    double pvSwap = PVC.visit(SWAP, CURVES);
    double pvCfe = PVC.visit(cfe, CURVES);
    assertEquals("Cash flow equivalent - Swap: present value", pvSwap, pvCfe, 1E-5);
  }

}
