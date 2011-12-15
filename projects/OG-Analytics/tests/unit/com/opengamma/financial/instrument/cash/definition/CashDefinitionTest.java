/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash.definition;

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
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class CashDefinitionTest {
  private static final Currency CCY = Currency.USD;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String NAME = "CONVENTION";
  private static final Convention CONVENTION = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2011, 1, 25);
  private static final ZonedDateTime MATURITY = DateUtils.getUTCDate(2011, 7, 25);
  private static final double NOTIONAL = 1000;
  private static final double RATE = 0.05;
  private static final CashDefinition CASH = new CashDefinition(CCY, MATURITY, NOTIONAL, RATE, CONVENTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CashDefinition(null, MATURITY, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    new CashDefinition(CCY, null, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new CashDefinition(CCY, MATURITY, NOTIONAL, RATE, null);
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
    assertEquals(CCY, CASH.getCurrency());
    assertEquals(MATURITY, CASH.getMaturity());
    assertEquals(RATE, CASH.getRate(), 0);
    assertEquals(CONVENTION, CASH.getConvention());
    assertEquals(NOTIONAL, CASH.getNotional());
    CashDefinition other = new CashDefinition(CCY, MATURITY, NOTIONAL, RATE, CONVENTION);
    assertEquals(other, CASH);
    assertEquals(other.hashCode(), CASH.hashCode());
    other = new CashDefinition(Currency.CAD, MATURITY, NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(CASH));
    other = new CashDefinition(CCY, MATURITY.plusDays(1), NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(CASH));
    other = new CashDefinition(CCY, MATURITY, NOTIONAL + 100, RATE, CONVENTION);
    assertFalse(other.equals(CASH));
    other = new CashDefinition(CCY, MATURITY, NOTIONAL, RATE + 0.01, CONVENTION);
    assertFalse(other.equals(CASH));
    other = new CashDefinition(CCY, MATURITY, NOTIONAL, RATE, new Convention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME));
    assertFalse(other.equals(CASH));
  }

  @Test
  public void testConversion() {
    final String name = "YC";
    Cash cash = CASH.toDerivative(DATE, name);
    assertEquals(cash.getCurrency(), CCY);
    assertEquals(cash.getEndTime(), 181. / 365, 0);
    assertEquals(cash.getRate(), RATE, 0);
    assertEquals(cash.getStartTime(), 2. / 365, 0);
    assertEquals(cash.getAccrualFactor(), 179. / 360, 0);
    assertEquals(cash.getYieldCurveName(), name);
    cash = CASH.toDerivative(DATE, name, name, name);
    assertEquals(cash.getCurrency(), CCY);
    assertEquals(cash.getEndTime(), 181. / 365, 0);
    assertEquals(cash.getRate(), RATE, 0);
    assertEquals(cash.getStartTime(), 2. / 365, 0);
    assertEquals(cash.getAccrualFactor(), 179. / 360, 0);
    assertEquals(cash.getYieldCurveName(), name);
  }
}
