/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.bond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;

/**
 * 
 */
public class BondConventionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final int EX_DIVIDEND_DAYS = 0;
  private static final boolean IS_EOM = true;
  private static final String NAME = "CONVENTION";
  private static final YieldConvention YIELD_CONVENTION = SimpleYieldConvention.US_TREASURY_EQUIVALANT;
  private static final BondConvention CONVENTION = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSettlementDays() {
    new BondConvention(-SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDayCount() {
    new BondConvention(SETTLEMENT_DAYS, null, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, null, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalendar() {
    new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, null, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeExDividendDays() {
    new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, -1, YIELD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, null, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYieldConvention() {
    new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, null);
  }

  @Test
  public void testGetters() {
    assertEquals(CONVENTION.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(CONVENTION.getDayCount(), DAY_COUNT);
    assertEquals(CONVENTION.getExDividendDays(), EX_DIVIDEND_DAYS);
    assertEquals(CONVENTION.getName(), NAME);
    assertEquals(CONVENTION.getSettlementDays(), SETTLEMENT_DAYS);
    assertEquals(CONVENTION.getWorkingDayCalendar(), CALENDAR);
    assertEquals(CONVENTION.getYieldConvention(), YIELD_CONVENTION);
  }

  @Test
  public void testHashCodeAndEquals() {
    BondConvention other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    other = new BondConvention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("None"), CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, new Calendar() {

      @Override
      public boolean isWorkingDay(final LocalDate date) {
        return false;
      }

      @Override
      public String getConventionName() {
        return null;
      }

    }, IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS + 1, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, !IS_EOM, NAME, EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME + ")", EX_DIVIDEND_DAYS, YIELD_CONVENTION);
    assertFalse(CONVENTION.equals(other));
    other = new BondConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME, EX_DIVIDEND_DAYS, SimpleYieldConvention.JGB_SIMPLE);
    assertFalse(CONVENTION.equals(other));
  }
}
