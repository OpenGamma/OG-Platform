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
public class EndOfMonthQuarterlyScheduleCalculatorTest {
  private static final EndOfMonthQuarterlyScheduleCalculator CALCULATOR = new EndOfMonthQuarterlyScheduleCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart1() {
    CALCULATOR.getSchedule(null, LocalDate.of(2011, 1, 1), true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart2() {
    CALCULATOR.getSchedule(null, DateUtils.getUTCDate(2010, 1, 1), true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd1() {
    CALCULATOR.getSchedule(LocalDate.of(2000, 1, 1), null, true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd2() {
    CALCULATOR.getSchedule(DateUtils.getUTCDate(2000, 1, 1), null, true, true);
  }

  @Test
  public void testShortSeries() {
    final LocalDate start1 = LocalDate.of(2010, 6, 30);
    final LocalDate end1 = LocalDate.of(2010, 8, 31);
    LocalDate[] schedule1 = CALCULATOR.getSchedule(start1, end1, true);
    assertEquals(schedule1.length, 1);
    assertEquals(schedule1[0], end1);
    schedule1 = CALCULATOR.getSchedule(start1, end1, false);
    assertEquals(schedule1.length, 1);
    assertEquals(schedule1[0], start1);
    final ZonedDateTime start2 = DateUtils.getUTCDate(2010, 6, 1);
    final ZonedDateTime end2 = DateUtils.getUTCDate(2010, 8, 31);
    ZonedDateTime[] schedule2 = CALCULATOR.getSchedule(start2, end2, true);
    assertEquals(schedule2.length, 1);
    assertEquals(schedule2[0], end2);
    schedule2 = CALCULATOR.getSchedule(start2, end2, false);
    assertEquals(schedule2.length, 1);
    assertEquals(schedule2[0], DateUtils.getUTCDate(2010, 6, 30));
  }

  @Test
  public void test1() {
    LocalDate start1 = LocalDate.of(1994, 8, 31);
    LocalDate end1 = LocalDate.of(1998, 8, 31);
    final LocalDate[] expected = new LocalDate[] {LocalDate.of(1994, 8, 31), LocalDate.of(1994, 11, 30), LocalDate.of(1995, 2, 28), LocalDate.of(1995, 5, 31), LocalDate.of(1995, 8, 31),
        LocalDate.of(1995, 11, 30), LocalDate.of(1996, 2, 29), LocalDate.of(1996, 5, 31), LocalDate.of(1996, 8, 31), LocalDate.of(1996, 11, 30), LocalDate.of(1997, 2, 28), LocalDate.of(1997, 5, 31),
        LocalDate.of(1997, 8, 31), LocalDate.of(1997, 11, 30), LocalDate.of(1998, 2, 28), LocalDate.of(1998, 5, 31), LocalDate.of(1998, 8, 31)};
    LocalDate[] result = CALCULATOR.getSchedule(start1, end1, true);
    assertArrayEquals(expected, result);
    result = CALCULATOR.getSchedule(start1, end1, false);
    assertArrayEquals(expected, result);
    start1 = LocalDate.of(1994, 6, 30);
    end1 = LocalDate.of(1998, 8, 31);
    final LocalDate[] backward = new LocalDate[] {LocalDate.of(1994, 8, 31), LocalDate.of(1994, 11, 30), LocalDate.of(1995, 2, 28), LocalDate.of(1995, 5, 31), LocalDate.of(1995, 8, 31),
        LocalDate.of(1995, 11, 30), LocalDate.of(1996, 2, 29), LocalDate.of(1996, 5, 31), LocalDate.of(1996, 8, 31), LocalDate.of(1996, 11, 30), LocalDate.of(1997, 2, 28), LocalDate.of(1997, 5, 31),
        LocalDate.of(1997, 8, 31), LocalDate.of(1997, 11, 30), LocalDate.of(1998, 2, 28), LocalDate.of(1998, 5, 31), LocalDate.of(1998, 8, 31)};
    result = CALCULATOR.getSchedule(start1, end1, true);
    assertArrayEquals(backward, result);
    final LocalDate[] forward = new LocalDate[] {LocalDate.of(1994, 6, 30), LocalDate.of(1994, 9, 30), LocalDate.of(1994, 12, 31), LocalDate.of(1995, 3, 31), LocalDate.of(1995, 6, 30),
        LocalDate.of(1995, 9, 30), LocalDate.of(1995, 12, 31), LocalDate.of(1996, 3, 31), LocalDate.of(1996, 6, 30), LocalDate.of(1996, 9, 30), LocalDate.of(1996, 12, 31), LocalDate.of(1997, 3, 31),
        LocalDate.of(1997, 6, 30), LocalDate.of(1997, 9, 30), LocalDate.of(1997, 12, 31), LocalDate.of(1998, 3, 31), LocalDate.of(1998, 6, 30)};
    result = CALCULATOR.getSchedule(start1, end1, false);
    assertArrayEquals(forward, result);
  }

  @Test
  public void test2() {
    ZonedDateTime start1 = DateUtils.getUTCDate(1994, 8, 31);
    ZonedDateTime end1 = DateUtils.getUTCDate(1998, 8, 31);
    final ZonedDateTime[] expected = new ZonedDateTime[] {DateUtils.getUTCDate(1994, 8, 31), DateUtils.getUTCDate(1994, 11, 30), DateUtils.getUTCDate(1995, 2, 28), DateUtils.getUTCDate(1995, 5, 31),
        DateUtils.getUTCDate(1995, 8, 31), DateUtils.getUTCDate(1995, 11, 30), DateUtils.getUTCDate(1996, 2, 29), DateUtils.getUTCDate(1996, 5, 31), DateUtils.getUTCDate(1996, 8, 31),
        DateUtils.getUTCDate(1996, 11, 30), DateUtils.getUTCDate(1997, 2, 28), DateUtils.getUTCDate(1997, 5, 31), DateUtils.getUTCDate(1997, 8, 31), DateUtils.getUTCDate(1997, 11, 30),
        DateUtils.getUTCDate(1998, 2, 28), DateUtils.getUTCDate(1998, 5, 31), DateUtils.getUTCDate(1998, 8, 31)};
    ZonedDateTime[] result = CALCULATOR.getSchedule(start1, end1, true);
    assertArrayEquals(expected, result);
    result = CALCULATOR.getSchedule(start1, end1, false);
    assertArrayEquals(expected, result);
    start1 = DateUtils.getUTCDate(1994, 6, 30);
    end1 = DateUtils.getUTCDate(1998, 8, 31);
    final ZonedDateTime[] backward = new ZonedDateTime[] {DateUtils.getUTCDate(1994, 8, 31), DateUtils.getUTCDate(1994, 11, 30), DateUtils.getUTCDate(1995, 2, 28), DateUtils.getUTCDate(1995, 5, 31),
        DateUtils.getUTCDate(1995, 8, 31), DateUtils.getUTCDate(1995, 11, 30), DateUtils.getUTCDate(1996, 2, 29), DateUtils.getUTCDate(1996, 5, 31), DateUtils.getUTCDate(1996, 8, 31),
        DateUtils.getUTCDate(1996, 11, 30), DateUtils.getUTCDate(1997, 2, 28), DateUtils.getUTCDate(1997, 5, 31), DateUtils.getUTCDate(1997, 8, 31), DateUtils.getUTCDate(1997, 11, 30),
        DateUtils.getUTCDate(1998, 2, 28), DateUtils.getUTCDate(1998, 5, 31), DateUtils.getUTCDate(1998, 8, 31)};
    result = CALCULATOR.getSchedule(start1, end1, true);
    assertArrayEquals(backward, result);
    final ZonedDateTime[] forward = new ZonedDateTime[] {DateUtils.getUTCDate(1994, 6, 30), DateUtils.getUTCDate(1994, 9, 30), DateUtils.getUTCDate(1994, 12, 31), DateUtils.getUTCDate(1995, 3, 31),
        DateUtils.getUTCDate(1995, 6, 30), DateUtils.getUTCDate(1995, 9, 30), DateUtils.getUTCDate(1995, 12, 31), DateUtils.getUTCDate(1996, 3, 31), DateUtils.getUTCDate(1996, 6, 30),
        DateUtils.getUTCDate(1996, 9, 30), DateUtils.getUTCDate(1996, 12, 31), DateUtils.getUTCDate(1997, 3, 31), DateUtils.getUTCDate(1997, 6, 30), DateUtils.getUTCDate(1997, 9, 30),
        DateUtils.getUTCDate(1997, 12, 31), DateUtils.getUTCDate(1998, 3, 31), DateUtils.getUTCDate(1998, 6, 30)};
    result = CALCULATOR.getSchedule(start1, end1, false);
    assertArrayEquals(forward, result);
  }
}
