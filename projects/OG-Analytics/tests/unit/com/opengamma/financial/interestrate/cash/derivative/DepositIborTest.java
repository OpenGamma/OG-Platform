/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR3M;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

public class DepositIborTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IborIndex INDEX = new EURIBOR6M(TARGET);
  private static final Currency EUR = INDEX.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, INDEX.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, INDEX);
  private static final double DEPOSIT_AF = INDEX.getDayCount().getDayCountFraction(SPOT_DATE, END_DATE);
  private static final double SPOT_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, SPOT_DATE);
  private static final double END_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, END_DATE);

  private static final String CURVE_NAME = "Curve";

  private static final DepositIbor DEPOSIT_IBOR = new DepositIbor(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new DepositIbor(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, null, CURVE_NAME);
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositIbor: getter", SPOT_TIME, DEPOSIT_IBOR.getStartTime());
    assertEquals("DepositIbor: getter", END_TIME, DEPOSIT_IBOR.getEndTime());
    assertEquals("DepositIbor: getter", NOTIONAL, DEPOSIT_IBOR.getNotional());
    assertEquals("DepositIbor: getter", RATE, DEPOSIT_IBOR.getRate());
    assertEquals("DepositIbor: getter", EUR, DEPOSIT_IBOR.getCurrency());
    assertEquals("DepositIbor: getter", DEPOSIT_AF, DEPOSIT_IBOR.getAccrualFactor());
    assertEquals("DepositIbor: getter", RATE * NOTIONAL * DEPOSIT_AF, DEPOSIT_IBOR.getInterestAmount());
    assertEquals("DepositIbor: getter", INDEX, DEPOSIT_IBOR.getIndex());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertTrue("DepositIbor: equal hash", DEPOSIT_IBOR.equals(DEPOSIT_IBOR));
    DepositIbor depositIbor2 = new DepositIbor(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, INDEX, CURVE_NAME);
    assertTrue("DepositIbor: equal hash", DEPOSIT_IBOR.equals(depositIbor2));
    assertEquals("DepositIbor: equal hash", DEPOSIT_IBOR.hashCode(), depositIbor2.hashCode());
    DepositIbor other;
    other = new DepositIbor(EUR, SPOT_TIME, END_TIME, NOTIONAL, NOTIONAL, RATE, DEPOSIT_AF, new EURIBOR3M(TARGET), CURVE_NAME);
    assertFalse("DepositIbor: equal hash", DEPOSIT_IBOR.equals(other));
  }

}
