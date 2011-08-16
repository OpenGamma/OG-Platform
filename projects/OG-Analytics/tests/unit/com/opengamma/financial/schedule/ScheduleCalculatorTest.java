/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.ModifiedFollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.ThirtyEThreeSixty;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@SuppressWarnings("synthetic-access")
public class ScheduleCalculatorTest {
  private static final Calendar ALL = new AllCalendar();
  private static final Calendar WEEKEND = new WeekendCalendar();
  private static final Calendar FIRST = new FirstOfMonthCalendar();
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2010, 1, 1);

  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final boolean SHORT_STUB = true;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEffectiveDate1() {
    ScheduleCalculator.getUnadjustedDateSchedule(null, DateUtils.getUTCDate(2010, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEffectiveDate2() {
    ScheduleCalculator.getUnadjustedDateSchedule(null, DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualDate() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), null, DateUtils.getUTCDate(2010, 7, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate1() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), null, PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate2() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), null, PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency1() {
    PeriodFrequency nullfrequency = null;
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), nullfrequency);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency2() {
    PeriodFrequency nullfrequency = null;
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), nullfrequency);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadMaturityDate1() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2009, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadMaturityDate2() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2009, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadMaturityDate3() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2008, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2009, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongFrequencyType() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), new Frequency() {

      @Override
      public String getConventionName() {
        return null;
      }

    });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateArray1() {
    ScheduleCalculator.getAdjustedDateSchedule(null, new ModifiedFollowingBusinessDayConvention(), ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateArray2() {
    ScheduleCalculator.getTimes(null, new ThirtyEThreeSixty(), NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyDateArray1() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[0], new ModifiedFollowingBusinessDayConvention(), ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmtpyDateArray2() {
    ScheduleCalculator.getTimes(new ZonedDateTime[0], new ThirtyEThreeSixty(), NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, null, ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, new ModifiedFollowingBusinessDayConvention(), null, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    ScheduleCalculator.getTimes(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, null, NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    ScheduleCalculator.getTimes(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, new ThirtyEThreeSixty(), null);
  }

  @Test
  public void testUnadjustedDates() {
    final ZonedDateTime effective = DateUtils.getUTCDate(2010, 6, 1);
    final ZonedDateTime accrual = DateUtils.getUTCDate(2010, 9, 1);
    final ZonedDateTime maturity = DateUtils.getUTCDate(2015, 6, 1);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.ANNUAL), 5, DateUtils.getUTCDate(2011, 6, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.ANNUAL), 5, DateUtils.getUTCDate(2011, 6, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.SEMI_ANNUAL), 10, DateUtils.getUTCDate(2010, 12, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.SEMI_ANNUAL), 10, DateUtils.getUTCDate(2010, 12, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.QUARTERLY), 20, DateUtils.getUTCDate(2010, 9, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.QUARTERLY), 20, DateUtils.getUTCDate(2010, 9, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.MONTHLY), 60, DateUtils.getUTCDate(2010, 7, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.MONTHLY), 60, DateUtils.getUTCDate(2010, 7, 1), maturity);
  }

  @Test
  public void testAdjustedDates() {
    final ZonedDateTime effective = DateUtils.getUTCDate(2010, 1, 1);
    final ZonedDateTime maturity = DateUtils.getUTCDate(2011, 1, 1);
    final ZonedDateTime[] unadjusted = ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.MONTHLY);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), ALL), unadjusted);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), ALL), unadjusted);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), ALL), unadjusted);
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 1),
            DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
            DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 1),
            DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
            DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 6, 1),
            DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 7, 30), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
            DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2010, 12, 31)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 2), DateUtils.getUTCDate(2010, 3, 2), DateUtils.getUTCDate(2010, 4, 2), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 2),
            DateUtils.getUTCDate(2010, 7, 2), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 2), DateUtils.getUTCDate(2010, 10, 4), DateUtils.getUTCDate(2010, 11, 2),
            DateUtils.getUTCDate(2010, 12, 2), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 2), DateUtils.getUTCDate(2010, 3, 2), DateUtils.getUTCDate(2010, 4, 2), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 2),
            DateUtils.getUTCDate(2010, 7, 2), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 2), DateUtils.getUTCDate(2010, 10, 4), DateUtils.getUTCDate(2010, 11, 2),
            DateUtils.getUTCDate(2010, 12, 2), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 29), DateUtils.getUTCDate(2010, 2, 26), DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 31),
            DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 30), DateUtils.getUTCDate(2010, 8, 31), DateUtils.getUTCDate(2010, 9, 30), DateUtils.getUTCDate(2010, 10, 29),
            DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31)});
    // End date is modified
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18)});
    // Check modified in modified following.
    ZonedDateTime settlementDateModified = DateUtils.getUTCDate(2011, 3, 31);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(settlementDateModified, ANNUITY_TENOR, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2012, 3, 30), DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29)});
    // End-of-month
    ZonedDateTime settlementDateEOM = DateUtils.getUTCDate(2011, 2, 28);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(settlementDateEOM, ANNUITY_TENOR, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 8, 31), DateUtils.getUTCDate(2013, 2, 28)});
    // Stub: short-last
    Period tenorLong = Period.ofMonths(27);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorLong, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18), DateUtils.getUTCDate(2013, 6, 17)});
    // Stub: long-last
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorLong, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, !SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 6, 17)});
    // Stub: very short period: short stub.
    Period tenorVeryShort = Period.ofMonths(3);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorVeryShort, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 6, 17)});
    // Stub: very short period: long stub.
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorVeryShort, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, !SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 6, 17)});
  }

  @Test
  public void testPaymentTimes() {
    final DayCount daycount = new DayCount() {

      @Override
      public String getConventionName() {
        return "";
      }

      @Override
      public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
        return ((double) (secondDate.getMonthOfYear().getValue() - firstDate.getMonthOfYear().getValue())) / 12;
      }

      @Override
      public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
        return 0;
      }

    };
    final ZonedDateTime now = DateUtils.getUTCDate(2010, 1, 1);
    final ZonedDateTime dates[] = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1),
        DateUtils.getUTCDate(2010, 5, 1), DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), DateUtils.getUTCDate(2010, 9, 1),
        DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2010, 12, 1)};
    final double[] times = ScheduleCalculator.getTimes(dates, daycount, now);
    assertEquals(times.length, dates.length);
    for (int i = 0; i < times.length; i++) {
      assertEquals(times[i], i / 12., 1e-15);
    }
  }

  private void assertUnadjustedDates(final ZonedDateTime[] dates, final int length, final ZonedDateTime first, final ZonedDateTime last) {
    assertEquals(dates.length, length);
    assertEquals(dates[0], first);
    assertEquals(dates[length - 1], last);
  }

  private void assertDateArray(final ZonedDateTime[] dates1, final ZonedDateTime[] dates2) {
    assertEquals(dates1.length, dates2.length);
    for (int i = 0; i < dates1.length; i++) {
      assertEquals(dates1[i], dates2[i]);
    }
  }

  private static class FirstOfMonthCalendar implements Calendar {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      final DayOfWeek day = date.getDayOfWeek();
      if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
        return false;
      }
      if (date.getDayOfMonth() == 1) {
        return false;
      }
      return true;
    }
  }

  private static class WeekendCalendar implements Calendar {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      final DayOfWeek day = date.getDayOfWeek();
      if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
        return false;
      }
      return true;
    }
  }

  private static class AllCalendar implements Calendar {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      return true;
    }
  }
}
