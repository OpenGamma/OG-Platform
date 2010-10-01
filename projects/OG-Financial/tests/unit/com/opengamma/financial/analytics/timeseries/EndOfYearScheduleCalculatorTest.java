/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;

import org.junit.Test;

/**
 * 
 */
public class EndOfYearScheduleCalculatorTest {
  private static final Schedule CALCULATOR = new EndOfYearScheduleCalculator();

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
  public void testStartAndEndSameButInvalid1() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 12, 3), LocalDate.of(2001, 12, 3), false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid2() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 10, 30), LocalDate.of(2001, 10, 30), false);
  }

  @Test
  public void testStartAndEndSame() {
    final LocalDate date = LocalDate.of(2000, 12, 31);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void test() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 12, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 0);
    assertEquals(backward.length, 0);
    startDate = LocalDate.of(2000, 1, 2);
    endDate = LocalDate.of(2000, 12, 31);
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], endDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 2, 1);
    endDate = LocalDate.of(2001, 2, 1);
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], LocalDate.of(2000, 12, 31));
    assertEquals(backward[0], LocalDate.of(2000, 12, 31));
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 2, 9);
    final int months = 10;
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, months);
    assertEquals(backward.length, months);
    assertEquals(forward[0], LocalDate.of(2000, 12, 31));
    assertEquals(backward[0], LocalDate.of(2000, 12, 31));
    final LocalDate lastDate = LocalDate.of(2009, 12, 31);
    assertEquals(forward[months - 1], lastDate);
    assertEquals(backward[months - 1], lastDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
        assertEquals(forward[i].getMonthOfYear(), 12);
        assertEquals(forward[i].getDayOfMonth(), 31);
        assertEquals(backward[i].getYear() - backward[i - 1].getYear(), 1);
        assertEquals(backward[i].getMonthOfYear(), 12);
        assertEquals(backward[i].getDayOfMonth(), 31);
      }
    }
  }
}
