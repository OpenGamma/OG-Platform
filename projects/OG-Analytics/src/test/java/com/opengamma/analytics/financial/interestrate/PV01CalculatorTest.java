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
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
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
public class PV01CalculatorTest {
  private final static YieldAndDiscountCurve FUNDING_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.006, 0.1, 0.05)));
  private final static YieldAndDiscountCurve LIBOR_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.005, 0.11, 0.055)));
  private final static PV01Calculator PV01 = PV01Calculator.getInstance();
  private final static PresentValueCalculator PV = PresentValueCalculator.getInstance();
  private final static double EPS = 1e-8;

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
    final double paymentTime = 0.5;
    final double paymentYearFraction = 30. / 360;
    final double fixingTime = paymentTime - 2. / 365;
    final double fixingPeriodStart = paymentTime;
    final double fixingPeriodEnd = 7. / 12;
    final double fixingYearFraction = 31. / 365;
    final double rate = 0.15;
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING,
        true);
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
  //    final int quantity = 123;
  //    final double price = 0.973;
  //    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, price, 1, paymentAccrualFactor, quantity,
  //        "K", FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  //    doTest(ir, CURVES);
  //  }

  @Test
  public void testFixedCouponAnnuity() {
    final int n = 15;
    final double alpha = 0.49;
    final double yearFrac = 0.51;
    final double[] paymentTimes = new double[n];

    final double[] yearFracs = new double[n];
    final double coupon = 0.03;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      yearFracs[i] = yearFrac;
    }

    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CUR, paymentTimes, 31234.31231, coupon, yearFracs, FUNDING_CURVE_NAME, true);
    doTest(annuity, CURVES);
  }

  //  @Test
  //  public void testForwardLiborAnnuity() {
  //    final int n = 15;
  //    final double alpha = 0.245;
  //    final double yearFrac = 0.25;
  //    final double spread = 0.01;
  //    final double[] paymentTimes = new double[n];
  //    final double[] indexFixing = new double[n];
  //    final double[] indexMaturity = new double[n];
  //    final double[] paymentYearFracs = new double[n];
  //    final double[] forwardYearFracs = new double[n];
  //    final double[] spreads = new double[n];
  //    for (int i = 0; i < n; i++) {
  //      paymentTimes[i] = (i + 1) * alpha - 0.001;
  //      indexFixing[i] = i * alpha + 0.1;
  //      indexMaturity[i] = paymentTimes[i] + 0.1;
  //      paymentYearFracs[i] = yearFrac;
  //      forwardYearFracs[i] = alpha;
  //      spreads[i] = spread + 0.001 * i;
  //    }
  //
  //    final AnnuityCouponIbor annuity = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, INDEX, indexFixing, indexMaturity, paymentYearFracs, forwardYearFracs, spreads, Math.E, FUNDING_CURVE_NAME,
  //        LIBOR_CURVE_NAME, true);
  //    doTest(annuity, CURVES);
  //
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
    final BondFixedTransaction trade = new BondFixedTransaction(bond, 100, 100, bond, 90);
    doTest(trade, CURVES);
  }

  //  @Test
  //  public void testTenorSwap() {
  //    final int n = 20;
  //    final double tau = 0.25;
  //    final double[] paymentTimes = new double[n];
  //    final double[] spreads = new double[n];
  //    final double[] yearFracs = new double[n];
  //    final double[] indexFixing = new double[n];
  //    final double[] indexMaturity = new double[n];
  //    for (int i = 0; i < n; i++) {
  //      paymentTimes[i] = (i + 1) * tau;
  //      indexFixing[i] = i * tau;
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
    final Map<String, Double> ana = PV01.visit(ird, curves);
    final Map<String, Double> fd = finiteDifferancePV01(ird, curves);
    final Set<String> names = curves.getAllNames();
    for (final String name : names) {
      if (ana.containsKey(name)) {
        assertEquals(ana.get(name), fd.get(name), EPS);
      } else {
        assertEquals(0.0, fd.get(name), 0.0);
      }
    }
  }

  private Map<String, Double> finiteDifferancePV01(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    final Map<String, Double> result = new HashMap<>();
    final Set<String> names = curves.getAllNames();
    for (final String name : names) {
      final YieldAndDiscountCurve curve = curves.getCurve(name);
      final YieldAndDiscountCurve upCurve = curve.withParallelShift(EPS);
      final YieldCurveBundle newCurves = new YieldCurveBundle();
      newCurves.addAll(curves);
      newCurves.replaceCurve(name, upCurve);
      final double upPV = ird.accept(PV, newCurves);
      final YieldAndDiscountCurve downCurve = curve.withParallelShift(-EPS);
      newCurves.replaceCurve(name, downCurve);
      final double downPV = ird.accept(PV, newCurves);

      final double res = (upPV - downPV) / 10000 / 2 / EPS;
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
