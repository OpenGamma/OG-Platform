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
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class PresentValueCalculatorTest {

  private static final PresentValueCalculator PVC = new PresentValueCalculator();
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
    double pv = PVC.getPresentValue(cash, CURVES);
    assertEquals(0.0, pv, 1e-8);

    double tradeTime = 2.0 / 365.0;
    double yearFrac = 5.0 / 360.0;
    r = 1 / yearFrac * (curve.getDiscountFactor(tradeTime) / curve.getDiscountFactor(t) - 1);
    cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    pv = PVC.getPresentValue(cash, CURVES);
    assertEquals(0.0, pv, 1e-8);
  }

  @Test
  public void TestFixedAnnuity() {
    FixedAnnuity annuity = new FixedAnnuity(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, ZERO_PC_CURVE_NAME);
    double pv = PVC.getPresentValue(annuity, CURVES);
    assertEquals(10.0, pv, 1e-8);
    int n = 15;
    double alpha = 0.49;
    double yearFrac = 0.51;
    double[] paymentTimes = new double[n];
    double[] paymentAmounts = new double[n];
    double[] yearFracs = new double[n];
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double rate = curve.getInterestRate(0.0);
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      paymentAmounts[i] = Math.exp((i + 1) * alpha * rate);
      yearFracs[i] = yearFrac;
    }
    annuity = new FixedAnnuity(paymentTimes, paymentAmounts, FIVE_PC_CURVE_NAME);
    pv = PVC.getPresentValue(annuity, CURVES);
    assertEquals(n, pv, 1e-8);

    annuity = new FixedAnnuity(paymentTimes, Math.PI, paymentAmounts, yearFracs, FIVE_PC_CURVE_NAME);
    pv = PVC.getPresentValue(annuity, CURVES);
    assertEquals(Math.PI * yearFrac * n, pv, 1e-8);
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
    double pv = PVC.getPresentValue(annuity, CURVES);
    assertEquals(0.0, pv, 1e-8);

    annuity = new VariableAnnuity(paymentTimes, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double forward = 1 / alpha * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    pv = PVC.getPresentValue(annuity, CURVES);
    assertEquals(alpha * forward * n, pv, 1e-8);

    forward = 1 / yearFrac * (1 / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(alpha) - 1);
    annuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    pv = PVC.getPresentValue(annuity, CURVES);
    assertEquals(yearFrac * (spread + forward) * n * Math.E, pv, 1e-8);
  }
}
