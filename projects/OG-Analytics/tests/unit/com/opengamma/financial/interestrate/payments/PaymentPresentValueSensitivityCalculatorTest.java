/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondDoublesPairComparator;

/**
 * 
 */
public class PaymentPresentValueSensitivityCalculatorTest {

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

    Map<String, List<DoublesPair>> sense = PaymentPresentValueSensitivityCalculator.getInstance().calculate(payment, CURVES);

    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    assertEquals(1, temp.size(), 0);
    assertEquals(time, temp.get(0).first, 0);
    assertEquals(-CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time) * time * amount, temp.get(0).second, 0);

  }

  @Test
  public void testFixedCouponPayment() {
    double time = 1.23;
    double yearFrac = 0.56;
    double coupon = 0.07;
    double notional = 100;

    FixedPayment payment = new FixedCouponPayment(time, notional, yearFrac, coupon, ZERO_PC_CURVE_NAME);

    Map<String, List<DoublesPair>> sense = PaymentPresentValueSensitivityCalculator.getInstance().calculate(payment, CURVES);

    assertFalse(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertTrue(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<DoublesPair> temp = sense.get(ZERO_PC_CURVE_NAME);
    assertEquals(1, temp.size(), 0);
    assertEquals(time, temp.get(0).first, 0);
    assertEquals(-time * notional * yearFrac * coupon, temp.get(0).second, 0);

  }

  @Test
  public void ForwardLiborPayment() {
    double time = 2.45;
    double resetTime = 2.0;
    double maturity = 2.5;
    double paymentYF = 0.48;
    double forwardYF = 0.5;
    double spread = 0.04;
    double notional = 100000000;

    ForwardLiborPayment payment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    double[] nodeTimes = new double[] {resetTime, maturity};
    double[] yields = new double[] {0.05, 0.05};

    YieldAndDiscountCurve tempCurve = new InterpolatedYieldCurve(nodeTimes, yields, new LinearInterpolator1D());

    ForwardLiborPayment bumpedPayment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, ZERO_PC_CURVE_NAME, "Bumped Curve");

    final double pv = PaymentPresentValueCalculator.getInstance().calculate(payment, CURVES);
    final Map<String, List<DoublesPair>> sense = PaymentPresentValueSensitivityCalculator.getInstance().calculate(payment, CURVES);

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);

    double eps = 1e-8;

    for (int i = 0; i < 2; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(nodeTimes[i], eps);
      final YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PaymentPresentValueCalculator.getInstance().calculate(bumpedPayment, curves);
      final double res = (bumpedpv - pv) / eps;
      final DoublesPair pair = temp.get(i);
      assertEquals(nodeTimes[i], pair.getFirst(), 0.0);
      assertEquals(res, pair.getSecond(), notional * 1e-7);
    }

    bumpedPayment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, "Bumped Curve", FIVE_PC_CURVE_NAME);

    temp = sense.get(ZERO_PC_CURVE_NAME);
    final YieldAndDiscountCurve bumpedCurve = new ConstantYieldCurve(eps);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.addAll(CURVES);
    curves.setCurve("Bumped Curve", bumpedCurve);
    final double bumpedpv = PaymentPresentValueCalculator.getInstance().calculate(bumpedPayment, curves);
    final double res = (bumpedpv - pv) / eps;
    final DoublesPair pair = temp.get(0);
    assertEquals(time, pair.getFirst(), 0.0);
    assertEquals(res, pair.getSecond(), notional * 1e-7);
  }

  @Test
  public void ForwardLiborPaymen2t() {
    double time = 2.45;
    double resetTime = 2.0;
    double maturity = 2.5;
    double paymentYF = 0.48;
    double forwardYF = 0.5;
    double spread = 0.04;

    ForwardLiborPayment payment = new ForwardLiborPayment(time, 1.0, resetTime, maturity, paymentYF, forwardYF, spread, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    double[] nodeTimes = new double[] {resetTime, time, maturity};
    double[] yields = new double[] {0.05, 0.05, 0.05};

    YieldAndDiscountCurve tempCurve = new InterpolatedYieldCurve(nodeTimes, yields, new LinearInterpolator1D());

    ForwardLiborPayment bumpedPayment = new ForwardLiborPayment(time, 1.0, resetTime, maturity, paymentYF, forwardYF, spread, "Bumped Curve", "Bumped Curve");

    final double pv = PaymentPresentValueCalculator.getInstance().calculate(payment, CURVES);
    final Map<String, List<DoublesPair>> sense = PaymentPresentValueSensitivityCalculator.getInstance().calculate(payment, CURVES);

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    Set<DoublesPair> sorted = new TreeSet<DoublesPair>(new FirstThenSecondDoublesPairComparator());
    sorted.addAll(temp);

    double eps = 1e-8;

    Iterator<DoublesPair> interator = sorted.iterator();
    for (int i = 0; i < 3; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(nodeTimes[i], eps);
      final YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PaymentPresentValueCalculator.getInstance().calculate(bumpedPayment, curves);
      final double res = (bumpedpv - pv) / eps;
      final DoublesPair pair = interator.next();
      assertEquals(nodeTimes[i], pair.getFirst(), 0.0);
      assertEquals(res, pair.getSecond(), 1e-6);
    }
  }

}
