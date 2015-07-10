/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction and the conversion to derivative of DepositZeroDefinition.
 */
@Test(groups = TestGroup.UNIT)
public class DepositZeroDefinitionTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE_FIGURE = 0.0250;
  private static final InterestRate RATE = new ContinuousInterestRate(RATE_FIGURE);
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final DayCount DAY_COUNT = DayCounts.ACT_365;
  private static final double DEPOSIT_AF = DAY_COUNT.getDayCountFraction(SPOT_DATE, END_DATE);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final DepositZeroDefinition DEPOSIT_DEFINITION = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
  private static final String CURVE_NAME = "Curve";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new DepositZeroDefinition(null, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStart() {
    new DepositZeroDefinition(EUR, null, END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEnd() {
    new DepositZeroDefinition(EUR, SPOT_DATE, null, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullRate() {
    new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, null, CALENDAR, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    DepositZeroDefinition.from(EUR, SPOT_DATE, END_DATE, null, RATE, CALENDAR, DAY_COUNT);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositZeroDefinition: getter", SPOT_DATE, DEPOSIT_DEFINITION.getStartDate());
    assertEquals("DepositZeroDefinition: getter", END_DATE, DEPOSIT_DEFINITION.getEndDate());
    assertEquals("DepositZeroDefinition: getter", NOTIONAL, DEPOSIT_DEFINITION.getNotional());
    assertEquals("DepositZeroDefinition: getter", RATE, DEPOSIT_DEFINITION.getRate());
    assertEquals("DepositZeroDefinition: getter", EUR, DEPOSIT_DEFINITION.getCurrency());
    assertEquals("DepositZeroDefinition: getter", DEPOSIT_AF, DEPOSIT_DEFINITION.getPaymentAccrualFactor());
    final double interestAmount = (1.0 / RATE.getDiscountFactor(DEPOSIT_AF) - 1) * NOTIONAL;
    assertEquals("DepositZeroDefinition: getter", interestAmount, DEPOSIT_DEFINITION.getInterestAmount());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertEquals("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION, DEPOSIT_DEFINITION);
    final DepositZeroDefinition other = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
    assertEquals("DepositZeroDefinition: equal-hash code", other, DEPOSIT_DEFINITION);
    assertEquals("DepositZeroDefinition: equal-hash code", other.hashCode(), DEPOSIT_DEFINITION.hashCode());
    DepositZeroDefinition modified;
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.EUR, SPOT_DATE.plusDays(1), END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.EUR, SPOT_DATE, END_DATE.plusDays(1), NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.EUR, SPOT_DATE, END_DATE, NOTIONAL + 1000, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF + 0.01, RATE, CALENDAR, DAY_COUNT);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, new PeriodicInterestRate(RATE_FIGURE, 1), CALENDAR, DAY_COUNT);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the builders.
   */
  public void from() {
    final DepositZeroDefinition from = DepositZeroDefinition.from(EUR, SPOT_DATE, END_DATE, DAY_COUNT, RATE, CALENDAR, DAY_COUNT);
    final DepositZeroDefinition comp = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, 1.0, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);
    assertEquals("DepositZeroDefinition - From", comp, from);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double startTime = DAY_COUNT.getDayCountFraction(referenceDate, SPOT_DATE, CALENDAR);
    final double endTime = DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR);
    final DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, DEPOSIT_DEFINITION.getInterestAmount());
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double startTime = DAY_COUNT.getDayCountFraction(referenceDate, SPOT_DATE, CALENDAR);
    final double endTime = DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR);
    final DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, DEPOSIT_DEFINITION.getInterestAmount());
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double startTime = DAY_COUNT.getDayCountFraction(referenceDate, SPOT_DATE, CALENDAR);
    final double endTime = DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR);
    final DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, DEPOSIT_DEFINITION.getInterestAmount());
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double startTime = DAY_COUNT.getDayCountFraction(SPOT_DATE, referenceDate, CALENDAR);
    final double endTime = DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR);
    final DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, DEPOSIT_DEFINITION.getInterestAmount());
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double startTime = DAY_COUNT.getDayCountFraction(SPOT_DATE, referenceDate, CALENDAR);
    final double endTime = DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR);
    final DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, DEPOSIT_DEFINITION.getInterestAmount());
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }
}
