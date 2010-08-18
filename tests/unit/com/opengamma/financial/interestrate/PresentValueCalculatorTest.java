/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class PresentValueCalculatorTest {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;

  static {
    YieldAndDiscountCurve curve = new ConstantYieldCurve(0.05);
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = new ConstantYieldCurve(0.0);
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void TestCash() {
    double t = 7 / 365.0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double r = 1 / t * (1 / curve.getDiscountFactor(t) - 1);
    Cash cash = new Cash(t, r, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);

    double tradeTime = 2.0 / 365.0;
    double yearFrac = 5.0 / 360.0;
    r = 1 / yearFrac * (curve.getDiscountFactor(tradeTime) / curve.getDiscountFactor(t) - 1);
    cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);
  }

  @Test
  public void TestFRA() {
    double settlement = 0.5;
    double maturity = 7.0 / 12.0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double strike = (curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) - 1.0) * 12.0;
    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, strike, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(fra, CURVES);
    assertEquals(0.0, pv, 1e-12);

    double fixingDate = settlement - 2.0 / 365.0;
    double forwardYearFrac = 31.0 / 365.0;
    double discountYearFrac = 30.0 / 360;
    double forwardRate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / forwardYearFrac;
    double fv = (forwardRate - strike) * forwardYearFrac / (1 + forwardRate * discountYearFrac);
    double pv2 = fv * curve.getDiscountFactor(settlement);
    fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(fra, CURVES);
    assertEquals(pv2, pv, 1e-12);
  }

  @Test
  public void TestFutures() {
    double settlementDate = 1.453;
    double fixingDate = 1.467;
    double maturity = 1.75;
    double indexYearFraction = 0.267;
    double valueYearFraction = 0.25;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double rate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / indexYearFraction;
    double price = 100 * (1 - rate);
    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(edf, CURVES);
    assertEquals(0.0, pv, 1e-12);

    double deltaPrice = 1.0;
    edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price + deltaPrice, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(edf, CURVES);
    // NB the market price of a euro dollar future depends on the future rate (strictly the rate is implied from the price) - the test here (fixed rate, but making
    // a new future with a higher price) is equivalent to a drop in market price (implying an increase in rates), will means a negative p&l
    assertEquals(-deltaPrice * valueYearFraction / 100, pv, 1e-12);
  }

  @Test
  public void TestFixedAnnuity() {
    FixedAnnuity annuity = new FixedAnnuity(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, new double[] {1., 1., 1., 1., 1., 1., 1., 1., 1., 1.}, ZERO_PC_CURVE_NAME);
    double pv = PVC.getValue(annuity, CURVES);
    assertEquals(10.0, pv, 1e-12);
    int n = 15;
    double alpha = 0.49;
    double yearFrac = 0.51;
    double[] paymentTimes = new double[n];
    double[] coupons = new double[n];
    double[] yearFracs = new double[n];
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double rate = curve.getInterestRate(0.0);
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      coupons[i] = Math.exp((i + 1) * rate * alpha);
      yearFracs[i] = yearFrac;
    }
    annuity = new FixedAnnuity(paymentTimes, 1.0 / alpha, coupons, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(annuity, CURVES);
    assertEquals(n, pv, 1e-12);

    annuity = new FixedAnnuity(paymentTimes, Math.PI, coupons, yearFracs, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(annuity, CURVES);
    assertEquals(Math.PI * yearFrac * n, pv, 1e-12);
  }

  @Test
  public void TestVariableAnnuity() {
    int n = 15;
    double alpha = 0.245;
    double yearFrac = 0.25;
    double spread = 0.01;
    double[] paymentTimes = new double[n];
    double[] deltaStart = new double[n];
    double[] deltaEnd = new double[n];
    double[] yearFracs = new double[n];
    double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      deltaStart[i] = deltaEnd[i] = 0.1;
      yearFracs[i] = yearFrac;
      spreads[i] = spread;
    }
    VariableAnnuity annuity = new VariableAnnuity(paymentTimes, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    double pv = PVC.getValue(annuity, CURVES);
    assertEquals(0.0, pv, 1e-12);

    annuity = new VariableAnnuity(paymentTimes, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double forward = 1 / alpha * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    pv = PVC.getValue(annuity, CURVES);
    assertEquals(alpha * forward * n, pv, 1e-12);

    forward = 1 / yearFrac * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    annuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(annuity, CURVES);
    assertEquals(yearFrac * (spread + forward) * n * Math.E, pv, 1e-12);
  }

  @Test
  public void TestBond() {
    int n = 20;
    double tau = 0.5;
    double yearFrac = 180 / 365.0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double coupon = (1.0 / curve.getDiscountFactor(tau) - 1.0) / yearFrac;
    double[] coupons = new double[n];
    double[] yearFracs = new double[n];
    double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      coupons[i] = coupon;
      yearFracs[i] = yearFrac;
    }

    Bond bond = new Bond(paymentTimes, 0.0, ZERO_PC_CURVE_NAME);
    double pv = PVC.getValue(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);

    bond = new Bond(paymentTimes, coupons, yearFracs, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);
  }

  @Test
  public void TestFixedFloatSwap() {
    int n = 20;
    double[] fixedPaymentTimes = new double[n];
    double[] floatPaymentTimes = new double[2 * n];
    double[] fwdStartOffsets = new double[2 * n];
    double[] fwdEndOffsets = new double[2 * n];
    double sum = 0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
        sum += curve.getDiscountFactor(fixedPaymentTimes[i / 2]);
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
    }
    double swapRate = (1 - curve.getDiscountFactor(10.0)) / 0.5 / sum;

    Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, fwdStartOffsets, fwdEndOffsets, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);

  }

  @Test
  public void TestBasisSwap() {
    int n = 20;
    double tau = 0.25;
    double[] paymentTimes = new double[n];
    double[] spreads = new double[n];
    double[] yearFracs = new double[n];
    double[] fwdStartOffsets = new double[n];
    double[] fwdEndOffsets = new double[n];
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double forward = (1.0 / curve.getDiscountFactor(tau) - 1.0) / tau;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      spreads[i] = forward;
      yearFracs[i] = tau;
    }

    VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, spreads, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);

    Swap swap = new BasisSwap(payLeg, receiveLeg);
    double pv = PVC.getValue(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);

  }

}
