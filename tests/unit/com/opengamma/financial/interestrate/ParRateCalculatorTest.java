/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
public class ParRateCalculatorTest {
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
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
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;

    Cash cash = new Cash(t, 0, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final double rate = PRC.getValue(cash, CURVES);
    cash = new Cash(t, rate, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(cash, CURVES), 1e-12);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;

    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, 0.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double rate = PRC.getValue(fra, CURVES);
    fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, rate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(fra, CURVES), 1e-12);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.453;
    final double fixingDate = settlementDate;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;
    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, 100.0, FIVE_PC_CURVE_NAME);
    final double rate = PRC.getValue(edf, CURVES);
    final double price = 100 * (1 - rate);
    edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(edf, CURVES), 1e-12);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;

    final double[] yearFracs = new double[n];
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      yearFracs[i] = yearFrac;
    }
    Bond bond = new Bond(paymentTimes, 0.0, yearFracs, FIVE_PC_CURVE_NAME);
    final double rate = PRC.getValue(bond, CURVES);
    bond = new Bond(paymentTimes, rate, yearFracs, FIVE_PC_CURVE_NAME);
    assertEquals(1.0, PVC.getValue(bond, CURVES), 1e-12);
  }

  @Test
  public void testFixedFloatSwap() {
    final int n = 20;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];
    final double[] fwdStartOffsets = new double[2 * n];
    final double[] fwdEndOffsets = new double[2 * n];

    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
    }

    Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, 0.0, fwdStartOffsets, fwdEndOffsets, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double rate = PRC.getValue(swap, CURVES);
    swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, rate, fwdStartOffsets, fwdEndOffsets, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(swap, CURVES), 1e-12);
  }

  @Test
  public void testBasisSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] yearFracs = new double[n];
    final double[] fwdStartOffsets = new double[n];
    final double[] fwdEndOffsets = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      yearFracs[i] = tau;
    }

    final VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    Swap swap = new BasisSwap(payLeg, receiveLeg);
    final double rate = PRC.getValue(swap, CURVES);
    final double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      spreads[i] = rate;
    }
    receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, spreads, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    swap = new BasisSwap(payLeg, receiveLeg);
    assertEquals(0.0, PVC.getValue(swap, CURVES), 1e-12);
  }

}
