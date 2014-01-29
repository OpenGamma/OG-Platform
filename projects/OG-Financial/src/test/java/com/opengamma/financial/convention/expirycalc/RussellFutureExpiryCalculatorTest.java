/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RussellFutureExpiryCalculatorTest {
  
  private static final RussellFutureExpiryCalculator CALCULATOR = RussellFutureExpiryCalculator.getInstance();
  static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("a");
  private static final Calendar MY_CALENDAR = new MyCalendar();
  private static final LocalDate AUGUST = LocalDate.of(2012, 8, 1);
  private static final LocalDate SEPTEMBER_START = LocalDate.of(2012, 9, 1);
  private static final LocalDate SEPTEMBER_EXPIRY = LocalDate.of(2012, 9, 21);  // TODO - Add this to tests
  private static final LocalDate SEPTEMBER_END = LocalDate.of(2012, 9, 29);
  private static final LocalDate OCTOBER = LocalDate.of(2012, 10, 1);

  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeN() {
    CALCULATOR.getExpiryDate(-1, AUGUST, MY_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroN() {
    CALCULATOR.getExpiryDate(0, AUGUST, MY_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CALCULATOR.getExpiryDate(1, null, MY_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    CALCULATOR.getExpiryDate(2, AUGUST, null);
  }
  
  @Test
  public void testCases() {
    assertEquals(LocalDate.of(2012, 9, 21), CALCULATOR.getExpiryDate(1, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 9, 21), CALCULATOR.getExpiryDate(1, SEPTEMBER_EXPIRY, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(1, SEPTEMBER_EXPIRY, MY_CALENDAR)); // 9/21 is a holiday in CALENDAR
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(1, SEPTEMBER_END, MY_CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(2, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 3, 14), CALCULATOR.getExpiryDate(2, SEPTEMBER_END, MY_CALENDAR)); // 3/14 is a holiday in CALENDAR
    assertEquals(LocalDate.of(2013, 3, 15), CALCULATOR.getExpiryDate(3, SEPTEMBER_EXPIRY, WEEKEND_CALENDAR));    
    assertEquals(LocalDate.of(2013, 6, 21), CALCULATOR.getExpiryDate(3, SEPTEMBER_EXPIRY, MY_CALENDAR)); 
    assertEquals(LocalDate.of(2013, 12, 20), CALCULATOR.getExpiryDate(6, SEPTEMBER_START, MY_CALENDAR));
    assertEquals(LocalDate.of(2014, 3, 21), CALCULATOR.getExpiryDate(6, SEPTEMBER_END, MY_CALENDAR));

    assertEquals(LocalDate.of(2012, 9, 21), CALCULATOR.getExpiryDate(1, AUGUST, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 9, 20), CALCULATOR.getExpiryDate(1, AUGUST, MY_CALENDAR)); 
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(2, AUGUST, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 12, 20), CALCULATOR.getExpiryDate(6, AUGUST, MY_CALENDAR));
    
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(1, OCTOBER, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(1, OCTOBER, MY_CALENDAR));
    assertEquals(LocalDate.of(2013, 3, 15), CALCULATOR.getExpiryDate(2, OCTOBER, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 3, 14), CALCULATOR.getExpiryDate(2, OCTOBER, MY_CALENDAR));
    assertEquals(LocalDate.of(2014, 3, 21), CALCULATOR.getExpiryDate(6, OCTOBER, MY_CALENDAR));
  }
  
  private static class MyCalendar implements Calendar {
    private static final LocalDate HOLIDAY1 = LocalDate.of(2012, 9, 21);
    private static final LocalDate HOLIDAY2 = LocalDate.of(2012, 11, 23);
    private static final LocalDate HOLIDAY3 = LocalDate.of(2013, 2, 2);
    private static final LocalDate HOLIDAY4 = LocalDate.of(2013, 3, 15);
    
    public MyCalendar() {
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      if (date.equals(HOLIDAY1) || date.equals(HOLIDAY2) || date.equals(HOLIDAY3) || date.equals(HOLIDAY4)) {
        return false;
      }
      return WEEKEND_CALENDAR.isWorkingDay(date);
    }

    @Override
    public String getConventionName() {
      return null;
    }

    @Override
    public String getName() {
      return null;
    }

  }
  
}
