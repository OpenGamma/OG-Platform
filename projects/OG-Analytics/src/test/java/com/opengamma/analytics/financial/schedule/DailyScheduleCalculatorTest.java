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
import org.threeten.bp.temporal.Temporal;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DailyScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final DailyScheduleCalculator CALCULATOR = new DailyScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test
  public void testSameStartAndEnd1() {
    final LocalDate date = LocalDate.of(2000, 1, 1);
    final LocalDate[] forward = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], date);
    assertArrayEquals(CALCULATOR.getSchedule(date, date), forward);
    assertArrayEquals(CALCULATOR.getSchedule(date, date, true, true), forward);
  }

  @Test
  public void testSameStartAndEnd2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2000, 1, 1);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], date);
    assertArrayEquals(CALCULATOR.getSchedule(date, date), forward);
    assertArrayEquals(CALCULATOR.getSchedule(date, date, true, true), forward);
  }

  @Test
  public void test1() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    final LocalDate endDate = LocalDate.of(2002, 2, 9);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate);
    assertCalculator(startDate, endDate, forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, false), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, false), forward);
  }

  @Test
  public void test2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2002, 2, 9);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate);
    assertCalculator(startDate, endDate, forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, false), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, false), forward);
  }

  private <T extends Temporal> void assertCalculator(final T startDate, final T endDate, final T[] forward) {
    final int days = 771;
    assertEquals(forward.length, days);
    assertEquals(forward[0], startDate);
    assertEquals(forward[days - 1], endDate);
    assertEquals(forward[days - 1], endDate);
    for (int i = 1; i < days; i++) {
      assertEquals(DateUtils.getDaysBetween(forward[i], forward[i - 1]), 1);
    }
  }

}
