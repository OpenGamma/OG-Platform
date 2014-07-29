/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the expiry adjuster that moves a date to a number of working days from the last working
 * day of the month.
 */
@Test(groups = TestGroup.UNIT)
public class DaysFromEndOfMonthExpiryAdjusterTest {
  /** The calendar */
  private static final Calendar CALENDAR = new MondayToFridayCalendar("Weekend");

  /**
   * Tests adjuster for various dates when the number of days to adjust from the end of month is one.
   */
  @Test
  public void testOneDayAdjuster() {
    final DaysFromEndOfMonthExpiryAdjuster adjuster = new DaysFromEndOfMonthExpiryAdjuster(1);
    LocalDate date = LocalDate.of(2014, 2, 14);
    LocalDate expected = LocalDate.of(2014, 2, 27);
    assertEquals(expected, adjuster.getExpiryDate(1, date, CALENDAR));
    expected = LocalDate.of(2014, 3, 28);
    assertEquals(expected, adjuster.getExpiryDate(2, date, CALENDAR));
    expected = LocalDate.of(2014, 4, 29);
    assertEquals(expected, adjuster.getExpiryDate(3, date, CALENDAR));
    expected = LocalDate.of(2014, 6, 27);
    assertEquals(expected, adjuster.getExpiryDate(5, date, CALENDAR));
    date = LocalDate.of(2014, 1, 31);
    expected = LocalDate.of(2014, 2, 27);
    assertEquals(expected, adjuster.getExpiryDate(1, date, CALENDAR));
    expected = LocalDate.of(2014, 3, 28);
    assertEquals(expected, adjuster.getExpiryDate(2, date, CALENDAR));
    expected = LocalDate.of(2014, 4, 29);
    assertEquals(expected, adjuster.getExpiryDate(3, date, CALENDAR));
    expected = LocalDate.of(2014, 6, 27);
    assertEquals(expected, adjuster.getExpiryDate(5, date, CALENDAR));
  }

  /**
   * Tests adjuster for various dates when the number of days to adjust from the end of month is five;
   * this will hit holidays.
   */
  @Test
  public void testFiveDayAdjuster() {
    final DaysFromEndOfMonthExpiryAdjuster adjuster = new DaysFromEndOfMonthExpiryAdjuster(5);
    LocalDate date = LocalDate.of(2014, 2, 14);
    LocalDate expected = LocalDate.of(2014, 2, 21);
    assertEquals(expected, adjuster.getExpiryDate(1, date, CALENDAR));
    expected = LocalDate.of(2014, 3, 24);
    assertEquals(expected, adjuster.getExpiryDate(2, date, CALENDAR));
    expected = LocalDate.of(2014, 4, 23);
    assertEquals(expected, adjuster.getExpiryDate(3, date, CALENDAR));
    expected = LocalDate.of(2014, 6, 23);
    assertEquals(expected, adjuster.getExpiryDate(5, date, CALENDAR));
    date = LocalDate.of(2014, 1, 31);
    expected = LocalDate.of(2014, 2, 21);
    assertEquals(expected, adjuster.getExpiryDate(1, date, CALENDAR));
    expected = LocalDate.of(2014, 3, 24);
    assertEquals(expected, adjuster.getExpiryDate(2, date, CALENDAR));
    expected = LocalDate.of(2014, 4, 23);
    assertEquals(expected, adjuster.getExpiryDate(3, date, CALENDAR));
    expected = LocalDate.of(2014, 6, 23);
    assertEquals(expected, adjuster.getExpiryDate(5, date, CALENDAR));
  }
}
