/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.junit.Test;

import com.opengamma.core.convention.Calendar;
import com.opengamma.financial.analytics.timeseries.DailyScheduleCalculator;
import com.opengamma.financial.analytics.timeseries.WeeklyScheduleOnDayCalculator;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class PreviousValuePaddingTimeSeriesSamplingFunctionTest {
  private static final LocalDate START = LocalDate.of(2009, 1, 1);
  private static final LocalDate END = LocalDate.of(2010, 10, 1);
  private static final HolidayDateRemovalFunction WEEKEND_REMOVER = new HolidayDateRemovalFunction();
  private static final DailyScheduleCalculator DAILY = new DailyScheduleCalculator();
  private static final WeeklyScheduleOnDayCalculator WEEKLY_TUESDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.TUESDAY);
  private static final WeeklyScheduleOnDayCalculator WEEKLY_FRIDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.FRIDAY);
  private static final WeeklyScheduleOnDayCalculator WEEKLY_MONDAY = new WeeklyScheduleOnDayCalculator(DayOfWeek.MONDAY);
  private static final PreviousValuePaddingTimeSeriesSamplingFunction F = new PreviousValuePaddingTimeSeriesSamplingFunction();
  private static final Calendar WEEKEND_CALENDAR = new Calendar() {

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      return !(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    @Override
    public String getConventionName() {
      return null;
    }

  };
  private static final LocalDate[] DAILY_SCHEDULE = WEEKEND_REMOVER.getStrippedSchedule(DAILY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] TUESDAY_SCHEDULE = WEEKEND_REMOVER.getStrippedSchedule(WEEKLY_TUESDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] FRIDAY_SCHEDULE = WEEKEND_REMOVER.getStrippedSchedule(WEEKLY_FRIDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate[] MONDAY_SCHEDULE = WEEKEND_REMOVER.getStrippedSchedule(WEEKLY_MONDAY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate MISSING_DAY_FRIDAY = LocalDate.of(2009, 2, 6);
  private static final LocalDate MISSING_DAY_MONDAY_1 = LocalDate.of(2009, 2, 9);
  private static final LocalDate MISSING_DAY_MONDAY_2 = LocalDate.of(2009, 2, 16);
  private static final LocalDateDoubleTimeSeries TS_NO_MISSING_DATA;
  private static final LocalDateDoubleTimeSeries TS_ONE_MISSING_DATA_POINT;
  private static final LocalDateDoubleTimeSeries TS_THREE_MISSING_DATA_POINTS;
  private static final LocalDateDoubleTimeSeries TS_MISSING_MONTH;

  static {
    final List<LocalDate> t1 = new ArrayList<LocalDate>();
    final List<Double> d1 = new ArrayList<Double>();
    final List<LocalDate> t2 = new ArrayList<LocalDate>();
    final List<Double> d2 = new ArrayList<Double>();
    final List<LocalDate> t3 = new ArrayList<LocalDate>();
    final List<Double> d3 = new ArrayList<Double>();
    final List<LocalDate> t4 = new ArrayList<LocalDate>();
    final List<Double> d4 = new ArrayList<Double>();
    for (int i = 0; i < DAILY_SCHEDULE.length; i++) {
      t1.add(DAILY_SCHEDULE[i].toLocalDate());
      d1.add(Double.valueOf(i));
      if (!DAILY_SCHEDULE[i].toLocalDate().equals(MISSING_DAY_FRIDAY)) {
        t2.add(DAILY_SCHEDULE[i].toLocalDate());
        d2.add(Double.valueOf(i));
        if (!(DAILY_SCHEDULE[i].toLocalDate().equals(MISSING_DAY_MONDAY_1) || DAILY_SCHEDULE[i].toLocalDate().equals(MISSING_DAY_MONDAY_2))) {
          t3.add(DAILY_SCHEDULE[i].toLocalDate());
          d3.add(Double.valueOf(i));
        }
      }
      if (!(DAILY_SCHEDULE[i].getMonthOfYear() == MonthOfYear.FEBRUARY && DAILY_SCHEDULE[i].getYear() == 2009)) {
        t4.add(DAILY_SCHEDULE[i].toLocalDate());
        d4.add(Double.valueOf(i));
      }
    }
    TS_NO_MISSING_DATA = new ArrayLocalDateDoubleTimeSeries(t1, d1);
    TS_ONE_MISSING_DATA_POINT = new ArrayLocalDateDoubleTimeSeries(t2, d2);
    TS_THREE_MISSING_DATA_POINTS = new ArrayLocalDateDoubleTimeSeries(t3, d3);
    TS_MISSING_MONTH = new ArrayLocalDateDoubleTimeSeries(t4, d4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    F.getSampledTimeSeries(null, DAILY_SCHEDULE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSchedule() {
    F.getSampledTimeSeries(TS_NO_MISSING_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonOverlappingSchedule() {
    F.getSampledTimeSeries(TS_MISSING_MONTH, new LocalDate[] {LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2)});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingDataOnFirstDay() {
    F.getSampledTimeSeries(new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2), LocalDate.of(2000, 1, 3)}, new double[] {1, 2, 3}),
        new LocalDate[] {LocalDate.of(1999, 12, 31), LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2), LocalDate.of(2000, 1, 3)});
  }

  @Test
  public void testNoMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, DAILY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i].toLocalDate());
      assertEquals(entry.getValue(), i++, 0);
    }
  }

  @Test
  public void testNoMissingDataWeekly() {
    LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, TUESDAY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), TUESDAY_SCHEDULE.length);
    int i = 0, j = 3;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), TUESDAY_SCHEDULE[i++].toLocalDate());
      assertEquals(entry.getValue(), j, 0);
      j += 5;
    }
    result = F.getSampledTimeSeries(TS_ONE_MISSING_DATA_POINT, TUESDAY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), TUESDAY_SCHEDULE.length);
    i = 0;
    j = 3;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), TUESDAY_SCHEDULE[i++].toLocalDate());
      assertEquals(entry.getValue(), j, 0);
      j += 5;
    }
  }

  @Test
  public void testOneDayMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_ONE_MISSING_DATA_POINT, DAILY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i].toLocalDate());
      if (entry.getKey().equals(MISSING_DAY_FRIDAY.toLocalDate())) {
        assertEquals(entry.getValue(), i - 1, 0);
      } else {
        assertEquals(entry.getValue(), i, 0);
      }
      i++;
    }
  }

  @Test
  public void testMissingDataWeekly() {
    LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_ONE_MISSING_DATA_POINT, FRIDAY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), FRIDAY_SCHEDULE.length);
    int i = 0, j = 1;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), FRIDAY_SCHEDULE[i++].toLocalDate());
      if (entry.getKey().equals(MISSING_DAY_FRIDAY.toLocalDate())) {
        assertEquals(entry.getValue(), j - 1, 0);
      } else {
        assertEquals(entry.getValue(), j, 0);
      }
      j += 5;
    }
    result = F.getSampledTimeSeries(TS_THREE_MISSING_DATA_POINTS, MONDAY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), MONDAY_SCHEDULE.length);
    i = 0;
    j = 2;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), MONDAY_SCHEDULE[i++].toLocalDate());
      if (entry.getKey().equals(MISSING_DAY_MONDAY_1)) {
        assertEquals(entry.getValue(), j - 2, 0);
      } else if (entry.getKey().equals(MISSING_DAY_MONDAY_2)) {
        assertEquals(entry.getValue(), j - 1, 0);
      } else {
        assertEquals(entry.getValue(), j, 0);
      }
      j += 5;
    }
  }

  @Test
  public void testThreeDaysMissingDataDaily() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_THREE_MISSING_DATA_POINTS, DAILY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i].toLocalDate());
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
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_MISSING_MONTH, DAILY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i].toLocalDate());
      if (entry.getKey().getMonthOfYear() == MonthOfYear.FEBRUARY && entry.getKey().getYear() == 2009) {
        assertEquals(entry.getValue(), 21, 0);
      } else {
        assertEquals(entry.getValue(), i, 0);
      }
      i++;
    }
  }
}
