/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class CashDefinitionTest {

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
  private static final CashDefinition DEPOSIT_DEFINITION = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF);

  private static final String CURVE_NAME = "Curve";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartDate() {
    new CashDefinition(EUR, null, SPOT_DATE, NOTIONAL, RATE, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate() {
    new CashDefinition(EUR, TRADE_DATE, null, NOTIONAL, RATE, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CashDefinition(null, TRADE_DATE, SPOT_DATE, NOTIONAL, RATE, 1.0);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("CashDefinition: getter", SPOT_DATE, DEPOSIT_DEFINITION.getStartDate());
    assertEquals("CashDefinition: getter", END_DATE, DEPOSIT_DEFINITION.getEndDate());
    assertEquals("CashDefinition: getter", NOTIONAL, DEPOSIT_DEFINITION.getNotional());
    assertEquals("CashDefinition: getter", RATE, DEPOSIT_DEFINITION.getRate());
    assertEquals("CashDefinition: getter", EUR, DEPOSIT_DEFINITION.getCurrency());
    assertEquals("CashDefinition: getter", DEPOSIT_AF, DEPOSIT_DEFINITION.getAccrualFactor());
    assertEquals("CashDefinition: getter", RATE * NOTIONAL * DEPOSIT_AF, DEPOSIT_DEFINITION.getInterestAmount());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertEquals("CashDefinition: equal-hash code", DEPOSIT_DEFINITION, DEPOSIT_DEFINITION);
    CashDefinition other = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF);
    assertEquals("CashDefinition: equal-hash code", other, DEPOSIT_DEFINITION);
    assertEquals("CashDefinition: equal-hash code", other.hashCode(), DEPOSIT_DEFINITION.hashCode());
    CashDefinition modified;
    modified = new CashDefinition(Currency.USD, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF);
    assertFalse("CashDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new CashDefinition(EUR, SPOT_DATE.plusDays(1), END_DATE, NOTIONAL, RATE, DEPOSIT_AF);
    assertFalse("CashDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new CashDefinition(EUR, SPOT_DATE, END_DATE.plusDays(1), NOTIONAL, RATE, DEPOSIT_AF);
    assertFalse("CashDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL + 1000.0, RATE, DEPOSIT_AF);
    assertFalse("CashDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + 0.0010, DEPOSIT_AF);
    assertFalse("CashDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF - 0.001);
    assertFalse("CashDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the builders.
   */
  public void from() {
    CashDefinition fromTradeTenor = CashDefinition.fromTrade(TRADE_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR);
    assertEquals("CashDefinition: from", DEPOSIT_DEFINITION, fromTradeTenor);
    CashDefinition fromStartTenor = CashDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR);
    assertEquals("CashDefinition: from", DEPOSIT_DEFINITION, fromStartTenor);
    int start = 1;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, start, TARGET);
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, TARGET);
    double af = GENERATOR.getDayCount().getDayCountFraction(startDate, endDate);
    CashDefinition on = new CashDefinition(EUR, startDate, endDate, NOTIONAL, RATE, af);
    CashDefinition fromTradeON = CashDefinition.fromTrade(TRADE_DATE, start, NOTIONAL, RATE, GENERATOR);
    assertEquals("CashDefinition: from", on, fromTradeON);
    CashDefinition fromStartON = CashDefinition.fromStart(startDate, NOTIONAL, RATE, GENERATOR);
    assertEquals("CashDefinition: from", on, fromStartON);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    Cash converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    Cash expected = new Cash(EUR, startTime, endTime, NOTIONAL, RATE, DEPOSIT_AF, CURVE_NAME);
    assertEquals("CashDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    Cash converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    Cash expected = new Cash(EUR, startTime, endTime, NOTIONAL, RATE, DEPOSIT_AF, CURVE_NAME);
    assertEquals("CashDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    Cash converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    Cash expected = new Cash(EUR, startTime, endTime, NOTIONAL, RATE, DEPOSIT_AF, CURVE_NAME);
    assertEquals("CashDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    Cash converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    Cash expected = new Cash(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, CURVE_NAME);
    assertEquals("CashDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    Cash converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    Cash expected = new Cash(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, CURVE_NAME);
    assertEquals("CashDefinition: toDerivative", expected, converted);
  }

}
