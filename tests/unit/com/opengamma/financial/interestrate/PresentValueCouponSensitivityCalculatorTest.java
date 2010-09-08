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
public class PresentValueCouponSensitivityCalculatorTest {
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PVCSC = PresentValueCouponSensitivityCalculator
      .getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final double DELTA = 1e-5;

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
    double r = 0.0456;
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    Cash cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    Cash cashUp = new Cash(t, r + DELTA, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    Cash cashDown = new Cash(t, r - DELTA, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    double pvUp = PVC.getValue(cashUp, CURVES);
    double pvDown = PVC.getValue(cashDown, CURVES);
    double temp = (pvUp - pvDown) / 2 / DELTA;
    PVCSC.getValue(cash, CURVES);

    assertEquals(temp, PVCSC.getValue(cash, CURVES), 1e-10);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double strike = 0.06534;
    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;

    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac,
        discountYearFrac, strike, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    ForwardRateAgreement fraUp = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac,
        discountYearFrac, strike + DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    ForwardRateAgreement fraDown = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac,
        discountYearFrac, strike - DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    double pvUp = PVC.getValue(fraUp, CURVES);
    double pvDown = PVC.getValue(fraDown, CURVES);
    double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.getValue(fra, CURVES), 1e-10);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.453;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;
    final double rate = 0.0356;
    InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction,
        valueYearFraction, 100 * (1 - rate), FIVE_PC_CURVE_NAME);
    InterestRateFuture edfUp = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction,
        valueYearFraction, 100 * (1 - rate - DELTA), FIVE_PC_CURVE_NAME);
    InterestRateFuture edfDown = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction,
        valueYearFraction, 100 * (1 - rate + DELTA), FIVE_PC_CURVE_NAME);

    double pvUp = PVC.getValue(edfUp, CURVES);
    double pvDown = PVC.getValue(edfDown, CURVES);
    double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.getValue(edf, CURVES), 1e-10);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;

    final double coupon = 0.07;
    final double[] yearFracs = new double[n];
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      yearFracs[i] = yearFrac;
    }

    Bond bond = new Bond(paymentTimes, coupon, yearFracs, FIVE_PC_CURVE_NAME);
    Bond bondUp = new Bond(paymentTimes, coupon + DELTA, yearFracs, FIVE_PC_CURVE_NAME);
    Bond bondDown = new Bond(paymentTimes, coupon - DELTA, yearFracs, FIVE_PC_CURVE_NAME);

    double pvUp = PVC.getValue(bondUp, CURVES);
    double pvDown = PVC.getValue(bondDown, CURVES);
    double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.getValue(bond, CURVES), 1e-10);
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
    final double swapRate = 0.04;

    final Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, FIVE_PC_CURVE_NAME,
        FIVE_PC_CURVE_NAME);
    final Swap swapUp = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate + DELTA, FIVE_PC_CURVE_NAME,
        FIVE_PC_CURVE_NAME);
    final Swap swapDown = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate - DELTA,
        FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    double pvUp = PVC.getValue(swapUp, CURVES);
    double pvDown = PVC.getValue(swapDown, CURVES);
    double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.getValue(swap, CURVES), 1e-10);
  }

  @Test
  public void testBasisSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] spreads = new double[n];
    final double[] spreadsUp = new double[n];
    final double[] spreadsDown = new double[n];
    final double[] yearFracs = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final double spread = 0.001;
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = spread;
      spreadsUp[i] = spread + DELTA;
      spreadsDown[i] = spread - DELTA;
      yearFracs[i] = tau;
    }

    final VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0,
        FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs,
        spreads, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    final VariableAnnuity receiveLegUp = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs,
        spreadsUp, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    final VariableAnnuity receiveLegDown = new VariableAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs,
        spreadsDown, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);

    final Swap swap = new BasisSwap(payLeg, receiveLeg);
    final Swap swapUp = new BasisSwap(payLeg, receiveLegUp);
    final Swap swapDown = new BasisSwap(payLeg, receiveLegDown);
    double pvUp = PVC.getValue(swapUp, CURVES);
    double pvDown = PVC.getValue(swapDown, CURVES);
    double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.getValue(swap, CURVES), 1e-10);
  }

}
