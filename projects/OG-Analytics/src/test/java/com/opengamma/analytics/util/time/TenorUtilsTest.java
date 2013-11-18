/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.ExceptionCalendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class TenorUtilsTest {
  /** Empty holiday calendar */
  private static final Calendar NO_HOLIDAYS = new NoHolidayCalendar();
  /** Holiday calendar containing only weekends */
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  /** Holiday calendar containing weekends and 1/1/2014 */
  private static final Calendar CALENDAR = new MyCalendar();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustZonedDateTime1() {
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_YEAR));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_MONTH));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ON);
  }

  @Test
  public void testAdjustZonedDateTime2() {
    final int spotDays = 2;
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 4), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 6), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 6), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, CALENDAR, spotDays));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustLocalDateTime1() {
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_YEAR));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_MONTH));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(localDateTime, Tenor.ON);
  }

  @Test
  public void testAdjustLocalDateTime2() {
    final int spotDays = 2;
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 4, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, CALENDAR, spotDays));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustLocalDate1() {
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_YEAR));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_MONTH));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(localDate, Tenor.ON);
  }

  @Test
  public void testAdjustLocalDate2() {
    final int spotDays = 2;
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 4), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, CALENDAR, spotDays));
  }

  /**
   * Calendar with weekends and 1-1-2013, 1-1-2014 as holidays
   */
  private static class MyCalendar extends ExceptionCalendar {

    /**
     * Default constructor
     */
    protected MyCalendar() {
      super("");
    }

    @Override
    protected boolean isNormallyWorkingDay(final LocalDate date) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        return false;
      }
      if (date.equals(LocalDate.of(2014, 1, 1))) {
        return false;
      }
      return true;
    }

  }
}
