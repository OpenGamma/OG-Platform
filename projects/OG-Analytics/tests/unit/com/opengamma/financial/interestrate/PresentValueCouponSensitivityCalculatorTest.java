/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class PresentValueCouponSensitivityCalculatorTest {
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PVCSC = PresentValueCouponSensitivityCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final double DELTA = 1e-5;

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
    final double r = 0.0456;
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    final Cash cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final Cash cashUp = new Cash(t, r + DELTA, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final Cash cashDown = new Cash(t, r - DELTA, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final double pvUp = PVC.visit(cashUp, CURVES);
    final double pvDown = PVC.visit(cashDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    PVCSC.visit(cash, CURVES);

    assertEquals(temp, PVCSC.visit(cash, CURVES), 1e-10);
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double strike = 0.06534;
    final double fixingDate = settlement - 2.0 / 365.0;
    final double forwardYearFrac = 31.0 / 365.0;
    final double discountYearFrac = 30.0 / 360;

    final ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final ForwardRateAgreement fraUp = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike + DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final ForwardRateAgreement fraDown = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike - DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    final double pvUp = PVC.visit(fraUp, CURVES);
    final double pvDown = PVC.visit(fraDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(fra, CURVES), 1e-10);
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.468;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;
    final double rate = 0.0356;
    final InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, 100 * (1 - rate), FIVE_PC_CURVE_NAME);
    final InterestRateFuture edfUp = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, 100 * (1 - rate - DELTA), FIVE_PC_CURVE_NAME);
    final InterestRateFuture edfDown = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, 100 * (1 - rate + DELTA), FIVE_PC_CURVE_NAME);

    final double pvUp = PVC.visit(edfUp, CURVES);
    final double pvDown = PVC.visit(edfDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(edf, CURVES), 1e-10);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.52;
    final double yearFrac = 0.5;

    final double coupon = 0.07;
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);

    }

    final Bond bond = new Bond(paymentTimes, coupon, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    final Bond bondUp = new Bond(paymentTimes, coupon + DELTA, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    final Bond bondDown = new Bond(paymentTimes, coupon - DELTA, yearFrac, 0.0, FIVE_PC_CURVE_NAME);

    final double pvUp = PVC.visit(bondUp, CURVES);
    final double pvDown = PVC.visit(bondDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(bond, CURVES), 1e-10);
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

    final FixedFloatSwap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final FixedFloatSwap swapUp = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate + DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final FixedFloatSwap swapDown = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate - DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    final double pvUp = PVC.visit(swapUp, CURVES);
    final double pvDown = PVC.visit(swapDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(swap, CURVES), 1e-10);
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

    final ForwardLiborAnnuity payLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final ForwardLiborAnnuity receiveLeg = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    final ForwardLiborAnnuity receiveLegUp = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreadsUp, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    final ForwardLiborAnnuity receiveLegDown = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreadsDown, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);

    final TenorSwap swap = new TenorSwap(payLeg, receiveLeg);
    final TenorSwap swapUp = new TenorSwap(payLeg, receiveLegUp);
    final TenorSwap swapDown = new TenorSwap(payLeg, receiveLegDown);
    final double pvUp = PVC.visit(swapUp, CURVES);
    final double pvDown = PVC.visit(swapDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(swap, CURVES), 1e-10);
  }

}
