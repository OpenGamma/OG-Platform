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
public class LiveCattleFutureOptionExpiryCalculatorTest {

  private static final LiveCattleFutureOptionExpiryCalculator EXPIRY_CALC = LiveCattleFutureOptionExpiryCalculator.getInstance();

  static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("weekday");

  private static final LocalDate DATE = LocalDate.of(2013, 2, 1);

  @Test
  public void getExpiryDate() {
    assertEquals(LocalDate.of(2013, 3, 1), EXPIRY_CALC.getExpiryDate(1, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 4, 5), EXPIRY_CALC.getExpiryDate(2, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 6, 7), EXPIRY_CALC.getExpiryDate(3, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 8, 2), EXPIRY_CALC.getExpiryDate(4, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 10, 4), EXPIRY_CALC.getExpiryDate(5, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 12, 6), EXPIRY_CALC.getExpiryDate(6, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2014, 2, 7), EXPIRY_CALC.getExpiryDate(7, DATE, WEEKEND_CALENDAR));
  }

}
