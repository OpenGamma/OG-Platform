/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FixedSwapLegDefinitionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final boolean IS_EOM = true;
  private static final String NAME = "CONVENTION";
  private static final SwapConvention CONVENTION = new SwapConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME);
  private static final ZonedDateTime[] NOMINAL_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 3), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 3), DateUtil.getUTCDate(2015, 7, 3)};
  private static final ZonedDateTime[] SETTLEMENT_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 6), DateUtil.getUTCDate(2011, 7, 6), DateUtil.getUTCDate(2012, 1, 6),
      DateUtil.getUTCDate(2012, 7, 6), DateUtil.getUTCDate(2013, 1, 8), DateUtil.getUTCDate(2013, 7, 8), DateUtil.getUTCDate(2014, 1, 8), DateUtil.getUTCDate(2014, 7, 8),
      DateUtil.getUTCDate(2015, 1, 7), DateUtil.getUTCDate(2015, 7, 8)};
  private static final double NOTIONAL = 1000000;
  private static final double RATE = 0.05;
  private static final FixedSwapLegDefinition DEFINITION = new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);

  @Test(expected = IllegalArgumentException.class)
  public void testNullNominalDates() {
    new FixedSwapLegDefinition(null, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDates() {
    new FixedSwapLegDefinition(NOMINAL_DATES, null, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConvention() {
    new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeRate() {
    new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, -RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDatesLength() {
    new FixedSwapLegDefinition(NOMINAL_DATES, new ZonedDateTime[] {}, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, new String[] {"B"});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNames() {
    DEFINITION.toDerivative(LocalDate.of(2011, 2, 1), (String[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyNames() {
    DEFINITION.toDerivative(LocalDate.of(2011, 2, 1), new String[0]);
  }

  @Test
  public void test() {
    assertEquals(DEFINITION.getConvention(), CONVENTION);
    assertArrayEquals(DEFINITION.getNominalDates(), NOMINAL_DATES);
    assertEquals(DEFINITION.getNotional(), NOTIONAL, 0);
    assertEquals(DEFINITION.getRate(), RATE, 0);
    assertArrayEquals(DEFINITION.getSettlementDates(), SETTLEMENT_DATES);
    FixedSwapLegDefinition other = new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
    assertEquals(other, DEFINITION);
    assertEquals(other.hashCode(), DEFINITION.hashCode());
    other = new FixedSwapLegDefinition(SETTLEMENT_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(NOMINAL_DATES, NOMINAL_DATES, NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL + 1, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE + 1, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, new SwapConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, !IS_EOM, NAME));
    assertFalse(other.equals(DEFINITION));
  }
}
