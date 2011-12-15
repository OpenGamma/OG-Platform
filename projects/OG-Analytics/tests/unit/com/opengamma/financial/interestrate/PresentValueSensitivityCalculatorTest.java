/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.financial.interestrate.FDCurveSensitivityCalculator.curveSensitvityFDCalculator;
import static com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils.clean;
import static com.opengamma.financial.interestrate.SimpleInstrumentFactory.makeOISSwap;
import static com.opengamma.financial.interestrate.TestUtils.assertSensitivityEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.SwapGenerator;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
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
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PresentValueSensitivityCalculatorTest {
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();
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
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};

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
    Cash cash = new Cash(CUR, 0, t, 1, r, t, FIVE_PC_CURVE_NAME);
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
    cash = new Cash(CUR, tradeTime, t, 1, r, yearFrac, FIVE_PC_CURVE_NAME);
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
    double eps = 1e-9;

    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);

    final double paymentTime = 0.5;
    final double fixingTime = paymentTime;
    final double fixingPeriodStart = paymentTime;
    final double fixingPeriodEnd = 7. / 12;
    final double paymentYearFraction = fixingPeriodEnd - paymentTime;
    final double fixingYearFraction = paymentYearFraction;
    final double[] nodeTimes = new double[] {fixingPeriodStart, fixingPeriodEnd};
    final double tau = 1.0 / 12.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (curve.getDiscountFactor(paymentTime) / curve.getDiscountFactor(fixingPeriodEnd) - 1.0) / tau;
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, ZERO_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction,
        rate, FIVE_PC_CURVE_NAME);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(fra, CURVES);
    List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(fra, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  @Test
  public void testFutures() {
    double eps = 1e-9;
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final double[] nodeTimes = new double[] {fixingPeriodStartTime, fixingPeriodEndTime};

    final InterestRateFuture ir = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 0.0, 1.0, paymentAccrualFactor, "K",
        FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(ir, CURVES);

    List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(ir, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);

    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  @Test
  public void testFixedCouponAnnuity() {
    double eps = 1e-9;
    final boolean isPayer = true;
    final int n = 15;
    final double alpha = 0.49;
    final double yearFrac = 0.51;
    final double[] paymentTimes = new double[n];
    final double[] yearFracs = new double[n];
    final double coupon = 0.07;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      yearFracs[i] = yearFrac;
    }

    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CUR, paymentTimes, Math.PI, coupon, yearFracs, FIVE_PC_CURVE_NAME, isPayer);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);

    List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(annuity, PVC, CURVES, FIVE_PC_CURVE_NAME, paymentTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  @Test
  public void testAnnuityCouponIbor() {
    double eps = 1e-9;
    double notional = 1e8;
    double relTol = eps;
    double absTol = notional * eps;

    final int settlementDays = 2;
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final boolean isEOM = true;
    final boolean isPayer = true;
    final ZonedDateTime settleDate = DateUtils.getUTCDate(2014, 3, 20);
    final Period indexTenor = Period.ofMonths(3);
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final IborIndex INDEX = new IborIndex(CUR, indexTenor, settlementDays, CALENDAR, dayCount, businessDayConvention, isEOM);
    final AnnuityCouponIborDefinition iborAnnuityDefinition = AnnuityCouponIborDefinition.from(settleDate, Period.ofYears(5), notional, INDEX, !isPayer);

    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final GenericAnnuity<? extends Payment> iborAnnuity1Curve = iborAnnuityDefinition.toDerivative(REFERENCE_DATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
    final GenericAnnuity<? extends Payment> iborAnnuity = iborAnnuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);

    //produce a array of strictly ascending times
    final Set<Double> times = new TreeSet<Double>();
    for (int i = 0; i < iborAnnuity.getNumberOfPayments(); i++) {
      CouponIbor coupon = (CouponIbor) iborAnnuity.getNthPayment(i);
      times.add(coupon.getPaymentTime());
      times.add(coupon.getFixingPeriodStartTime());
      times.add(coupon.getFixingPeriodEndTime());
    }
    Double[] tArray = times.toArray(new Double[] {});
    double[] t = new double[times.size()];
    for (int i = 0; i < times.size(); i++) {
      t[i] = tArray[i];
    }

    //single curve 
    Map<String, List<DoublesPair>> sense = PVSC.visit(iborAnnuity1Curve, curves);
    assertTrue(!sense.containsValue(FORWARD_CURVE_NAME));
    List<DoublesPair> senseAnal = clean(sense.get(FUNDING_CURVE_NAME), relTol, absTol);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(iborAnnuity1Curve, PVC, curves, FUNDING_CURVE_NAME, t, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    //2 curves 
    sense = PVSC.visit(iborAnnuity, curves);
    senseAnal = clean(sense.get(FUNDING_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(iborAnnuity, PVC, curves, FUNDING_CURVE_NAME, t, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    senseAnal = clean(sense.get(FORWARD_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(iborAnnuity, PVC, curves, FORWARD_CURVE_NAME, t, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);
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
    final AnnuityCouponIbor annuitySingleCurve = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, IBOR_INDEX, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E,
        FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, isPayer);
    final AnnuityCouponIbor annuity = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, IBOR_INDEX, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, ZERO_PC_CURVE_NAME,
        FIVE_PC_CURVE_NAME, isPayer);

    final Map<String, List<DoublesPair>> senseSingleCurve = PVSC.visit(annuitySingleCurve, CURVES);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);

    //1. single curve sense   
    List<DoublesPair> senseAnal = clean(senseSingleCurve.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(annuitySingleCurve, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);

    // 2. Forward curve sensitivity
    senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    senseFD = curveSensitvityFDCalculator(annuity, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);

    // 3. Funding curve sensitivity
    senseAnal = clean(sense.get(ZERO_PC_CURVE_NAME), eps, eps);
    senseFD = curveSensitvityFDCalculator(annuity, PVC, CURVES, ZERO_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  @Test
  public void testGenericAnnuity() {
    double eps = 1e-5;
    final int n = 5;
    final double[] times = new double[] {0.01, 0.5, 1, 3, 10};
    final double[] amounts = new double[] {100000, 1, 234, 452, 0.034}; //{100000, 1, 234, -452, 0.034}
    final String[] curveNames = new String[] {FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME};

    final Payment[] payments = new Payment[5];
    for (int i = 0; i < n; i++) {
      payments[i] = new PaymentFixed(CUR, times[i], amounts[i], curveNames[i]);
    }

    final GenericAnnuity<Payment> annuity = new GenericAnnuity<Payment>(payments);
    final Map<String, List<DoublesPair>> sense = PVSC.visit(annuity, CURVES);
    List<DoublesPair> sense0FD = curveSensitvityFDCalculator(annuity, PVC, CURVES, ZERO_PC_CURVE_NAME, times, eps);
    List<DoublesPair> sense5FD = curveSensitvityFDCalculator(annuity, PVC, CURVES, FIVE_PC_CURVE_NAME, times, eps);
    assertSensitivityEquals(sense0FD, sense.get(ZERO_PC_CURVE_NAME), eps);
    assertSensitivityEquals(sense5FD, sense.get(FIVE_PC_CURVE_NAME), eps);
  }

  @Test
  public void testFixedPayment() {
    final double eps = 1e-9;
    final double time = 1.23;
    final double amount = 4345.3;
    final PaymentFixed payment = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(payment, CURVES);
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));
    double senseFD = curveSensitvityFDCalculator(payment, PVC, CURVES, FIVE_PC_CURVE_NAME, time);
    assertEquals(senseFD, sense.get(FIVE_PC_CURVE_NAME).get(0).second, eps * amount);
  }

  @Test
  public void testFixedCouponPayment() {
    final double eps = 1e-9;
    final double time = 1.23;
    final double yearFrac = 0.56;
    final double coupon = 0.07;
    final double notional = 100;

    final CouponFixed payment = new CouponFixed(CUR, time, ZERO_PC_CURVE_NAME, yearFrac, notional, coupon);

    final Map<String, List<DoublesPair>> sense = PVSC.visit(payment, CURVES);

    assertFalse(sense.containsKey(FIVE_PC_CURVE_NAME));

    double senseFD = curveSensitvityFDCalculator(payment, PVC, CURVES, ZERO_PC_CURVE_NAME, time);
    assertEquals(senseFD, sense.get(ZERO_PC_CURVE_NAME).get(0).second, eps * notional);

  }

  @Test
  public void testCouponIbor() {
    double eps = 1e-9;
    final double paymentTime = 2.45;
    final double resetTime = 2.0;
    final double maturity = 2.5;
    final double paymentYF = 0.48;
    final double forwardYF = 0.5;
    final double spread = 0.04;
    final double notional = 100000000;

    final CouponIbor payment1Curve = new CouponIbor(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYF, notional, resetTime, IBOR_INDEX, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);
    final CouponIbor payment = new CouponIbor(CUR, paymentTime, ZERO_PC_CURVE_NAME, paymentYF, notional, resetTime, IBOR_INDEX, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);

    final double[] nodeTimes = new double[] {resetTime, paymentTime, maturity};

    //single curve 
    Map<String, List<DoublesPair>> sense = clean(PVSC.visit(payment1Curve, CURVES), eps, eps * notional);
    List<DoublesPair> sense5FD = curveSensitvityFDCalculator(payment1Curve, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(sense5FD, sense.get(FIVE_PC_CURVE_NAME), eps * notional);

    //2 curves 
    sense = clean(PVSC.visit(payment, CURVES), eps, eps * notional);
    List<DoublesPair> sense0FD = curveSensitvityFDCalculator(payment, PVC, CURVES, ZERO_PC_CURVE_NAME, nodeTimes, eps);
    sense5FD = curveSensitvityFDCalculator(payment, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(sense0FD, sense.get(ZERO_PC_CURVE_NAME), eps * notional);
    assertSensitivityEquals(sense5FD, sense.get(FIVE_PC_CURVE_NAME), eps * notional);
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

    final Swap<?, ?> swapSingleCurve = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, IBOR_INDEX, swapRate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, isPayer);
    final Swap<?, ?> swapPayer = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, IBOR_INDEX, swapRate, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, isPayer);
    final Swap<?, ?> swapReceiver = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, IBOR_INDEX, swapRate, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, !isPayer);
    final Map<String, List<DoublesPair>> sensiPayer = PVSC.visit(swapPayer, CURVES);
    final Map<String, List<DoublesPair>> sensiReceiver = PVSC.visit(swapReceiver, CURVES);
    final Map<String, List<DoublesPair>> sensiSwapSingleCurve = PVSC.visit(swapSingleCurve, CURVES);

    //1. Single curve sensitivity 
    List<DoublesPair> senSwapSingleCurve = clean(sensiSwapSingleCurve.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> fdsenSwapSingleCurve = curveSensitvityFDCalculator(swapSingleCurve, PVC, CURVES, FIVE_PC_CURVE_NAME, floatPaymentTimes, eps);
    assertSensitivityEquals(senSwapSingleCurve, fdsenSwapSingleCurve, eps);

    // 2. Forward curve sensitivity
    List<DoublesPair> senPayerFwd = clean(sensiPayer.get(FIVE_PC_CURVE_NAME), eps, eps);
    List<DoublesPair> senReceiverFwd = clean(sensiReceiver.get(FIVE_PC_CURVE_NAME), eps, eps);

    List<DoublesPair> fdSenPayerFwd = curveSensitvityFDCalculator(swapPayer, PVC, CURVES, FIVE_PC_CURVE_NAME, floatPaymentTimes, eps);
    List<DoublesPair> fdSenReceiverFwd = curveSensitvityFDCalculator(swapReceiver, PVC, CURVES, FIVE_PC_CURVE_NAME, floatPaymentTimes, eps);

    assertSensitivityEquals(fdSenPayerFwd, senPayerFwd, eps);
    assertSensitivityEquals(fdSenReceiverFwd, senReceiverFwd, eps);

    // 3. Funding curve sensitivity
    senPayerFwd = clean(sensiPayer.get(ZERO_PC_CURVE_NAME), eps, eps);
    senReceiverFwd = clean(sensiReceiver.get(ZERO_PC_CURVE_NAME), eps, eps);

    fdSenPayerFwd = curveSensitvityFDCalculator(swapPayer, PVC, CURVES, ZERO_PC_CURVE_NAME, floatPaymentTimes, eps);
    fdSenReceiverFwd = curveSensitvityFDCalculator(swapReceiver, PVC, CURVES, ZERO_PC_CURVE_NAME, floatPaymentTimes, eps);

    assertSensitivityEquals(fdSenPayerFwd, senPayerFwd, eps);
    assertSensitivityEquals(fdSenReceiverFwd, senReceiverFwd, eps);

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
  private static final IndexSwap CMS_INDEX = new IndexSwap(SWAP_GENERATOR, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final FixedCouponSwap<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);

  @Test
  public void testFixedCouponSwap() {
    double eps = 1e-9;
    final YieldCurveBundle curves = TestsDataSets.createCurves1();

    GenericAnnuity<CouponFixed> fixedLeg = SWAP_PAYER.getFirstLeg();
    GenericAnnuity<Coupon> floatLeg = SWAP_PAYER.getSecondLeg();

    //produce a array of strictly ascending times
    final Set<Double> times = new TreeSet<Double>();
    for (int i = 0; i < fixedLeg.getNumberOfPayments(); i++) {
      CouponFixed coupon = fixedLeg.getNthPayment(i);
      times.add(coupon.getPaymentTime());
    }
    for (int i = 0; i < floatLeg.getNumberOfPayments(); i++) {
      CouponIbor coupon = (CouponIbor) floatLeg.getNthPayment(i);
      times.add(coupon.getPaymentTime());
      times.add(coupon.getFixingPeriodStartTime());
      times.add(coupon.getFixingPeriodEndTime());
    }
    Double[] tArray = times.toArray(new Double[] {});
    double[] t = new double[times.size()];
    for (int i = 0; i < times.size(); i++) {
      t[i] = tArray[i];
    }
    Map<String, List<DoublesPair>> sense = clean(PVSC.visit(SWAP_PAYER, curves), eps, eps * NOTIONAL);

    List<DoublesPair> fdFundSense = curveSensitvityFDCalculator(SWAP_PAYER, PVC, curves, FUNDING_CURVE_NAME, t, eps * NOTIONAL);
    List<DoublesPair> fdFwdSense = curveSensitvityFDCalculator(SWAP_PAYER, PVC, curves, FORWARD_CURVE_NAME, t, eps * NOTIONAL);

    assertSensitivityEquals(fdFundSense, sense.get(FUNDING_CURVE_NAME), eps * NOTIONAL);
    assertSensitivityEquals(fdFwdSense, sense.get(FORWARD_CURVE_NAME), eps * NOTIONAL);
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
    List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), relTol, absTol);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(swap, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    swap = makeOISSwap(maturity, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, rate, notional);
    sense = PVSC.visit(swap, CURVES);

    //    //2. index curve sense   
    senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(swap, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    //    //2. funding curve sense   
    senseAnal = clean(sense.get(ZERO_PC_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(swap, PVC, CURVES, ZERO_PC_CURVE_NAME, nodeTimes, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);
  }

}
