/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.DoublesPair;

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
    YieldAndDiscountCurve curve = new ConstantYieldCurve(0.05);
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = new ConstantYieldCurve(0.0);
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void TestCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double df = curve.getDiscountFactor(t);
    double r = 1 / t * (1 / df - 1);
    Cash cash = new Cash(t, r, FIVE_PC_CURVE_NAME);
    Map<String, List<DoublesPair>> sense = PVSC.getValue(cash, CURVES);

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
    sense = PVSC.getValue(cash, CURVES);
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
  public void TestFRA() {
    final double settlement = 0.5;
    final double maturity = 7.0 / 12.0;
    final double tau = 1.0 / 12.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double strike = (curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) - 1.0) / tau;
    final ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, strike, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double ratio = curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) / (1 + tau * strike);

    final Map<String, List<DoublesPair>> sense = PVSC.getValue(fra, CURVES);
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
  public void TestFutures() {
    final double settlementDate = 1.453;
    final double yearFraction = 0.25;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(settlementDate) / curve.getDiscountFactor(settlementDate + yearFraction) - 1.0) / yearFraction;
    final double price = 100 * (1 - rate);
    final InterestRateFuture edf = new InterestRateFuture(settlementDate, yearFraction, price, FIVE_PC_CURVE_NAME);
    final Map<String, List<DoublesPair>> sense = PVSC.getValue(edf, CURVES);
    final double ratio = 100 * curve.getDiscountFactor(settlementDate) / curve.getDiscountFactor(settlementDate + yearFraction);

    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == settlementDate) {
        assertEquals(settlementDate * ratio, pair.getSecond(), 1e-12);
      } else if (pair.getFirst() == settlementDate + yearFraction) {
        assertEquals(-(settlementDate + yearFraction) * ratio, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
  }

  @Test
  public void TestFixedAnnuity() {

    final int n = 15;
    final double alpha = 0.49;
    final double yearFrac = 0.51;
    final double[] paymentTimes = new double[n];
    final double[] paymentAmounts = new double[n];
    final double[] yearFracs = new double[n];
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = curve.getInterestRate(0.0);
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      paymentAmounts[i] = Math.exp((i + 1) * alpha * rate);
      yearFracs[i] = yearFrac;
    }

    final FixedAnnuity annuity = new FixedAnnuity(paymentTimes, Math.PI, paymentAmounts, yearFracs, FIVE_PC_CURVE_NAME);
    final Map<String, List<DoublesPair>> sense = PVSC.getValue(annuity, CURVES);
    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    final Iterator<DoublesPair> iterator = temp.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      final DoublesPair pair = iterator.next();
      assertEquals(paymentTimes[index], pair.getFirst(), 0.0);
      assertEquals(-paymentTimes[index] * yearFrac * Math.PI, pair.getSecond(), 1e-12);
      index++;
    }
  }

  @Test
  public void TestVariableAnnuity() {
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double yield = curve.getInterestRate(0.0);
    final double eps = 1e-8;

    final int n = 15;
    final double alpha = 0.245;
    final double yearFrac = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] deltaStart = new double[n];
    final double[] deltaEnd = new double[n];
    final double[] yearFracs = new double[n];
    final double[] spreads = new double[n];
    final double[] nodeTimes = new double[n + 1];
    final double[] yields = new double[n + 1];
    nodeTimes[0] = 0.0;
    yields[0] = yield;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      deltaStart[i] = deltaEnd[i] = 0.0;
      yearFracs[i] = yearFrac;
      nodeTimes[i + 1] = paymentTimes[i];
      yields[i + 1] = yield;
    }

    final YieldAndDiscountCurve tempCurve = new InterpolatedYieldCurve(nodeTimes, yields, new LinearInterpolator1D());

    final VariableAnnuity annuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final VariableAnnuity bumpedAnnuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, ZERO_PC_CURVE_NAME, "Bumped Curve");
    final double pv = PVC.getValue(annuity, CURVES);
    final Map<String, List<DoublesPair>> sense = PVSC.getValue(annuity, CURVES);

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    temp = mergeSameTimes(temp);

    for (int i = 0; i < n; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(paymentTimes[i], eps);
      final YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PVC.getValue(bumpedAnnuity, curves);
      final double res = (bumpedpv - pv) / eps;
      final DoublesPair pair = temp.get(i + 1);
      assertEquals(paymentTimes[i], pair.getFirst(), 0.0);
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
