/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PreviousValuePaddingTimeSeriesSamplingFunctionTest {
  private static final LocalDate START = LocalDate.of(2009, 1, 1);
  private static final LocalDate END = LocalDate.of(2010, 10, 1);
  private static final DailyScheduleCalculator DAILY = new DailyScheduleCalculator();
  private static final WeeklyScheduleOnDayCalculator WEEKLY_TUESDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.TUESDAY);
  private static final WeeklyScheduleOnDayCalculator WEEKLY_FRIDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.FRIDAY);
  private static final WeeklyScheduleOnDayCalculator WEEKLY_MONDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.MONDAY);
  private static final PreviousValuePaddingTimeSeriesSamplingFunction F = new PreviousValuePaddingTimeSeriesSamplingFunction();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final LocalDate[] DAILY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(DAILY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] TUESDAY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(WEEKLY_TUESDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] FRIDAY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(WEEKLY_FRIDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] MONDAY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(WEEKLY_MONDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate MISSING_DAY_FRIDAY = LocalDate.of(2009, 2, 6);
  private static final LocalDate MISSING_DAY_MONDAY_1 = LocalDate.of(2009, 2, 9);
  private static final LocalDate MISSING_DAY_MONDAY_2 = LocalDate.of(2009, 2, 16);
  private static final LocalDateDoubleTimeSeries TS_NO_MISSING_DATA;
  private static final LocalDateDoubleTimeSeries TS_ONE_MISSING_DAY;
  private static final LocalDateDoubleTimeSeries TS_THREE_MISSING_DAYS;
  private static final LocalDateDoubleTimeSeries TS_MISSING_MONTH;

  static {
    final List<LocalDate> t1 = new ArrayList<>();
    final List<Double> d1 = new ArrayList<>();
    final List<LocalDate> t2 = new ArrayList<>();
    final List<Double> d2 = new ArrayList<>();
    final List<LocalDate> t3 = new ArrayList<>();
    final List<Double> d3 = new ArrayList<>();
    final List<LocalDate> t4 = new ArrayList<>();
    final List<Double> d4 = new ArrayList<>();
    final List<LocalDate> t5 = new ArrayList<>();
    final List<Double> d5 = new ArrayList<>();
    for (int i = 0; i < DAILY_SCHEDULE.length; i++) {
      t1.add(DAILY_SCHEDULE[i]);
      d1.add(Double.valueOf(i));
      if (!DAILY_SCHEDULE[i].equals(MISSING_DAY_FRIDAY)) {
        t2.add(DAILY_SCHEDULE[i]);
        d2.add(Double.valueOf(i));
        if (!(DAILY_SCHEDULE[i].equals(MISSING_DAY_MONDAY_1) || DAILY_SCHEDULE[i].equals(MISSING_DAY_MONDAY_2))) {
          t3.add(DAILY_SCHEDULE[i]);
          d3.add(Double.valueOf(i));
        }
      }
      if (!(DAILY_SCHEDULE[i].getMonth() == Month.FEBRUARY && DAILY_SCHEDULE[i].getYear() == 2009)) {
        t4.add(DAILY_SCHEDULE[i]);
        d4.add(Double.valueOf(i));
      }
      if (WEEKEND_CALENDAR.isWorkingDay(DAILY_SCHEDULE[i])) {
        t5.add(DAILY_SCHEDULE[i]);
        d5.add(Double.valueOf(i));
      }
    }
    TS_NO_MISSING_DATA = ImmutableLocalDateDoubleTimeSeries.of(t1, d1);
    TS_ONE_MISSING_DAY = ImmutableLocalDateDoubleTimeSeries.of(t2, d2);
    TS_THREE_MISSING_DAYS = ImmutableLocalDateDoubleTimeSeries.of(t3, d3);
    TS_MISSING_MONTH = ImmutableLocalDateDoubleTimeSeries.of(t4, d4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    F.getSampledTimeSeries(null, DAILY_SCHEDULE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSchedule() {
    F.getSampledTimeSeries(TS_NO_MISSING_DATA, null);
  }

  @Test
  public void testNoMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, DAILY_SCHEDULE);
    assertEquals(TS_NO_MISSING_DATA.size(), result.size());
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(TS_NO_MISSING_DATA.getTimeAtIndex(i), entry.getKey());
      assertEquals(TS_NO_MISSING_DATA.getValueAtIndex(i++), entry.getValue(), 0);
    }
  }

  @Test
  public void testNoMissingDataWeekly() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, TUESDAY_SCHEDULE);
    assertEquals(TUESDAY_SCHEDULE.length, result.size());
    int i = 0, j = 3;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(TUESDAY_SCHEDULE[i++], entry.getKey());
      assertEquals(j, entry.getValue(), 0);
      j += 5;
    }
  }

  @Test
  public void testOneDayMissingDataDaily() {
    LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_ONE_MISSING_DAY, DAILY_SCHEDULE);
    assertEquals(TS_NO_MISSING_DATA.size(), result.size());
    int i = 0;
    result = F.getSampledTimeSeries(TS_ONE_MISSING_DAY, DAILY_SCHEDULE);
    assertEquals(TS_NO_MISSING_DATA.size(), result.size());
    i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(TS_NO_MISSING_DATA.getTimeAtIndex(i), entry.getKey());
      if (entry.getKey().equals(MISSING_DAY_FRIDAY)) {
        assertEquals(TS_NO_MISSING_DATA.getValueAtIndex(i - 1), entry.getValue(), 0);
      } else {
        assertEquals(TS_NO_MISSING_DATA.getValueAtIndex(i), entry.getValue(), 0);
      }
      i++;
    }
  }

  @Test
  public void testMissingDataWeekly() {
    LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_ONE_MISSING_DAY, FRIDAY_SCHEDULE);
    assertEquals(FRIDAY_SCHEDULE.length, result.size());
    int i = 0, j = 1;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(FRIDAY_SCHEDULE[i++], entry.getKey());
      if (entry.getKey().equals(MISSING_DAY_FRIDAY)) {
        assertEquals(j - 1, entry.getValue(), 0);
      } else {
        assertEquals(j, entry.getValue(), 0);
      }
      j += 5;
    }
    result = F.getSampledTimeSeries(TS_THREE_MISSING_DAYS, MONDAY_SCHEDULE);
    assertEquals(MONDAY_SCHEDULE.length, result.size());
    i = 0;
    j = 2;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(MONDAY_SCHEDULE[i++], entry.getKey());
      if (entry.getKey().equals(MISSING_DAY_MONDAY_1)) {
        assertEquals(j - 2, entry.getValue(), 0);
      } else if (entry.getKey().equals(MISSING_DAY_MONDAY_2)) {
        assertEquals(j - 1, entry.getValue(), 0);
      } else {
        assertEquals(j, entry.getValue(), 0);
      }
      j += 5;
    }
  }

  @Test
  public void testThreeDaysMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_THREE_MISSING_DAYS, DAILY_SCHEDULE);
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i]);
      if (entry.getKey().equals(MISSING_DAY_FRIDAY)) {
        assertEquals(entry.getValue(), i - 1, 0);
      } else if (entry.getKey().equals(MISSING_DAY_MONDAY_1)) {
        assertEquals(entry.getValue(), i - 2, 0);
      } else if (entry.getKey().equals(MISSING_DAY_MONDAY_2)) {
        assertEquals(entry.getValue(), i - 1, 0);
      } else {
        assertEquals(entry.getValue(), i, 0);
      }
      i++;
    }
  }

  @Test
  public void testMissingMonth() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_MISSING_MONTH, DAILY_SCHEDULE);
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i]);
      if (entry.getKey().getMonth() == Month.FEBRUARY && entry.getKey().getYear() == 2009) {
        assertEquals(entry.getValue(), 21, 0);
      } else {
        assertEquals(entry.getValue(), i, 0);
      }
      i++;
    }
  }
}
