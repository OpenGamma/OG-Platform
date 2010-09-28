/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class WeeklyScheduleOnDayCalculatorTest {
  private static final Schedule CALCULATOR = new WeeklyScheduleOnDayCalculator(DayOfWeek.SATURDAY);

  @Test(expected = IllegalArgumentException.class)
  public void testNullStart() {
    CALCULATOR.getSchedule(null, LocalDate.of(2000, 1, 1), true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEnd() {
    CALCULATOR.getSchedule(LocalDate.of(2000, 1, 1), null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAfterEnd() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 1, 1), LocalDate.of(2000, 1, 1), true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    CALCULATOR.getSchedule(date, date, false);
  }

  @Test
  public void testStartAndEndSame() {
    final LocalDate date = LocalDate.of(2001, 2, 17);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, false);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testWeeklyOnDay() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 1, 6);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], startDate);
    startDate = LocalDate.of(2002, 2, 7);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], endDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    int weeks = 111;
    assertEquals(forward.length, weeks);
    assertEquals(backward.length, weeks);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], startDate);
    assertEquals(forward[weeks - 1], endDate);
    assertEquals(forward[weeks - 1], endDate);
    for (int i = 1; i < weeks; i++) {
      assertEquals(forward[i], backward[i]);
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
    }
    final Schedule calculator = new WeeklyScheduleOnDayCalculator(DayOfWeek.WEDNESDAY);
    forward = calculator.getSchedule(startDate, endDate, false);
    backward = calculator.getSchedule(startDate, endDate, true);
    weeks = 110;
    assertEquals(forward.length, weeks);
    assertEquals(backward.length, weeks);
    final LocalDate firstWednesday = LocalDate.of(2000, 1, 5);
    assertEquals(forward[0], firstWednesday);
    assertEquals(backward[0], firstWednesday);
    final LocalDate lastWednesday = LocalDate.of(2002, 2, 6);
    assertEquals(forward[weeks - 1], lastWednesday);
    assertEquals(forward[weeks - 1], lastWednesday);
    for (int i = 1; i < weeks; i++) {
      assertEquals(forward[i], backward[i]);
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
    }
  }
}
