/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the DepositCounterpart instruments construction.
 */
@Test(groups = TestGroup.UNIT)
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
  private static final LegalEntity COUNTERPARTY = new LegalEntity(null, COUNTERPART_NAME, null, null, null);
  private static final double SPOT_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, SPOT_DATE);
  private static final double END_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, END_DATE);

  private static final DepositCounterpart DEPOSIT_CTP = new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, null);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetCurveName() {
    DEPOSIT_CTP.getYieldCurveName();
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
    assertEquals("DepositCounterpart: getter", COUNTERPARTY, DEPOSIT_CTP.getCounterparty());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertTrue("DepositCounterpart: equal hash", DEPOSIT_CTP.equals(DEPOSIT_CTP));
    final DepositCounterpart depositCtp2 = new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, COUNTERPART_NAME);
    assertTrue("DepositCounterpart: equal hash", DEPOSIT_CTP.equals(depositCtp2));
    assertEquals("DepositCounterpart: equal hash", DEPOSIT_CTP.hashCode(), depositCtp2.hashCode());
    DepositCounterpart other;
    other = new DepositCounterpart(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, "Different name");
    assertFalse("DepositIbor: equal hash", DEPOSIT_CTP.equals(other));
  }
}
