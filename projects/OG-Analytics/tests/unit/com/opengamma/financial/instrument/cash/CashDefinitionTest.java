/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class CashDefinitionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String NAME = "CONVENTION";
  private static final Convention CONVENTION = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2011, 1, 25);
  private static final ZonedDateTime MATURITY = DateUtil.getUTCDate(2011, 7, 25);
  private static final double RATE = 0.05;
  private static final CashDefinition CASH = new CashDefinition(MATURITY, RATE, CONVENTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    new CashDefinition(null, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new CashDefinition(MATURITY, RATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CASH.toDerivative(null, new String[] {"A"});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveNames() {
    CASH.toDerivative(DATE, (String[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCurveNames() {
    CASH.toDerivative(DATE, new String[0]);
  }

  @Test
  public void test() {
    assertEquals(MATURITY, CASH.getMaturity());
    assertEquals(RATE, CASH.getRate(), 0);
    assertEquals(CONVENTION, CASH.getConvention());
    CashDefinition other = new CashDefinition(MATURITY, RATE, CONVENTION);
    assertEquals(other, CASH);
    assertEquals(other.hashCode(), CASH.hashCode());
    other = new CashDefinition(MATURITY.plusDays(1), RATE, CONVENTION);
    assertFalse(other.equals(CASH));
    other = new CashDefinition(MATURITY, RATE + 0.01, CONVENTION);
    assertFalse(other.equals(CASH));
    other = new CashDefinition(MATURITY, RATE, new Convention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME));
    assertFalse(other.equals(CASH));
  }

  @Test
  public void testConversion() {
    final String name = "YC";
    Cash cash = CASH.toDerivative(DATE, name);
    assertEquals(cash.getMaturity(), 181. / 365, 0);
    assertEquals(cash.getRate(), RATE, 0);
    assertEquals(cash.getTradeTime(), 2. / 365, 0);
    assertEquals(cash.getYearFraction(), 179. / 360, 0);
    assertEquals(cash.getYieldCurveName(), name);
    cash = CASH.toDerivative(DATE, name, name, name);
    assertEquals(cash.getMaturity(), 181. / 365, 0);
    assertEquals(cash.getRate(), RATE, 0);
    assertEquals(cash.getTradeTime(), 2. / 365, 0);
    assertEquals(cash.getYearFraction(), 179. / 360, 0);
    assertEquals(cash.getYieldCurveName(), name);
  }
}
