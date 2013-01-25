/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the constructor and method of GeneratorDeposit.
 */
public class GeneratorForexSwapTest {
  // USD deposits
  private static final String NAME = "EUR/USD Swap";
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD = new GeneratorForexSwap(NAME, EUR, USD, CALENDAR, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    new GeneratorForexSwap(NAME, null, USD, CALENDAR, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    new GeneratorForexSwap(NAME, EUR, null, CALENDAR, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new GeneratorForexSwap(NAME, EUR, USD, null, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorForexSwap(NAME, EUR, USD, CALENDAR, SETTLEMENT_DAYS, null, IS_EOM);
  }

  @Test
  public void getter() {
    assertEquals("Generator Deposit: getter", NAME, GENERATOR_FX_EURUSD.getName());
    assertEquals("Generator Deposit: getter", EUR, GENERATOR_FX_EURUSD.getCurrency1());
    assertEquals("Generator Deposit: getter", USD, GENERATOR_FX_EURUSD.getCurrency2());
    assertEquals("Generator Deposit: getter", CALENDAR, GENERATOR_FX_EURUSD.getCalendar());
    assertEquals("Generator Deposit: getter", SETTLEMENT_DAYS, GENERATOR_FX_EURUSD.getSpotLag());
    assertEquals("Generator Deposit: getter", BUSINESS_DAY, GENERATOR_FX_EURUSD.getBusinessDayConvention());
    assertEquals("Generator Deposit: getter", IS_EOM, GENERATOR_FX_EURUSD.isEndOfMonth());
  }

  @Test
  public void generateInstrument() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    Period tenor = Period.ofMonths(6);
    double pts = 0.01;
    double eurUsd = 1.25;
    double notional = 123000000;
    FXMatrix fxMatrix = new FXMatrix(EUR, USD, eurUsd);
    GeneratorAttributeFX attribute = new GeneratorAttributeFX(tenor, fxMatrix);
    ForexSwapDefinition insGenerated = GENERATOR_FX_EURUSD.generateInstrument(referenceDate, pts, notional, attribute);
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, BUSINESS_DAY, CALENDAR, IS_EOM);
    ForexSwapDefinition insExpected = new ForexSwapDefinition(EUR, USD, startDate, endDate, notional, eurUsd, pts);
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
  }

}
