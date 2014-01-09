/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
    final DepositCounterpartDefinition fromTradeTenor = DepositCounterpartDefinition.fromTrade(TRADE_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", DEPOSIT_CTP_DEFINITION, fromTradeTenor);
    final DepositCounterpartDefinition fromStartTenor = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", DEPOSIT_CTP_DEFINITION, fromStartTenor);
    final int start = 1;
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, start, TARGET);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, TARGET);
    final double af = GENERATOR.getDayCount().getDayCountFraction(startDate, endDate);
    final DepositCounterpartDefinition on = new DepositCounterpartDefinition(EUR, startDate, endDate, NOTIONAL, RATE, af, COUNTERPART);
    final DepositCounterpartDefinition fromTradeON = DepositCounterpartDefinition.fromTrade(TRADE_DATE, start, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", on, fromTradeON);
    final DepositCounterpartDefinition fromStartON = DepositCounterpartDefinition.fromStart(startDate, NOTIONAL, RATE, GENERATOR, COUNTERPART);
    assertEquals("DepositDefinition: from", on, fromStartON);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTradeDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettleDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettleDeprecated() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturityDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturityDeprecated() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, COUNTERPART, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, COUNTERPART);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositCounterpart converted = DEPOSIT_CTP_DEFINITION.toDerivative(referenceDate);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositCounterpart expected = new DepositCounterpart(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, COUNTERPART);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }
}
