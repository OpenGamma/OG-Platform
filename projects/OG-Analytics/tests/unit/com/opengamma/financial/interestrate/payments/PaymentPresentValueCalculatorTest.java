/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class PaymentPresentValueCalculatorTest {

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
  public void testFixedPayment() {
    double time = 1.23;
    double amount = 4345.3;
    FixedPayment payment = new FixedPayment(time, amount, FIVE_PC_CURVE_NAME);
    double expected = amount * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    double pv = PaymentPresentValueCalculator.getInstance().calculate(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  public void testFixedCouponPayment() {
    double time = 1.23;
    double yearFrac = 0.56;
    double coupon = 0.07;
    double notional = 1000;

    FixedPayment payment = new FixedCouponPayment(time, notional, yearFrac, coupon, ZERO_PC_CURVE_NAME);
    double expected = notional * yearFrac * coupon;
    double pv = PaymentPresentValueCalculator.getInstance().calculate(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  public void ForwardLiborPayment() {
    double time = 2.45;
    double resetTime = 2.0;
    double maturity = 2.5;
    double paymentYF = 0.48;
    double forwardYF = 0.5;
    double spread = 0.04;
    double notional = 4.53;

    ForwardLiborPayment payment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME);
    double expected = notional * paymentYF * spread * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    double pv = PaymentPresentValueCalculator.getInstance().calculate(payment, CURVES);
    assertEquals(expected, pv, 1e-8);

    payment = new ForwardLiborPayment(time, 1.0, resetTime, maturity, paymentYF, forwardYF, spread, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double forward = (Math.exp(0.05 * (maturity - resetTime)) - 1) / forwardYF;

    expected = paymentYF * (forward + spread);
    pv = PaymentPresentValueCalculator.getInstance().calculate(payment, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

}
