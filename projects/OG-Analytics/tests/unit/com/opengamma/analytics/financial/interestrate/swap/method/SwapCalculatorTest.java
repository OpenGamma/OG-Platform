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

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.horizon.ConstantSpreadYieldCurveBundleRolldownFunction;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.DoublesPair;

public class SwapCalculatorTest {

  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD Calendar");
  private static final GeneratorSwapTestsMaster GENERATOR_SWAP_MASTER = GeneratorSwapTestsMaster.getInstance();
  private static final IndexIborTestsMaster INDEX_IBOR_MASTER = IndexIborTestsMaster.getInstance();

  // Swap Fixed-Ibor
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR_USD);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);

  // Swap Ibor-ibor
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_MASTER.getIndex("USDLIBOR3M", CALENDAR_USD);
  private static final double SPREAD3 = 0.0020;
  private static final IborIndex USDLIBOR6M = INDEX_IBOR_MASTER.getIndex("USDLIBOR6M", CALENDAR_USD);
  private static final double SPREAD6 = 0.0005;
  private static final SwapIborIborDefinition SWAP_IBORSPREAD_IBORSPREAD_DEFINITION = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL,
      USDLIBOR3M, SPREAD3, true), AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
  private static final SwapDefinition SWAP_IBOR_IBORSPREAD_DEFINITION = new SwapDefinition(AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, true),
      AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  private static final ArrayZonedDateTimeDoubleTimeSeries FIXING_TS_3 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 11, 15)}, new double[] {
      0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160});
  private static final ArrayZonedDateTimeDoubleTimeSeries FIXING_TS_6 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16)}, new double[] {0.0095, 0.0120, 0.0130});
  private static final ArrayZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = new ArrayZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6};

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;

  @Test
  public void parSpreadFixedIborBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    double parSpread = PSC.visit(swap, CURVES);
    SwapFixedIborDefinition swap0Definition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, CURVE_NAMES);
    double pv = PVC.visit(swap0, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadFixedIborAfterFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    double parSpread = PSC.visit(swap, CURVES);
    SwapFixedIborDefinition swap0Definition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    double pv = PVC.visit(swap0, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    double parSpread = PSC.visit(swap, CURVES);
    SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
    Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, CURVE_NAMES);
    double pv = PVC.visit(swap0, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadAfterFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    double parSpread = PSC.visit(swap, CURVES);
    SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
    Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    double pv = PVC.visit(swap0, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  /**
   * Test for a swap with first leg without spread and par spread computed on that leg.
   */
  public void parSpreadIborIborBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    @SuppressWarnings("unchecked")
    Swap<? extends Payment, ? extends Payment> swap = new Swap<Payment, Payment>((Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getFirstLeg().toDerivative(referenceDate, CURVE_NAMES),
        (Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getSecondLeg().toDerivative(referenceDate, new String[] {CURVE_NAMES[0], CURVE_NAMES[2]}));
    double parSpread = PSC.visit(swap, CURVES);
    SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, parSpread, true),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
    Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, CURVE_NAMES);
    double pv = PVC.visit(swap0, CURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv, 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadCurveSensitivityFixedIborBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    String fwdCurveName = ((CouponIbor) swap.getSecondLeg().getNthPayment(0)).getForwardCurveName();
    InterestRateCurveSensitivity pscsComputed = PSCSC.visit(swap, CURVES);
    pscsComputed = pscsComputed.cleaned();
    double[] timesDsc = new double[swap.getSecondLeg().getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      timesDsc[loopcpn] = swap.getSecondLeg().getNthPayment(loopcpn).getPaymentTime();
    }
    List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, swap.getFirstLeg().getDiscountCurve(), timesDsc, 1.0E-10);
    List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(swap.getFirstLeg().getDiscountCurve());
    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));
    Set<Double> timesFwdSet = new TreeSet<Double>();
    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      timesFwdSet.add(((CouponIbor) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
      timesFwdSet.add(((CouponIbor) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
    }
    Double[] timesFwd = timesFwdSet.toArray(new Double[0]);
    List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, fwdCurveName, ArrayUtils.toPrimitive(timesFwd), 1.0E-10);
    List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(fwdCurveName);
    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  }

  @Test
  public void parSpreadCurveSensitivityIborSpreadIborSpreadBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    String fwdCurveName = ((CouponIborSpread) swap.getSecondLeg().getNthPayment(0)).getForwardCurveName();
    InterestRateCurveSensitivity pscsComputed = PSCSC.visit(swap, CURVES);
    pscsComputed = pscsComputed.cleaned();
    double[] timesDsc = new double[swap.getFirstLeg().getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < swap.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      timesDsc[loopcpn] = swap.getFirstLeg().getNthPayment(loopcpn).getPaymentTime();
    }
    List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, swap.getFirstLeg().getDiscountCurve(), timesDsc, 1.0E-10);
    List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(swap.getFirstLeg().getDiscountCurve());
    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));

    Set<Double> timesFwdSet = new TreeSet<Double>();
    for (int loopcpn = 0; loopcpn < swap.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      timesFwdSet.add(((CouponIborSpread) swap.getFirstLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
      timesFwdSet.add(((CouponIborSpread) swap.getFirstLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
    }
    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      timesFwdSet.add(((CouponIborSpread) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
      timesFwdSet.add(((CouponIborSpread) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
    }
    Double[] timesFwd = timesFwdSet.toArray(new Double[0]);
    List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSC, CURVES, fwdCurveName, ArrayUtils.toPrimitive(timesFwd), 1.0E-10);
    List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(fwdCurveName);
    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  }

  @Test
  public void todayPaymentFixedIborBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    MultipleCurrencyAmount cash = TPC.visit(swap);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborOnFirstIborPayment() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    MultipleCurrencyAmount cash = TPC.visit(swap);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0100 * NOTIONAL * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(0).getPaymentYearFraction(),
        cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborOnFirstFixedPayment() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 19);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    MultipleCurrencyAmount cash = TPC.visit(swap);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", SWAP_FIXED_IBOR_DEFINITION.getFixedLeg().getNthPayment(0).getAmount() + 0.0140 * NOTIONAL
        * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(1).getPaymentYearFraction(), cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborBetweenPayments() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 14);
    SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    MultipleCurrencyAmount cash = TPC.visit(swap);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void thetaFixedIborBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 11);
    ConstantSpreadHorizonThetaCalculator calculatorTheta = new ConstantSpreadHorizonThetaCalculator(referenceDate, 1);
    MultipleCurrencyAmount theta = calculatorTheta.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6);
    SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
    double pvToday = PVC.visit(swapToday, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

  @Test
  public void thetaFixedIborOneDayBeforeFirstFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    ConstantSpreadHorizonThetaCalculator calculatorTheta = new ConstantSpreadHorizonThetaCalculator(referenceDate, 1);
    MultipleCurrencyAmount theta = calculatorTheta.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6);
    SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    ArrayZonedDateTimeDoubleTimeSeries fixing3extended = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15)},
        new double[] {0.0090, 0.0090});
    ArrayZonedDateTimeDoubleTimeSeries[] fixing36 = new ArrayZonedDateTimeDoubleTimeSeries[] {fixing3extended, FIXING_TS_6};
    SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), fixing36, CURVE_NAMES);
    double pvToday = PVC.visit(swapToday, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

  @Test
  public void thetaFixedIborOverFirstPayment() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    ConstantSpreadHorizonThetaCalculator calculatorTheta = new ConstantSpreadHorizonThetaCalculator(referenceDate, 1);
    MultipleCurrencyAmount theta = calculatorTheta.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6);
    SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
    double pvToday = PVC.visit(swapToday, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
    double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - (pvToday - todayCash), theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

}
