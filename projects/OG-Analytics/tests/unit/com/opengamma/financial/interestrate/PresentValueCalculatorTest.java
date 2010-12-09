/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class PresentValueCalculatorTest {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;

  static {
    YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.05));
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = new YieldCurve(ConstantDoublesCurve.from(0.0));
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double r = 1 / t * (1 / curve.getDiscountFactor(t) - 1);
    Cash cash = new Cash(t, r, FIVE_PC_CURVE_NAME);
    double pv = PVC.visit(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    r = 1 / yearFrac * (curve.getDiscountFactor(tradeTime) / curve.getDiscountFactor(t) - 1);
    cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    pv = PVC.visit(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double strike = (curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) - 1.0) * 12.0;
    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, strike, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double pv = PVC.visit(fra, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;
    final double forwardRate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / forwardYearFrac;
    final double fv = (forwardRate - strike) * forwardYearFrac / (1 + forwardRate * discountYearFrac);
    final double pv2 = fv * curve.getDiscountFactor(settlement);
    fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    pv = PVC.visit(fra, CURVES);
    assertEquals(pv2, pv, 1e-12);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.473;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / indexYearFraction;
    final double price = 100 * (1 - rate);
    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price, FIVE_PC_CURVE_NAME);
    double pv = PVC.visit(edf, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double deltaPrice = 1.0;
    edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price + deltaPrice, FIVE_PC_CURVE_NAME);
    pv = PVC.visit(edf, CURVES);
    // NB the market price of a euro dollar future depends on the future rate (strictly the rate is implied from the price) - the test here (fixed rate, but making
    // a new future with a higher price) is equivalent to a drop in market price (implying an increase in rates), will means a negative p&l
    assertEquals(-deltaPrice * valueYearFraction / 100, pv, 1e-12);
  }

  @Test
  public void testFixedCouponAnnuity() {
    FixedCouponAnnuity annuity = new FixedCouponAnnuity(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, ZERO_PC_CURVE_NAME);

    double pv = PVC.visit(annuity, CURVES);
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
    annuity = new FixedCouponAnnuity(paymentTimes, Math.PI, rate, yearFracs, ZERO_PC_CURVE_NAME);
    pv = PVC.visit(annuity, CURVES);
    assertEquals(n * yearFrac * rate * Math.PI, pv, 1e-12);
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
    ForwardLiborAnnuity annuity = new ForwardLiborAnnuity(paymentTimes, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    double pv = PVC.visit(annuity, CURVES);
    assertEquals(0.0, pv, 1e-12);

    annuity = new ForwardLiborAnnuity(paymentTimes, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double forward = 1 / alpha * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    pv = PVC.visit(annuity, CURVES);
    assertEquals(alpha * forward * n, pv, 1e-12);

    forward = 1 / alpha * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    annuity = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, paymentYearFracs, forwardYearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    pv = PVC.visit(annuity, CURVES);
    assertEquals(yearFrac * (spread + forward) * n * Math.E, pv, 1e-12);
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

    Bond bond = new Bond(paymentTimes, 0.0, ZERO_PC_CURVE_NAME);
    double pv = PVC.visit(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);

    bond = new Bond(paymentTimes, coupons, yearFracs, 0.3, FIVE_PC_CURVE_NAME);
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

    final Swap<?, ?> swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double pv = PVC.visit(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);

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

    final ForwardLiborAnnuity payLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final ForwardLiborAnnuity receiveLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);

    final Swap<?, ?> swap = new TenorSwap(payLeg, receiveLeg);
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

    Payment temp = new FixedPayment(time, amount, FIVE_PC_CURVE_NAME);
    expected += amount * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    list.add(temp);
    temp = new FixedCouponPayment(time, notional, yearFrac, coupon, FIVE_PC_CURVE_NAME);
    expected += notional * yearFrac * coupon * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    list.add(temp);
    temp = new ForwardLiborPayment(time, notional, resetTime, time, yearFrac, yearFrac, 0.0, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    expected += notional * (CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(resetTime) / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time) - 1);
    list.add(temp);

    final GenericAnnuity<Payment> annuity = new GenericAnnuity<Payment>(list, Payment.class);
    final double pv = PVC.visit(annuity, CURVES);
    assertEquals(expected, pv, 1e-12);
  }

  @Test
  public void testFixedPayment() {
    final double time = 1.23;
    final double amount = 4345.3;
    final FixedPayment payment = new FixedPayment(time, amount, FIVE_PC_CURVE_NAME);
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

    final FixedPayment payment = new FixedCouponPayment(time, notional, yearFrac, coupon, ZERO_PC_CURVE_NAME);
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

    ForwardLiborPayment payment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    double expected = notional * paymentYF * spread * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    double pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);

    payment = new ForwardLiborPayment(time, 1.0, resetTime, maturity, paymentYF, forwardYF, spread, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double forward = (Math.exp(0.05 * (maturity - resetTime)) - 1) / forwardYF;

    expected = paymentYF * (forward + spread);
    pv = PVC.visit(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

}
