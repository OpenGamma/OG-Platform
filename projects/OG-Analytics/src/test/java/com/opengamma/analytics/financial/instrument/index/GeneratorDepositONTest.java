/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * Tests the constructor and method of GeneratorDepositON.
 */
public class GeneratorDepositONTest {
  // USD deposits
  private static final String NAME = "USD Deposit ON";
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final Currency CUR = Currency.USD;
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON(NAME, CUR, CALENDAR, DAY_COUNT);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorDepositON(NAME, null, CALENDAR, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new GeneratorDepositON(NAME, CUR, null, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorDepositON(NAME, CUR, CALENDAR, null);
  }

  @Test
  public void getter() {
    assertEquals("Generator Deposit: getter", NAME, GENERATOR_DEPOSIT_ON_USD.getName());
    assertEquals("Generator Deposit: getter", CUR, GENERATOR_DEPOSIT_ON_USD.getCurrency());
    assertEquals("Generator Deposit: getter", CALENDAR, GENERATOR_DEPOSIT_ON_USD.getCalendar());
    assertEquals("Generator Deposit: getter", DAY_COUNT, GENERATOR_DEPOSIT_ON_USD.getDayCount());
  }

  @Test
  public void equalHash() {
    assertEquals(GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD);
    GeneratorDepositON duplicate = new GeneratorDepositON(NAME, CUR, CALENDAR, DAY_COUNT);
    assertEquals("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_ON_USD, duplicate);
    assertEquals("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_ON_USD.hashCode(), duplicate.hashCode());
    GeneratorDepositON other;
    other = new GeneratorDepositON(NAME, Currency.EUR, CALENDAR, DAY_COUNT);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_ON_USD.equals(other));
    other = new GeneratorDepositON(NAME, CUR, new MondayToFridayCalendar("B"), DAY_COUNT);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_ON_USD.equals(other));
    other = new GeneratorDepositON(NAME, CUR, CALENDAR, DayCountFactory.INSTANCE.getDayCount("Actual/365"));
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_ON_USD.equals(other));
  }
}
