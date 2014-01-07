/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.test.TestGroup;

/**
 * Tests SimpleHoliday.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleHolidayTest {

  private static LocalDate DATE1 = LocalDate.of(2013, 6, 1);
  private static LocalDate DATE2 = LocalDate.of(2013, 6, 2);
  private static LocalDate DATE3 = LocalDate.of(2013, 6, 3);
  private static LocalDate DATE4 = LocalDate.of(2013, 6, 4);
  private static LocalDate DATE5 = LocalDate.of(2013, 6, 5);

  @Test
  public void test_addHolidayDate() {
    final SimpleHoliday holidays = new SimpleHoliday();
    holidays.addHolidayDate(DATE3);
    holidays.addHolidayDate(DATE2);
    holidays.addHolidayDate(DATE4);
    holidays.addHolidayDate(DATE2);
    holidays.addHolidayDate(DATE1);
    
    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE1, holidays.getHolidayDates().get(0));
    assertEquals(DATE2, holidays.getHolidayDates().get(1));
    assertEquals(DATE3, holidays.getHolidayDates().get(2));
    assertEquals(DATE4, holidays.getHolidayDates().get(3));
  }

  @Test
  public void test_addHolidayDates_Iterable() {
    final SimpleHoliday holidays = new SimpleHoliday();
    holidays.addHolidayDate(DATE3);
    holidays.addHolidayDates(ImmutableList.of(DATE4, DATE3, DATE2, DATE5));
    
    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE2, holidays.getHolidayDates().get(0));
    assertEquals(DATE3, holidays.getHolidayDates().get(1));
    assertEquals(DATE4, holidays.getHolidayDates().get(2));
    assertEquals(DATE5, holidays.getHolidayDates().get(3));
  }

  @Test
  public void test_constructor_Iterable() {
    final SimpleHoliday holidays = new SimpleHoliday(ImmutableList.of(DATE4, DATE3, DATE2, DATE3, DATE5));
    
    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE2, holidays.getHolidayDates().get(0));
    assertEquals(DATE3, holidays.getHolidayDates().get(1));
    assertEquals(DATE4, holidays.getHolidayDates().get(2));
    assertEquals(DATE5, holidays.getHolidayDates().get(3));
  }

}
