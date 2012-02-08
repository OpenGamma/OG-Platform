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

public class CMSIndexTest {
  //Libor3m
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_IBOR = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, IS_EOM);
  //CMS index: CMS2YUSD3M6M - Semi-bond/Quarterly-money
  private static final Period FIXED_LEG_PERIOD = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final Period CMS_TENOR = Period.ofYears(2);
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, CMS_TENOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLegPeriod() {
    new IndexSwap(null, DAY_COUNT_FIXED, IBOR_INDEX, CMS_TENOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedDayCount() {
    new IndexSwap(FIXED_LEG_PERIOD, null, IBOR_INDEX, CMS_TENOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborIndex() {
    new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, null, CMS_TENOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCMSTenor() {
    new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, null);
  }

  @Test
  public void testGetter() {
    assertEquals(CMS_INDEX.getCurrency(), CUR);
    assertEquals(CMS_INDEX.getFixedLegDayCount(), DAY_COUNT_FIXED);
    assertEquals(CMS_INDEX.getFixedLegPeriod(), FIXED_LEG_PERIOD);
    assertEquals(CMS_INDEX.getIborIndex(), IBOR_INDEX);
    assertEquals(CMS_INDEX.getTenor(), CMS_TENOR);
    GeneratorSwap generator = new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX);
    String name = CMS_TENOR.toString() + generator.getName();
    assertEquals(name, CMS_INDEX.getName());
    assertEquals(CMS_INDEX.toString(), CMS_INDEX.getName());
  }

  @Test
  public void testEqualHash() {
    assertEquals(CMS_INDEX, CMS_INDEX);
    IndexSwap indexDuplicate = new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, CMS_TENOR);
    assertEquals(CMS_INDEX, indexDuplicate);
    assertEquals(CMS_INDEX.hashCode(), indexDuplicate.hashCode());
    IndexSwap indexModified;
    Period otherPeriod = Period.ofMonths(12);
    indexModified = new IndexSwap(otherPeriod, DAY_COUNT_FIXED, IBOR_INDEX, CMS_TENOR);
    assertFalse(CMS_INDEX.equals(indexModified));
    indexModified = new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, otherPeriod);
    assertFalse(CMS_INDEX.equals(indexModified));
    indexModified = new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_IBOR, IBOR_INDEX, CMS_TENOR);
    assertFalse(CMS_INDEX.equals(indexModified));
    IborIndex otherIborIndex = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, !IS_EOM);
    indexModified = new IndexSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, otherIborIndex, CMS_TENOR);
    assertFalse(CMS_INDEX.equals(indexModified));
    assertFalse(CMS_INDEX.equals(null));
    assertFalse(CMS_INDEX.equals(CUR));
  }

}
