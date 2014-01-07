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
public class NoPaddingTimeSeriesSamplingFunctionTest {
  private static final LocalDate START = LocalDate.of(2009, 1, 1);
  private static final LocalDate END = LocalDate.of(2010, 10, 1);
  private static final DailyScheduleCalculator DAILY = new DailyScheduleCalculator();
  private static final NoPaddingTimeSeriesSamplingFunction F = new NoPaddingTimeSeriesSamplingFunction();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final LocalDate[] DAILY_SCHEDULE = HOLIDAY_REMOVER.getStrippedSchedule(DAILY.getSchedule(START, END, true, true), WEEKEND_CALENDAR);
  private static final LocalDate MISSING_DAY_TUESDAY = LocalDate.of(2009, 2, 6);
  private static final LocalDateDoubleTimeSeries TS_NO_MISSING_DATA;
  private static final LocalDateDoubleTimeSeries TS_MISSING_DATA;
  private static final LocalDateDoubleTimeSeries TS_MISSING_MONTH_DATA;

  static {
    final List<LocalDate> t1 = new ArrayList<>();
    final List<Double> d1 = new ArrayList<>();
    final List<LocalDate> t2 = new ArrayList<>();
    final List<Double> d2 = new ArrayList<>();
    final List<LocalDate> t3 = new ArrayList<>();
    final List<Double> d3 = new ArrayList<>();
    for (int i = 0; i < DAILY_SCHEDULE.length; i++) {
      t1.add(DAILY_SCHEDULE[i]);
      d1.add(Double.valueOf(i));
      if (WEEKEND_CALENDAR.isWorkingDay(DAILY_SCHEDULE[i])) {
        t3.add(DAILY_SCHEDULE[i]);
        d3.add(Double.valueOf(i));
      }
      if (!DAILY_SCHEDULE[i].equals(MISSING_DAY_TUESDAY)) {
        t2.add(DAILY_SCHEDULE[i]);
        d2.add(Double.valueOf(i));
      }
    }
    TS_NO_MISSING_DATA = ImmutableLocalDateDoubleTimeSeries.of(t1, d1);
    TS_MISSING_DATA = ImmutableLocalDateDoubleTimeSeries.of(t2, d2);
    TS_MISSING_MONTH_DATA = ImmutableLocalDateDoubleTimeSeries.of(t3, d3);
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
  public void testNoMissingData() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_NO_MISSING_DATA, DAILY_SCHEDULE);
    assertEquals(TS_NO_MISSING_DATA.size(), result.size());
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(TS_NO_MISSING_DATA.getTimeAtIndex(i), entry.getKey());
      assertEquals(TS_NO_MISSING_DATA.getValueAtIndex(i++), entry.getValue(), 0);
    }
  }

  @Test
  public void testOneDayMissingData() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_MISSING_DATA, DAILY_SCHEDULE);
    assertEquals(result.size(), TS_MISSING_DATA.size());
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(TS_MISSING_DATA.getTimeAtIndex(i), entry.getKey());
      if (entry.getKey().equals(MISSING_DAY_TUESDAY)) {
        assertEquals(TS_MISSING_DATA.getValueAtIndex(i - 1), entry.getValue(), 0);
      } else {
        assertEquals(TS_MISSING_DATA.getValueAtIndex(i), entry.getValue(), 0);
      }
      i++;
    }
  }

  @Test
  public void testOneMonthMissingData() {
    final LocalDateDoubleTimeSeries result = F.getSampledTimeSeries(TS_MISSING_MONTH_DATA, DAILY_SCHEDULE);
    assertEquals(result.size(), TS_MISSING_MONTH_DATA.size());
    int i = 0;
    for (final Entry<LocalDate, Double> entry : result) {
      assertEquals(TS_MISSING_MONTH_DATA.getTimeAtIndex(i), entry.getKey());
      assertEquals(TS_MISSING_MONTH_DATA.getValueAtIndex(i), entry.getValue(), 0);
      i++;
    }
  }
}