/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static com.opengamma.analytics.financial.interestrate.FDCurveSensitivityCalculator.curveSensitvityFDCalculator;
import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.clean;
import static com.opengamma.analytics.financial.interestrate.TestUtils.assertSensitivityEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PresentValueSensitivityCalculatorTest {
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};

  static {
    YieldAndDiscountCurve curve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = YieldCurve.from(ConstantDoublesCurve.from(0.0));
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double df = curve.getDiscountFactor(t);
    double r = 1 / t * (1 / df - 1);
    Cash cash = new Cash(CUR, 0, t, 1, r, t, FIVE_PC_CURVE_NAME);
    Map<String, List<DoublesPair>> sense = cash.accept(PVSC, CURVES);

    assertTrue(sense.containsKey(FIVE_PC_CURVE_NAME));
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));

    List<DoublesPair> temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (pair.getFirst() == 0.0) {
        assertEquals(0.0, pair.getSecond(), 1e-12);
      } else if (Double.compare(pair.getFirst(), t) == 0) {
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
    sense = cash.accept(PVSC, CURVES);
    temp = sense.get(FIVE_PC_CURVE_NAME);
    for (final DoublesPair pair : temp) {
      if (Double.compare(pair.getFirst(), tradeTime) == 0) {
        assertEquals(dfa * tradeTime, pair.getSecond(), 1e-12);
      } else if (Double.compare(pair.getFirst(), t) == 0) {
        assertEquals(-t * df * (1 + r * yearFrac), pair.getSecond(), 1e-12);
      } else {
        assertFalse(true);
      }
    }
  }

  @Test
  public void testFRA() {
    final double eps = 1e-9;

    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true);

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

    final Map<String, List<DoublesPair>> sense = fra.accept(PVSC, CURVES);
    final List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    final List<DoublesPair> senseFD = curveSensitvityFDCalculator(fra, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  //  @Test
  //  public void testFutures() {
  //    final double eps = 1e-7;
  //    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCounts.ACT_365,
  //        BusinessDayConventions.FOLLOWING, true);
  //    final double lastTradingTime = 1.473;
  //    final double fixingPeriodStartTime = 1.467;
  //    final double fixingPeriodEndTime = 1.75;
  //    final double fixingPeriodAccrualFactor = 0.267;
  //    final double paymentAccrualFactor = 0.25;
  //    final int quantity = 123;
  //    final double[] nodeTimes = new double[] {fixingPeriodStartTime, fixingPeriodEndTime };
  //
  //    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 0.0, 1.0, paymentAccrualFactor, quantity,
  //        "K", ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //
  //    final Map<String, List<DoublesPair>> sense = ir.accept(PVSC, CURVES);
  //
  //    final List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
  //    final List<DoublesPair> senseFD = curveSensitvityFDCalculator(ir, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
  //
  //    assertSensitivityEquals(senseFD, senseAnal, eps);
  //  }

  @Test
  public void testFixedCouponAnnuity() {
    final double eps = 1e-9;
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
    final Map<String, List<DoublesPair>> sense = annuity.accept(PVSC, CURVES);

    final List<DoublesPair> senseAnal = clean(sense.get(FIVE_PC_CURVE_NAME), eps, eps);
    final List<DoublesPair> senseFD = curveSensitvityFDCalculator(annuity, PVC, CURVES, FIVE_PC_CURVE_NAME, paymentTimes, eps);
    assertSensitivityEquals(senseFD, senseAnal, eps);
  }

  @Test
  public void testAnnuityCouponIbor() {
    final double eps = 1e-9;
    final double notional = 1e8;
    final double relTol = eps;
    final double absTol = notional * eps;

    final int settlementDays = 2;
    final BusinessDayConvention businessDayConvention = BusinessDayConventions.MODIFIED_FOLLOWING;
    final boolean isEOM = true;
    final boolean isPayer = true;
    final ZonedDateTime settleDate = DateUtils.getUTCDate(2014, 3, 20);
    final Period indexTenor = Period.ofMonths(3);
    final DayCount dayCount = DayCounts.ACT_360;
    final IborIndex INDEX = new IborIndex(CUR, indexTenor, settlementDays, dayCount, businessDayConvention, isEOM);
    final AnnuityCouponIborDefinition iborAnnuityDefinition = AnnuityCouponIborDefinition.from(settleDate, Period.ofYears(5), notional, INDEX, !isPayer, CALENDAR);

    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final Annuity<? extends Payment> iborAnnuity1Curve = iborAnnuityDefinition.toDerivative(REFERENCE_DATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
    final Annuity<? extends Payment> iborAnnuity = iborAnnuityDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);

    //produce a array of strictly ascending times
    final Set<Double> times = new TreeSet<>();
    for (int i = 0; i < iborAnnuity.getNumberOfPayments(); i++) {
      final CouponIbor coupon = (CouponIbor) iborAnnuity.getNthPayment(i);
      times.add(coupon.getPaymentTime());
      times.add(coupon.getFixingPeriodStartTime());
      times.add(coupon.getFixingPeriodEndTime());
    }
    final Double[] tArray = times.toArray(new Double[times.size()]);
    final double[] t = new double[times.size()];
    for (int i = 0; i < times.size(); i++) {
      t[i] = tArray[i];
    }

    //single curve
    Map<String, List<DoublesPair>> sense = iborAnnuity1Curve.accept(PVSC, curves);
    assertTrue(!sense.containsKey(FORWARD_CURVE_NAME));
    List<DoublesPair> senseAnal = clean(sense.get(FUNDING_CURVE_NAME), relTol, absTol);
    List<DoublesPair> senseFD = curveSensitvityFDCalculator(iborAnnuity1Curve, PVC, curves, FUNDING_CURVE_NAME, t, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    //2 curves
    sense = iborAnnuity.accept(PVSC, curves);
    senseAnal = clean(sense.get(FUNDING_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(iborAnnuity, PVC, curves, FUNDING_CURVE_NAME, t, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);

    senseAnal = clean(sense.get(FORWARD_CURVE_NAME), relTol, absTol);
    senseFD = curveSensitvityFDCalculator(iborAnnuity, PVC, curves, FORWARD_CURVE_NAME, t, absTol);
    assertSensitivityEquals(senseFD, senseAnal, absTol);
  }

  @Test
  public void testGenericAnnuity() {
    final double eps = 1e-5;
    final int n = 5;
    final double[] times = new double[] {0.01, 0.5, 1, 3, 10};
    final double[] amounts = new double[] {100000, 1, 234, 452, 0.034}; //{100000, 1, 234, -452, 0.034}
    final String[] curveNames = new String[] {FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME};

    final Payment[] payments = new Payment[5];
    for (int i = 0; i < n; i++) {
      payments[i] = new PaymentFixed(CUR, times[i], amounts[i], curveNames[i]);
    }

    final Annuity<Payment> annuity = new Annuity<>(payments);
    final Map<String, List<DoublesPair>> sense = annuity.accept(PVSC, CURVES);
    final List<DoublesPair> sense0FD = curveSensitvityFDCalculator(annuity, PVC, CURVES, ZERO_PC_CURVE_NAME, times, eps);
    final List<DoublesPair> sense5FD = curveSensitvityFDCalculator(annuity, PVC, CURVES, FIVE_PC_CURVE_NAME, times, eps);
    assertSensitivityEquals(sense0FD, sense.get(ZERO_PC_CURVE_NAME), eps);
    assertSensitivityEquals(sense5FD, sense.get(FIVE_PC_CURVE_NAME), eps);
  }

  @Test
  public void testFixedPayment() {
    final double eps = 1e-9;
    final double time = 1.23;
    final double amount = 4345.3;
    final PaymentFixed payment = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);

    final Map<String, List<DoublesPair>> sense = payment.accept(PVSC, CURVES);
    assertFalse(sense.containsKey(ZERO_PC_CURVE_NAME));
    final double senseFD = curveSensitvityFDCalculator(payment, PVC, CURVES, FIVE_PC_CURVE_NAME, time);
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

    final Map<String, List<DoublesPair>> sense = payment.accept(PVSC, CURVES);

    assertFalse(sense.containsKey(FIVE_PC_CURVE_NAME));

    final double senseFD = curveSensitvityFDCalculator(payment, PVC, CURVES, ZERO_PC_CURVE_NAME, time);
    assertEquals(senseFD, sense.get(ZERO_PC_CURVE_NAME).get(0).second, eps * notional);

  }

  @Test
  public void testCouponIbor() {
    final double eps = 1e-9;
    final double paymentTime = 2.45;
    final double resetTime = 2.0;
    final double maturity = 2.5;
    final double paymentYF = 0.48;
    final double forwardYF = 0.5;
    final double spread = 0.04;
    final double notional = 100000000;

    final CouponIborSpread payment1Curve = new CouponIborSpread(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYF, notional, resetTime, IBOR_INDEX, resetTime, maturity, forwardYF, spread,
        FIVE_PC_CURVE_NAME);
    final CouponIborSpread payment = new CouponIborSpread(CUR, paymentTime, ZERO_PC_CURVE_NAME, paymentYF, notional, resetTime, IBOR_INDEX, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);

    final double[] nodeTimes = new double[] {resetTime, paymentTime, maturity};

    //single curve
    Map<String, List<DoublesPair>> sense = clean(payment1Curve.accept(PVSC, CURVES), eps, eps * notional);
    List<DoublesPair> sense5FD = curveSensitvityFDCalculator(payment1Curve, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(sense5FD, sense.get(FIVE_PC_CURVE_NAME), eps * notional);

    //2 curves
    sense = clean(payment.accept(PVSC, CURVES), eps, eps * notional);
    final List<DoublesPair> sense0FD = curveSensitvityFDCalculator(payment, PVC, CURVES, ZERO_PC_CURVE_NAME, nodeTimes, eps);
    sense5FD = curveSensitvityFDCalculator(payment, PVC, CURVES, FIVE_PC_CURVE_NAME, nodeTimes, eps);
    assertSensitivityEquals(sense0FD, sense.get(ZERO_PC_CURVE_NAME), eps * notional);
    assertSensitivityEquals(sense5FD, sense.get(FIVE_PC_CURVE_NAME), eps * notional);
  }

  // Swaption description
  //  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2014, 3, 18);
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2014, 3, 20);
  //ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final GeneratorSwapFixedIbor SWAP_GENERATOR = new GeneratorSwapFixedIbor("Swap Generator", FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, CALENDAR);
  private static final IndexSwap CMS_INDEX = new IndexSwap(SWAP_GENERATOR, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);

  @Test
  public void testFixedCouponSwap() {
    final double eps = 1e-9;
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();

    final Annuity<CouponFixed> fixedLeg = SWAP_PAYER.getFirstLeg();
    final Annuity<Coupon> floatLeg = SWAP_PAYER.getSecondLeg();

    //produce a array of strictly ascending times
    final Set<Double> times = new TreeSet<>();
    for (int i = 0; i < fixedLeg.getNumberOfPayments(); i++) {
      final CouponFixed coupon = fixedLeg.getNthPayment(i);
      times.add(coupon.getPaymentTime());
    }
    for (int i = 0; i < floatLeg.getNumberOfPayments(); i++) {
      final CouponIbor coupon = (CouponIbor) floatLeg.getNthPayment(i);
      times.add(coupon.getPaymentTime());
      times.add(coupon.getFixingPeriodStartTime());
      times.add(coupon.getFixingPeriodEndTime());
    }
    final Double[] tArray = times.toArray(new Double[times.size()]);
    final double[] t = new double[times.size()];
    for (int i = 0; i < times.size(); i++) {
      t[i] = tArray[i];
    }
    final Map<String, List<DoublesPair>> sense = clean(SWAP_PAYER.accept(PVSC, curves), eps, eps * NOTIONAL);

    final List<DoublesPair> fdFundSense = curveSensitvityFDCalculator(SWAP_PAYER, PVC, curves, FUNDING_CURVE_NAME, t, eps * NOTIONAL);
    final List<DoublesPair> fdFwdSense = curveSensitvityFDCalculator(SWAP_PAYER, PVC, curves, FORWARD_CURVE_NAME, t, eps * NOTIONAL);

    assertSensitivityEquals(fdFundSense, sense.get(FUNDING_CURVE_NAME), eps * NOTIONAL);
    assertSensitivityEquals(fdFwdSense, sense.get(FORWARD_CURVE_NAME), eps * NOTIONAL);
  }

}
