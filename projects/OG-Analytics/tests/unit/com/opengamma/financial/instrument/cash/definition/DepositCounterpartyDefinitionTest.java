/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

public class DepositCounterpartyDefinitionTest {

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
  private static final String COUNTERPART = "Ctp";

  private static final DepositCounterpartDefinition DEPOSIT_CTP_DEFINITION = new DepositCounterpartDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART);

  private static final String CURVE_NAME = "Curve";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new DepositCounterpartDefinition(null, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCpty() {
    new DepositCounterpartDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF, null);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositIborDefinition: getter", SPOT_DATE, DEPOSIT_CTP_DEFINITION.getStartDate());
    assertEquals("DepositIborDefinition: getter", END_DATE, DEPOSIT_CTP_DEFINITION.getEndDate());
    assertEquals("DepositIborDefinition: getter", NOTIONAL, DEPOSIT_CTP_DEFINITION.getNotional());
    assertEquals("DepositIborDefinition: getter", RATE, DEPOSIT_CTP_DEFINITION.getRate());
    assertEquals("DepositIborDefinition: getter", EUR, DEPOSIT_CTP_DEFINITION.getCurrency());
    assertEquals("DepositIborDefinition: getter", DEPOSIT_AF, DEPOSIT_CTP_DEFINITION.getAccrualFactor());
    assertEquals("DepositIborDefinition: getter", RATE * NOTIONAL * DEPOSIT_AF, DEPOSIT_CTP_DEFINITION.getInterestAmount());
    assertEquals("DepositIborDefinition: getter", COUNTERPART, DEPOSIT_CTP_DEFINITION.getCounterpartName());
  }

  @Test
  /**
   * Tests the builders.
   */
  public void from() {
    DepositCounterpartDefinition fromTradeTenor = DepositCounterpartDefinition.fromTrade(TRADE_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", DEPOSIT_CTP_DEFINITION, fromTradeTenor);
    DepositCounterpartDefinition fromStartTenor = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", DEPOSIT_CTP_DEFINITION, fromStartTenor);
    int start = 1;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, start, TARGET);
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, TARGET);
    double af = GENERATOR.getDayCount().getDayCountFraction(startDate, endDate);
    DepositCounterpartDefinition on = new DepositCounterpartDefinition(EUR, startDate, endDate, NOTIONAL, RATE, af, COUNTERPART);
    DepositCounterpartDefinition fromTradeON = DepositCounterpartDefinition.fromTrade(TRADE_DATE, start, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", on, fromTradeON);
    DepositCounterpartDefinition fromStartON = DepositCounterpartDefinition.fromStart(startDate, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", on, fromStartON);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

}
