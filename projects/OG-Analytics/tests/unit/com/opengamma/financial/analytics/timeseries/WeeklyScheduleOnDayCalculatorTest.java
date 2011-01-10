/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class WeeklyScheduleOnDayCalculatorTest extends ScheduleCalculatorTestCase {
  private static final WeeklyScheduleOnDayCalculator CALCULATOR = new WeeklyScheduleOnDayCalculator(DayOfWeek.SATURDAY);

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid1() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    CALCULATOR.getSchedule(date, date, false, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid2() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 2, 13);
    CALCULATOR.getSchedule(date, date, false, true);
  }

  @Test
  public void testStartAndEndSame1() {
    final LocalDate date = LocalDate.of(2001, 2, 17);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testStartAndEndSame2() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 2, 17);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testWeeklyOnDay1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 1, 6);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], startDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    startDate = LocalDate.of(2002, 2, 7);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], endDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    int weeks = 111;
    assertEquals(forward.length, weeks);
    assertEquals(forward[0], startDate);
    assertEquals(forward[weeks - 1], endDate);
    assertEquals(forward[weeks - 1], endDate);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    final WeeklyScheduleOnDayCalculator calculator = new WeeklyScheduleOnDayCalculator(DayOfWeek.WEDNESDAY);
    forward = calculator.getSchedule(startDate, endDate, false, true);
    weeks = 110;
    assertEquals(forward.length, weeks);
    final LocalDate firstWednesday = LocalDate.of(2000, 1, 5);
    assertEquals(forward[0], firstWednesday);
    final LocalDate lastWednesday = LocalDate.of(2002, 2, 6);
    assertEquals(forward[weeks - 1], lastWednesday);
    assertEquals(forward[weeks - 1], lastWednesday);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
    }
    assertArrayEquals(forward, calculator.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, calculator.getSchedule(startDate, endDate));
  }

  @Test
  public void testWeeklyOnDay2() {
    ZonedDateTime startDate = DateUtil.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtil.getUTCDate(2000, 1, 6);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], startDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    startDate = DateUtil.getUTCDate(2002, 2, 7);
    endDate = DateUtil.getUTCDate(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], endDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    startDate = DateUtil.getUTCDate(2000, 1, 1);
    endDate = DateUtil.getUTCDate(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    int weeks = 111;
    assertEquals(forward.length, weeks);
    assertEquals(forward[0], startDate);
    assertEquals(forward[weeks - 1], endDate);
    assertEquals(forward[weeks - 1], endDate);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    final WeeklyScheduleOnDayCalculator calculator = new WeeklyScheduleOnDayCalculator(DayOfWeek.WEDNESDAY);
    forward = calculator.getSchedule(startDate, endDate, false, true);
    weeks = 110;
    assertEquals(forward.length, weeks);
    final ZonedDateTime firstWednesday = DateUtil.getUTCDate(2000, 1, 5);
    assertEquals(forward[0], firstWednesday);
    final ZonedDateTime lastWednesday = DateUtil.getUTCDate(2002, 2, 6);
    assertEquals(forward[weeks - 1], lastWednesday);
    assertEquals(forward[weeks - 1], lastWednesday);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
    }
    assertArrayEquals(forward, calculator.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, calculator.getSchedule(startDate, endDate));
  }
}
