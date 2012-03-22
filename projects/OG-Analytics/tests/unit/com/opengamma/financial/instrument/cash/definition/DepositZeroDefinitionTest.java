/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.interestrate.AnnualInterestRate;
import com.opengamma.financial.interestrate.ContinuousInterestRate;
import com.opengamma.financial.interestrate.InterestRate;
import com.opengamma.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests related to the construction and the conversion to derivative of DepositZeroDefinition.
 */
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
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double DEPOSIT_AF = DAY_COUNT.getDayCountFraction(SPOT_DATE, END_DATE);
  private static final DepositZeroDefinition DEPOSIT_DEFINITION = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE);

  private static final String CURVE_NAME = "Curve";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new DepositZeroDefinition(null, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStart() {
    new DepositZeroDefinition(EUR, null, END_DATE, NOTIONAL, DEPOSIT_AF, RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEnd() {
    new DepositZeroDefinition(EUR, SPOT_DATE, null, NOTIONAL, DEPOSIT_AF, RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullRate() {
    new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, null);
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
    double interestAmount = 1.0 / RATE.getDiscountFactor(DEPOSIT_AF) * NOTIONAL;
    assertEquals("DepositZeroDefinition: getter", interestAmount, DEPOSIT_DEFINITION.getInterestAmount());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertEquals("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION, DEPOSIT_DEFINITION);
    DepositZeroDefinition other = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE);
    assertEquals("DepositZeroDefinition: equal-hash code", other, DEPOSIT_DEFINITION);
    assertEquals("DepositZeroDefinition: equal-hash code", other.hashCode(), DEPOSIT_DEFINITION.hashCode());
    DepositZeroDefinition modified;
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE.plusDays(1), END_DATE, NOTIONAL, DEPOSIT_AF, RATE);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE, END_DATE.plusDays(1), NOTIONAL, DEPOSIT_AF, RATE);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE, END_DATE, NOTIONAL + 1000, DEPOSIT_AF, RATE);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF + 0.01, RATE);
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
    modified = new DepositZeroDefinition(Currency.USD, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, new AnnualInterestRate(RATE_FIGURE));
    assertFalse("DepositZeroDefinition: equal-hash code", DEPOSIT_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the builders.
   */
  public void from() {
    DepositZeroDefinition from = DepositZeroDefinition.from(EUR, SPOT_DATE, END_DATE, DAY_COUNT, RATE);
    DepositZeroDefinition comp = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, 1.0, DEPOSIT_AF, RATE);
    assertEquals("DepositZeroDefinition - From", comp, from);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE,
        DEPOSIT_DEFINITION.getInterestAmount(), CURVE_NAME);
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE,
        DEPOSIT_DEFINITION.getInterestAmount(), CURVE_NAME);
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = TimeCalculator.getTimeBetween(referenceDate, SPOT_DATE);
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositZero expected = new DepositZero(EUR, startTime, endTime, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE,
        DEPOSIT_DEFINITION.getInterestAmount(), CURVE_NAME);
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositZero expected = new DepositZero(EUR, startTime, endTime, 0.0, NOTIONAL, DEPOSIT_AF, RATE,
        DEPOSIT_DEFINITION.getInterestAmount(), CURVE_NAME);
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

  @Test
  /**
   * Tests toDerivative.
   */
  public void toDerivativeMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    DepositZero converted = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    double startTime = 0;
    double endTime = TimeCalculator.getTimeBetween(referenceDate, END_DATE);
    DepositZero expected = new DepositZero(EUR, startTime, endTime, 0.0, NOTIONAL, DEPOSIT_AF, RATE,
        DEPOSIT_DEFINITION.getInterestAmount(), CURVE_NAME);
    assertEquals("DepositZeroDefinition: toDerivative", expected, converted);
  }

}
