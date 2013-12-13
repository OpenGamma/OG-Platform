/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ParRateParallelSensitivityCalculatorTest {

  private final static ParRateParallelSensitivityCalculator PRPSC = ParRateParallelSensitivityCalculator.getInstance();
  private final static ParRateCalculator PRC = ParRateCalculator.getInstance();

  private final static YieldAndDiscountCurve FUNDING_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.007, 0.1, 0.05)));
  private final static YieldAndDiscountCurve LIBOR_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.006, 0.11, 0.055)));
  private final static double EPS = 1e-6;

  private static final String FUNDING_CURVE_NAME = "funding curve";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.EUR;

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
    final Cash cash = new Cash(CUR, 0, t, 1, r, t, FUNDING_CURVE_NAME);
    doTest(cash, CURVES);
  }

  @Test
  public void testFRA() {
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING,
        true);
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

  //  @Test
  //  public void testFutures() {
  //    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCounts.ACT_365,
  //        BusinessDayConventions.FOLLOWING, true);
  //    final double lastTradingTime = 1.473;
  //    final double fixingPeriodStartTime = 1.467;
  //    final double fixingPeriodEndTime = 1.75;
  //    final double fixingPeriodAccrualFactor = 0.267;
  //    final double paymentAccrualFactor = 0.25;
  //    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
  //    final int quantity = 123;
  //    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice, 1, paymentAccrualFactor,
  //        quantity, "L", FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  //    doTest(ir, CURVES);
  //  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;
    final double initialCoupon = 0.015;
    final double ramp = 0.0025;
    final CouponFixed[] coupons = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      coupons[i] = new CouponFixed(CUR, tau * (i + 1), FUNDING_CURVE_NAME, yearFrac, initialCoupon + i * ramp);
    }
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, tau * n, 1, FUNDING_CURVE_NAME) });
    final BondFixedSecurity bond = new BondFixedSecurity(nominal, new AnnuityCouponFixed(coupons), 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FUNDING_CURVE_NAME, "S");
    doTest(bond, CURVES);
  }

  //  @Test
  //  public void testFixedFloatSwap() {
  //    final int n = 20;
  //    final double[] fixedPaymentTimes = new double[n];
  //    final double[] floatPaymentTimes = new double[2 * n];
  //    final double[] indexFixingTimes = new double[2 * n];
  //    final double[] indexMaturityTimes = new double[2 * n];
  //    final double[] yearFrac = new double[2 * n];
  //
  //    for (int i = 0; i < n * 2; i++) {
  //      if (i % 2 == 0) {
  //        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
  //      }
  //      floatPaymentTimes[i] = (i + 1) * 0.25;
  //      indexFixingTimes[i] = i * 0.25;
  //      indexMaturityTimes[i] = floatPaymentTimes[i];
  //      yearFrac[i] = 0.25;
  //    }
  //    final double swapRate = 0.04;
  //
  //    Swap<?, ?> swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, INDEX, swapRate, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
  //    doTest(swap, CURVES);
  //
  //    final AnnuityCouponIbor va = new AnnuityCouponIbor(CUR, floatPaymentTimes, indexFixingTimes, INDEX, indexFixingTimes, indexMaturityTimes, yearFrac, yearFrac, new double[2 * n], 3.43,
  //        FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
  //    final AnnuityCouponFixed ca = new AnnuityCouponFixed(CUR, fixedPaymentTimes, swapRate, FUNDING_CURVE_NAME, false);
  //    swap = new FixedFloatSwap(ca, va);
  //    doTest(swap, CURVES);
  //  }

  //  @Test
  //  public void testBasisSwap() {
  //    final int n = 20;
  //    final double tau = 0.25;
  //    final double[] paymentTimes = new double[n];
  //    final double[] spreads = new double[n];
  //    final double[] yearFracs = new double[n];
  //    final double[] indexFixing = new double[n];
  //    final double[] indexMaturity = new double[n];
  //    for (int i = 0; i < n; i++) {
  //      indexFixing[i] = i * tau;
  //      paymentTimes[i] = (i + 1) * tau;
  //      indexMaturity[i] = paymentTimes[i];
  //      spreads[i] = i * 0.001;
  //      yearFracs[i] = tau;
  //    }
  //
  //    final GenericAnnuity<CouponIborSpread> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, INDEX, indexMaturity, yearFracs, 1.0, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);
  //    final GenericAnnuity<CouponIborSpread> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, INDEX, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FUNDING_CURVE_NAME,
  //        FUNDING_CURVE_NAME, false);
  //
  //    final Swap<?, ?> swap = new TenorSwap<CouponIborSpread>(payLeg, receiveLeg);
  //    doTest(swap, CURVES);
  //  }

  private void doTest(final InstrumentDerivative ird, final YieldCurveBundle curves) {
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

  private Map<String, Double> finiteDifferanceSense(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    final Map<String, Double> result = new HashMap<>();
    final Set<String> names = curves.getAllNames();
    for (final String name : names) {
      final YieldAndDiscountCurve curve = curves.getCurve(name);
      final YieldAndDiscountCurve upCurve = curve.withParallelShift(EPS);
      final YieldCurveBundle newCurves = new YieldCurveBundle();
      newCurves.addAll(curves);
      newCurves.replaceCurve(name, upCurve);
      final double upRate = ird.accept(PRC, newCurves);
      final YieldAndDiscountCurve downCurve = curve.withParallelShift(-EPS);
      newCurves.replaceCurve(name, downCurve);
      final double downRate = ird.accept(PRC, newCurves);

      final double res = (upRate - downRate) / 2 / EPS;
      result.put(name, res);
    }
    return result;
  }

  private static class MyFunction extends Function1D<Double, Double> {
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
    public Double evaluate(final Double t) {

      return (_a + _b * t) * Math.exp(-_c * t) + _d;
    }

  }
}
