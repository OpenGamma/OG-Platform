/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondDoublesPairComparator;

/**
 * 
 */
public class PresentValueSensitivityCalculatorTest {
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;

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
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double df = curve.getDiscountFactor(t);
    double r = 1 / t * (1 / df - 1);
    Cash cash = new Cash(t, r, FIVE_PC_CURVE_NAME);
    Map<String, List<DoublesPair>> sense = PVSC.visit(cash, CURVES);

    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == 0.0) {
        assertEquals(0.0, pair.getSecond(), 1e-12);
      } else if (pair.getFirst() == t) {
        assertEquals(-t * df * (1 + r * t), pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }

    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    final double dfa = curve.getDiscountFactor(tradeTime);
    r = 1 / yearFrac * (dfa / df - 1);
    cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    sense = PVSC.visit(cash, CURVES);
    temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == tradeTime) {
        assertEquals(dfa * tradeTime, pair.getSecond(), 1e-12);
      } else if (pair.getFirst() == t) {
        assertEquals(-t * df * (1 + r * yearFrac), pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
  }

  @Test
  public void testFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double tau = 1.0 / 12.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double strike = (curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) - 1.0) / tau;
    final ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, strike, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double ratio = curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) / (1 + tau * strike);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(fra, CURVES);
    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertTrue(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<DoublesPair> temp = sense.get(ZERO_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == settlement) {
        assertEquals(0.0, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }

    temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == settlement) {
        assertEquals(-settlement * ratio, pair.getSecond(), 1e-12);
      } else if (pair.getFirst() == maturity) {
        assertEquals(maturity * ratio, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
  }

  @Test
  public void testFutures() {
    final double settlementDate = 1.473;
    final double fixingDate = 1.467;
    final double maturity = 1.75;
    final double indexYearFraction = 0.267;
    final double valueYearFraction = 0.25;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity) - 1.0) / indexYearFraction;
    final double price = 100 * (1 - rate);
    final InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price, FIVE_PC_CURVE_NAME);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(edf, CURVES);
    final double ratio = valueYearFraction / indexYearFraction * curve.getDiscountFactor(fixingDate) / curve.getDiscountFactor(maturity);

    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {

      if (CompareUtils.closeEquals(pair.getFirst(), fixingDate, 1e-16)) {
        assertEquals(fixingDate * ratio, pair.getSecond(), 1e-12);
      } else if (CompareUtils.closeEquals(pair.getFirst(), maturity, 1e-16)) {
        assertEquals(-maturity * ratio, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
  }

  @Test
  public void testFixedCouponAnnuity() {

    final int n = 15;
    final double alpha = 0.49;
    final double yearFrac = 0.51;
    final double[] paymentTimes = new double[n];
    final double[] yearFracs = new double[n];
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double coupon = 0.07;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      yearFracs[i] = yearFrac;
    }

    final FixedCouponAnnuity annuity = new FixedCouponAnnuity(paymentTimes, Math.PI, coupon, yearFracs, FIVE_PC_CURVE_NAME);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);
    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    final Iterator<DoublesPair> iterator = temp.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      final DoublesPair pair = iterator.next();
      final double t = paymentTimes[index];
      assertEquals(t, pair.getFirst(), 0.0);
      assertEquals(-t * yearFrac * Math.PI * coupon * curve.getDiscountFactor(t), pair.getSecond(), 1e-12);
      index++;
    }
  }

  @Test
  public void testForwardLiborAnnuity() {
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double yield = curve.getInterestRate(0.0);
    final double eps = 1e-8;

    final int n = 15;
    final double alpha = 0.245;
    final double yearFrac = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final double[] yearFracs = new double[n];
    final double[] spreads = new double[n];
    final double[] nodeTimes = new double[n + 1];
    final double[] yields = new double[n + 1];

    nodeTimes[0] = 0.0;

    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      indexFixing[i] = i * alpha;
      indexMaturity[i] = paymentTimes[i];
      yearFracs[i] = yearFrac;
      nodeTimes[i + 1] = paymentTimes[i];
      yields[i + 1] = yield;
    }

    final YieldAndDiscountCurve tempCurve = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));

    final ForwardLiborAnnuity annuity = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final ForwardLiborAnnuity bumpedAnnuity = new ForwardLiborAnnuity(paymentTimes, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME, "Bumped Curve");
    final double pv = PVC.visit(annuity, CURVES);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    temp = mergeSameTimes(temp);

    for (int i = 0; i < n + 1; i++) {

      final YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(nodeTimes[i], eps);
      final YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PVC.visit(bumpedAnnuity, curves);
      final double res = (bumpedpv - pv) / eps;
      final DoublesPair pair = temp.get(i);
      assertEquals(nodeTimes[i], pair.getFirst(), 0.0);
      assertEquals(res, pair.getSecond(), 1e-6);
    }
  }

  @Test
  public void testGenericAnnuity() {

    final int n = 5;
    final double[] times = new double[] {0.01, 0.5, 1, 3, 10};
    final double[] amounts = new double[] {100000, 1, 234, -452, 0.034};
    final String[] curveNames = new String[] {FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME};

    final Payment[] payments = new Payment[5];
    for (int i = 0; i < n; i++) {
      payments[i] = new FixedPayment(times[i], amounts[i], curveNames[i]);
    }

    final GenericAnnuity<Payment> annuity = new GenericAnnuity<Payment>(payments);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);

    int count0pc = 0;
    int count5pc = 0;
    assertEquals(sense.get(ZERO_PC_CURVE_NAME).size(), 1, 0);
    assertEquals(sense.get(FIVE_PC_CURVE_NAME).size(), 4, 0);

    for (int i = 0; i < n; i++) {
      final List<DoublesPair> list = sense.get(curveNames[i]);
      if (curveNames[i] == ZERO_PC_CURVE_NAME) {
        assertEquals(times[i], list.get(count0pc).first, 0.0);
        assertEquals(-amounts[i] * times[i], list.get(count0pc).second, 0.0);
        count0pc++;
      } else {
        assertEquals(times[i], list.get(count5pc).first, 0.0);
        assertEquals(-amounts[i] * times[i] * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(times[i]), list.get(count5pc).second, 0.0);
        count5pc++;
      }

    }
  }

  @Test
  public void testFixedPayment() {
    final double time = 1.23;
    final double amount = 4345.3;
    final FixedPayment payment = new FixedPayment(time, amount, FIVE_PC_CURVE_NAME);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(payment, CURVES);

    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));

    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    assertEquals(1, temp.size(), 0);
    assertEquals(time, temp.get(0).first, 0);
    assertEquals(-CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time) * time * amount, temp.get(0).second, 0);

  }

  @Test
  public void testFixedCouponPayment() {
    final double time = 1.23;
    final double yearFrac = 0.56;
    final double coupon = 0.07;
    final double notional = 100;

    final FixedPayment payment = new FixedCouponPayment(time, notional, yearFrac, coupon, ZERO_PC_CURVE_NAME);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(payment, CURVES);

    assertFalse(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertTrue(sense.containsKey(ZERO_PC_CURVE_NAME));

    final List<DoublesPair> temp = sense.get(ZERO_PC_CURVE_NAME);
    assertEquals(1, temp.size(), 0);
    assertEquals(time, temp.get(0).first, 0);
    assertEquals(-time * notional * yearFrac * coupon, temp.get(0).second, 0);

  }

  @Test
  public void testForwardLiborPayment() {
    final double time = 2.45;
    final double resetTime = 2.0;
    final double maturity = 2.5;
    final double paymentYF = 0.48;
    final double forwardYF = 0.5;
    final double spread = 0.04;
    final double notional = 100000000;

    final ForwardLiborPayment payment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    final double[] nodeTimes = new double[] {resetTime, maturity};
    final double[] yields = new double[] {0.05, 0.05};

    final YieldAndDiscountCurve tempCurve = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));

    ForwardLiborPayment bumpedPayment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, ZERO_PC_CURVE_NAME, "Bumped Curve");

    final double pv = PVC.visit(payment, CURVES);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(payment, CURVES);

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);

    final double eps = 1e-8;

    for (int i = 0; i < 2; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(nodeTimes[i], eps);
      final YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PVC.visit(bumpedPayment, curves);
      final double res = (bumpedpv - pv) / eps;
      final DoublesPair pair = temp.get(i);
      assertEquals(nodeTimes[i], pair.getFirst(), 0.0);
      assertEquals(res, pair.getSecond(), notional * 1e-7);
    }

    bumpedPayment = new ForwardLiborPayment(time, notional, resetTime, maturity, paymentYF, forwardYF, spread, "Bumped Curve", FIVE_PC_CURVE_NAME);

    temp = sense.get(ZERO_PC_CURVE_NAME);
    final YieldAndDiscountCurve bumpedCurve = new YieldCurve(ConstantDoublesCurve.from(eps));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.addAll(CURVES);
    curves.setCurve("Bumped Curve", bumpedCurve);
    final double bumpedpv = PVC.visit(bumpedPayment, curves);
    final double res = (bumpedpv - pv) / eps;
    final DoublesPair pair = temp.get(0);
    assertEquals(time, pair.getFirst(), 0.0);
    assertEquals(res, pair.getSecond(), notional * 1e-7);
  }

  @Test
  public void testForwardLiborPayment2() {
    final double time = 2.45;
    final double resetTime = 2.0;
    final double maturity = 2.5;
    final double paymentYF = 0.48;
    final double forwardYF = 0.5;
    final double spread = 0.04;

    final ForwardLiborPayment payment = new ForwardLiborPayment(time, 1.0, resetTime, maturity, paymentYF, forwardYF, spread, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    final double[] nodeTimes = new double[] {resetTime, time, maturity};
    final double[] yields = new double[] {0.05, 0.05, 0.05};

    final YieldAndDiscountCurve tempCurve = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));

    final ForwardLiborPayment bumpedPayment = new ForwardLiborPayment(time, 1.0, resetTime, maturity, paymentYF, forwardYF, spread, "Bumped Curve", "Bumped Curve");

    final double pv = PVC.visit(payment, CURVES);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(payment, CURVES);

    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    final Set<DoublesPair> sorted = new TreeSet<DoublesPair>(new FirstThenSecondDoublesPairComparator());
    sorted.addAll(temp);

    final double eps = 1e-8;

    final Iterator<DoublesPair> interator = sorted.iterator();
    for (int i = 0; i < 3; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(nodeTimes[i], eps);
      final YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PVC.visit(bumpedPayment, curves);
      final double res = (bumpedpv - pv) / eps;
      final DoublesPair pair = interator.next();
      assertEquals(nodeTimes[i], pair.getFirst(), 0.0);
      assertEquals(res, pair.getSecond(), 1e-6);
    }
  }

  List<DoublesPair> mergeSameTimes(final List<DoublesPair> old) {
    final List<DoublesPair> res = new ArrayList<DoublesPair>();
    final Iterator<DoublesPair> iterator = old.iterator();
    DoublesPair pair = iterator.next();
    double t = pair.getFirst();
    double sum = pair.getSecond();

    while (iterator.hasNext()) {
      pair = iterator.next();
      if (CompareUtils.closeEquals(pair.getFirst(), t, 1e-6)) {
        sum += pair.getSecond();
      } else {
        res.add(new DoublesPair(t, sum));
        t = pair.getFirst();
        sum = pair.getSecond();
      }
    }
    res.add(new DoublesPair(t, sum));

    return res;
  }

}
