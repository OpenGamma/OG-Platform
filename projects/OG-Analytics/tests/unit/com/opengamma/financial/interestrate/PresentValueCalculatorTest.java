/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class PresentValueCalculatorTest {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String FOUR_PC_CURVE_NAME = "4%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.USD;

  static {
    YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.05));
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = new YieldCurve(ConstantDoublesCurve.from(0.04));
    CURVES.setCurve(FOUR_PC_CURVE_NAME, curve);
    curve = new YieldCurve(ConstantDoublesCurve.from(0.0));
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double r = 1 / t * (1 / curve.getDiscountFactor(t) - 1);
    Cash cash = new Cash(CUR, t, 1, r, FIVE_PC_CURVE_NAME);
    double pv = PVC.visit(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    r = 1 / yearFrac * (curve.getDiscountFactor(tradeTime) / curve.getDiscountFactor(t) - 1);
    cash = new Cash(CUR, t, 1, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    pv = PVC.visit(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);
  }

  @Test
  public void testFRA() {
    final double paymentTime = 0.5;
    final double fixingPeriodEnd = 7. / 12.;
    String fundingCurveName = ZERO_PC_CURVE_NAME;
    final String forwardCurveName = FIVE_PC_CURVE_NAME;
    double paymentYearFraction = fixingPeriodEnd - paymentTime;
    final double notional = 1;
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    double fixingTime = paymentTime;
    final double fixingPeriodStart = paymentTime;
    double fixingYearFraction = paymentYearFraction;
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (forwardCurve.getDiscountFactor(paymentTime) / forwardCurve.getDiscountFactor(fixingPeriodEnd) - 1.0) * 12.0;
    ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, fundingCurveName, paymentYearFraction, notional, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction,
        rate, forwardCurveName);
    double pv = PVC.visit(fra, CURVES);
    assertEquals(0.0, pv, 1e-12);

    fixingTime = paymentTime - 2. / 365.;
    fixingYearFraction = 31. / 365;
    paymentYearFraction = 30. / 360;
    fundingCurveName = FIVE_PC_CURVE_NAME;
    final double forwardRate = (forwardCurve.getDiscountFactor(fixingPeriodStart) / forwardCurve.getDiscountFactor(fixingPeriodEnd) - 1) / fixingYearFraction;
    final double fv = (forwardRate - rate) * paymentYearFraction / (1 + forwardRate * paymentYearFraction);
    final double pv2 = fv * forwardCurve.getDiscountFactor(paymentTime);
    fra = new ForwardRateAgreement(CUR, paymentTime, fundingCurveName, paymentYearFraction, notional, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction, rate, forwardCurveName);
    pv = PVC.visit(fra, CURVES);
    assertEquals(pv, pv2, 1e-12);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(fixingPeriodStartTime) / curve.getDiscountFactor(fixingPeriodEndTime) - 1.0) / fixingPeriodAccrualFactor;
    final double price = 1 - rate;
    final double notional = 1;
    InterestRateFutureTransaction ir = new InterestRateFutureTransaction(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingPeriodAccrualFactor, notional, paymentAccrualFactor, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME), 1, price);
    double pv = PVC.visit(ir, CURVES);
    assertEquals(0.0, pv, 1e-12);
    final double deltaPrice = 0.01;
    ir = new InterestRateFutureTransaction(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, notional,
        paymentAccrualFactor, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME), 1, price + deltaPrice);
    pv = PVC.visit(ir, CURVES);
    assertEquals(-deltaPrice * paymentAccrualFactor, pv, 1e-12);
  }

  @Test
  public void testFixedCouponAnnuity() {
    AnnuityCouponFixed annuityReceiver = new AnnuityCouponFixed(CUR, new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, ZERO_PC_CURVE_NAME, false);

    double pv = PVC.visit(annuityReceiver, CURVES);
    assertEquals(10.0, pv, 1e-12);
    final int n = 15;
    final double alpha = 0.49;
    final double yearFrac = 0.51;
    final double[] paymentTimes = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFracs = new double[n];
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = curve.getInterestRate(0.0);
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      coupons[i] = Math.exp((i + 1) * rate * alpha);
      yearFracs[i] = yearFrac;
    }
    annuityReceiver = new AnnuityCouponFixed(CUR, paymentTimes, Math.PI, rate, yearFracs, ZERO_PC_CURVE_NAME, false);
    pv = PVC.visit(annuityReceiver, CURVES);
    assertEquals(n * yearFrac * rate * Math.PI, pv, 1e-12);

    final AnnuityCouponFixed annuityPayer = new AnnuityCouponFixed(CUR, paymentTimes, Math.PI, rate, yearFracs, ZERO_PC_CURVE_NAME, true);
    assertEquals(pv, -PVC.visit(annuityPayer, CURVES), 1e-12);
  }

  @Test
  public void testForwardLiborAnnuity() {
    final int n = 15;
    final double alpha = 0.245;
    final double yearFrac = 0.25;
    final double spread = 0.01;
    final double[] paymentTimes = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final double[] paymentYearFracs = new double[n];
    final double[] forwardYearFracs = new double[n];
    final double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * alpha + 0.1;
      paymentTimes[i] = (i + 1) * alpha;
      indexMaturity[i] = paymentTimes[i] + 0.1;
      paymentYearFracs[i] = yearFrac;
      forwardYearFracs[i] = alpha;
      spreads[i] = spread;
    }
    AnnuityCouponIbor annuity = new AnnuityCouponIbor(CUR, paymentTimes, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME, true);
    double pv = PVC.visit(annuity, CURVES);
    assertEquals(0.0, pv, 1e-12);

    annuity = new AnnuityCouponIbor(CUR, paymentTimes, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, false);
    double forward = 1 / alpha * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    pv = PVC.visit(annuity, CURVES);
    assertEquals(alpha * forward * n, pv, 1e-12);

    forward = 1 / alpha * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    annuity = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, paymentYearFracs, forwardYearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, false);
    pv = PVC.visit(annuity, CURVES);
    assertEquals(yearFrac * (spread + forward) * n * Math.E, pv, 1e-12);
    final AnnuityCouponIbor annuityPayer = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, paymentYearFracs, forwardYearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME,
        FIVE_PC_CURVE_NAME, true);
    assertEquals(pv, -PVC.visit(annuityPayer, CURVES), 1e-12);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double coupon = (1.0 / curve.getDiscountFactor(tau) - 1.0) / yearFrac;
    final double[] coupons = new double[n];
    final double[] yearFracs = new double[n];
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      coupons[i] = coupon;
      yearFracs[i] = yearFrac;
    }

    Bond bond = new Bond(CUR, paymentTimes, 0.0, ZERO_PC_CURVE_NAME);
    double pv = PVC.visit(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);

    bond = new Bond(CUR, paymentTimes, coupons, yearFracs, 0.3, FIVE_PC_CURVE_NAME);
    pv = PVC.visit(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);
  }

  @Test
  public void testFixedFloatSwap() {
    final int n = 20;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];
    double sum = 0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
        sum += curve.getDiscountFactor(fixedPaymentTimes[i / 2]);
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
    }
    final double swapRate = (1 - curve.getDiscountFactor(10.0)) / 0.5 / sum;

    final Swap<?, ?> swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, false);
    final double pv = PVC.visit(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double swapRateNonATM = 0.05;
    final Swap<?, ?> swapPayer = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRateNonATM, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    final Swap<?, ?> swapReceiver = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRateNonATM, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, false);
    final double pvPayer = PVC.visit(swapPayer, CURVES);
    final double pvReceiver = PVC.visit(swapReceiver, CURVES);
    assertEquals(0.0, pvPayer + pvReceiver, 1e-12);

  }

  @Test
  public void testTenorSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] spreads = new double[n];
    final double[] yearFracs = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double forward = (1.0 / curve.getDiscountFactor(tau) - 1.0) / tau;
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = forward;
      yearFracs[i] = tau;
    }

    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    final GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FIVE_PC_CURVE_NAME,
        ZERO_PC_CURVE_NAME, false);

    final Swap<?, ?> swap = new TenorSwap<CouponIbor>(payLeg, receiveLeg);
    final double pv = PVC.visit(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);
  }

  @Test
  public void testGenericAnnuity() {
    final double time = 3.4;
    final double amount = 34.3;
    final double coupon = 0.05;
    final double yearFrac = 0.5;
    final double resetTime = 2.9;
    final double notional = 56;

    final List<Payment> list = new ArrayList<Payment>();
    double expected = 0.0;
    Payment temp = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);
    expected += amount * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    list.add(temp);
    temp = new CouponFixed(CUR, time, FIVE_PC_CURVE_NAME, yearFrac, notional, coupon);
    expected += notional * yearFrac * coupon * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    list.add(temp);
    temp = new CouponIbor(CUR, time, ZERO_PC_CURVE_NAME, yearFrac, notional, resetTime, resetTime, time, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    expected += notional * (CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(resetTime) / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time) - 1);
    list.add(temp);

    final GenericAnnuity<Payment> annuity = new GenericAnnuity<Payment>(list, Payment.class, true);
    final double pv = PVC.visit(annuity, CURVES);
    assertEquals(expected, pv, 1e-12);
  }

  @Test
  public void testFixedPayment() {
    final double time = 1.23;
    final double amount = 4345.3;
    final PaymentFixed payment = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);
    final double expected = amount * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    final double pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  public void testFixedCouponPayment() {
    final double time = 1.23;
    final double yearFrac = 0.56;
    final double coupon = 0.07;
    final double notional = 1000;

    final CouponFixed payment = new CouponFixed(CUR, time, ZERO_PC_CURVE_NAME, yearFrac, notional, coupon);
    final double expected = notional * yearFrac * coupon;
    final double pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  public void ForwardLiborPayment() {
    final double time = 2.45;
    final double resetTime = 2.0;
    final double maturity = 2.5;
    final double paymentYF = 0.48;
    final double forwardYF = 0.5;
    final double spread = 0.04;
    final double notional = 4.53;

    CouponIbor payment = new CouponIbor(CUR, time, FIVE_PC_CURVE_NAME, paymentYF, notional, resetTime, resetTime, maturity, forwardYF, spread, ZERO_PC_CURVE_NAME);
    double expected = notional * paymentYF * spread * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    double pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);

    payment = new CouponIbor(CUR, time, ZERO_PC_CURVE_NAME, paymentYF, 1.0, resetTime, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);
    final double forward = (Math.exp(0.05 * (maturity - resetTime)) - 1) / forwardYF;

    expected = paymentYF * (forward + spread);
    pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  /**
   * Tests CouponCMS pricing by simple discounting (no convexity adjustment).
   */
  public void testCouponCMS() {
    final String discountCurve = FOUR_PC_CURVE_NAME;
    final String forwardCurve = FIVE_PC_CURVE_NAME;
    // Swap: 5Y x 10Y semi/quarterly
    final int n = 20;
    final double settleTime = 5.0;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];
    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25 + settleTime;
      }
      floatPaymentTimes[i] = (i + 1) * 0.25 + settleTime;
    }
    final FixedCouponSwap<? extends Payment> swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, 1.0, discountCurve, forwardCurve, true);
    // CMS coupon
    final double notional = 10000000.0; //10m
    final double paymentYearFraction = 0.51;
    final double cmsFixing = settleTime - 2.0 / 365.0;
    final double paymentTime = settleTime + 0.51;
    final CouponCMS payment = new CouponCMS(CUR, paymentTime, paymentYearFraction, notional, cmsFixing, swap, settleTime);
    // Pricing
    final ParRateCalculator parRateCalc = ParRateCalculator.getInstance();
    final double rate = parRateCalc.visit(swap, CURVES);
    final double df = CURVES.getCurve(discountCurve).getDiscountFactor(paymentTime);
    final double expected = notional * paymentYearFraction * rate * df;
    final double pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

}
