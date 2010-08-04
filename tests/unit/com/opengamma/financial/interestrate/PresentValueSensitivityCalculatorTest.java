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
import com.opengamma.util.tuple.Pair;

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
    double t = 7 / 365.0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double df = curve.getDiscountFactor(t);
    double r = 1 / t * (1 / df - 1);
    Cash cash = new Cash(t, r, FIVE_PC_CURVE_NAME);
    Map<String, List<Pair<Double, Double>>> sense = PVSC.getValue(cash, CURVES);

    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<Pair<Double, Double>> temp = sense.get(FIVE_PC_CURVE_NAME);
    for (Pair<Double, Double> pair : temp) {
      if (pair.getFirst() == 0.0) {
        assertEquals(0.0, pair.getSecond(), 1e-12);
      } else if (pair.getFirst() == t) {
        assertEquals(-t * df * (1 + r * t), pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }

    double tradeTime = 2.0 / 365.0;
    double yearFrac = 5.0 / 360.0;
    double dfa = curve.getDiscountFactor(tradeTime);
    r = 1 / yearFrac * (dfa / df - 1);
    cash = new Cash(t, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    sense = PVSC.getValue(cash, CURVES);
    temp = sense.get(FIVE_PC_CURVE_NAME);
    for (Pair<Double, Double> pair : temp) {
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
    double settlement = 0.5;
    double maturity = 7.0 / 12.0;
    double tau = 1.0 / 12.0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double strike = (curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) - 1.0) / tau;
    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, strike, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    double ratio = curve.getDiscountFactor(settlement) / curve.getDiscountFactor(maturity) / (1 + tau * strike);

    Map<String, List<Pair<Double, Double>>> sense = PVSC.getValue(fra, CURVES);
    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertTrue(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<Pair<Double, Double>> temp = sense.get(ZERO_PC_CURVE_NAME);
    for (Pair<Double, Double> pair : temp) {
      if (pair.getFirst() == settlement) {
        assertEquals(0.0, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }

    temp = sense.get(FIVE_PC_CURVE_NAME);
    for (Pair<Double, Double> pair : temp) {
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
    double settlementDate = 1.453;
    double yearFraction = 0.25;
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double rate = (curve.getDiscountFactor(settlementDate) / curve.getDiscountFactor(settlementDate + yearFraction) - 1.0) / yearFraction;
    double price = 100 * (1 - rate);
    InterestRateFuture edf = new InterestRateFuture(settlementDate, yearFraction, price, FIVE_PC_CURVE_NAME);
    Map<String, List<Pair<Double, Double>>> sense = PVSC.getValue(edf, CURVES);
    double ratio = 100 * curve.getDiscountFactor(settlementDate) / curve.getDiscountFactor(settlementDate + yearFraction);

    List<Pair<Double, Double>> temp = sense.get(FIVE_PC_CURVE_NAME);
    for (Pair<Double, Double> pair : temp) {
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

    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, Math.PI, paymentAmounts, yearFracs, FIVE_PC_CURVE_NAME);
    Map<String, List<Pair<Double, Double>>> sense = PVSC.getValue(annuity, CURVES);
    List<Pair<Double, Double>> temp = sense.get(FIVE_PC_CURVE_NAME);
    Iterator<Pair<Double, Double>> iterator = temp.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      Pair<Double, Double> pair = iterator.next();
      assertEquals(paymentTimes[index], pair.getFirst(), 0.0);
      assertEquals(-paymentTimes[index] * yearFrac * Math.PI, pair.getSecond(), 1e-12);
      index++;
    }
  }

  @Test
  public void TestVariableAnnuity() {
    YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double yield = curve.getInterestRate(0.0);
    double eps = 1e-8;

    int n = 15;
    double alpha = 0.245;
    double yearFrac = 0.25;
    double[] paymentTimes = new double[n];
    double[] deltaStart = new double[n];
    double[] deltaEnd = new double[n];
    double[] yearFracs = new double[n];
    double[] spreads = new double[n];
    double[] nodeTimes = new double[n + 1];
    double[] yields = new double[n + 1];
    nodeTimes[0] = 0.0;
    yields[0] = yield;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      deltaStart[i] = deltaEnd[i] = 0.0;
      yearFracs[i] = yearFrac;
      nodeTimes[i + 1] = paymentTimes[i];
      yields[i + 1] = yield;
    }

    YieldAndDiscountCurve tempCurve = new InterpolatedYieldCurve(nodeTimes, yields, new LinearInterpolator1D());

    VariableAnnuity annuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    VariableAnnuity bumpedAnnuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, ZERO_PC_CURVE_NAME, "Bumped Curve");
    double pv = PVC.getValue(annuity, CURVES);
    Map<String, List<Pair<Double, Double>>> sense = PVSC.getValue(annuity, CURVES);

    List<Pair<Double, Double>> temp = sense.get(FIVE_PC_CURVE_NAME);
    temp = mergeSameTimes(temp);

    for (int i = 0; i < n; i++) {
      YieldAndDiscountCurve bumpedCurve = tempCurve.withSingleShift(paymentTimes[i], eps);
      YieldCurveBundle curves = new YieldCurveBundle();
      curves.addAll(CURVES);
      curves.setCurve("Bumped Curve", bumpedCurve);
      double bumpedpv = PVC.getValue(bumpedAnnuity, curves);
      double res = (bumpedpv - pv) / eps;
      Pair<Double, Double> pair = temp.get(i + 1);
      assertEquals(paymentTimes[i], pair.getFirst(), 0.0);
      assertEquals(res, pair.getSecond(), 1e-6);
    }
  }

  List<Pair<Double, Double>> mergeSameTimes(List<Pair<Double, Double>> old) {
    List<Pair<Double, Double>> res = new ArrayList<Pair<Double, Double>>();
    Iterator<Pair<Double, Double>> iterator = old.iterator();
    Pair<Double, Double> pair = iterator.next();
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
