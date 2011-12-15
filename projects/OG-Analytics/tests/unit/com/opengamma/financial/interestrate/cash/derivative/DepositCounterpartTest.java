/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests related to the DepositCounterpart instruments construction.
 */
public class DepositCounterpartTest {

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
  private static final String COUNTERPART_NAME = "Ctp";

  private static final double SPOT_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, SPOT_DATE);
  private static final double END_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, END_DATE);

  private static final String CURVE_NAME = "Curve";

  private static final DepositCounterpart DEPOSIT_CTP = new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART_NAME, CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, null, CURVE_NAME);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositCounterpart: getter", SPOT_TIME, DEPOSIT_CTP.getStartTime());
    assertEquals("DepositCounterpart: getter", END_TIME, DEPOSIT_CTP.getEndTime());
    assertEquals("DepositCounterpart: getter", NOTIONAL, DEPOSIT_CTP.getNotional());
    assertEquals("DepositCounterpart: getter", RATE, DEPOSIT_CTP.getRate());
    assertEquals("DepositCounterpart: getter", EUR, DEPOSIT_CTP.getCurrency());
    assertEquals("DepositCounterpart: getter", DEPOSIT_AF, DEPOSIT_CTP.getAccrualFactor());
    assertEquals("DepositCounterpart: getter", RATE * NOTIONAL * DEPOSIT_AF, DEPOSIT_CTP.getInterestAmount());
    assertEquals("DepositCounterpart: getter", COUNTERPART_NAME, DEPOSIT_CTP.getCounterpartName());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertTrue("DepositCounterpart: equal hash", DEPOSIT_CTP.equals(DEPOSIT_CTP));
    DepositCounterpart depositCtp2 = new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART_NAME, CURVE_NAME);
    assertTrue("DepositCounterpart: equal hash", DEPOSIT_CTP.equals(depositCtp2));
    assertEquals("DepositCounterpart: equal hash", DEPOSIT_CTP.hashCode(), depositCtp2.hashCode());
    DepositCounterpart other;
    other = new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, "Different name", CURVE_NAME);
    assertFalse("DepositIbor: equal hash", DEPOSIT_CTP.equals(other));
  }
}
