/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
    final ZonedDateTime date = DateUtils.getUTCDate(2000, 1, 1);
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
      assertEquals(DateUtils.getDaysBetween(forward[i], forward[i - 1]), 7);
      assertEquals(DateUtils.getDaysBetween(backward[i], backward[i - 1]), 7);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false));
    assertArrayEquals(backward, CALCULATOR.getSchedule(startDate, endDate, true));
  }

  @Test
  public void testWeekly2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2002, 2, 8);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    final int weeks = 110;
    assertEquals(forward.length, weeks);
    assertEquals(forward[0], startDate);
    assertEquals(forward[weeks - 1], DateUtils.getUTCDate(2002, 2, 2));
    assertEquals(backward.length, weeks);
    assertEquals(backward[0], DateUtils.getUTCDate(2000, 1, 7));
    assertEquals(backward[weeks - 1], endDate);
    for (int i = 1; i < weeks; i++) {
      assertEquals(DateUtils.getDaysBetween(forward[i], forward[i - 1]), 7);
      assertEquals(DateUtils.getDaysBetween(backward[i], backward[i - 1]), 7);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false));
    assertArrayEquals(backward, CALCULATOR.getSchedule(startDate, endDate, true));
  }

}
