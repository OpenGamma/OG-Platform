/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class WeeklyScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final WeeklyScheduleCalculator CALCULATOR = new WeeklyScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test
  public void testSameStartAndEnd1() {
    final LocalDate date = LocalDate.of(2000, 1, 1);
    final LocalDate[] forward = CALCULATOR.getSchedule(date, date, false, true);
    final LocalDate[] backward = CALCULATOR.getSchedule(date, date, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], date);
    assertEquals(backward[0], date);
  }

  @Test
  public void testSameStartAndEnd2() {
    final ZonedDateTime date = DateUtil.getUTCDate(2000, 1, 1);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(date, date, false, true);
    final ZonedDateTime[] backward = CALCULATOR.getSchedule(date, date, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], date);
    assertEquals(backward[0], date);
  }

  @Test
  public void testWeekly1() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    final LocalDate endDate = LocalDate.of(2002, 2, 8);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    final int weeks = 110;
    assertEquals(forward.length, weeks);
    assertEquals(forward[0], startDate);
    assertEquals(forward[weeks - 1], LocalDate.of(2002, 2, 2));
    assertEquals(backward.length, weeks);
    assertEquals(backward[0], LocalDate.of(2000, 1, 7));
    assertEquals(backward[weeks - 1], endDate);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
      assertEquals(DateUtil.getDaysBetween(backward[i], backward[i - 1]), 7);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false));
    assertArrayEquals(backward, CALCULATOR.getSchedule(startDate, endDate, true));
  }

  @Test
  public void testWeekly2() {
    final ZonedDateTime startDate = DateUtil.getUTCDate(2000, 1, 1);
    final ZonedDateTime endDate = DateUtil.getUTCDate(2002, 2, 8);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    final int weeks = 110;
    assertEquals(forward.length, weeks);
    assertEquals(forward[0], startDate);
    assertEquals(forward[weeks - 1], DateUtil.getUTCDate(2002, 2, 2));
    assertEquals(backward.length, weeks);
    assertEquals(backward[0], DateUtil.getUTCDate(2000, 1, 7));
    assertEquals(backward[weeks - 1], endDate);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 7);
      assertEquals(DateUtil.getDaysBetween(backward[i], backward[i - 1]), 7);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false));
    assertArrayEquals(backward, CALCULATOR.getSchedule(startDate, endDate, true));
  }

}
