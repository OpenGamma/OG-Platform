/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;
import com.opengamma.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

public class DepositIborDefinitionTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IborIndex INDEX = new EURIBOR6M(TARGET);
  private static final Currency EUR = INDEX.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, INDEX.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  //  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, INDEX);
  private static final double DEPOSIT_AF = INDEX.getDayCount().getDayCountFraction(SPOT_DATE, END_DATE);
  private static final DepositIborDefinition DEPOSIT_IBOR_DEFINITION = new DepositIborDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF, INDEX);

  private static final String CURVE_NAME = "Curve";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new DepositIborDefinition(null, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF, INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new DepositIborDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF, null);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositIborDefinition: getter", SPOT_DATE, DEPOSIT_IBOR_DEFINITION.getStartDate());
    assertEquals("DepositIborDefinition: getter", END_DATE, DEPOSIT_IBOR_DEFINITION.getEndDate());
    assertEquals("DepositIborDefinition: getter", NOTIONAL, DEPOSIT_IBOR_DEFINITION.getNotional());
    assertEquals("DepositIborDefinition: getter", RATE, DEPOSIT_IBOR_DEFINITION.getRate());
    assertEquals("DepositIborDefinition: getter", EUR, DEPOSIT_IBOR_DEFINITION.getCurrency());
    assertEquals("DepositIborDefinition: getter", DEPOSIT_AF, DEPOSIT_IBOR_DEFINITION.getAccrualFactor());
    assertEquals("DepositIborDefinition: getter", RATE * NOTIONAL * DEPOSIT_AF, DEPOSIT_IBOR_DEFINITION.getInterestAmount());
    assertEquals("DepositIborDefinition: getter", INDEX, DEPOSIT_IBOR_DEFINITION.getIndex());
  }

  @Test
  /**
   * Tests the builders.
   */
  public void from() {
    DepositIborDefinition fromTradeTenor = DepositIborDefinition.fromTrade(TRADE_DATE, NOTIONAL, RATE, INDEX);
    assertEquals("DepositDefinition: from", DEPOSIT_IBOR_DEFINITION, fromTradeTenor);
    DepositIborDefinition fromStartTenor = DepositIborDefinition.fromStart(SPOT_DATE, NOTIONAL, RATE, INDEX);
    assertEquals("DepositDefinition: from", DEPOSIT_IBOR_DEFINITION, fromStartTenor);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

}
