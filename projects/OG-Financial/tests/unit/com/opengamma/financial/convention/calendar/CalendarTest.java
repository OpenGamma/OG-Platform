/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.core.convention.Calendar;

/**
 * Test Calendar.
 */
public class CalendarTest {

  @Test
  public void testUKBankHolidays() {
    final Calendar cal = CalendarFactory.INSTANCE.getCalendar("UK Bank Holidays");
    assertNotNull(cal);
    assertEquals("UK Bank Holidays", cal.getConventionName());
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 1, 1))); // Friday (BH)
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 1, 2))); // Sat
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 1, 3))); // Sun
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 1, 4))); // Mon
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 4, 1))); // Thursday
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 4, 2))); // Friday (BH)
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 4, 3))); // Sat
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 4, 4))); // Sun
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 4, 5))); // Monday (BH)
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 4, 6))); // Tue
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 5, 2))); // Sun
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 5, 3))); // Monday (BH)
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 5, 4))); // Tue
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 5, 30))); // Sunday
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 5, 31))); // Monday (BH)
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 6, 1))); // Tuesday
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 8, 29))); // Sunday
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 8, 30))); // Monday (BH)
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 8, 31))); // Tuesday
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 12, 24))); // Friday
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 12, 25))); // Sat
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 12, 26))); // Sun
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 12, 27))); // Monday (BH)
    assertFalse(cal.isWorkingDay(LocalDate.of(2010, 12, 28))); // Tuesday (BH)
    assertTrue(cal.isWorkingDay(LocalDate.of(2010, 12, 29))); // Wed
  }

}
