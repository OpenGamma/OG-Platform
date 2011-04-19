/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.future.IRFutureConvention;
import com.opengamma.financial.schedule.NoHolidayCalendar;

/**
 * 
 */
public class IRFutureConventionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final double YEAR_FRACTION = 0.25;
  private static final String NAME = "CONVENTION";
  private static final IRFutureConvention CONVENTION = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSettlementDays() {
    new IRFutureConvention(-SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new IRFutureConvention(SETTLEMENT_DAYS, null, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, null, CALENDAR, YEAR_FRACTION, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, null, YEAR_FRACTION, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeYearFraction() {
    new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, -YEAR_FRACTION, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, null);
  }

  @Test
  public void test() {
    assertEquals(CONVENTION.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(CONVENTION.getDayCount(), DAY_COUNT);
    assertEquals(CONVENTION.getName(), NAME);
    assertEquals(CONVENTION.getSettlementDays(), SETTLEMENT_DAYS);
    assertEquals(CONVENTION.getWorkingDayCalendar(), CALENDAR);
    assertEquals(CONVENTION.getYearFraction(), YEAR_FRACTION, 0);
    IRFutureConvention other = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);
    assertEquals(other, CONVENTION);
    assertEquals(other.hashCode(), CONVENTION.hashCode());
    other = new IRFutureConvention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);
    assertFalse(other.equals(CONVENTION));
    other = new IRFutureConvention(SETTLEMENT_DAYS, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);
    assertFalse(other.equals(CONVENTION));
    other = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding"), CALENDAR, YEAR_FRACTION, NAME);
    assertFalse(other.equals(CONVENTION));
    other = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, new NoHolidayCalendar(), YEAR_FRACTION, NAME);
    assertFalse(other.equals(CONVENTION));
    other = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION + 1, NAME);
    assertFalse(other.equals(CONVENTION));
    other = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME + "e");
    assertFalse(other.equals(CONVENTION));
  }
}
