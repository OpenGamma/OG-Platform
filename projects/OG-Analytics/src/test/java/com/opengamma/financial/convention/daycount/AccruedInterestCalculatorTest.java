/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static com.opengamma.financial.convention.daycount.AccruedInterestCalculator.getAccruedInterest;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test AccruedInterestCalculator.
 */
@Test(groups = TestGroup.UNIT)
public class AccruedInterestCalculatorTest {

  private static final DayCount DC1 = DayCounts.ACT_ACT_ICMA;
  private static final ZonedDateTime DATE1 = DateUtils.getUTCDate(2006, 1, 4);
  private static final ZonedDateTime[] SCHEDULE1 = new ZonedDateTime[] {DateUtils.getUTCDate(2005, 8, 15), DateUtils.getUTCDate(2006, 2, 15), DateUtils.getUTCDate(2006, 8, 15),
      DateUtils.getUTCDate(2007, 2, 15)};
  private static final DayCount DC2 = DayCounts.THIRTY_U_360;
  private static final ZonedDateTime DATE2 = DateUtils.getUTCDate(2006, 1, 6);
  private static final ZonedDateTime[] SCHEDULE2 = new ZonedDateTime[] {DateUtils.getUTCDate(2005, 8, 15), DateUtils.getUTCDate(2006, 2, 15), DateUtils.getUTCDate(2006, 8, 15),
      DateUtils.getUTCDate(2007, 2, 14)};
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    getAccruedInterest(null, DATE1, SCHEDULE1, 0.2, 1, false, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate() {
    getAccruedInterest(DC1, null, SCHEDULE1, 0.04, 1, false, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSchedule() {
    getAccruedInterest(DC1, DATE1, null, 0.02, 1, false, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullElementInSchedule() {
    getAccruedInterest(DC1, DATE1, new ZonedDateTime[] {null}, 0.02, 1, false, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentsPerYear() {
    getAccruedInterest(DC1, DATE1, SCHEDULE1, 0.02, -1, false, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadDate() {
    getAccruedInterest(DC1, DateUtils.getUTCDate(2000, 1, 1), SCHEDULE1, 0.02, 1, false, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testActActICMANormalEOM() {
    getAccruedInterest(new ActualActualICMANormal(), DATE1, SCHEDULE1, 0.02, 1, true, 0, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    getAccruedInterest(DC1, DATE1, SCHEDULE1, 0.4, 1, false, 0, null);
  }

  @Test
  public void test() {
    assertEquals(getAccruedInterest(DC1, DATE1, SCHEDULE1, 2.25, 2, true, 0, CALENDAR), 2.25 * 0.5 * 142 / 184, EPS);
    assertEquals(getAccruedInterest(DC2, DATE2, SCHEDULE2, 7.75, 2, true, 0, CALENDAR), 7.75 * 0.5 * 141 / 180, EPS);
  }
}
