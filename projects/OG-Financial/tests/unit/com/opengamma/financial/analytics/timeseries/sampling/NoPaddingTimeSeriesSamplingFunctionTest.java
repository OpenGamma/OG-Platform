/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

import com.opengamma.financial.analytics.timeseries.DailyScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class NoPaddingTimeSeriesSamplingFunctionTest {
  private static final LocalDate START = LocalDate.of(2009, 1, 1);
  private static final LocalDate END = LocalDate.of(2010, 10, 1);
  private static final HolidayDateRemovalFunction WEEKEND_REMOVER = new HolidayDateRemovalFunction();
  private static final DailyScheduleCalculator DAILY = new DailyScheduleCalculator();
  private static final NoPaddingTimeSeriesSamplingFunction F = new NoPaddingTimeSeriesSamplingFunction();
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
  private static final LocalDate MISSING_DAY_TUESDAY = LocalDate.of(2009, 2, 6);
  private static final LocalDateDoubleTimeSeries TS_NO_MISSING_DATA;
  private static final LocalDateDoubleTimeSeries TS_MISSING_DATA_POINT;

  static {
    final List<LocalDate> t1 = new ArrayList<LocalDate>();
    final List<Double> d1 = new ArrayList<Double>();
    final List<LocalDate> t2 = new ArrayList<LocalDate>();
    final List<Double> d2 = new ArrayList<Double>();
    final List<LocalDate> t3 = new ArrayList<LocalDate>();
    final List<Double> d3 = new ArrayList<Double>();
    for (int i = 0; i < DAILY_SCHEDULE.length; i++) {
      t1.add(DAILY_SCHEDULE[i].toLocalDate());
      d1.add(Double.valueOf(i));
      if (!DAILY_SCHEDULE[i].equals(MISSING_DAY_TUESDAY)) {
        t2.add(DAILY_SCHEDULE[i].toLocalDate());
        d2.add(Double.valueOf(i));
      }
      if (!(DAILY_SCHEDULE[i].getMonthOfYear() == MonthOfYear.FEBRUARY && DAILY_SCHEDULE[i].getYear() == 2009)) {
        t3.add(DAILY_SCHEDULE[i].toLocalDate());
        d3.add(Double.valueOf(i));
      }
    }
    TS_NO_MISSING_DATA = new ArrayLocalDateDoubleTimeSeries(t1, d1);
    TS_MISSING_DATA_POINT = new ArrayLocalDateDoubleTimeSeries(t2, d2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    F.getSampledTimeSeries(null, DAILY_SCHEDULE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSchedule() {
    F.getSampledTimeSeries(TS_NO_MISSING_DATA, null);
  }

  @Test
  public void testNoMissingData() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, DAILY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), DAILY_SCHEDULE.length);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(entry.getKey(), DAILY_SCHEDULE[i].toLocalDate());
      assertEquals(entry.getValue(), i++, 0);
    }
  }

  @Test
  public void testOneDayMissingData() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_MISSING_DATA_POINT, DAILY_SCHEDULE).toLocalDateDoubleTimeSeries();
    assertEquals(result.size(), DAILY_SCHEDULE.length - 1);
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      if (entry.getKey().isBefore(MISSING_DAY_TUESDAY.toLocalDate())) {
        assertEquals(entry.getKey(), DAILY_SCHEDULE[i].toLocalDate());
        assertEquals(entry.getValue(), i, 0);
      } else {
        assertEquals(entry.getKey(), DAILY_SCHEDULE[i + 1].toLocalDate());
        assertEquals(entry.getValue(), i + 1, 0);
      }
      i++;
    }
  }
}
