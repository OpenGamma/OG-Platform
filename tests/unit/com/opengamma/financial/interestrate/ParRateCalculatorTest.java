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
  public void TestCash() {
    double t = 7 / 365.0;
    double tradeTime = 2.0 / 365.0;
    double yearFrac = 5.0 / 360.0;

    Cash cash = new Cash(t, 0, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    double rate = PRC.getValue(cash, CURVES);
    cash = new Cash(t, rate, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(cash, CURVES), 1e-12);
  }

  @Test
  public void TestFRA() {
    double settlement = 0.5;
    double maturity = 7.0 / 12.0;
    double fixingDate = settlement - 2.0 / 365.0;
    double forwardYearFrac = 31.0 / 365.0;
    double discountYearFrac = 30.0 / 360;

    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, 0.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double rate = PRC.getValue(fra, CURVES);
    fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, rate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(fra, CURVES), 1e-12);
  }

  @Test
  public void TestFutures() {
    double settlementDate = 1.453;
    double yearFraction = 0.25;
    InterestRateFuture edf = new InterestRateFuture(settlementDate, yearFraction, 100.0, FIVE_PC_CURVE_NAME);
    double rate = PRC.getValue(edf, CURVES);
    double price = 100 * (1 - rate);
    edf = new InterestRateFuture(settlementDate, yearFraction, price, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(edf, CURVES), 1e-12);
  }

  @Test
  public void TestBond() {
    int n = 20;
    double tau = 0.5;
    double yearFrac = 180 / 365.0;

    double[] yearFracs = new double[n];
    double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      yearFracs[i] = yearFrac;
    }
    Bond bond = new Bond(paymentTimes, 0.0, yearFracs, FIVE_PC_CURVE_NAME);
    double rate = PRC.getValue(bond, CURVES);
    bond = new Bond(paymentTimes, rate, yearFracs, FIVE_PC_CURVE_NAME);
    assertEquals(1.0, PVC.getValue(bond, CURVES), 1e-12);
  }

  @Test
  public void TestFixedFloatSwap() {
    int n = 20;
    double[] fixedPaymentTimes = new double[n];
    double[] floatPaymentTimes = new double[2 * n];
    double[] fwdStartOffsets = new double[2 * n];
    double[] fwdEndOffsets = new double[2 * n];

    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
    }

    Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, 0.0, fwdStartOffsets, fwdEndOffsets, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double rate = PRC.getValue(swap, CURVES);
    swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, rate, fwdStartOffsets, fwdEndOffsets, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.getValue(swap, CURVES), 1e-12);
  }

  @Test
  public void TestBasisSwap() {
    int n = 20;
    double tau = 0.25;
    double[] paymentTimes = new double[n];
    double[] yearFracs = new double[n];
    double[] fwdStartOffsets = new double[n];
    double[] fwdEndOffsets = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      yearFracs[i] = tau;
    }

    VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    Swap swap = new BasisSwap(payLeg, receiveLeg);
    double rate = PRC.getValue(swap, CURVES);
    double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      spreads[i] = rate;
    }
    receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, spreads, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    swap = new BasisSwap(payLeg, receiveLeg);
    assertEquals(0.0, PVC.getValue(swap, CURVES), 1e-12);
  }

}
