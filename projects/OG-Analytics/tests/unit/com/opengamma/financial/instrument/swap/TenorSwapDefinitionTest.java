/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class TenorSwapDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final boolean IS_EOM = true;
  private static final String NAME = "CONVENTION";
  private static final SwapConvention CONVENTION = new SwapConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME);
  private static final LocalDate DATE = LocalDate.of(2011, 8, 1);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime[] NOMINAL_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 3), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 3), DateUtil.getUTCDate(2015, 7, 3)};
  private static final ZonedDateTime[] SETTLEMENT_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 4), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 5), DateUtil.getUTCDate(2015, 7, 3)};
  private static final ZonedDateTime[] RESET_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 1), DateUtil.getUTCDate(2011, 7, 1), DateUtil.getUTCDate(2012, 1, 2),
      DateUtil.getUTCDate(2012, 7, 2), DateUtil.getUTCDate(2013, 1, 1), DateUtil.getUTCDate(2013, 7, 1), DateUtil.getUTCDate(2014, 1, 1), DateUtil.getUTCDate(2014, 7, 1),
      DateUtil.getUTCDate(2015, 1, 1), DateUtil.getUTCDate(2015, 7, 1)};
  private static final ZonedDateTime[] MATURITY_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 7, 4), DateUtil.getUTCDate(2012, 1, 3), DateUtil.getUTCDate(2012, 7, 3),
      DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3), DateUtil.getUTCDate(2015, 1, 5),
      DateUtil.getUTCDate(2015, 7, 3), DateUtil.getUTCDate(2016, 1, 4)};
  private static final double NOTIONAL = 1000000;
  private static final double RATE = 0.05;
  private static final double SPREAD = 0.01;
  private static final FloatingSwapLegDefinition PAY_DEFINITION = new FloatingSwapLegDefinition(CUR, EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, -NOTIONAL, RATE, 0,
      CONVENTION);
  private static final FloatingSwapLegDefinition RECEIVE_DEFINITION = new FloatingSwapLegDefinition(CUR, EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE,
      SPREAD, CONVENTION);
  private static final TenorSwapDefinition SWAP = new TenorSwapDefinition(PAY_DEFINITION, RECEIVE_DEFINITION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLeg() {
    new TenorSwapDefinition(null, RECEIVE_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFloatLeg() {
    new TenorSwapDefinition(PAY_DEFINITION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonZeroSpreadOnPayLeg() {
    new TenorSwapDefinition(RECEIVE_DEFINITION, RECEIVE_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadYieldCurveNames() {
    SWAP.toDerivative(DATE, new String[] {"a", "b"});
  }

  @Test
  public void test() {
    assertEquals(SWAP.getFirstLeg(), PAY_DEFINITION);
    assertEquals(SWAP.getSecondLeg(), RECEIVE_DEFINITION);
    TenorSwapDefinition other = new TenorSwapDefinition(PAY_DEFINITION, RECEIVE_DEFINITION);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
    other = new TenorSwapDefinition(new FloatingSwapLegDefinition(CUR, EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE + 0.01, 0, CONVENTION),
        RECEIVE_DEFINITION);
    assertFalse(other.equals(SWAP));
    other = new TenorSwapDefinition(PAY_DEFINITION, PAY_DEFINITION);
    assertFalse(other.equals(SWAP));
  }

  @Test
  public void testConversion() {
    final String[] names = new String[] {"e", "r", "c"};
    final TenorSwap<Payment> swap = SWAP.toDerivative(DATE, names);
    assertEquals(swap.getFirstLeg(), PAY_DEFINITION.toDerivative(DATE, names[0], names[1]));
    assertEquals(swap.getSecondLeg(), RECEIVE_DEFINITION.toDerivative(DATE, names[0], names[2]));
  }
}
