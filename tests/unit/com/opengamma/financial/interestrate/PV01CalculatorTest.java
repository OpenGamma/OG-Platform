/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class PV01CalculatorTest {

  private final static YieldAndDiscountCurve FUNDING_CURVE = new DummyCurve(-0.04, 0.03, 0.1, 0.05);
  private final static YieldAndDiscountCurve LIBOR_CURVE = new DummyCurve(-0.04, 0.025, 0.11, 0.055);
  private final static PV01Calculator PV01 = new PV01Calculator();
  private final static PresentValueCalculator PV = PresentValueCalculator.getInstance();
  private final static double EPS = 1e-8;

  private static final String FUNDING_CURVE_NAME = "funding curve";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static YieldCurveBundle CURVES;

  static {
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    CURVES.setCurve(LIBOR_CURVE_NAME, LIBOR_CURVE);
  }

  @Test
  public void TestCash() {
    double t = 7 / 365.0;
    YieldAndDiscountCurve curve = CURVES.getCurve(FUNDING_CURVE_NAME);
    double df = curve.getDiscountFactor(t);
    double r = 1 / t * (1 / df - 1);
    Cash cash = new Cash(t, r, FUNDING_CURVE_NAME);
    doTest(cash, CURVES);
  }

  @Test
  public void TestFRA() {
    double settlement = 0.5;
    double maturity = 7.0 / 12.0;
    double strike = 0.15;
    double fixingDate = settlement - 2.0 / 365.0;
    double forwardYearFrac = 31.0 / 365.0;
    double discountYearFrac = 30.0 / 360;
    ForwardRateAgreement fra = new ForwardRateAgreement(settlement, maturity, fixingDate, forwardYearFrac, discountYearFrac, strike, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    doTest(fra, CURVES);
  }

  @Test
  public void TestFutures() {
    double settlementDate = 1.453;
    double yearFraction = 0.25;
    double price = 97.3;
    InterestRateFuture edf = new InterestRateFuture(settlementDate, yearFraction, price, LIBOR_CURVE_NAME);
    doTest(edf, CURVES);
  }

  @Test
  public void TestFixedAnnuity() {
    int n = 15;
    double alpha = 0.49;
    double yearFrac = 0.51;
    double[] paymentTimes = new double[n];

    double[] yearFracs = new double[n];
    double coupon = 0.03;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      yearFracs[i] = yearFrac;
    }

    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, 31234.31231, coupon, yearFracs, FUNDING_CURVE_NAME);
    doTest(annuity, CURVES);
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

    VariableAnnuity annuity = new VariableAnnuity(paymentTimes, Math.E, deltaStart, deltaEnd, yearFracs, spreads, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    doTest(annuity, CURVES);
  }

  @Test
  public void TestBond() {
    int n = 20;
    double tau = 0.5;
    double yearFrac = 180 / 365.0;
    double initalCoupon = 0.015;
    double ramp = 0.0025;
    double[] coupons = new double[n];
    double[] yearFracs = new double[n];
    double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      coupons[i] = initalCoupon + i * ramp;
      yearFracs[i] = yearFrac;
    }
    Bond bond = new Bond(paymentTimes, coupons, yearFracs, FUNDING_CURVE_NAME);
    doTest(bond, CURVES);
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
    double swapRate = 0.04;

    Swap swap = new FixedFloatSwap(fixedPaymentTimes, floatPaymentTimes, swapRate, fwdStartOffsets, fwdEndOffsets, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    doTest(swap, CURVES);
  }

  @Test
  public void TestBasisSwap() {
    int n = 20;
    double tau = 0.25;
    double[] paymentTimes = new double[n];
    double[] spreads = new double[n];
    double[] yearFracs = new double[n];
    double[] fwdStartOffsets = new double[n];
    double[] fwdEndOffsets = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      spreads[i] = i * 0.001;
      yearFracs[i] = tau;
    }

    VariableAnnuity payLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, new double[n], FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    VariableAnnuity receiveLeg = new VariableAnnuity(paymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, yearFracs, spreads, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);

    Swap swap = new BasisSwap(payLeg, receiveLeg);
    doTest(swap, CURVES);
  }

  private void doTest(InterestRateDerivative ird, YieldCurveBundle curves) {
    Map<String, Double> ana = PV01.getValue(ird, curves);
    Map<String, Double> fd = finiteDifferancePV01(ird, curves);
    Set<String> names = curves.getAllNames();
    for (String name : names) {
      if (ana.containsKey(name)) {
        assertEquals(ana.get(name), fd.get(name), EPS);
      } else {
        assertEquals(0.0, fd.get(name), 0.0);
      }
    }
  }

  private Map<String, Double> finiteDifferancePV01(InterestRateDerivative ird, YieldCurveBundle curves) {
    Map<String, Double> result = new HashMap<String, Double>();
    Set<String> names = curves.getAllNames();
    for (String name : names) {
      YieldAndDiscountCurve curve = curves.getCurve(name);
      YieldAndDiscountCurve upCurve = curve.withParallelShift(EPS);
      YieldCurveBundle newCurves = new YieldCurveBundle();
      newCurves.addAll(curves);
      newCurves.replaceCurve(name, upCurve);
      double upPV = PV.getValue(ird, newCurves);
      YieldAndDiscountCurve downCurve = curve.withParallelShift(-EPS);
      newCurves.replaceCurve(name, downCurve);
      double downPV = PV.getValue(ird, newCurves);

      double res = (upPV - downPV) / 10000 / 2 / EPS;
      result.put(name, res);
    }
    return result;
  }

  private static class DummyCurve extends YieldAndDiscountCurve {
    private final double _a;
    private final double _b;
    private final double _c;
    private final double _d;

    public DummyCurve(final double a, final double b, final double c, final double d) {
      Validate.isTrue(a + d > 0, "a+d>0");
      Validate.isTrue(d > 0, "d>0");
      Validate.isTrue(c > 0, "c>0");
      _a = a;
      _b = b;
      _c = c;
      _d = d;
    }

    @Override
    public double getDiscountFactor(Double t) {
      return Math.exp(-t * getInterestRate(t));
    }

    @Override
    public double getInterestRate(Double t) {
      return (_a + _b * t) * Math.exp(-_c * t) + _d;
    }

    @Override
    public Set<Double> getMaturities() {
      return null;
    }

    @Override
    public YieldAndDiscountCurve withMultipleShifts(Map<Double, Double> shifts) {
      return null;
    }

    @Override
    public YieldAndDiscountCurve withParallelShift(Double shift) {
      return new DummyCurve(_a, _b, _c, _d + shift);
    }

    @Override
    public YieldAndDiscountCurve withSingleShift(Double t, Double shift) {
      return null;
    }

  }

}
