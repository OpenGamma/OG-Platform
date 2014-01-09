/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.ModifiedFollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.daycount.ThirtyEThreeSixty;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@SuppressWarnings("synthetic-access")
@Test(groups = TestGroup.UNIT)
public class ScheduleCalculatorTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final GeneratorDeposit GENERATOR_DEPOSIT = new EURDeposit(CALENDAR);
  private static final IborIndex INDEX_EURIBOR6M = IndexIborMaster.getInstance().getIndex("EURIBOR6M");

  private static final Calendar ALL = new AllCalendar();
  private static final Calendar WEEKEND = new WeekendCalendar();
  private static final Calendar FIRST = new FirstOfMonthCalendar();
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2010, 1, 1);

  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final boolean SHORT_STUB = true;

  private static final BusinessDayConvention MOD_FOL = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOL = BusinessDayConventions.FOLLOWING;
  private static final BusinessDayConvention PRE = BusinessDayConventions.PRECEDING;

  @Test
  /**
   * Tests the adjusted dates shifted by a number of days. Reviewed 13-Dec-2011.
   */
  public void adjustedDatesDays() {
    final ZonedDateTime aMonday = DateUtils.getUTCDate(2011, 12, 12);
    final ZonedDateTime aTuesday = DateUtils.getUTCDate(2011, 12, 13);
    final ZonedDateTime aWednesday = DateUtils.getUTCDate(2011, 12, 14);
    final ZonedDateTime aThursday = DateUtils.getUTCDate(2011, 12, 15);
    final ZonedDateTime aFriday = DateUtils.getUTCDate(2011, 12, 16);
    final ZonedDateTime aSaturday = DateUtils.getUTCDate(2011, 12, 17);
    final ZonedDateTime aSunday = DateUtils.getUTCDate(2011, 12, 18);
    final ZonedDateTime aMonday2 = DateUtils.getUTCDate(2011, 12, 19);
    final ZonedDateTime aTuesday2 = DateUtils.getUTCDate(2011, 12, 20);
    final ZonedDateTime aWednesday2 = DateUtils.getUTCDate(2011, 12, 21);
    assertEquals("Adjusted date", aMonday, ScheduleCalculator.getAdjustedDate(aMonday, 0, CALENDAR));
    assertEquals("Adjusted date", aFriday, ScheduleCalculator.getAdjustedDate(aFriday, 0, CALENDAR));
    assertArrayEquals("Adjusted date", new ZonedDateTime[] {aMonday, aTuesday },
        ScheduleCalculator.getAdjustedDate(new ZonedDateTime[] {aMonday, aTuesday }, 0, CALENDAR));
    assertEquals("Adjusted date", aMonday2, ScheduleCalculator.getAdjustedDate(aSaturday, 0, CALENDAR));
    assertEquals("Adjusted date", aMonday2, ScheduleCalculator.getAdjustedDate(aSunday, 0, CALENDAR));
    assertEquals("Adjusted date", aTuesday, ScheduleCalculator.getAdjustedDate(aMonday, 1, CALENDAR));
    assertEquals("Adjusted date", aMonday2, ScheduleCalculator.getAdjustedDate(aFriday, 1, CALENDAR));
    assertEquals("Adjusted date", aTuesday2, ScheduleCalculator.getAdjustedDate(aSaturday, 1, CALENDAR));
    assertEquals("Adjusted date", aTuesday2, ScheduleCalculator.getAdjustedDate(aSunday, 1, CALENDAR));
    assertEquals("Adjusted date", aWednesday, ScheduleCalculator.getAdjustedDate(aMonday, 2, CALENDAR));
    assertEquals("Adjusted date", aWednesday, ScheduleCalculator.getAdjustedDate(aMonday, Period.ofDays(2), CALENDAR));
    assertEquals("Adjusted date", aTuesday2, ScheduleCalculator.getAdjustedDate(aFriday, 2, CALENDAR));
    assertEquals("Adjusted date", aWednesday2, ScheduleCalculator.getAdjustedDate(aSaturday, 2, CALENDAR));
    assertEquals("Adjusted date", aWednesday2, ScheduleCalculator.getAdjustedDate(aSunday, 2, CALENDAR));
    assertEquals("Adjusted date", aMonday, ScheduleCalculator.getAdjustedDate(aWednesday, -2, CALENDAR));
    assertEquals("Adjusted date", aFriday, ScheduleCalculator.getAdjustedDate(aTuesday2, -2, CALENDAR));
    assertEquals("Adjusted date", aThursday, ScheduleCalculator.getAdjustedDate(aSaturday, -2, CALENDAR));
    assertEquals("Adjusted date", aThursday, ScheduleCalculator.getAdjustedDate(aSunday, -2, CALENDAR));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesDaysNullDate() {
    ScheduleCalculator.getAdjustedDate((ZonedDateTime) null, 2, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesDaysNullCalendar() {
    ScheduleCalculator.getAdjustedDate(NOW, 2, null);
  }

  @Test
  /**
   * Tests the adjusted dates shifted by periods. Reviewed 13-Dec-2011.
   */
  public void adjustedDatesPeriod() {
    final Period m1 = Period.ofMonths(1);
    final Period m2 = Period.ofMonths(2);
    final Period m3 = Period.ofMonths(3);
    final Period m6 = Period.ofMonths(6);
    final ZonedDateTime stdStart = DateUtils.getUTCDate(2011, 11, 15); //1m
    final ZonedDateTime stdEnd = DateUtils.getUTCDate(2011, 12, 15);
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, MOD_FOL, CALENDAR));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, MOD_FOL, CALENDAR, false));
    final ZonedDateTime ngbdStart = DateUtils.getUTCDate(2011, 11, 17); //1m
    final ZonedDateTime ngbdEnd = DateUtils.getUTCDate(2011, 12, 19);
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, MOD_FOL, CALENDAR));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, MOD_FOL, CALENDAR, false));
    final ZonedDateTime eom31NGBD = DateUtils.getUTCDate(2011, 7, 29);
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, MOD_FOL, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, MOD_FOL, CALENDAR, false));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 29), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, MOD_FOL, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 30), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 29), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, MOD_FOL, CALENDAR, false));
    final ZonedDateTime eom30 = DateUtils.getUTCDate(2011, 11, 30);
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 30), ScheduleCalculator.getAdjustedDate(eom30, m6, MOD_FOL, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 31), ScheduleCalculator.getAdjustedDate(eom30, m6, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 30), ScheduleCalculator.getAdjustedDate(eom30, m6, MOD_FOL, CALENDAR, false));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 30), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 31), ScheduleCalculator.getAdjustedDate(eom30, m6, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, INDEX_EURIBOR6M, CALENDAR));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, INDEX_EURIBOR6M, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, INDEX_EURIBOR6M, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 30), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, INDEX_EURIBOR6M, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 31), ScheduleCalculator.getAdjustedDate(eom30, m6, INDEX_EURIBOR6M, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 31), ScheduleCalculator.getAdjustedDate(eom30, INDEX_EURIBOR6M, CALENDAR));
    assertArrayEquals("Adjusted date", new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 31), DateUtils.getUTCDate(2012, 5, 31) },
        ScheduleCalculator.getAdjustedDate(new ZonedDateTime[] {eom30, eom30 }, INDEX_EURIBOR6M, CALENDAR));
    //    ZonedDateTime eom31 = DateUtils.getUTCDate(2011, 10, 31);
    //    ZonedDateTime eom30NGBD = DateUtils.getUTCDate(2011, 4, 29);
  }

  @Test
  /**
   * Tests the adjusted dates shifted by a tenor (including ON, TN).
   */
  public void adjustedDatesTenor() {
    final Tenor m1 = Tenor.of(Period.ofMonths(1));
    final Tenor m2 = Tenor.of(Period.ofMonths(2));
    final Tenor m3 = Tenor.of(Period.ofMonths(3));
    final ZonedDateTime stdStart = DateUtils.getUTCDate(2011, 11, 15); //1m
    final ZonedDateTime stdEnd = DateUtils.getUTCDate(2011, 12, 15);
    assertEquals("Adjusted date tenor", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, MOD_FOL, CALENDAR, false));
    final ZonedDateTime ngbdStart = DateUtils.getUTCDate(2011, 11, 17); //1m
    final ZonedDateTime ngbdEnd = DateUtils.getUTCDate(2011, 12, 19);
    assertEquals("Adjusted date tenor", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, MOD_FOL, CALENDAR, false));
    final ZonedDateTime eom31NGBD = DateUtils.getUTCDate(2011, 7, 29);
    assertEquals("Adjusted date tenor", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, MOD_FOL, CALENDAR, false));
    assertEquals("Adjusted date tenor", DateUtils.getUTCDate(2011, 9, 30), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", DateUtils.getUTCDate(2011, 9, 29), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, MOD_FOL, CALENDAR, false));
    final Tenor on = Tenor.OVERNIGHT;
    final ZonedDateTime stdStartON = DateUtils.getUTCDate(2013, 12, 19); //1m
    final ZonedDateTime stdEndON = DateUtils.getUTCDate(2013, 12, 20);
    assertEquals("Adjusted date tenor", stdEndON, ScheduleCalculator.getAdjustedDate(stdStartON, on, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", stdEndON, ScheduleCalculator.getAdjustedDate(stdStartON, on, MOD_FOL, CALENDAR, false));
    final ZonedDateTime eomStartON = DateUtils.getUTCDate(2013, 10, 31); //1m
    final ZonedDateTime eomEndON = DateUtils.getUTCDate(2013, 11, 1);
    assertEquals("Adjusted date tenor", eomEndON, ScheduleCalculator.getAdjustedDate(eomStartON, on, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", eomEndON, ScheduleCalculator.getAdjustedDate(eomStartON, on, MOD_FOL, CALENDAR, false));
    final ZonedDateTime weStartON = DateUtils.getUTCDate(2013, 12, 20); //1m
    final ZonedDateTime weEndON = DateUtils.getUTCDate(2013, 12, 23);
    assertEquals("Adjusted date tenor", weEndON, ScheduleCalculator.getAdjustedDate(weStartON, on, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", weEndON, ScheduleCalculator.getAdjustedDate(weStartON, on, MOD_FOL, CALENDAR, false));
    final Tenor tn = Tenor.TN;
    final ZonedDateTime stdStartTN = DateUtils.getUTCDate(2013, 12, 18); //1m
    final ZonedDateTime stdEndTN = DateUtils.getUTCDate(2013, 12, 20);
    assertEquals("Adjusted date tenor", stdEndTN, ScheduleCalculator.getAdjustedDate(stdStartTN, tn, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", stdEndTN, ScheduleCalculator.getAdjustedDate(stdStartTN, tn, MOD_FOL, CALENDAR, false));
    final ZonedDateTime eomStartTN = DateUtils.getUTCDate(2013, 10, 30); //1m
    final ZonedDateTime eomEndTN = DateUtils.getUTCDate(2013, 11, 1);
    assertEquals("Adjusted date tenor", eomEndTN, ScheduleCalculator.getAdjustedDate(eomStartTN, tn, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", eomEndTN, ScheduleCalculator.getAdjustedDate(eomStartTN, tn, MOD_FOL, CALENDAR, false));
    final ZonedDateTime weStartTN = DateUtils.getUTCDate(2013, 12, 20); //1m
    final ZonedDateTime weEndTN = DateUtils.getUTCDate(2013, 12, 24);
    assertEquals("Adjusted date tenor", weEndTN, ScheduleCalculator.getAdjustedDate(weStartTN, tn, MOD_FOL, CALENDAR, true));
    assertEquals("Adjusted date tenor", weEndTN, ScheduleCalculator.getAdjustedDate(weStartTN, tn, MOD_FOL, CALENDAR, false));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullDate() {
    ScheduleCalculator.getAdjustedDate(null, PAYMENT_TENOR, MOD_FOL, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullTenor() {
    ScheduleCalculator.getAdjustedDate(NOW, null, MOD_FOL, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullBusinessDay() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, null, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullCalendar() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, MOD_FOL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullDate() {
    ScheduleCalculator.getAdjustedDate(null, PAYMENT_TENOR, MOD_FOL, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullPeriod() {
    ScheduleCalculator.getAdjustedDate(NOW, (Period) null, MOD_FOL, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullTenor() {
    ScheduleCalculator.getAdjustedDate(NOW, (Tenor) null, MOD_FOL, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullBusinessDay() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, null, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullCalendar() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, MOD_FOL, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodGeneratorNullDate() {
    ScheduleCalculator.getAdjustedDate(null, PAYMENT_TENOR, GENERATOR_DEPOSIT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodGeneratorNullTenor() {
    ScheduleCalculator.getAdjustedDate(NOW, null, GENERATOR_DEPOSIT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodGeneratorNullGenerator() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, (GeneratorDeposit) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodIndexNullDate() {
    ScheduleCalculator.getAdjustedDate((ZonedDateTime) null, INDEX_EURIBOR6M, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodIndexNullIndex() {
    ScheduleCalculator.getAdjustedDate(NOW, (IborIndex) null, CALENDAR);
  }

  @Test
  /**
   * Tests the unadjusted date schedule. Reviewed 19-Jan-2012.
   */
  public void unadjustedDateSchedule() {
    final Period m6 = Period.ofMonths(6);
    final Period m15 = Period.ofMonths(15);
    final Period m30 = Period.ofMonths(30);
    final Period y1 = Period.ofYears(1);
    final Period y2 = Period.ofYears(2);
    final ZonedDateTime midMonth = DateUtils.getUTCDate(2012, 1, 19);
    final ZonedDateTime monthEndDec = DateUtils.getUTCDate(2011, 12, 31);
    final ZonedDateTime monthEndJan = DateUtils.getUTCDate(2012, 1, 31);
    final ZonedDateTime monthEndFeb = DateUtils.getUTCDate(2012, 2, 29);
    //    final StubType stubLongStart = StubType.LONG_START; // false-true
    //    final StubType stubLongEnd = StubType.LONG_END; // false-false
    //    final StubType stubShortStart = StubType.SHORT_START; // true-true
    //    final StubType stubShortEnd = StubType.SHORT_END; // true-false
    final ZonedDateTime[] scheduleMidMonth2YStubType = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, StubType.LONG_END);
    final ZonedDateTime[] scheduleMidMonth2Y = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, false, false);
    final ZonedDateTime[] scheduleMidMonth2YExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 1, 19), DateUtils.getUTCDate(2013, 7, 19),
      DateUtils.getUTCDate(2014, 1, 19) };
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, scheduleMidMonth2Y);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, scheduleMidMonth2YStubType);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, true, false));
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, false, true));
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, true, true));

    final ZonedDateTime[] scheduleMidMonth30MStubLE = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, StubType.LONG_END);
    final ZonedDateTime[] scheduleMidMonth30MFF = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, false, false);
    final ZonedDateTime[] scheduleMidMonth30MFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 1, 19), DateUtils.getUTCDate(2014, 7, 19) };
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MFFExpected, scheduleMidMonth30MFF);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MFFExpected, scheduleMidMonth30MStubLE);
    final ZonedDateTime[] scheduleMidMonth30MSE = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, StubType.SHORT_END);
    final ZonedDateTime[] scheduleMidMonth30MTF = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, true, false);
    final ZonedDateTime[] scheduleMidMonth30MTFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 1, 19), DateUtils.getUTCDate(2014, 1, 19), DateUtils.getUTCDate(2014, 7, 19) };
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MTFExpected, scheduleMidMonth30MTF);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MTFExpected, scheduleMidMonth30MSE);
    final ZonedDateTime[] scheduleMidMonth30MLS = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, StubType.LONG_START);
    final ZonedDateTime[] scheduleMidMonth30MFT = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, false, true);
    final ZonedDateTime[] scheduleMidMonth30MFTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 7, 19), DateUtils.getUTCDate(2014, 7, 19) };
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MFTExpected, scheduleMidMonth30MFT);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MFTExpected, scheduleMidMonth30MLS);
    final ZonedDateTime[] scheduleMidMonth30MSS = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, StubType.SHORT_START);
    final ZonedDateTime[] scheduleMidMonth30MTT = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, true, true);
    final ZonedDateTime[] scheduleMidMonth30MTTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 7, 19), DateUtils.getUTCDate(2014, 7, 19) };
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MTTExpected, scheduleMidMonth30MTT);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MTTExpected, scheduleMidMonth30MSS);

    final ZonedDateTime[] scheduleMonthEndDec1YLE = ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, StubType.LONG_END);
    final ZonedDateTime[] scheduleMonthEndDec1YFF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, false, false);
    final ZonedDateTime[] scheduleMonthEndDec1YFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 12, 31) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, scheduleMonthEndDec1YLE);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, scheduleMonthEndDec1YFF);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, false, false));
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, true, false));
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, false, true));
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, true, true));

    final ZonedDateTime[] scheduleMonthEndJan15MLE = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, StubType.LONG_END);
    final ZonedDateTime[] scheduleMonthEndJan15MFF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, false, false);
    final ZonedDateTime[] scheduleMonthEndJan15MFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2013, 4, 30) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MFFExpected, scheduleMonthEndJan15MLE);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MFFExpected, scheduleMonthEndJan15MFF);
    final ZonedDateTime[] scheduleMonthEndJan15MSE = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, StubType.SHORT_END);
    final ZonedDateTime[] scheduleMonthEndJan15MTF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, true, false);
    final ZonedDateTime[] scheduleMonthEndJan15MTFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2013, 1, 31), DateUtils.getUTCDate(2013, 4, 30) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MTFExpected, scheduleMonthEndJan15MSE);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MTFExpected, scheduleMonthEndJan15MTF);
    final ZonedDateTime[] scheduleMonthEndJan15MLS = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, StubType.LONG_START);
    final ZonedDateTime[] scheduleMonthEndJan15MFT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, false, true);
    final ZonedDateTime[] scheduleMonthEndJan15MFTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 10, 30), DateUtils.getUTCDate(2013, 4, 30) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MFTExpected, scheduleMonthEndJan15MLS);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MFTExpected, scheduleMonthEndJan15MFT);
    final ZonedDateTime[] scheduleMonthEndJan15MSS = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, StubType.SHORT_START);
    final ZonedDateTime[] scheduleMonthEndJan15MTT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, true, true);
    final ZonedDateTime[] scheduleMonthEndJan15MTTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 4, 30), DateUtils.getUTCDate(2012, 10, 30), DateUtils.getUTCDate(2013, 4, 30) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MTTExpected, scheduleMonthEndJan15MSS);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MTTExpected, scheduleMonthEndJan15MTT);

    final ZonedDateTime[] scheduleMonthEndFeb15MLE = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, StubType.LONG_END);
    final ZonedDateTime[] scheduleMonthEndFeb15MFF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, false, false);
    final ZonedDateTime[] scheduleMonthEndFeb15MFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 29), DateUtils.getUTCDate(2013, 5, 29) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MFFExpected, scheduleMonthEndFeb15MLE);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MFFExpected, scheduleMonthEndFeb15MFF);
    final ZonedDateTime[] scheduleMonthEndFeb15MSE = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, StubType.SHORT_END);
    final ZonedDateTime[] scheduleMonthEndFeb15MTF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, true, false);
    final ZonedDateTime[] scheduleMonthEndFeb15MTFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 29), DateUtils.getUTCDate(2013, 2, 28), DateUtils.getUTCDate(2013, 5, 29) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MTFExpected, scheduleMonthEndFeb15MSE);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MTFExpected, scheduleMonthEndFeb15MTF);
    final ZonedDateTime[] scheduleMonthEndFeb15MLS = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, StubType.LONG_START);
    final ZonedDateTime[] scheduleMonthEndFeb15MFT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, false, true);
    final ZonedDateTime[] scheduleMonthEndFeb15MFTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 11, 29), DateUtils.getUTCDate(2013, 5, 29) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MFTExpected, scheduleMonthEndFeb15MLS);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MFTExpected, scheduleMonthEndFeb15MFT);
    final ZonedDateTime[] scheduleMonthEndFeb15MSS = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, StubType.SHORT_START);
    final ZonedDateTime[] scheduleMonthEndFeb15MTT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, true, true);
    final ZonedDateTime[] scheduleMonthEndFeb15MTTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 29), DateUtils.getUTCDate(2012, 11, 29), DateUtils.getUTCDate(2013, 5, 29) };
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MTTExpected, scheduleMonthEndFeb15MSS);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MTTExpected, scheduleMonthEndFeb15MTT);
  }

  @Test
  /**
   * Tests the adjusted date schedule. Reviewed 23-Jan-2012.
   */
  public void adjustedDateSchedule1() {
    final Period m6 = Period.ofMonths(6);
    final Period y2 = Period.ofYears(2);
    final Period y3 = Period.ofYears(3);
    final ZonedDateTime midMonth = DateUtils.getUTCDate(2012, 1, 19);
    final ZonedDateTime monthEndMarch = DateUtils.getUTCDate(2012, 3, 30);
    final ZonedDateTime[] midMonthUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, false, false);
    final ZonedDateTime[] midMonthModFol = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, MOD_FOL, CALENDAR, false);
    final ZonedDateTime[] midMonthModFolExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 1, 21), DateUtils.getUTCDate(2013, 7, 19),
      DateUtils.getUTCDate(2014, 1, 20) };
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFol);
    final ZonedDateTime[] midMonthFol = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthFol);
    final ZonedDateTime[] midMonthPre = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, PRE, CALENDAR, false);
    final ZonedDateTime[] midMonthPreExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 1, 18), DateUtils.getUTCDate(2013, 7, 19),
      DateUtils.getUTCDate(2014, 1, 17) };
    assertArrayEquals("Adjusted schedule", midMonthPreExpected, midMonthPre);
    final ZonedDateTime[] midMonthEOM = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, MOD_FOL, CALENDAR, true); // Not natural to apply EOM when in mid month, nut this is only a test!
    final ZonedDateTime[] midMonthEOMExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2013, 1, 31), DateUtils.getUTCDate(2013, 7, 31),
      DateUtils.getUTCDate(2014, 1, 31) };
    assertArrayEquals("Adjusted schedule", midMonthEOMExpected, midMonthEOM);
    final ZonedDateTime[] endMarchUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(monthEndMarch, monthEndMarch.plus(y3), m6, false, false);
    final ZonedDateTime[] endMarchModFol = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, MOD_FOL, CALENDAR, false);
    final ZonedDateTime[] endMarchModFolExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29), DateUtils.getUTCDate(2013, 9, 30),
      DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 30) };
    assertArrayEquals("Adjusted schedule", endMarchModFolExpected, endMarchModFol);
    final ZonedDateTime[] endMarchModFolEOM = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, MOD_FOL, CALENDAR, true);
    final ZonedDateTime[] endMarchModFolExpectedEOM = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29), DateUtils.getUTCDate(2013, 9, 30),
      DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 31) };
    assertArrayEquals("Adjusted schedule", endMarchModFolExpectedEOM, endMarchModFolEOM);
    final ZonedDateTime[] endMarchPre = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, PRE, CALENDAR, false);
    final ZonedDateTime[] endMarchPreExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29), DateUtils.getUTCDate(2013, 9, 30),
      DateUtils.getUTCDate(2014, 3, 28), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 30) };
    assertArrayEquals("Adjusted schedule", endMarchPreExpected, endMarchPre);
    final ZonedDateTime[] endMarchFol = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, FOL, CALENDAR, false);
    final ZonedDateTime[] endMarchFolExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 10, 1), DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 9, 30),
      DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 30) };
    assertArrayEquals("Adjusted schedule", endMarchFolExpected, endMarchFol);
    final ZonedDateTime[] endMarchFolEOM = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, FOL, CALENDAR, true);
    assertArrayEquals("Adjusted schedule", endMarchModFolExpectedEOM, endMarchFolEOM);
  }

  @Test
  /**
   * Tests the adjusted date schedule. Reviewed 22-Feb-2012.
   */
  public void adjustedDateSchedule2() {
    final Period m6 = Period.ofMonths(6);
    final Frequency semi = PeriodFrequency.SEMI_ANNUAL;
    final Period y5 = Period.ofYears(5);
    final ZonedDateTime midMonth = DateUtils.getUTCDate(2012, 1, 19);
    final ZonedDateTime[] midMonthUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y5), m6, false, false);
    final ZonedDateTime[] midMonthModFolExpected = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, MOD_FOL, CALENDAR, false);
    final ZonedDateTime[] midMonthModFolDateStub = ScheduleCalculator.getAdjustedDateSchedule(midMonth, midMonth.plus(y5), m6, StubType.LONG_START, MOD_FOL, CALENDAR, false);
    final ZonedDateTime[] midMonthModFolDate = ScheduleCalculator.getAdjustedDateSchedule(midMonth, midMonth.plus(y5), m6, false, false, MOD_FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolDate);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolDateStub);
    final ZonedDateTime[] midMonthModFolTenor = ScheduleCalculator.getAdjustedDateSchedule(midMonth, y5, m6, false, false, MOD_FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolTenor);
    final IborIndex ibor = new IborIndex(Currency.EUR, m6, 0, DayCounts.ACT_360, MOD_FOL, false, "Ibor");
    final ZonedDateTime[] midMonthModFolIbor = ScheduleCalculator.getAdjustedDateSchedule(midMonth, y5, false, false, ibor, CALENDAR);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolIbor);
    final ZonedDateTime[] midMonthModFolFreq = ScheduleCalculator.getAdjustedDateSchedule(midMonth, midMonth.plus(y5), semi, false, false, MOD_FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolFreq);
  }

  // TODO: review

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEffectiveDate1() {
    ScheduleCalculator.getUnadjustedDateSchedule(null, DateUtils.getUTCDate(2010, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEffectiveDate2() {
    ScheduleCalculator.getUnadjustedDateSchedule(null, DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualDate() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), null, DateUtils.getUTCDate(2010, 7, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate1() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), null, PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate2() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), null, PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency1() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency2() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), (PeriodFrequency) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadMaturityDate1() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2009, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadMaturityDate2() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2009, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadMaturityDate3() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2008, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2009, 6, 1), PeriodFrequency.ANNUAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongFrequencyType() {
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), new Frequency() {

      @Override
      public String getConventionName() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

    });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateArray1() {
    ScheduleCalculator.getAdjustedDateSchedule(null, new ModifiedFollowingBusinessDayConvention(), ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateArray2() {
    ScheduleCalculator.getTimes(null, new ThirtyEThreeSixty(), NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyDateArray1() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[0], new ModifiedFollowingBusinessDayConvention(), ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmtpyDateArray2() {
    ScheduleCalculator.getTimes(new ZonedDateTime[0], new ThirtyEThreeSixty(), NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1) }, null, ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1) }, new ModifiedFollowingBusinessDayConvention(), null, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    ScheduleCalculator.getTimes(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1) }, null, NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    ScheduleCalculator.getTimes(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1) }, new ThirtyEThreeSixty(), null);
  }

  @Test
  public void testUnadjustedDates() {
    final ZonedDateTime effective = DateUtils.getUTCDate(2010, 6, 1);
    final ZonedDateTime accrual = DateUtils.getUTCDate(2010, 9, 1);
    final ZonedDateTime maturity = DateUtils.getUTCDate(2015, 6, 1);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.ANNUAL), 5, DateUtils.getUTCDate(2011, 6, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.ANNUAL), 5, DateUtils.getUTCDate(2011, 6, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.SEMI_ANNUAL), 10, DateUtils.getUTCDate(2010, 12, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.SEMI_ANNUAL), 10, DateUtils.getUTCDate(2010, 12, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.QUARTERLY), 20, DateUtils.getUTCDate(2010, 9, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.QUARTERLY), 20, DateUtils.getUTCDate(2010, 9, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.MONTHLY), 60, DateUtils.getUTCDate(2010, 7, 1), maturity);
    assertUnadjustedDates(ScheduleCalculator.getUnadjustedDateSchedule(effective, accrual, maturity, PeriodFrequency.MONTHLY), 60, DateUtils.getUTCDate(2010, 7, 1), maturity);
  }

  @Test
  public void testAdjustedDates() {
    final ZonedDateTime effective = DateUtils.getUTCDate(2010, 1, 1);
    final ZonedDateTime maturity = DateUtils.getUTCDate(2011, 1, 1);
    final ZonedDateTime[] unadjusted = ScheduleCalculator.getUnadjustedDateSchedule(effective, maturity, PeriodFrequency.MONTHLY);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), ALL), unadjusted);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), ALL), unadjusted);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), ALL), unadjusted);
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 1),
          DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
          DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 3) });
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 1),
          DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
          DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 3) });
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 6, 1),
          DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 7, 30), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
          DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2010, 12, 31) });
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 2), DateUtils.getUTCDate(2010, 3, 2), DateUtils.getUTCDate(2010, 4, 2), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 2),
          DateUtils.getUTCDate(2010, 7, 2), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 2), DateUtils.getUTCDate(2010, 10, 4), DateUtils.getUTCDate(2010, 11, 2),
          DateUtils.getUTCDate(2010, 12, 2), DateUtils.getUTCDate(2011, 1, 3) });
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 2), DateUtils.getUTCDate(2010, 3, 2), DateUtils.getUTCDate(2010, 4, 2), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 2),
          DateUtils.getUTCDate(2010, 7, 2), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 2), DateUtils.getUTCDate(2010, 10, 4), DateUtils.getUTCDate(2010, 11, 2),
          DateUtils.getUTCDate(2010, 12, 2), DateUtils.getUTCDate(2011, 1, 3) });
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 29), DateUtils.getUTCDate(2010, 2, 26), DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 4, 30),
          DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 30), DateUtils.getUTCDate(2010, 8, 31), DateUtils.getUTCDate(2010, 9, 30),
          DateUtils.getUTCDate(2010, 10, 29), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31) });
    // End date is modified
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18) });
    // Check modified in modified following.
    final ZonedDateTime settlementDateModified = DateUtils.getUTCDate(2011, 3, 31);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(settlementDateModified, ANNUITY_TENOR, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2012, 3, 30), DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29) });
    // End-of-month
    final ZonedDateTime settlementDateEOM = DateUtils.getUTCDate(2011, 2, 28);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(settlementDateEOM, ANNUITY_TENOR, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 8, 31), DateUtils.getUTCDate(2013, 2, 28) });
    // Stub: short-last
    final Period tenorLong = Period.ofMonths(27);
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorLong, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18),
          DateUtils.getUTCDate(2013, 6, 17) });
    // Stub: long-last
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorLong, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, !SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 6, 17) });
    // Stub: very short period: short stub.
    final Period tenorVeryShort = Period.ofMonths(3);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorVeryShort, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 6, 17) });
    // Stub: very short period: long stub.
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorVeryShort, PAYMENT_TENOR, MOD_FOL, CALENDAR, IS_EOM, !SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 6, 17) });
  }

  @Test
  public void testPaymentTimes() {
    final DayCount daycount = new DayCount() {

      @Override
      public String getConventionName() {
        return "";
      }

      @Override
      public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
        return ((double) (secondDate.getMonthValue() - firstDate.getMonthValue())) / 12;
      }

      @Override
      public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
        return 0;
      }

      @Override
      public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
        return ((double) (secondDate.getMonthValue() - firstDate.getMonthValue())) / 12;
      }

      @Override
      public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
        return 0;
      }

      @Override
      public double getDayCountFraction(LocalDate firstDate, LocalDate secondDate, Calendar calendar) {
        return 0;
      }

      @Override
      public double getDayCountFraction(ZonedDateTime firstDate, ZonedDateTime secondDate, Calendar calendar) {
        return 0;
      }

      @Override
      public String getName() {
        return "";
      }

    };
    final ZonedDateTime now = DateUtils.getUTCDate(2010, 1, 1);
    final ZonedDateTime dates[] = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1),
      DateUtils.getUTCDate(2010, 5, 1), DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), DateUtils.getUTCDate(2010, 9, 1),
      DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2010, 12, 1) };
    final double[] times = ScheduleCalculator.getTimes(dates, daycount, now);
    assertEquals(times.length, dates.length);
    for (int i = 0; i < times.length; i++) {
      assertEquals(times[i], i / 12., 1e-15);
    }
  }

  private void assertUnadjustedDates(final ZonedDateTime[] dates, final int length, final ZonedDateTime first, final ZonedDateTime last) {
    assertEquals(dates.length, length);
    assertEquals(dates[0], first);
    assertEquals(dates[length - 1], last);
  }

  private void assertDateArray(final ZonedDateTime[] dates1, final ZonedDateTime[] dates2) {
    assertEquals(dates1.length, dates2.length);
    for (int i = 0; i < dates1.length; i++) {
      assertEquals(dates1[i], dates2[i]);
    }
  }

  private static class FirstOfMonthCalendar implements Calendar {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      final DayOfWeek day = date.getDayOfWeek();
      if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
        return false;
      }
      if (date.getDayOfMonth() == 1) {
        return false;
      }
      return true;
    }

    @Override
    public String getName() {
      return "";
    }
  }

  private static class WeekendCalendar implements Calendar {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      final DayOfWeek day = date.getDayOfWeek();
      if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
        return false;
      }
      return true;
    }

    @Override
    public String getName() {
      return "";
    }
  }

  private static class AllCalendar implements Calendar {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      return true;
    }

    @Override
    public String getName() {
      return "";
    }
  }
}
