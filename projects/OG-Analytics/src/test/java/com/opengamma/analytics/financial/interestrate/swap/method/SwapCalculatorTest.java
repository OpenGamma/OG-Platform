/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.horizon.ConstantSpreadYieldCurveBundleRolldownFunction;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated methods.
 */
@Deprecated
public class SwapCalculatorTest {

  private static final Calendar CALENDAR_NONE = new NoHolidayCalendar();
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD Calendar");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final IndexIborMaster INDEX_IBOR_MASTER = IndexIborMaster.getInstance();

  // Swap Fixed-Ibor
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR_USD);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);

  // Swap Ibor-ibor
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_MASTER.getIndex("USDLIBOR3M");
  private static final double SPREAD3 = 0.0020;
  private static final IborIndex USDLIBOR6M = INDEX_IBOR_MASTER.getIndex("USDLIBOR6M");
  private static final double SPREAD6 = 0.0005;
  private static final SwapIborIborDefinition SWAP_IBORSPREAD_IBORSPREAD_DEFINITION = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL,
      USDLIBOR3M, SPREAD3, true, CALENDAR_USD), AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, CALENDAR_USD));
  private static final SwapDefinition SWAP_IBOR_IBORSPREAD_DEFINITION = new SwapDefinition(AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, true, CALENDAR_USD),
      AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, CALENDAR_USD));

  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2(USD);
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_3 = ImmutableZonedDateTimeDoubleTimeSeries.of(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10), DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15),
          DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 11, 15) },
          new double[] {0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160 }, ZoneOffset.UTC);
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_6 = ImmutableZonedDateTimeDoubleTimeSeries.of(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10), DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16) },
      new double[] {0.0095, 0.0120, 0.0130 }, ZoneOffset.UTC);
  private static final ZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = new ZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6 };
  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;

  @Test
  public void parSpreadFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final double parSpread = swap.accept(PSC, CURVES);
    final SwapFixedIborDefinition swap0Definition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    final SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, CURVE_NAMES);
    final double pv = swap0.accept(PVC, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadFixedIborAfterFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double parSpread = swap.accept(PSC, CURVES);
    final SwapFixedIborDefinition swap0Definition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    final SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double pv = swap0.accept(PVC, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final double parSpread = swap.accept(PSC, CURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true, CALENDAR_USD),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, CALENDAR_USD));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, CURVE_NAMES);
    final double pv = swap0.accept(PVC, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadAfterFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double parSpread = swap.accept(PSC, CURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true, CALENDAR_USD),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, CALENDAR_USD));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double pv = swap0.accept(PVC, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  /**
   * Test for a swap with first leg without spread and par spread computed on that leg.
   */
  public void parSpreadIborIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final Swap<? extends Payment, ? extends Payment> swap = new Swap<>((Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getFirstLeg().toDerivative(referenceDate, CURVE_NAMES),
        (Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getSecondLeg().toDerivative(referenceDate, new String[] {CURVE_NAMES[0], CURVE_NAMES[2] }));
    final double parSpread = swap.accept(PSC, CURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, parSpread, true, CALENDAR_USD),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, CALENDAR_USD));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, CURVE_NAMES);
    final double pv = swap0.accept(PVC, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadCurveSensitivityFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final String fwdCurveName = ((CouponIbor) swap.getSecondLeg().getNthPayment(0)).getForwardCurveName();
    InterestRateCurveSensitivity pscsComputed = swap.accept(PSCSC, CURVES);
    pscsComputed = pscsComputed.cleaned();
    final double[] timesDsc = new double[swap.getSecondLeg().getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      timesDsc[loopcpn] = swap.getSecondLeg().getNthPayment(loopcpn).getPaymentTime();
    }
    final List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, swap.getFirstLeg().getDiscountCurve(), timesDsc, 1.0E-10);
    final List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(swap.getFirstLeg().getDiscountCurve());
    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));
    final Set<Double> timesFwdSet = new TreeSet<>();
    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      timesFwdSet.add(((CouponIbor) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
      timesFwdSet.add(((CouponIbor) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
    }
    final Double[] timesFwd = timesFwdSet.toArray(new Double[timesFwdSet.size()]);
    final List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, fwdCurveName, ArrayUtils.toPrimitive(timesFwd), 1.0E-10);
    final List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(fwdCurveName);
    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  }

  @Test
  public void parSpreadCurveSensitivityIborSpreadIborSpreadBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final String fwdCurveName = ((CouponIborSpread) swap.getSecondLeg().getNthPayment(0)).getForwardCurveName();
    InterestRateCurveSensitivity pscsComputed = swap.accept(PSCSC, CURVES);
    pscsComputed = pscsComputed.cleaned();
    final double[] timesDsc = new double[swap.getFirstLeg().getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < swap.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      timesDsc[loopcpn] = swap.getFirstLeg().getNthPayment(loopcpn).getPaymentTime();
    }
    final List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, swap.getFirstLeg().getDiscountCurve(), timesDsc, 1.0E-10);
    final List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(swap.getFirstLeg().getDiscountCurve());
    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));

    final Set<Double> timesFwdSet = new TreeSet<>();
    for (int loopcpn = 0; loopcpn < swap.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      timesFwdSet.add(((CouponIborSpread) swap.getFirstLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
      timesFwdSet.add(((CouponIborSpread) swap.getFirstLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
    }
    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      timesFwdSet.add(((CouponIborSpread) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
      timesFwdSet.add(((CouponIborSpread) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
    }
    final Double[] timesFwd = timesFwdSet.toArray(new Double[timesFwdSet.size()]);
    final List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, fwdCurveName, ArrayUtils.toPrimitive(timesFwd), 1.0E-10);
    final List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(fwdCurveName);
    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  }

  @Test
  public void todayPaymentFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborOnFirstIborPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0100 * NOTIONAL * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(0).getPaymentYearFraction(),
        cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborOnFirstFixedPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 19);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", SWAP_FIXED_IBOR_DEFINITION.getFixedLeg().getNthPayment(0).getAmount() + 0.0140 * NOTIONAL
        * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(1).getPaymentYearFraction(), cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborBetweenPayments() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void thetaFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 11);
    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1, CALENDAR_USD);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
    final double pvToday = swapToday.accept(PVC, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    final double pvTomorrow = swapTomorrow.accept(PVC, tomorrowData);
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

  @Test
  public void thetaFixedIborOneDayBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1, CALENDAR_USD);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final ZonedDateTimeDoubleTimeSeries fixing3extended = ImmutableZonedDateTimeDoubleTimeSeries.of(
        new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15) },
        new double[] {0.0090, 0.0090 }, ZoneOffset.UTC);
    final ZonedDateTimeDoubleTimeSeries[] fixing36 = new ZonedDateTimeDoubleTimeSeries[] {fixing3extended, FIXING_TS_6 };
    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), fixing36, CURVE_NAMES);
    final double pvToday = swapToday.accept(PVC, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    final double pvTomorrow = swapTomorrow.accept(PVC, tomorrowData);
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

  @Test
  public void thetaFixedIborOverFirstPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1, CALENDAR_USD);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
    final double pvToday = swapToday.accept(PVC, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    final double pvTomorrow = swapTomorrow.accept(PVC, tomorrowData);
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }
}
