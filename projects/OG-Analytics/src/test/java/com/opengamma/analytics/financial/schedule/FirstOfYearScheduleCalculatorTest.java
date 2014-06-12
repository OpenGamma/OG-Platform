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
public class FirstOfYearScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final FirstOfYearScheduleCalculator CALCULATOR = new FirstOfYearScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid1() {
    CALCULATOR.getSchedule(DateUtils.getUTCDate(2001, 1, 3), DateUtils.getUTCDate(2001, 1, 3), false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid2() {
    CALCULATOR.getSchedule(DateUtils.getUTCDate(2001, 2, 1), DateUtils.getUTCDate(2001, 2, 1), false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid3() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 1, 3), LocalDate.of(2001, 1, 3), false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid4() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 2, 1), LocalDate.of(2001, 2, 1), false, true);
  }

  @Test
  public void testStartAndEndSame1() {
    final LocalDate date = LocalDate.of(2000, 1, 1);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testStartAndEndSame2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2000, 1, 1);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void test1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 12, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], startDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    startDate = LocalDate.of(2000, 1, 2);
    endDate = LocalDate.of(2000, 12, 30);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 0);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    startDate = LocalDate.of(2002, 2, 2);
    endDate = LocalDate.of(2003, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], LocalDate.of(2003, 1, 1));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 2, 9);
    final int months = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, months);
    assertEquals(forward[0], startDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    final LocalDate lastDate = LocalDate.of(2010, 1, 1);
    assertEquals(forward[months - 1], lastDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
        assertEquals(forward[i].getMonthValue(), 1);
        assertEquals(forward[i].getDayOfMonth(), 1);
      }
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
  }

  @Test
  public void test2() {
    ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtils.getUTCDate(2000, 12, 30);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], startDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    startDate = DateUtils.getUTCDate(2000, 1, 2);
    endDate = DateUtils.getUTCDate(2000, 12, 30);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 0);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    startDate = DateUtils.getUTCDate(2002, 2, 2);
    endDate = DateUtils.getUTCDate(2003, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertEquals(forward[0], DateUtils.getUTCDate(2003, 1, 1));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    startDate = DateUtils.getUTCDate(2000, 1, 1);
    endDate = DateUtils.getUTCDate(2010, 2, 9);
    final int months = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, months);
    assertEquals(forward[0], startDate);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    final ZonedDateTime lastDate = DateUtils.getUTCDate(2010, 1, 1);
    assertEquals(forward[months - 1], lastDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
        assertEquals(forward[i].getMonthValue(), 1);
        assertEquals(forward[i].getDayOfMonth(), 1);
      }
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
  }
}
