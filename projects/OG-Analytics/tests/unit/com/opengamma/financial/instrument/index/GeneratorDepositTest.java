/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.generator.USDDeposit;
import com.opengamma.util.money.Currency;

/**
 * Tests the constructor and method of GeneratorDeposit.
 */
public class GeneratorDepositTest {
  // USD deposits
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final GeneratorDeposit GENERATOR_DEPOSIT_USD = new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorDeposit(null, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new GeneratorDeposit(CUR, null, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, null, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, null, IS_EOM);
  }

  @Test
  public void getter() {
    assertEquals("Generator Deposit: getter", CUR, GENERATOR_DEPOSIT_USD.getCurrency());
    assertEquals("Generator Deposit: getter", CALENDAR, GENERATOR_DEPOSIT_USD.getCalendar());
    assertEquals("Generator Deposit: getter", SETTLEMENT_DAYS, GENERATOR_DEPOSIT_USD.getSpotLag());
    assertEquals("Generator Deposit: getter", DAY_COUNT, GENERATOR_DEPOSIT_USD.getDayCount());
    assertEquals("Generator Deposit: getter", BUSINESS_DAY, GENERATOR_DEPOSIT_USD.getBusinessDayConvention());
    assertEquals("Generator Deposit: getter", IS_EOM, GENERATOR_DEPOSIT_USD.isEndOfMonth());
  }

  @Test
  public void usdDeposit() {
    GeneratorDeposit preDefined = new USDDeposit(CALENDAR);
    assertEquals("Generator Deposit: getter", CUR, preDefined.getCurrency());
    assertEquals("Generator Deposit: getter", CALENDAR, preDefined.getCalendar());
    assertEquals("Generator Deposit: getter", SETTLEMENT_DAYS, preDefined.getSpotLag());
    assertEquals("Generator Deposit: getter", DAY_COUNT, preDefined.getDayCount());
    assertEquals("Generator Deposit: getter", BUSINESS_DAY, preDefined.getBusinessDayConvention());
    assertEquals("Generator Deposit: getter", IS_EOM, preDefined.isEndOfMonth());
  }

  @Test
  public void equalHash() {
    assertEquals(GENERATOR_DEPOSIT_USD, GENERATOR_DEPOSIT_USD);
    GeneratorDeposit duplicate = new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertEquals("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD, duplicate);
    assertEquals("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.hashCode(), duplicate.hashCode());
    GeneratorDeposit other;
    other = new GeneratorDeposit(Currency.EUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(CUR, new MondayToFridayCalendar("B"), SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(CUR, CALENDAR, 1, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, !IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
  }

}
