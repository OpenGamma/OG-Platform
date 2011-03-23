/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.future.IRFutureConvention;
import com.opengamma.financial.instrument.future.IRFutureDefinition;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class IRFutureDefinitionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String NAME = "CONVENTION";
  private static final double YEAR_FRACTION = 0.25;
  private static final IRFutureConvention CONVENTION = new IRFutureConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME);
  private static final LocalDate DATE = LocalDate.of(2011, 2, 1);
  private static final ZonedDateTime LAST_TRADE_DATE = DateUtil.getUTCDate(2011, 3, 14);
  private static final ZonedDateTime MATURITY_DATE = DateUtil.getUTCDate(2011, 6, 15);
  private static final double RATE = 95;
  private static final IRFutureDefinition DEFINITION = new IRFutureDefinition(LAST_TRADE_DATE, MATURITY_DATE, CONVENTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLastTradeDate() {
    new IRFutureDefinition(null, MATURITY_DATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate() {
    new IRFutureDefinition(LAST_TRADE_DATE, null, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new IRFutureDefinition(LAST_TRADE_DATE, MATURITY_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMaturityBeforeLastTradeDate() {
    new IRFutureDefinition(MATURITY_DATE, LAST_TRADE_DATE, CONVENTION);
  }

  @Test
  public void test() {
    assertEquals(DEFINITION.getConvention(), CONVENTION);
    assertEquals(DEFINITION.getLastTradeDate(), LAST_TRADE_DATE);
    assertEquals(DEFINITION.getMaturity(), MATURITY_DATE);
    IRFutureDefinition other = new IRFutureDefinition(LAST_TRADE_DATE, MATURITY_DATE, CONVENTION);
    assertEquals(other, DEFINITION);
    assertEquals(other.hashCode(), DEFINITION.hashCode());
    other = new IRFutureDefinition(LAST_TRADE_DATE.plusDays(1), MATURITY_DATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new IRFutureDefinition(LAST_TRADE_DATE, MATURITY_DATE.plusDays(1), CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new IRFutureDefinition(LAST_TRADE_DATE, MATURITY_DATE, new IRFutureConvention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, YEAR_FRACTION, NAME));
    assertFalse(other.equals(DEFINITION));
  }

  @Test
  public void testConversion() {
    final String name = "index";
    final InterestRateFuture ir = DEFINITION.toDerivative(DATE, RATE, name);
    assertEquals(ir.getCurveName(), name);
    assertEquals(ir.getFixingDate(), 41. / 360, 0);
    assertEquals(ir.getIndexYearFraction(), 93. / 360, 0);
    assertEquals(ir.getMaturity(), 134. / 360, 0);
    assertEquals(ir.getPrice(), RATE, 0);
    assertEquals(ir.getSettlementDate(), 43. / 360, 0);
    assertEquals(ir.getValueYearFraction(), YEAR_FRACTION, 0);
  }
}
