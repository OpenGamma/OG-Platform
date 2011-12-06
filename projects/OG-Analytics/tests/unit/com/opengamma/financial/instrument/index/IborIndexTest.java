/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class IborIndexTest {
  //Libor3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final String NAME = "USD LIBOR 3M";
  private static final IborIndex INDEX2 = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new IborIndex(null, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTenor() {
    new IborIndex(CUR, null, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, null, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, null, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, null, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testName() {
    new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, null);
  }

  @Test
  public void getter() {
    assertEquals(INDEX.getCurrency(), CUR);
    assertEquals(INDEX.getTenor(), TENOR);
    assertEquals(INDEX.getSpotLag(), SETTLEMENT_DAYS);
    assertEquals(INDEX.getCalendar(), CALENDAR);
    assertEquals(INDEX.getDayCount(), DAY_COUNT);
    assertEquals(INDEX.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(INDEX.isEndOfMonth(), IS_EOM);
    String name = "Ibor";
    assertEquals(name, INDEX.getName());
    assertEquals(name, INDEX.toString());
    assertEquals(NAME, INDEX2.getName());
  }

  @Test
  public void testEqualHash() {
    assertEquals(INDEX, INDEX);
    IborIndex indexDuplicate = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertEquals(INDEX, indexDuplicate);
    assertEquals(INDEX.hashCode(), indexDuplicate.hashCode());
    IborIndex indexNoEOM1 = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, !IS_EOM);
    IborIndex indexNoEOM2 = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, !IS_EOM);
    assertEquals(indexNoEOM1.hashCode(), indexNoEOM2.hashCode());
    Currency currencyModified = Currency.EUR;
    IborIndex indexModified = new IborIndex(currencyModified, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, !IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    Period tenorModified = Period.ofMonths(2);
    indexModified = new IborIndex(CUR, tenorModified, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS + 1, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    indexModified = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    Currency otherCurrency = Currency.EUR;
    indexModified = new IborIndex(otherCurrency, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    Period otherTenor = Period.ofMonths(6);
    indexModified = new IborIndex(CUR, otherTenor, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse(INDEX.equals(indexModified));
    assertFalse(INDEX.equals(null));
    assertFalse(INDEX.equals(CUR));
  }

}
