/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HolidayDateRemovalFunctionTest {
  private static final HolidayDateRemovalFunction F = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final Schedule DAILY = new DailyScheduleCalculator();
  private static final LocalDate START = LocalDate.of(2009, 1, 1);
  private static final LocalDate END = LocalDate.of(2010, 1, 1);
  private static final LocalDate[] SCHEDULE = DAILY.getSchedule(START, END, true, false);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates() {
    F.getStrippedSchedule(null, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidays() {
    F.getStrippedSchedule(SCHEDULE, null);
  }

  @Test
  public void test() {
    final LocalDate[] stripped = F.getStrippedSchedule(SCHEDULE, WEEKEND_CALENDAR);
    assertEquals(SCHEDULE.length, 366);
    assertEquals(stripped.length, 262);
    int i = 0, j = 0;
    for (final LocalDate date : SCHEDULE) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        i++;
      } else {
        assertEquals(SCHEDULE[i++], stripped[j++]);
      }
    }
  }
}
