/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.generator.USDDeposit;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the constructor and method of GeneratorDeposit.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorDepositTest {
  // USD deposits
  private static final String NAME = "USD Deposit";
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final GeneratorDeposit GENERATOR_DEPOSIT_USD = new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorDeposit(NAME, null, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new GeneratorDeposit(NAME, CUR, null, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, null, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, null, IS_EOM);
  }

  @Test
  public void getter() {
    assertEquals("Generator Deposit: getter", NAME, GENERATOR_DEPOSIT_USD.getName());
    assertEquals("Generator Deposit: getter", CUR, GENERATOR_DEPOSIT_USD.getCurrency());
    assertEquals("Generator Deposit: getter", CALENDAR, GENERATOR_DEPOSIT_USD.getCalendar());
    assertEquals("Generator Deposit: getter", SETTLEMENT_DAYS, GENERATOR_DEPOSIT_USD.getSpotLag());
    assertEquals("Generator Deposit: getter", DAY_COUNT, GENERATOR_DEPOSIT_USD.getDayCount());
    assertEquals("Generator Deposit: getter", BUSINESS_DAY, GENERATOR_DEPOSIT_USD.getBusinessDayConvention());
    assertEquals("Generator Deposit: getter", IS_EOM, GENERATOR_DEPOSIT_USD.isEndOfMonth());
  }

  @Test
  public void usdDeposit() {
    final GeneratorDeposit preDefined = new USDDeposit(CALENDAR);
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
    final GeneratorDeposit duplicate = new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertEquals("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD, duplicate);
    assertEquals("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.hashCode(), duplicate.hashCode());
    GeneratorDeposit other;
    other = new GeneratorDeposit(NAME, Currency.EUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(NAME, CUR, new MondayToFridayCalendar("B"), SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(NAME, CUR, CALENDAR, 1, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, DayCounts.ACT_365, BUSINESS_DAY, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventions.FOLLOWING, IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
    other = new GeneratorDeposit(NAME, CUR, CALENDAR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, !IS_EOM);
    assertFalse("Generator Deposit: equal-hash", GENERATOR_DEPOSIT_USD.equals(other));
  }

  @Test
  public void generateInstrument() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    final Period tenor = Period.ofMonths(6);
    final double rate = 0.01;
    final double notional = 12345;
    final GeneratorAttributeIR attribute = new GeneratorAttributeIR(tenor);
    final CashDefinition insGenerated = GENERATOR_DEPOSIT_USD.generateInstrument(referenceDate, rate, notional, attribute);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, BUSINESS_DAY, CALENDAR, IS_EOM);
    final double accrualFactor = DAY_COUNT.getDayCountFraction(startDate, endDate);
    final CashDefinition insExpected = new CashDefinition(CUR, startDate, endDate, notional, rate, accrualFactor);
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
  }

}
