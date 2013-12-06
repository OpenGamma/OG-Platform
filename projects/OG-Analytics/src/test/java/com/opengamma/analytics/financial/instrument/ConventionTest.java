/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConventionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String NAME = "CONVENTION";
  private static final Convention CONVENTION = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSettlementDays() {
    new Convention(-SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new Convention(SETTLEMENT_DAYS, null, BUSINESS_DAY, CALENDAR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    new Convention(SETTLEMENT_DAYS, DAY_COUNT, null, CALENDAR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, null);
  }

  @Test
  public void testGetters() {
    assertEquals(CONVENTION.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(CONVENTION.getDayCount(), DAY_COUNT);
    assertEquals(CONVENTION.getName(), NAME);
    assertEquals(CONVENTION.getSettlementDays(), SETTLEMENT_DAYS);
    assertEquals(CONVENTION.getWorkingDayCalendar(), CALENDAR);
  }

  @Test
  public void testHashCodeAndEquals() {
    Convention other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    other = new Convention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DayCounts.ACT_365, BUSINESS_DAY, CALENDAR, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventions.NONE, CALENDAR, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, new Calendar() {

      @Override
      public boolean isWorkingDay(final LocalDate date) {
        return false;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public String getConventionName() {
        return null;
      }

    }, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME + ")");
    assertFalse(CONVENTION.equals(other));
  }

}
