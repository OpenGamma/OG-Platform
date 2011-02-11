/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

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
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FixedFloatSwapDefinitionTest {
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
  private static final FixedSwapLegDefinition FIXED_DEFINITION = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
  private static final double SPREAD = 0.01;
  private static final FloatingSwapLegDefinition FLOAT_DEFINITION = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD,
      CONVENTION);
  private static final FixedFloatSwapDefinition SWAP = new FixedFloatSwapDefinition(FIXED_DEFINITION, FLOAT_DEFINITION);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixedLeg() {
    new FixedFloatSwapDefinition(null, FLOAT_DEFINITION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFloatLeg() {
    new FixedFloatSwapDefinition(FIXED_DEFINITION, null);
  }

  @Test
  public void test() {
    assertEquals(SWAP.getFixedLeg(), FIXED_DEFINITION);
    assertEquals(SWAP.getFloatingLeg(), FLOAT_DEFINITION);
    FixedFloatSwapDefinition other = new FixedFloatSwapDefinition(FIXED_DEFINITION, FLOAT_DEFINITION);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
    other = new FixedFloatSwapDefinition(new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE + 0.01, CONVENTION), FLOAT_DEFINITION);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwapDefinition(FIXED_DEFINITION, new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE + 0.02, SPREAD,
        CONVENTION));
    assertFalse(other.equals(SWAP));
  }

  @Test
  public void testConversion() {
    final String[] names = new String[] {"e", "r"};
    final FixedCouponSwap<Payment> swap = SWAP.toDerivative(DATE, names);
    assertEquals(swap.getFixedLeg(), FIXED_DEFINITION.toDerivative(DATE, names));
    assertEquals(swap.getReceiveLeg(), FLOAT_DEFINITION.toDerivative(DATE, names));
  }
}
