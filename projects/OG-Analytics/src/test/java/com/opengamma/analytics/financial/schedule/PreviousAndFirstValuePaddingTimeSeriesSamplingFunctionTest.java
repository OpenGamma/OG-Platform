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

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PreviousAndFirstValuePaddingTimeSeriesSamplingFunctionTest {
  //TODO test start date = holiday
  private static final LocalDate START = LocalDate.of(2009, 1, 1);
  private static final LocalDate END = LocalDate.of(2010, 10, 1);
  private static final DailyScheduleCalculator DAILY = new DailyScheduleCalculator();
  private static final WeeklyScheduleOnDayCalculator WEEKLY_MONDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.MONDAY);
  private static final PreviousAndFirstValuePaddingTimeSeriesSamplingFunction F = new PreviousAndFirstValuePaddingTimeSeriesSamplingFunction();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final LocalDate[] DAILY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(DAILY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] MONDAY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(WEEKLY_MONDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate MISSING_DAY_MONDAY_1 = LocalDate.of(2009, 2, 9);
  private static final LocalDate MISSING_DAY_MONDAY_2 = LocalDate.of(2009, 2, 16);
  private static final LocalDateDoubleTimeSeries TS_NO_MISSING_DATA;
  private static final LocalDateDoubleTimeSeries TS_TWO_MISSING_DATA_POINTS;

  static {
    final List<LocalDate> t1 = new ArrayList<>();
    final List<Double> d1 = new ArrayList<>();
    final List<LocalDate> t2 = new ArrayList<>();
    final List<Double> d2 = new ArrayList<>();
    for (int i = 0; i < DAILY_SCHEDULE.length; i++) {
      t1.add(DAILY_SCHEDULE[i]);
      d1.add(Double.valueOf(i));
      if (!(DAILY_SCHEDULE[i].equals(MISSING_DAY_MONDAY_1) || DAILY_SCHEDULE[i].equals(MISSING_DAY_MONDAY_2))) {
        t2.add(DAILY_SCHEDULE[i]);
        d2.add(Double.valueOf(i));
      }
    }
    TS_NO_MISSING_DATA = ImmutableLocalDateDoubleTimeSeries.of(t1, d1);
    TS_TWO_MISSING_DATA_POINTS = ImmutableLocalDateDoubleTimeSeries.of(t2, d2);
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
  public void testMissingFirstData() {
    final LocalDate start = START.minusDays(21);
    final LocalDate[] daily = HOLIDAY_REMOVER.getStrippedSchedule(DAILY.getSchedule(start, END, true, true), WEEKEND_CALENDAR);
    final List<LocalDate> t = new ArrayList<>();
    final List<Double> d = new ArrayList<>();
    for (int i = 0; i < daily.length; i++) {
      if (daily[i].isAfter(START)) {
        t.add(daily[i]);
        d.add(Double.valueOf(i));
      }
    }
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(t, d);
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(ts, daily);
    assertEquals(result.size(), daily.length);
    final int offset = 16;
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), daily[i]);
      if (i < 16) {
        assertEquals(entry.getValue(), offset, 0);
      } else {
        assertEquals(entry.getValue(), i, 0);
      }
      i++;
    }
  }

  @Test
  public void testNoMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, DAILY_SCHEDULE);
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i]);
      assertEquals(entry.getValue(), i++, 0);
    }
  }

  @Test
  public void testMissingDataWeekly() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_TWO_MISSING_DATA_POINTS, MONDAY_SCHEDULE);
    assertEquals(result.size(), MONDAY_SCHEDULE.length);
    int i = 0;
    int j = 2;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), MONDAY_SCHEDULE[i++]);
      if (entry.getKey().equals(MISSING_DAY_MONDAY_1) || entry.getKey().equals(MISSING_DAY_MONDAY_2)) {
        assertEquals(entry.getValue(), j - 1, 0);
      } else {
        assertEquals(entry.getValue(), j, 0);
      }
      j += 5;
    }
  }

  @Test
  public void testDaysMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_TWO_MISSING_DATA_POINTS, DAILY_SCHEDULE);
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i]);
      if (entry.getKey().equals(MISSING_DAY_MONDAY_1) || entry.getKey().equals(MISSING_DAY_MONDAY_2)) {
        assertEquals(entry.getValue(), i - 1, 0);
      } else {
        assertEquals(entry.getValue(), i, 0);
      }
      i++;
    }
  }

}
