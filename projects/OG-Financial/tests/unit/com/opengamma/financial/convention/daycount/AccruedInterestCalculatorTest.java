/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static com.opengamma.financial.convention.daycount.AccruedInterestCalculator.getAccruedInterest;
import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class AccruedInterestCalculatorTest {
  private static final DayCount DC1 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final ZonedDateTime DATE1 = DateUtil.getUTCDate(2006, 1, 4);
  private static final ZonedDateTime[] SCHEDULE1 = new ZonedDateTime[] {DateUtil.getUTCDate(2005, 8, 15), DateUtil.getUTCDate(2006, 2, 15), DateUtil.getUTCDate(2006, 8, 15),
      DateUtil.getUTCDate(2007, 2, 15)};
  private static final DayCount DC2 = DayCountFactory.INSTANCE.getDayCount("30U/360");
  private static final ZonedDateTime DATE2 = DateUtil.getUTCDate(2006, 1, 6);
  private static final ZonedDateTime[] SCHEDULE2 = new ZonedDateTime[] {DateUtil.getUTCDate(2005, 8, 15), DateUtil.getUTCDate(2006, 2, 15), DateUtil.getUTCDate(2006, 8, 15),
      DateUtil.getUTCDate(2007, 2, 14)};
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDayCount() {
    getAccruedInterest(null, DATE1, SCHEDULE1, 0.2, 1, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDate() {
    getAccruedInterest(DC1, null, SCHEDULE1, 0.04, 1, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSchedule() {
    getAccruedInterest(DC1, DATE1, null, 0.02, 1, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullElementInSchedule() {
    getAccruedInterest(DC1, DATE1, new ZonedDateTime[] {null}, 0.02, 1, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePaymentsPerYear() {
    getAccruedInterest(DC1, DATE1, SCHEDULE1, 0.02, -1, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadDate() {
    getAccruedInterest(DC1, DateUtil.getUTCDate(2000, 1, 1), SCHEDULE1, 0.02, 1, false);
  }

  @Test
  public void test() {
    assertEquals(getAccruedInterest(DC1, DATE1, SCHEDULE1, 2.25, 2, true), 2.25 * 0.5 * 142 / 184, EPS);
    assertEquals(getAccruedInterest(DC2, DATE2, SCHEDULE2, 7.75, 2, true), 7.75 * 0.5 * 141 / 180, EPS);
  }
}
