/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ParRateParallelSensitivityCalculatorTest {

  private final static ParRateParallelSensitivityCalculator PRPSC = ParRateParallelSensitivityCalculator.getInstance();
  private final static ParRateCalculator PRC = ParRateCalculator.getInstance();

  private final static YieldAndDiscountCurve FUNDING_CURVE = new YieldCurve(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.007, 0.1, 0.05)));
  private final static YieldAndDiscountCurve LIBOR_CURVE = new YieldCurve(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.006, 0.11, 0.055)));
  private final static double EPS = 1e-6;

  private static final String FUNDING_CURVE_NAME = "funding curve";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.USD;

  static {
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    CURVES.setCurve(LIBOR_CURVE_NAME, LIBOR_CURVE);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FUNDING_CURVE_NAME);
    final double df = curve.getDiscountFactor(t);
    final double r = 1 / t * (1 / df - 1);
    final Cash cash = new Cash(CUR, t, 1, r, FUNDING_CURVE_NAME);
    doTest(cash, CURVES);
  }

  @Test
  public void testFRA() {
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double paymentTime = 0.5;
    final double fixingTime = paymentTime - 2.0 / 365.0;
    final double fixingPeriodEnd = 7.0 / 12.0;
    final double rate = 0.0;
    final double fixingPeriodStart = paymentTime;
    final double fixingYearFraction = 31.0 / 365.0;
    final double paymentYearFraction = 30.0 / 360;
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction,
        rate, LIBOR_CURVE_NAME);
    doTest(fra, CURVES);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final double price = 0.973;
    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingPeriodAccrualFactor, 1, paymentAccrualFactor, "L", FUNDING_CURVE_NAME, LIBOR_CURVE_NAME), 1, price);
    //final InterestRateFuture edf = new InterestRateFuture(settlementDate, fixingDate, maturity, indexYearFraction, valueYearFraction, price, LIBOR_CURVE_NAME);
    doTest(ir, CURVES);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;
    final double initalCoupon = 0.015;
    final double ramp = 0.0025;
    final double[] coupons = new double[n];
    final double[] yearFracs = new double[n];
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      coupons[i] = initalCoupon + i * ramp;
      yearFracs[i] = yearFrac;
    }
    final Bond bond = new Bond(CUR, paymentTimes, coupons, yearFracs, 0.0, FUNDING_CURVE_NAME);
    doTest(bond, CURVES);
  }

  @Test
  public void testFixedFloatSwap() {
    final int n = 20;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];
    final double[] indexFixingTimes = new double[2 * n];
    final double[] indexMaturityTimes = new double[2 * n];
    final double[] yearFrac = new double[2 * n];

    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
      indexFixingTimes[i] = i * 0.25;
      indexMaturityTimes[i] = floatPaymentTimes[i];
      yearFrac[i] = 0.25;
    }
    final double swapRate = 0.04;

    Swap<?, ?> swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
    doTest(swap, CURVES);

    final AnnuityCouponIbor va = new AnnuityCouponIbor(CUR, floatPaymentTimes, indexFixingTimes, indexFixingTimes, indexMaturityTimes, yearFrac, yearFrac, new double[2 * n], 3.43, FUNDING_CURVE_NAME,
        LIBOR_CURVE_NAME, true);
    final AnnuityCouponFixed ca = new AnnuityCouponFixed(CUR, fixedPaymentTimes, swapRate, FUNDING_CURVE_NAME, false);
    swap = new FixedFloatSwap(ca, va);
    doTest(swap, CURVES);
  }

  @Test
  public void testBasisSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] spreads = new double[n];
    final double[] yearFracs = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = i * 0.001;
      yearFracs[i] = tau;
    }

    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
    final GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FUNDING_CURVE_NAME,
        FUNDING_CURVE_NAME, false);

    final Swap<?, ?> swap = new TenorSwap<CouponIbor>(payLeg, receiveLeg);
    doTest(swap, CURVES);
  }

  private void doTest(final InterestRateDerivative ird, final YieldCurveBundle curves) {
    final Map<String, Double> ana = PRPSC.visit(ird, curves);
    final Map<String, Double> fd = finiteDifferanceSense(ird, curves);
    final Set<String> names = curves.getAllNames();
    for (final String name : names) {
      if (ana.containsKey(name)) {
        assertEquals(ana.get(name), fd.get(name), EPS);
      } else {
        assertEquals(0.0, fd.get(name), 0.0);
      }
    }
  }

  private Map<String, Double> finiteDifferanceSense(final InterestRateDerivative ird, final YieldCurveBundle curves) {
    final Map<String, Double> result = new HashMap<String, Double>();
    final Set<String> names = curves.getAllNames();
    for (final String name : names) {
      final YieldAndDiscountCurve curve = curves.getCurve(name);
      final YieldAndDiscountCurve upCurve = curve.withParallelShift(EPS);
      final YieldCurveBundle newCurves = new YieldCurveBundle();
      newCurves.addAll(curves);
      newCurves.replaceCurve(name, upCurve);
      final double upRate = PRC.visit(ird, newCurves);
      final YieldAndDiscountCurve downCurve = curve.withParallelShift(-EPS);
      newCurves.replaceCurve(name, downCurve);
      final double downRate = PRC.visit(ird, newCurves);

      final double res = (upRate - downRate) / 2 / EPS;
      result.put(name, res);
    }
    return result;
  }

  private static class MyFunction implements Function<Double, Double> {
    private final double _a;
    private final double _b;
    private final double _c;
    private final double _d;

    public MyFunction(final double a, final double b, final double c, final double d) {
      Validate.isTrue(a + d > 0, "a+d>0");
      Validate.isTrue(d > 0, "d>0");
      Validate.isTrue(c > 0, "c>0");
      _a = a;
      _b = b;
      _c = c;
      _d = d;
    }

    @Override
    public Double evaluate(final Double... x) {
      final double t = x[0];
      return (_a + _b * t) * Math.exp(-_c * t) + _d;
    }
  }
}
