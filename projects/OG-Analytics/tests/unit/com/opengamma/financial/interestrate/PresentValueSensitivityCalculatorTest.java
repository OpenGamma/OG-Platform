/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.financial.interestrate.SimpleInstrumentFactory.makeOISSwap;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.SwapGenerator;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.AddCurveSpreadFunction;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.curve.SpreadDoublesCurve;
import com.opengamma.math.curve.SubtractCurveSpreadFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondDoublesPairComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

/**
 * 
 */
public class PresentValueSensitivityCalculatorTest {
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };

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
    Cash cash = new Cash(CUR, t, 1, r, FIVE_PC_CURVE_NAME);
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
    cash = new Cash(CUR, t, 1, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
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
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);

    final double paymentTime = 0.5;
    final double fixingTime = paymentTime;
    final double fixingPeriodStart = paymentTime;
    final double fixingPeriodEnd = 7. / 12;
    final double paymentYearFraction = fixingPeriodEnd - paymentTime;
    final double fixingYearFraction = paymentYearFraction;
    final double tau = 1.0 / 12.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(paymentTime) / curve.getDiscountFactor(fixingPeriodEnd) - 1.0) / tau;
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, ZERO_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction,
        rate, FIVE_PC_CURVE_NAME);
    final double ratio = curve.getDiscountFactor(paymentTime) / curve.getDiscountFactor(fixingPeriodEnd) / (1 + tau * rate);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(fra, CURVES);
    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertTrue(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<DoublesPair> temp = sense.get(ZERO_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == paymentTime) {
        assertEquals(0.0, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }

    temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == paymentTime) {
        assertEquals(-paymentTime * ratio, pair.getSecond(), 1e-12);
      } else if (pair.getFirst() == fixingPeriodEnd) {
        assertEquals(fixingPeriodEnd * ratio, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
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
    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(fixingPeriodStartTime) / curve.getDiscountFactor(fixingPeriodEndTime) - 1.0) / fixingPeriodAccrualFactor;
    final double price = 1 - rate;
    final InterestRateFuture ir = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingPeriodAccrualFactor, referencePrice, 1, paymentAccrualFactor, "K", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final Map<String, List<DoublesPair>> sensitivities = PVSC.visit(ir, CURVES);
    final double ratio = paymentAccrualFactor / fixingPeriodAccrualFactor * curve.getDiscountFactor(fixingPeriodStartTime) / curve.getDiscountFactor(fixingPeriodEndTime);

    final List<DoublesPair> temp = sensitivities.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {

      if (CompareUtils.closeEquals(pair.getFirst(), fixingPeriodStartTime, 1e-16)) {
        assertEquals(fixingPeriodStartTime * ratio, pair.getSecond(), 1e-12);
      } else if (CompareUtils.closeEquals(pair.getFirst(), fixingPeriodEndTime, 1e-16)) {
        assertEquals(-fixingPeriodEndTime * ratio, pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
  }

  @Test
  public void testFixedCouponAnnuity() {

    final boolean isPayer = true;
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

    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CUR, paymentTimes, Math.PI, coupon, yearFracs, FIVE_PC_CURVE_NAME, isPayer);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);
    final List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    final Iterator<DoublesPair> iterator = temp.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      final DoublesPair pair = iterator.next();
      final double t = paymentTimes[index];
      assertEquals(t, pair.getFirst(), 0.0);
      assertEquals(-(isPayer ? -1 : 1) * t * yearFrac * Math.PI * coupon * curve.getDiscountFactor(t), pair.getSecond(), 1e-12);
      index++;
    }
  }

  @Test
  public void testAnnuityCouponIbor() {
    final int settlementDays = 2;
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final boolean isEOM = true;
    final boolean isPayer = true;
    final double notional = 100000000.0;
    final ZonedDateTime settleDate = DateUtils.getUTCDate(2014, 3, 20);
    final Period indexTenor = Period.ofMonths(3);
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final IborIndex INDEX = new IborIndex(CUR, indexTenor, settlementDays, CALENDAR, dayCount, businessDayConvention, isEOM);
    final AnnuityCouponIborDefinition iborAnnuityDefinition = AnnuityCouponIborDefinition.from(settleDate, Period.ofYears(5), notional, INDEX, !isPayer);

    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final GenericAnnuity<? extends Payment> iborAnnuity = iborAnnuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);

    // First coupon
    final List<Double> expectedListForward1 = Arrays.asList(new Double[] {((CouponIbor) iborAnnuity.getNthPayment(0)).getFixingPeriodStartTime(),
        ((CouponIbor) iborAnnuity.getNthPayment(0)).getFixingPeriodEndTime() });
    final PresentValueSensitivity sensi1 = new PresentValueSensitivity(PVSC.visit(iborAnnuity.getNthPayment(0), curves));
    for (int loopdsc = 0; loopdsc < expectedListForward1.size(); loopdsc++) {
      assertEquals(expectedListForward1.get(loopdsc), sensi1.getSensitivities().get(FORWARD_CURVE_NAME).get(loopdsc).first);
    }
    // All coupons
    PresentValueSensitivity sensi = new PresentValueSensitivity(PVSC.visit(iborAnnuity, curves));
    sensi = sensi.clean();
    // Time for discounting curve    
    final Set<Double> expectedSetDiscounting = new TreeSet<Double>();
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      expectedSetDiscounting.add(iborAnnuity.getNthPayment(loopcpn).getPaymentTime());
    }
    final List<Double> expectedListDiscounting = new ArrayList<Double>(expectedSetDiscounting);
    for (int loopdsc = 0; loopdsc < expectedListDiscounting.size(); loopdsc++) {
      assertEquals(expectedListDiscounting.get(loopdsc), sensi.getSensitivities().get(FUNDING_CURVE_NAME).get(loopdsc).first);
    }
    // Time for forward curve   
    final Set<Double> expectedSetForward = new TreeSet<Double>();
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      final CouponIbor iborCpn = (CouponIbor) iborAnnuity.getNthPayment(loopcpn);
      expectedSetForward.add(iborCpn.getFixingPeriodStartTime());
      expectedSetForward.add(iborCpn.getFixingPeriodEndTime());
    }
    final List<Double> expectedListForward = new ArrayList<Double>(expectedSetForward);
    for (int loopdsc = 0; loopdsc < expectedListForward.size(); loopdsc++) {
      assertEquals(expectedListForward.get(loopdsc), sensi.getSensitivities().get(FORWARD_CURVE_NAME).get(loopdsc).first);
    }

  }

  @Test
  public void testForwardLiborAnnuity() {
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double yield = curve.getInterestRate(0.0);
    final double eps = 1e-9;

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

    final boolean isPayer = true;
    final AnnuityCouponIbor annuitySingleCurve = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, FIVE_PC_CURVE_NAME,
        FIVE_PC_CURVE_NAME,
        isPayer);
    final AnnuityCouponIbor annuity = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME,
        isPayer);

    final Map<String, List<DoublesPair>> senseSingleCurve = PVSC.visit(annuitySingleCurve, CURVES);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);

    //1. single curve sense   
    List<DoublesPair> senseAnal = netSensitvities(senseSingleCurve.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(annuitySingleCurve, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);

    // 2. Forward curve sensitivity
    senseAnal = netSensitvities(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    senseFD = curveSensitvityFDCalculator(annuity, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);

    // 3. Funding curve sensitivity
    senseAnal = netSensitvities(sense.get(ZERO_PC_CURVE_NAME), eps, eps);
    senseFD = curveSensitvityFDCalculator(annuity, PVC, CURVES, ZERO_PC_CURVE_NAME, nodeTimes, eps, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  @Test
  public void testGenericAnnuity() {

    final int n = 5;
    final double[] times = new double[] {0.01, 0.5, 1, 3, 10 };
    final double[] amounts = new double[] {100000, 1, 234, 452, 0.034 }; //{100000, 1, 234, -452, 0.034}
    final String[] curveNames = new String[] {FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME };

    final Payment[] payments = new Payment[5];
    for (int i = 0; i < n; i++) {
      payments[i] = new PaymentFixed(CUR, times[i], amounts[i], curveNames[i]);
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
    final PaymentFixed payment = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);

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

    final CouponFixed payment = new CouponFixed(CUR, time, ZERO_PC_CURVE_NAME, yearFrac, notional, coupon);

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

    final CouponIbor payment = new CouponIbor(CUR, time, ZERO_PC_CURVE_NAME, paymentYF, notional, resetTime, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);

    final double[] nodeTimes = new double[] {resetTime, maturity };
    final double[] yields = new double[] {0.05, 0.05 };

    final YieldAndDiscountCurve tempCurve = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));

    CouponIbor bumpedPayment = new CouponIbor(CUR, time, ZERO_PC_CURVE_NAME, paymentYF, notional, resetTime, resetTime, maturity, forwardYF, spread, "Bumped Curve");

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

    bumpedPayment = new CouponIbor(CUR, time, "Bumped Curve", paymentYF, notional, resetTime, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);

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

    final CouponIbor payment = new CouponIbor(CUR, time, FIVE_PC_CURVE_NAME, paymentYF, 1.0, resetTime, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);

    final double[] nodeTimes = new double[] {resetTime, time, maturity };
    final double[] yields = new double[] {0.05, 0.05, 0.05 };

    final YieldAndDiscountCurve tempCurve = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yields, new LinearInterpolator1D()));

    final CouponIbor bumpedPayment = new CouponIbor(CUR, time, "Bumped Curve", paymentYF, 1.0, resetTime, resetTime, maturity, forwardYF, spread, "Bumped Curve");

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

  @Test
  public void testFixedFloatSwap() {
    final double eps = 1e-9;
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
    final boolean isPayer = true;

    final Swap<?, ?> swapSingleCurve = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, isPayer);
    final Swap<?, ?> swapPayer = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, isPayer);
    final Swap<?, ?> swapReceiver = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, !isPayer);
    final Map<String, List<DoublesPair>> sensiPayer = PVSC.visit(swapPayer, CURVES);
    final Map<String, List<DoublesPair>> sensiReceiver = PVSC.visit(swapReceiver, CURVES);
    final Map<String, List<DoublesPair>> sensiSwapSingleCurve = PVSC.visit(swapSingleCurve, CURVES);

    //1. Single curve sensitivity 
    List<DoublesPair> senSwapSingleCurve = netSensitvities(sensiSwapSingleCurve.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> fdsenSwapSingleCurve = curveSensitvityFDCalculator(swapSingleCurve, PVC, CURVES, FIVE_PC_CURVE_NAME, floatPaymentTimes,
        eps, eps);
    assertSensitivityEquals(senSwapSingleCurve, fdsenSwapSingleCurve, eps);

    // 2. Forward curve sensitivity
    List<DoublesPair> senPayerFwd = netSensitvities(sensiPayer.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senReceiverFwd = netSensitvities(sensiReceiver.get(FIVE_PC_CURVE_NAME), eps, eps);

    List<DoublesPair> fdSenPayerFwd = curveSensitvityFDCalculator(swapPayer, PVC, CURVES, FIVE_PC_CURVE_NAME, floatPaymentTimes,
            eps, eps);
    List<DoublesPair> fdSenReceiverFwd = curveSensitvityFDCalculator(swapReceiver, PVC, CURVES, FIVE_PC_CURVE_NAME, floatPaymentTimes,
            eps, eps);

    assertSensitivityEquals(fdSenPayerFwd, senPayerFwd, eps);
    assertSensitivityEquals(fdSenReceiverFwd, senReceiverFwd, eps);

    // 3. Funding curve sensitivity
    senPayerFwd = netSensitvities(sensiPayer.get(ZERO_PC_CURVE_NAME), eps, eps);
    senReceiverFwd = netSensitvities(sensiReceiver.get(ZERO_PC_CURVE_NAME), eps, eps);

    fdSenPayerFwd = curveSensitvityFDCalculator(swapPayer, PVC, CURVES, ZERO_PC_CURVE_NAME, floatPaymentTimes,
            eps, eps);
    fdSenReceiverFwd = curveSensitvityFDCalculator(swapReceiver, PVC, CURVES, ZERO_PC_CURVE_NAME, floatPaymentTimes
            , eps, eps);

    assertSensitivityEquals(fdSenPayerFwd, senPayerFwd, eps);
    assertSensitivityEquals(fdSenReceiverFwd, senReceiverFwd, eps);

  }

  private static void assertSensitivityEquals(List<DoublesPair> expected, List<DoublesPair> accual, double tol) {
    assertEquals(expected.size(), accual.size(), 0);
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).first, accual.get(i).first, 0.0);
      assertEquals(expected.get(i).second, accual.get(i).second, tol);
    }
  }

  // Swaption description
  //  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2014, 3, 18);
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2014, 3, 20);
  //ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final SwapGenerator SWAP_GENERATOR = new SwapGenerator(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX);
  private static final CMSIndex CMS_INDEX = new CMSIndex(SWAP_GENERATOR, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final FixedCouponSwap<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);

  @Test
  public void testFixedCouponSwap() {

    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    PresentValueSensitivity pvsSwapPayer = new PresentValueSensitivity(PVSC.visit(SWAP_PAYER, curves));
    pvsSwapPayer = pvsSwapPayer.clean();

    final double eps = 1e-8;
    final int nbPayDate = SWAP_PAYER.getSecondLeg().getPayments().length;
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesName = {bumpedCurveName, FORWARD_CURVE_NAME };
    final FixedCouponSwap<Coupon> swapBumpedFunding = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesName);
    final double pvSwap = PVC.visit(SWAP_PAYER, curves);
    // 2. Funding curve sensitivity
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    final double[] nodeTimes = new double[nbPayDate + 1];
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimes[i + 1] = SWAP_PAYER.getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimes[i + 1]);
    }
    final YieldAndDiscountCurve tempCurve_funding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimes, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> temp_funding = pvsSwapPayer.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurve_funding.withSingleShift(nodeTimes[i + 1], eps);
      final YieldCurveBundle curvesFunding = new YieldCurveBundle();
      curvesFunding.addAll(curves);
      curvesFunding.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpv = PVC.visit(swapBumpedFunding, curvesFunding);
      final double res = (bumpedpv - pvSwap) / eps;
      final DoublesPair pair = temp_funding.get(i);
      assertEquals("Node " + i, nodeTimes[i + 1], pair.getFirst(), 0.0);
      assertEquals("Node " + i, res, pair.getSecond(), 1E+0);
    }
  }

  @Test
  final void testOISSwap() {
    double eps = 1e-9;
    double notional = 1e8;
    double relTol = eps;
    double absTol = notional * eps;
    double maturity = 10.0;
    double rate = Math.exp(0.05) - 1;

    double[] nodeTimes = new double[10];
    for (int i = 0; i < 10; i++) {
      nodeTimes[i] = 1.0 * (i + 1);
    }

    OISSwap swap = makeOISSwap(maturity, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, rate, notional);
    Map<String, List<DoublesPair>> sense = PVSC.visit(swap, CURVES);

    //1. single curve sense   
    List<DoublesPair> senseAnal = netSensitvities(sense.get(FIVE_PC_CURVE_NAME), relTol, absTol);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(swap, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, relTol, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    swap = makeOISSwap(maturity, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, rate, notional);
    sense = PVSC.visit(swap, CURVES);

    //    //2. index curve sense   
    senseAnal = netSensitvities(sense.get(FIVE_PC_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(swap, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, relTol, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    //    //2. funding curve sense   
    senseAnal = netSensitvities(sense.get(ZERO_PC_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(swap, PVC, CURVES, ZERO_PC_CURVE_NAME, nodeTimes, relTol, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

  }

  final static List<DoublesPair> curveSensitvityFDCalculator(final InterestRateDerivative ird, AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final YieldCurveBundle curves, final String curveName, final double[] t, final double relTol, final double absTol) {
    List<DoublesPair> res = new ArrayList<DoublesPair>();
    for (double time : t) {
      double sense = curveSensitvityFDCalculator(ird, calculator, curves, curveName, time);
      res.add(new DoublesPair(time, sense));
    }
    return netSensitvities(res, relTol, absTol);
  }

  @SuppressWarnings("unchecked")
  final static double curveSensitvityFDCalculator(final InterestRateDerivative ird, AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final YieldCurveBundle curves, final String curveName, final double t) {

    final double eps = 1e-6;

    Function1D<Double, Double> blip = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return (x == t ? eps : 0.0); //TODO should we check within a tolerance? 
      }
    };

    FunctionalDoublesCurve blipCurve = FunctionalDoublesCurve.from(blip);
    YieldAndDiscountCurve originalCurve = curves.getCurve(curveName);

    @SuppressWarnings("rawtypes")
    Curve[] curveSet = new Curve[] {originalCurve.getCurve(), blipCurve };
    YieldAndDiscountCurve upCurve = new YieldCurve(SpreadDoublesCurve.from(curveSet, new AddCurveSpreadFunction()));
    YieldAndDiscountCurve downCurve = new YieldCurve(SpreadDoublesCurve.from(curveSet, new SubtractCurveSpreadFunction()));

    curves.replaceCurve(curveName, upCurve);
    double up = calculator.visit(ird, curves);
    curves.replaceCurve(curveName, downCurve);
    double down = calculator.visit(ird, curves);
    curves.replaceCurve(curveName, originalCurve);

    return (up - down) / 2 / eps;
  }

  @Test
  final void testNetSensitvities() {
    final List<DoublesPair> old = new ArrayList<DoublesPair>();
    old.add(new DoublesPair(0.23, 12.3));
    old.add(new DoublesPair(0.231, -12.3));
    old.add(new DoublesPair(0.23, -12.3));
    old.add(new DoublesPair(1.23, 3.24));
    old.add(new DoublesPair(1.78, -3.24));
    old.add(new DoublesPair(1.23, -1.0));
    old.add(new DoublesPair(1.23, 1.0));

    final List<DoublesPair> expected = new ArrayList<DoublesPair>();
    expected.add(new DoublesPair(0.231, -12.3));
    expected.add(new DoublesPair(1.23, 3.24));
    expected.add(new DoublesPair(1.78, -3.24));

    List<DoublesPair> res = netSensitvities(old, 1e-9, 1e-12);
    assertEquals(expected.size(), res.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).first, res.get(i).first, 0.0);
      assertEquals(expected.get(i).second, res.get(i).second, 1e-9);
    }
  }

  /**
   * Takes a list of curve sensitivities (i.e. an unordered list of pairs of times and sensitivities) and returns a list order by ascending
   * time, and with sensitivities that occur at the same time netted (zero net sensitivities are removed)
   * @param old An unordered list of pairs of times and sensitivities
   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored 
   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored 
   * @return A time ordered netted list
   */
  final static List<DoublesPair> netSensitvities(final List<DoublesPair> old, final double relTol, final double absTol) {

    Validate.notNull(old, "null list");
    Validate.isTrue(relTol >= 0.0 && absTol >= 0.0);

    final List<DoublesPair> res = new ArrayList<DoublesPair>();
    TreeSet<DoublesPair> ts = new TreeSet<DoublesPair>(FirstThenSecondDoublesPairComparator.INSTANCE);
    ts.addAll(old);

    Iterator<DoublesPair> iterator = ts.iterator();
    DoublesPair pair = iterator.next();
    double t = pair.getFirst();
    double sum = pair.getSecond();
    double scale = Math.abs(sum);
    while (iterator.hasNext()) {
      pair = iterator.next();
      if (pair.getFirstDouble() > t) {
        if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
          res.add(new DoublesPair(t, sum));
        }
        t = pair.getFirstDouble();
        sum = pair.getSecondDouble();
        scale = Math.abs(sum);
      } else {
        sum += pair.getSecondDouble();
        scale += Math.abs(pair.getSecondDouble());
      }
    }
    if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
      res.add(new DoublesPair(t, sum));
    }

    return res;
  }

  //  // Merge consecutive sensitivity with same time.
  //  List<DoublesPair> mergeSameTimes(final List<DoublesPair> old) {
  //    final List<DoublesPair> res = new ArrayList<DoublesPair>();
  //    final Iterator<DoublesPair> iterator = old.iterator();
  //    DoublesPair pair = iterator.next();
  //    double t = pair.getFirst();
  //    double sum = pair.getSecond();
  //
  //    while (iterator.hasNext()) {
  //      pair = iterator.next();
  //      if (CompareUtils.closeEquals(pair.getFirst(), t, 1e-6)) {
  //        sum += pair.getSecond();
  //      } else {
  //        res.add(new DoublesPair(t, sum));
  //        t = pair.getFirst();
  //        sum = pair.getSecond();
  //      }
  //    }
  //    res.add(new DoublesPair(t, sum));
  //
  //    return res;
  //  }

}
