/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborAverageFixingDatesDefinitionTest {

  private static final Period TENOR = Period.ofMonths(1);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index");

  private static final int NUM_OBS = 6;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[NUM_OBS];
  private static final double[] WEIGHTS = new double[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      FIXING_DATES[i] = DateUtils.getUTCDate(2011, i + 1, 3);
      WEIGHTS[i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
    }
  }

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000;

  private static final CouponIborAverageFixingDatesDefinition DFN1 = new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      INDEX, FIXING_DATES, WEIGHTS, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE_BEFORE_FISRT_FIXING = DateUtils.getUTCDate(2010, 12, 27);
  private static final ZonedDateTime REFERENCE_DATE_AFTER_LAST_FIXING = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime REFERENCE_DATE_ON_2ND_FIXING = DateUtils.getUTCDate(2011, 2, 3);
  private static final ZonedDateTime REFERENCE_DATE_AFTER_2_FIXING = DateUtils.getUTCDate(2011, 2, 16);

  private static ZonedDateTime[] EXP_START_DATES = new ZonedDateTime[NUM_OBS];
  private static ZonedDateTime[] EXP_END_DATES = new ZonedDateTime[NUM_OBS];
  private static double[] FIX_ACC_FACTORS = new double[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      EXP_START_DATES[i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[i], INDEX.getSpotLag(), CALENDAR);
      EXP_END_DATES[i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
      FIX_ACC_FACTORS[i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[i], EXP_END_DATES[i], CALENDAR);
    }
  }
  private static final CouponIborAverageFixingDatesDefinition DFN2 = new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);

  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS1 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 12, 7) }, new double[] {0.01 });
  private static final double[] FIXING_TS2_VALUES = {0.011, 0.012, 0.013, 0.014, 0.015, 0.016 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS2 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 3), DateUtils.getUTCDate(2011, 2, 3), DateUtils.getUTCDate(2011, 3, 3), DateUtils.getUTCDate(2011, 4, 3), DateUtils.getUTCDate(2011, 5, 3),
        DateUtils.getUTCDate(2011, 6, 3) }, FIXING_TS2_VALUES);
  private static final double[] FIXING_2_EX_VALUES = {0.011 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_2_EX = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 3) }, FIXING_2_EX_VALUES);
  private static final double[] FIXING_2_IN_VALUES = {0.011, 0.012 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_2_IN = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 3), DateUtils.getUTCDate(2011, 2, 3) }, FIXING_2_IN_VALUES);

  private static final double TOLERANCE_TIME = 1.0E-6;
  private static final double TOLERANCE_AMOUNT = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;

  @Test
  public void toDerivativeBeforeFirstFixing() {
    final Coupon derivative1 = DFN1.toDerivative(REFERENCE_DATE_BEFORE_FISRT_FIXING, FIXING_TS1);
    assertTrue((derivative1 instanceof CouponIborAverageFixingDates));
    final CouponIborAverageFixingDates coupon = (CouponIborAverageFixingDates) derivative1;
    double[] fixingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_BEFORE_FISRT_FIXING, FIXING_DATES);
    double[] fixingStart = TimeCalculator.getTimeBetween(REFERENCE_DATE_BEFORE_FISRT_FIXING, DFN1.getFixingPeriodStartDate());
    double[] fixingEnd = TimeCalculator.getTimeBetween(REFERENCE_DATE_BEFORE_FISRT_FIXING, DFN1.getFixingPeriodEndDate());
    ArrayAsserts.assertArrayEquals("CouponIborAverageFixingDatesDefinition: toDerivative", coupon.getFixingTime(), fixingTime, TOLERANCE_TIME);
    ArrayAsserts.assertArrayEquals("CouponIborAverageFixingDatesDefinition: toDerivative", coupon.getFixingPeriodStartTime(), fixingStart, TOLERANCE_TIME);
    ArrayAsserts.assertArrayEquals("CouponIborAverageFixingDatesDefinition: toDerivative", coupon.getFixingPeriodEndTime(), fixingEnd, TOLERANCE_TIME);
    assertEquals("CouponIborAverageFixingDatesDefinition: toDerivative", coupon.getNotional(), NOTIONAL);
  }

  @Test
  public void toDerivativeAfterLastFixing() {
    final Coupon derivative = DFN1.toDerivative(REFERENCE_DATE_AFTER_LAST_FIXING, FIXING_TS2);
    assertTrue((derivative instanceof CouponFixed));
    final CouponFixed coupon = (CouponFixed) derivative;
    double amountExpected = 0;
    for (int loopfix = 0; loopfix < NUM_OBS; loopfix++) {
      amountExpected += FIXING_TS2_VALUES[loopfix] * DFN1.getWeight()[loopfix];
    }
    amountExpected *= NOTIONAL * DFN1.getPaymentYearFraction();
    assertEquals("CouponIborAverageFixingDatesDefinition: toDerivative", coupon.getAmount(), amountExpected, TOLERANCE_AMOUNT);
  }

  @Test
  public void toDerivativeOn2ndFixing() {
    final Coupon derivativeEx = DFN1.toDerivative(REFERENCE_DATE_ON_2ND_FIXING, FIXING_2_EX);
    assertTrue((derivativeEx instanceof CouponIborAverageFixingDates));
    final CouponIborAverageFixingDates couponEx = (CouponIborAverageFixingDates) derivativeEx;
    double amountExpectedEx = FIXING_2_EX_VALUES[0] * DFN1.getWeight()[0];
    assertEquals("CouponIborAverageFixingDatesDefinition: toDerivative", couponEx.getAmountAccrued(), amountExpectedEx, TOLERANCE_RATE);
    final Coupon derivativeIn = DFN1.toDerivative(REFERENCE_DATE_ON_2ND_FIXING, FIXING_2_IN);
    assertTrue((derivativeIn instanceof CouponIborAverageFixingDates));
    final CouponIborAverageFixingDates couponIn = (CouponIborAverageFixingDates) derivativeIn;
    double amountExpectedIn = FIXING_2_IN_VALUES[0] * DFN1.getWeight()[0] + FIXING_2_IN_VALUES[1] * DFN1.getWeight()[1];
    assertEquals("CouponIborAverageFixingDatesDefinition: toDerivative", couponIn.getAmountAccrued(), amountExpectedIn, TOLERANCE_RATE);
  }

  @Test
  public void toDerivativeAfter2nd() {
    final DoubleTimeSeries<ZonedDateTime> fixingTS3 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 3), DateUtils.getUTCDate(2011, 2, 3) },
        new double[] {0.01, 0.02 });
    final CouponIborAverageFixingDates derivative3 = (CouponIborAverageFixingDates) DFN1.toDerivative(REFERENCE_DATE_AFTER_2_FIXING, fixingTS3);
    assertEquals(NUM_OBS - 2, derivative3.getFixingPeriodAccrualFactor().length);
    assertEquals(NUM_OBS - 2, derivative3.getFixingPeriodEndTime().length);
    assertEquals(NUM_OBS - 2, derivative3.getFixingPeriodStartTime().length);
    assertEquals(NUM_OBS - 2, derivative3.getFixingTime().length);
    assertEquals(NUM_OBS - 2, derivative3.getWeight().length);
    final double refValue = WEIGHTS[0] * fixingTS3.getValueAtIndex(0) + WEIGHTS[1] * fixingTS3.getValueAtIndex(1);
    assertEquals(refValue, derivative3.getAmountAccrued(), 1.e-14);
  }

  @Test
  public void toDerivativeException() {
    try {
      DFN1.toDerivative(PAYMENT_DATE.plusDays(10), FIXING_TS1);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("date is after payment date", e.getMessage());
    }
    try {
      DFN1.toDerivative(DateUtils.getUTCDate(2011, 7, 3), FIXING_TS1);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Could not get fixing value for date " + FIXING_DATES[0], e.getMessage());
    }
  }

  @Test
  public void exceptionTest() {
    try {
      new CouponIborAverageFixingDatesDefinition(Currency.GBP, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }
    try {
      new CouponIborAverageFixingDatesDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }

    final double[] shortWeight = Arrays.copyOf(WEIGHTS, NUM_OBS - 1);
    try {
      new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, shortWeight, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, shortWeight, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[] shortStartDates = Arrays.copyOf(EXP_START_DATES, NUM_OBS - 1);
    try {
      new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, shortStartDates, EXP_END_DATES, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDate length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[] shortEndDates = Arrays.copyOf(EXP_END_DATES, NUM_OBS - 1);
    try {
      new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, shortEndDates, FIX_ACC_FACTORS);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDate length different from fixingDate length", e.getMessage());
    }

    final double[] shortFcc = Arrays.copyOf(FIX_ACC_FACTORS, NUM_OBS - 1);
    try {
      new CouponIborAverageFixingDatesDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, shortFcc);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodAccrualFactor length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime afterPayment = PAYMENT_DATE.plusDays(1);
    try {
      DFN1.toDerivative(afterPayment);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("date is after payment date", e.getMessage());
    }
    final ZonedDateTime afterFixing = FIXING_DATES[0].plusDays(1);
    try {
      DFN1.toDerivative(afterFixing);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Do not have any fixing data but are asking for a derivative at " + afterFixing + " which is after fixing date " + FIXING_DATES[0], e.getMessage());
    }
  }

  @Test
  public void consistencyTest() {
    final CouponIborAverageFixingDatesDefinition dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

    assertEquals(DFN1.getIndex(), DFN2.getIndex());
    assertEquals(DFN1.getIndex(), dfn1WithDouble.getIndex());

    for (int i = 0; i < NUM_OBS; ++i) {
      assertEquals(DFN1.getWeight()[i], DFN2.getWeight()[i]);
      assertEquals(DFN1.getFixingDate()[i], DFN2.getFixingDate()[i]);
      assertEquals(DFN1.getFixingPeriodStartDate()[i], DFN2.getFixingPeriodStartDate()[i]);
      assertEquals(DFN1.getFixingPeriodEndDate()[i], DFN2.getFixingPeriodEndDate()[i]);

      assertEquals(DFN1.getWeight()[i], dfn1WithDouble.getWeight()[i]);
      assertEquals(DFN1.getFixingDate()[i], dfn1WithDouble.getFixingDate()[i]);
      assertEquals(DFN1.getFixingPeriodStartDate()[i], dfn1WithDouble.getFixingPeriodStartDate()[i]);
      assertEquals(DFN1.getFixingPeriodEndDate()[i], dfn1WithDouble.getFixingPeriodEndDate()[i]);
    }

    final CouponIborAverageFixingDatesDefinition dfn1 = CouponIborAverageFixingDatesDefinition.from(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, INDEX,
        FIXING_DATES, WEIGHTS, CALENDAR);
    final CouponIborAverageFixingDatesDefinition dfn2 = CouponIborAverageFixingDatesDefinition.from(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, INDEX,
        FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES, FIX_ACC_FACTORS);

    assertTrue(DFN1.equals(dfn1));
    assertEquals(DFN1.hashCode(), dfn1.hashCode());
    assertTrue(DFN2.equals(dfn2));
    assertEquals(DFN2.hashCode(), dfn2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));

    assertTrue(DFN1.toDerivative(REFERENCE_DATE_BEFORE_FISRT_FIXING).equals(dfn1.toDerivative(REFERENCE_DATE_BEFORE_FISRT_FIXING)));

  }
}
