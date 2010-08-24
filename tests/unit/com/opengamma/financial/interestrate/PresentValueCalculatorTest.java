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
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double r = 1 / t * (1 / curve.getDiscountFactor(t) - 1);
    Cash cash = new Cash(t, r, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    r = 1 / yearFrac * (curve.getDiscountFactor(tradeTime) / curve.getDiscountFactor(t) - 1);
    cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(cash, CURVES);
    assertEquals(0.0, pv, 1e-12);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double strike = (curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) - 1.0) * 12.0;
    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, strike, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(fra, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;
    final double forwardRate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / forwardYearFrac;
    final double fv = (forwardRate - strike) * forwardYearFrac / (1 + forwardRate * discountYearFrac);
    final double pv2 = fv * curve.getDiscountFactor(settlement);
    fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(fra, CURVES);
    assertEquals(pv2, pv, 1e-12);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.453;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / indexYearFraction;
    final double price = 100 * (1 - rate);
    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price, FIVE_PC_CURVE_NAME);
    double pv = PVC.getValue(edf, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double deltaPrice = 1.0;
    edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price + deltaPrice, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(edf, CURVES);
    // NB the market price of a euro dollar future depends on the future rate (strictly the rate is implied from the price) - the test here (fixed rate, but making
    // a new future with a higher price) is equivalent to a drop in market price (implying an increase in rates), will means a negative p&l
    assertEquals(-deltaPrice * valueYearFraction / 100, pv, 1e-12);
  }

  @Test
  public void testFixedAnnuity() {
    FixedAnnuity annuity = new FixedAnnuity(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, new double[] {1., 1., 1., 1., 1., 1., 1., 1., 1., 1.}, ZERO_PC_CURVE_NAME);
    double pv = PVC.getValue(annuity, CURVES);
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
    annuity = new FixedAnnuity(paymentTimes, 1.0 / alpha, coupons, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(annuity, CURVES);
    assertEquals(n, pv, 1e-12);

    annuity = new FixedAnnuity(paymentTimes, Math.PI, coupons, yearFracs, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(annuity, CURVES);
    assertEquals(Math.PI * yearFrac * n, pv, 1e-12);
  }

  @Test
  public void testVariableAnnuity() {
    final int n = 15;
    final double alpha = 0.245;
    final double yearFrac = 0.25;
    final double spread = 0.01;
    final double[] paymentTimes = new double[n];
    final double[] deltaStart = new double[n];
    final double[] deltaEnd = new double[n];
    final double[] yearFracs = new double[n];
    final double[] spreads = new double[n];
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
    double pv = PVC.getValue(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);

    bond = new Bond(paymentTimes, coupons, yearFracs, FIVE_PC_CURVE_NAME);
    pv = PVC.getValue(bond, CURVES);
    assertEquals(1.0, pv, 1e-12);
  }

  @Test
  public void testFixedFloatSwap() {
    final int n = 20;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];
    final double[] fwdStartOffsets = new double[2 * n];
    final double[] fwdEndOffsets = new double[2 * n];
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

    final Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, fwdStartOffsets, fwdEndOffsets, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double pv = PVC.getValue(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);

  }

  @Test
  public void testBasisSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] spreads = new double[n];
    final double[] yearFracs = new double[n];
    final double[] fwdStartOffsets = new double[n];
    final double[] fwdEndOffsets = new double[n];
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double forward = (1.0 / curve.getDiscountFactor(tau) - 1.0) / tau;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      spreads[i] = forward;
      yearFracs[i] = tau;
    }

    final VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, spreads, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);

    final Swap swap = new BasisSwap(payLeg, receiveLeg);
    final double pv = PVC.getValue(swap, CURVES);
    assertEquals(0.0, pv, 1e-12);

  }

}
