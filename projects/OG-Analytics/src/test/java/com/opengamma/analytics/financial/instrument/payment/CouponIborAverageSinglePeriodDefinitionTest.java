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
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborAverageSinglePeriodDefinitionTest {

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

  private static final CouponIborAverageSinglePeriodDefinition DFN1 = new CouponIborAverageSinglePeriodDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      INDEX, FIXING_DATES, WEIGHTS, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  private static ZonedDateTime[] EXP_START_DATES = new ZonedDateTime[NUM_OBS];
  private static ZonedDateTime[] EXP_END_DATES = new ZonedDateTime[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      EXP_START_DATES[i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[i], INDEX.getSpotLag(), CALENDAR);
      EXP_END_DATES[i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
    }
  }

  private static final CouponIborAverageSinglePeriodDefinition DFN2 = new CouponIborAverageSinglePeriodDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES);

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test
  public void exceptionTest() {
    try {
      new CouponIborAverageSinglePeriodDefinition(Currency.GBP, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }
    try {
      new CouponIborAverageSinglePeriodDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("index currency different from payment currency", e.getMessage());
    }

    final double[] shortWeight = Arrays.copyOf(WEIGHTS, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriodDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, shortWeight, CALENDAR);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingDate length", e.getMessage());
    }
    try {
      new CouponIborAverageSinglePeriodDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, shortWeight, EXP_START_DATES, EXP_END_DATES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("weight length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[] shortStartDates = Arrays.copyOf(EXP_START_DATES, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriodDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, shortStartDates, EXP_END_DATES);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodStartDate length different from fixingDate length", e.getMessage());
    }

    final ZonedDateTime[] shortEndDates = Arrays.copyOf(EXP_END_DATES, NUM_OBS - 1);
    try {
      new CouponIborAverageSinglePeriodDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
          INDEX, FIXING_DATES, WEIGHTS, EXP_START_DATES, shortEndDates);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("fixingPeriodEndDate length different from fixingDate length", e.getMessage());
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

  /**
   * 
   */
  @Test
  public void consistencyTest() {
    final CouponIborAverageSinglePeriodDefinition dfn1WithDouble = DFN1.withNotional(NOTIONAL * 2);

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

    final CouponIborAverageSinglePeriodDefinition dfn1 = CouponIborAverageSinglePeriodDefinition.from(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, INDEX,
        FIXING_DATES, WEIGHTS, CALENDAR);
    final CouponIborAverageSinglePeriodDefinition dfn2 = CouponIborAverageSinglePeriodDefinition.from(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, INDEX,
        FIXING_DATES, WEIGHTS, EXP_START_DATES, EXP_END_DATES);

    assertTrue(DFN1.equals(dfn1));
    assertEquals(DFN1.hashCode(), dfn1.hashCode());
    assertTrue(DFN2.equals(dfn2));
    assertEquals(DFN2.hashCode(), dfn2.hashCode());

    assertFalse(DFN1.hashCode() == dfn1WithDouble.hashCode());
    assertFalse(DFN1.equals(dfn1WithDouble));

    assertTrue(DFN1.toDerivative(REFERENCE_DATE).equals(dfn1.toDerivative(REFERENCE_DATE)));

  }

}
