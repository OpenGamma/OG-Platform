/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DepositIborDefinitionTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IborIndex INDEX = IndexIborMaster.getInstance().getIndex("EURIBOR6M");
  private static final Calendar NO_HOLIDAYS = new CalendarNoHoliday("No Holidays");
  private static final Currency EUR = INDEX.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, INDEX.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, INDEX, NO_HOLIDAYS);
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
    final DepositIborDefinition fromTradeTenor = DepositIborDefinition.fromTrade(TRADE_DATE, NOTIONAL, RATE, INDEX, NO_HOLIDAYS);
    assertEquals("DepositDefinition: from", DEPOSIT_IBOR_DEFINITION, fromTradeTenor);
    final DepositIborDefinition fromStartTenor = DepositIborDefinition.fromStart(SPOT_DATE, NOTIONAL, RATE, INDEX, NO_HOLIDAYS);
    assertEquals("DepositDefinition: from", DEPOSIT_IBOR_DEFINITION, fromStartTenor);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTradeDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettleDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettleDeprecated() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturityDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturityDeprecated() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, INDEX);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositIbor converted = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double startTime = 0;
    final double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    final DepositIbor expected = new DepositIbor(EUR, startTime, endTime, NOTIONAL, 0, RATE, DEPOSIT_AF, INDEX);
    assertEquals("DepositDefinition: toDerivative", expected, converted);
  }
}
