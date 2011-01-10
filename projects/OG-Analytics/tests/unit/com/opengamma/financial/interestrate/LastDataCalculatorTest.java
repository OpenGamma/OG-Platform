/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * 
 */
public class LastDataCalculatorTest {

  private static LastDateCalculator LDC = new LastDateCalculator();

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    Cash cash = new Cash(t, 0.0445, 1 / 365.0, 5.0 / 365, "t");
    assertEquals(t, LDC.visit(cash), 1e-12);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;

    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, 0.05, "", "");

    assertEquals(maturity, LDC.visit(fra), 1e-12);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.473;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;

    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, 98.4, "");
    assertEquals(maturity, LDC.visit(edf, fixingDate), 1e-12); // passing in fixingDate is just to show that anything can be passed in - it is ignored
  }

  @Test
  public void testFixedCouponAnnuity() {
    FixedCouponAnnuity annuity = new FixedCouponAnnuity(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, 1.0, "");
    assertEquals(10, LDC.visit(annuity), 1e-12);
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
    final double[] yearFracs = new double[n];
    final double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * alpha + 0.1;
      paymentTimes[i] = (i + 1) * alpha;
      indexMaturity[i] = paymentTimes[i] + 0.1;
      yearFracs[i] = yearFrac;
      spreads[i] = spread;
    }
    ForwardLiborAnnuity annuity = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, "Bill", "Ben");
    assertEquals(n * alpha + 0.1, LDC.visit(annuity), 1e-12);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 0.5;
    final double coupon = 0.06;
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
    }

    Bond bond = new Bond(paymentTimes, coupon, yearFrac, 0.0, "dummy");
    assertEquals(n * tau, LDC.visit(bond), 1e-12);
  }

  @Test
  public void testFixedFloatSwap() {
    final int n = 20;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];

    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
    }
    final double swapRate = 0.045;

    final Swap<?, ?> swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, "", "");
    assertEquals(n * 0.5, LDC.visit(swap), 1e-12);

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
    final double forward = 0.003;
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = forward;
      yearFracs[i] = tau;
    }

    final ForwardLiborAnnuity payLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, "", "");
    final ForwardLiborAnnuity receiveLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, "", "");

    final Swap<?, ?> swap = new TenorSwap(payLeg, receiveLeg);

    assertEquals(n * tau, LDC.visit(swap, swap), 1e-12);// passing in swap twice is just to show that anything can be passed in -second case is it is ignored

  }

}
