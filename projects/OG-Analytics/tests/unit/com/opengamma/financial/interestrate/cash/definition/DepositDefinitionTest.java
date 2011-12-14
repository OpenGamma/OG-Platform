/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.cash.DepositDefinition;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class DepositDefinitionTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final double DEPOSIT_AF = GENERATOR.getDayCount().getDayCountFraction(SPOT_DATE, END_DATE);
  private static final DepositDefinition DEPOSIT_DEFINITION = new DepositDefinition(SPOT_DATE, END_DATE, NOTIONAL, RATE, EUR, DEPOSIT_AF);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartDate() {
    new DepositDefinition(null, SPOT_DATE, NOTIONAL, RATE, EUR, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate() {
    new DepositDefinition(TRADE_DATE, null, NOTIONAL, RATE, EUR, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new DepositDefinition(TRADE_DATE, SPOT_DATE, NOTIONAL, RATE, null, 1.0);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositDefinition: getter", SPOT_DATE, DEPOSIT_DEFINITION.getStartDate());
    assertEquals("DepositDefinition: getter", END_DATE, DEPOSIT_DEFINITION.getEndDate());
    assertEquals("DepositDefinition: getter", NOTIONAL, DEPOSIT_DEFINITION.getNotional());
    assertEquals("DepositDefinition: getter", RATE, DEPOSIT_DEFINITION.getRate());
    assertEquals("DepositDefinition: getter", EUR, DEPOSIT_DEFINITION.getCurrency());
    assertEquals("DepositDefinition: getter", DEPOSIT_AF, DEPOSIT_DEFINITION.getAccrualFactor());
    assertEquals("DepositDefinition: getter", RATE * NOTIONAL * DEPOSIT_AF, DEPOSIT_DEFINITION.getInterestAmount());
  }

  @Test
  /**
   * Tests the builders.
   */
  public void from() {
    DepositDefinition fromTradeTenor = DepositDefinition.fromTrade(TRADE_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR);
    assertEquals("DepositDefinition: from", DEPOSIT_DEFINITION, fromTradeTenor);
    DepositDefinition fromStartTenor = DepositDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR);
    assertEquals("DepositDefinition: from", DEPOSIT_DEFINITION, fromStartTenor);
    int start = 1;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, start, TARGET);
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, TARGET);
    double af = GENERATOR.getDayCount().getDayCountFraction(startDate, endDate);
    DepositDefinition on = new DepositDefinition(startDate, endDate, NOTIONAL, RATE, EUR, af);
    DepositDefinition fromTradeON = DepositDefinition.fromTrade(TRADE_DATE, start, NOTIONAL, RATE, GENERATOR);
    assertEquals("DepositDefinition: from", on, fromTradeON);
    DepositDefinition fromStartON = DepositDefinition.fromStart(startDate, NOTIONAL, RATE, GENERATOR);
    assertEquals("DepositDefinition: from", on, fromStartON);
  }

}
