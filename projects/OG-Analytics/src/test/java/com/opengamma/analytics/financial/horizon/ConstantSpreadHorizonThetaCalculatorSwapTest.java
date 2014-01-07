/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests theta calculator on a swap
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ConstantSpreadHorizonThetaCalculatorSwapTest {
  // The Calculator
  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();
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
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2(USD);
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_3 = ImmutableZonedDateTimeDoubleTimeSeries.of(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10), DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15),
          DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 11, 15) },
          new double[] { 0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160 }, ZoneOffset.UTC);
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_6 = ImmutableZonedDateTimeDoubleTimeSeries.of(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10), DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16) },
      new double[] {0.0095, 0.0120, 0.0130 }, ZoneOffset.UTC);
  private static final ZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = new ZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6 };

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m

  // Tests

  @Test
  public void thetaFixedIborOverFirstPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1, CALENDAR_NONE);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
    final double pvToday = swapToday.accept(PVC, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    final double pvTomorrow = swapTomorrow.accept(PVC, tomorrowData);
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

  @Test(expectedExceptions = java.lang.IllegalArgumentException.class)
  public void badDaysForward() {
    THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, DateUtils.getUTCDate(2012, 8, 17), CURVE_NAMES, CURVES, FIXING_TS_3_6, 2, CALENDAR_NONE);
  }
}
