/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.ExceptionCalendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BusinessTwoFiveTwoTest {
  private static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime D2 = DateUtils.getUTCDate(2010, 4, 1);
  private static final ZonedDateTime D3 = DateUtils.getUTCDate(2010, 7, 1);
  private static final LocalDate D4 = LocalDate.of(2010, 1, 1);
  private static final LocalDate D5 = LocalDate.of(2010, 4, 1);
  private static final LocalDate D6 = LocalDate.of(2010, 7, 1);
  private static final double COUPON = 0.01;
  private static final int PAYMENTS = 4;
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final Calendar HOLIDAY_CALENDAR = new MyCalendar("Holiday");
  private static final BusinessTwoFiveTwo DC = new BusinessTwoFiveTwo();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate1() {
    DC.getDayCountFraction(null, D2, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate1() {
    DC.getDayCountFraction(D1, null, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder1() {
    DC.getDayCountFraction(D2, D1, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate2() {
    DC.getDayCountFraction(null, D5, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate2() {
    DC.getDayCountFraction(D4, null, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongOrder2() {
    DC.getDayCountFraction(D5, D4, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoCalendar1() {
    DC.getDayCountFraction(D4, D5);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoCalendar2() {
    DC.getDayCountFraction(D1, D3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar1() {
    DC.getDayCountFraction(D4, D5, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar2() {
    DC.getDayCountFraction(D1, D2, null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testAccruedInterest() {
    DC.getAccruedInterest(D4, D5, D6, COUPON, PAYMENTS);
  }

  @Test
  public void test() {
    assertEquals(DC.getName(), "Business/252");
    final LocalDate d1 = LocalDate.of(2012, 7, 16);
    final LocalDate d2 = LocalDate.of(2012, 7, 17);
    final LocalDate d3 = LocalDate.of(2012, 7, 23);
    final LocalDate d4 = LocalDate.of(2012, 7, 31);
    final LocalDate d5 = LocalDate.of(2012, 7, 29);
    final LocalDate d6 = LocalDate.of(2012, 7, 14);
    final LocalDate d7 = LocalDate.of(2012, 7, 26);
    assertEquals(1. / 252, DC.getDayCountFraction(d1, d2, WEEKEND_CALENDAR), 0);
    assertEquals(1. / 252, DC.getDayCountFraction(d1, d2, HOLIDAY_CALENDAR), 0);
    assertEquals(5. / 252, DC.getDayCountFraction(d1, d3, WEEKEND_CALENDAR), 0);
    assertEquals(4. / 252, DC.getDayCountFraction(d1, d3, HOLIDAY_CALENDAR), 0);
    assertEquals(11. / 252, DC.getDayCountFraction(d1, d4, WEEKEND_CALENDAR), 0);
    assertEquals(9. / 252, DC.getDayCountFraction(d1, d4, HOLIDAY_CALENDAR), 0);
    assertEquals(9. / 252, DC.getDayCountFraction(d1, d5, WEEKEND_CALENDAR), 0);
    assertEquals(7. / 252, DC.getDayCountFraction(d1, d5, HOLIDAY_CALENDAR), 0);
    assertEquals(1. / 252, DC.getDayCountFraction(d6, d2, WEEKEND_CALENDAR), 0);
    assertEquals(1. / 252, DC.getDayCountFraction(d6, d2, HOLIDAY_CALENDAR), 0);
    assertEquals(5. / 252, DC.getDayCountFraction(d6, d3, WEEKEND_CALENDAR), 0);
    assertEquals(4. / 252, DC.getDayCountFraction(d6, d3, HOLIDAY_CALENDAR), 0);
    assertEquals(11. / 252, DC.getDayCountFraction(d6, d4, WEEKEND_CALENDAR), 0);
    assertEquals(9. / 252, DC.getDayCountFraction(d6, d4, HOLIDAY_CALENDAR), 0);
    assertEquals(9. / 252, DC.getDayCountFraction(d6, d5, WEEKEND_CALENDAR), 0);
    assertEquals(7. / 252, DC.getDayCountFraction(d6, d5, HOLIDAY_CALENDAR), 0);
    assertEquals(8. / 252, DC.getDayCountFraction(d1, d7, WEEKEND_CALENDAR), 0);
    assertEquals(6. / 252, DC.getDayCountFraction(d1, d7, HOLIDAY_CALENDAR), 0);
  }

  private static class MyCalendar extends ExceptionCalendar {
    private static final long serialVersionUID = 1L;
    private static final LocalDate[] HOLIDAYS = new LocalDate[]{LocalDate.of(2012, 7, 19), LocalDate.of(2012, 7, 26)};

    protected MyCalendar(final String name) {
      super(name);
    }

    @Override
    protected boolean isNormallyWorkingDay(final LocalDate date) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        return false;
      }
      for (final LocalDate holiday : HOLIDAYS) {
        if (date.equals(holiday)) {
          return false;
        }
      }
      return true;
    }

  }
}
