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

/**
 * 
 */
public class LastDataCalculatorTest {

  private static LastDateCalculator LDC = new LastDateCalculator();

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    Cash cash = new Cash(t, 0.0445, 1 / 365.0, 5.0 / 365, "t");
    assertEquals(t, LDC.getValue(cash), 1e-12);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;

    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, 0.05, "", "");

    assertEquals(maturity, LDC.getValue(fra), 1e-12);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.453;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;

    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, 98.4, "");
    assertEquals(maturity, LDC.getValue(edf, fixingDate), 1e-12); // passing in fixingDate is just to show that anything can be passed in - it is ignored
  }

  @Test
  public void testFixedAnnuity() {
    FixedAnnuity annuity = new FixedAnnuity(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, new double[] {1., 1., 1., 1., 1., 1., 1., 1., 1., 1.}, "");
    assertEquals(10, LDC.getValue(annuity), 1e-12);
  }

  @Test
  public void testVariableAnnuity() {
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
    VariableAnnuity annuity = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, spreads, Math.E, "Bill", "Ben");
    assertEquals(n * alpha + 0.1, LDC.getValue(annuity), 1e-12);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;
    final double coupon = 0.06;
    final double[] coupons = new double[n];
    final double[] yearFracs = new double[n];
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      coupons[i] = coupon;
      yearFracs[i] = yearFrac;
    }

    Bond bond = new Bond(paymentTimes, coupons, yearFracs, "dummy");
    assertEquals(n * tau, LDC.getValue(bond), 1e-12);
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

    final Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, "", "");
    assertEquals(n * 0.5, LDC.getValue(swap), 1e-12);

  }

  @Test
  public void testBasisSwap() {
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

    final VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, "", "");
    final VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, spreads, 1.0, "", "");

    final Swap swap = new BasisSwap(payLeg, receiveLeg);

    assertEquals(n * tau, LDC.getValue(swap, swap), 1e-12);// passing in swap twice is just to show that anything can be passed in -second case is it is ignored

  }

}
