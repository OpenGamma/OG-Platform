/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.Period;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class IborIndexTest {
  //Libor3m
  private static final Tenor TENOR = new Tenor(Period.ofMonths(3));
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTenor() {
    new IborIndex(null, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalendar() {
    new IborIndex(TENOR, SETTLEMENT_DAYS, null, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDayCount() {
    new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, null, BUSINESS_DAY, IS_EOM);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, null, IS_EOM);
  }

  @Test
  public void test() {
    assertEquals(INDEX.getTenor(), TENOR);
    assertEquals(INDEX.getSettlementDays(), SETTLEMENT_DAYS);
    assertEquals(INDEX.getCalendar(), CALENDAR);
    assertEquals(INDEX.getDayCount(), DAY_COUNT);
    assertEquals(INDEX.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(INDEX.isEndOfMonth(), IS_EOM);
  }

  @Test
  public void testEqualHash() {
    IborIndex indexDuplicate = new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertEquals(INDEX, indexDuplicate);
    assertEquals(INDEX.hashCode(), indexDuplicate.hashCode());
    IborIndex indexModified = new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, !IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    Tenor tenorModified = new Tenor(Period.ofMonths(2));
    indexModified = new IborIndex(tenorModified, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(TENOR, SETTLEMENT_DAYS + 1, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), IS_EOM);
    assertFalse(INDEX.equals(indexModified));
  }

}
