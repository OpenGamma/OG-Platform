/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.ModifiedFollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.ThirtyEThreeSixty;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@SuppressWarnings("synthetic-access")
public class ScheduleCalculatorTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final GeneratorDeposit GENERATOR_DEPOSIT = new EURDeposit(CALENDAR);
  private static final IborIndex INDEX_EURIBOR6M = new EURIBOR6M(CALENDAR);

  private static final Calendar ALL = new AllCalendar();
  private static final Calendar WEEKEND = new WeekendCalendar();
  private static final Calendar FIRST = new FirstOfMonthCalendar();
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2010, 1, 1);

  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final boolean SHORT_STUB = true;

  private static final BusinessDayConvention MOD_FOL = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOL = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final BusinessDayConvention PRE = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding");

  @Test
  /**
   * Tests the adjusted dates shifted by a number of days. Reviewed 13-Dec-2011.
   */
  public void adjustedDatesDays() {
    ZonedDateTime aMonday = DateUtils.getUTCDate(2011, 12, 12);
    ZonedDateTime aTuesday = DateUtils.getUTCDate(2011, 12, 13);
    ZonedDateTime aWednesday = DateUtils.getUTCDate(2011, 12, 14);
    ZonedDateTime aThursday = DateUtils.getUTCDate(2011, 12, 15);
    ZonedDateTime aFriday = DateUtils.getUTCDate(2011, 12, 16);
    ZonedDateTime aSaturday = DateUtils.getUTCDate(2011, 12, 17);
    ZonedDateTime aSunday = DateUtils.getUTCDate(2011, 12, 18);
    ZonedDateTime aMonday2 = DateUtils.getUTCDate(2011, 12, 19);
    ZonedDateTime aTuesday2 = DateUtils.getUTCDate(2011, 12, 20);
    ZonedDateTime aWednesday2 = DateUtils.getUTCDate(2011, 12, 21);
    assertEquals("Adjusted date", aMonday, ScheduleCalculator.getAdjustedDate(aMonday, 0, CALENDAR));
    assertEquals("Adjusted date", aFriday, ScheduleCalculator.getAdjustedDate(aFriday, 0, CALENDAR));
    assertEquals("Adjusted date", aMonday2, ScheduleCalculator.getAdjustedDate(aSaturday, 0, CALENDAR));
    assertEquals("Adjusted date", aMonday2, ScheduleCalculator.getAdjustedDate(aSunday, 0, CALENDAR));
    assertEquals("Adjusted date", aTuesday, ScheduleCalculator.getAdjustedDate(aMonday, 1, CALENDAR));
    assertEquals("Adjusted date", aMonday2, ScheduleCalculator.getAdjustedDate(aFriday, 1, CALENDAR));
    assertEquals("Adjusted date", aTuesday2, ScheduleCalculator.getAdjustedDate(aSaturday, 1, CALENDAR));
    assertEquals("Adjusted date", aTuesday2, ScheduleCalculator.getAdjustedDate(aSunday, 1, CALENDAR));
    assertEquals("Adjusted date", aWednesday, ScheduleCalculator.getAdjustedDate(aMonday, 2, CALENDAR));
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
   * Tests the adjusted dates shifted by a number of days. Reviewed 13-Dec-2011.
   */
  public void adjustedDatesPeriod() {
    Period m1 = Period.ofMonths(1);
    Period m2 = Period.ofMonths(2);
    Period m3 = Period.ofMonths(3);
    Period m6 = Period.ofMonths(6);
    ZonedDateTime stdStart = DateUtils.getUTCDate(2011, 11, 15); //1m
    ZonedDateTime stdEnd = DateUtils.getUTCDate(2011, 12, 15);
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, BUSINESS_DAY, CALENDAR));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, BUSINESS_DAY, CALENDAR, true));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, BUSINESS_DAY, CALENDAR, false));
    ZonedDateTime ngbdStart = DateUtils.getUTCDate(2011, 11, 17); //1m
    ZonedDateTime ngbdEnd = DateUtils.getUTCDate(2011, 12, 19);
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, BUSINESS_DAY, CALENDAR));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, BUSINESS_DAY, CALENDAR, true));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, BUSINESS_DAY, CALENDAR, false));
    ZonedDateTime eom31NGBD = DateUtils.getUTCDate(2011, 7, 29);
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, BUSINESS_DAY, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, BUSINESS_DAY, CALENDAR, true));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, BUSINESS_DAY, CALENDAR, false));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 29), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, BUSINESS_DAY, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 30), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, BUSINESS_DAY, CALENDAR, true));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 29), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, BUSINESS_DAY, CALENDAR, false));
    ZonedDateTime eom30 = DateUtils.getUTCDate(2011, 11, 30);
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 30), ScheduleCalculator.getAdjustedDate(eom30, m6, BUSINESS_DAY, CALENDAR));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 31), ScheduleCalculator.getAdjustedDate(eom30, m6, BUSINESS_DAY, CALENDAR, true));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 30), ScheduleCalculator.getAdjustedDate(eom30, m6, BUSINESS_DAY, CALENDAR, false));
    assertEquals("Adjusted date", stdEnd, ScheduleCalculator.getAdjustedDate(stdStart, m1, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", ngbdEnd, ScheduleCalculator.getAdjustedDate(ngbdStart, m1, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 10, 31), ScheduleCalculator.getAdjustedDate(eom31NGBD, m3, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2011, 9, 30), ScheduleCalculator.getAdjustedDate(eom31NGBD, m2, GENERATOR_DEPOSIT));
    assertEquals("Adjusted date", DateUtils.getUTCDate(2012, 5, 31), ScheduleCalculator.getAdjustedDate(eom30, m6, GENERATOR_DEPOSIT));
    //    ZonedDateTime eom31 = DateUtils.getUTCDate(2011, 10, 31);
    //    ZonedDateTime eom30NGBD = DateUtils.getUTCDate(2011, 4, 29);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullDate() {
    ScheduleCalculator.getAdjustedDate(null, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullTenor() {
    ScheduleCalculator.getAdjustedDate(NOW, null, BUSINESS_DAY, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullBusinessDay() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, null, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodNullCalendar() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, BUSINESS_DAY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullDate() {
    ScheduleCalculator.getAdjustedDate(null, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullTenor() {
    ScheduleCalculator.getAdjustedDate(NOW, null, BUSINESS_DAY, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullBusinessDay() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, null, CALENDAR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodEOMNullCalendar() {
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, BUSINESS_DAY, null, true);
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
    ScheduleCalculator.getAdjustedDate(NOW, PAYMENT_TENOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodIndexNullDate() {
    ScheduleCalculator.getAdjustedDate(null, INDEX_EURIBOR6M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void adjustedDatesPeriodIndexNullIndex() {
    ScheduleCalculator.getAdjustedDate(NOW, null);
  }

  @Test
  /**
   * Tests the unadjusted date schedule. Reviewed 19-Jan-2012.
   */
  public void unadjustedDateSchedule() {
    Period m6 = Period.ofMonths(6);
    Period m15 = Period.ofMonths(15);
    Period m30 = Period.ofMonths(30);
    Period y1 = Period.ofYears(1);
    Period y2 = Period.ofYears(2);
    ZonedDateTime midMonth = DateUtils.getUTCDate(2012, 1, 19);
    ZonedDateTime monthEndDec = DateUtils.getUTCDate(2011, 12, 31);
    ZonedDateTime monthEndJan = DateUtils.getUTCDate(2012, 1, 31);
    ZonedDateTime monthEndFeb = DateUtils.getUTCDate(2012, 2, 29);
    ZonedDateTime[] scheduleMidMonth2Y = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, false, false);
    ZonedDateTime[] scheduleMidMonth2YExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 1, 19), DateUtils.getUTCDate(2013, 7, 19),
        DateUtils.getUTCDate(2014, 1, 19)};
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, scheduleMidMonth2Y);
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, true, false));
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, false, true));
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth2YExpected, ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, true, true));

    ZonedDateTime[] scheduleMidMonth30MFF = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, false, false);
    ZonedDateTime[] scheduleMidMonth30MFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 1, 19), DateUtils.getUTCDate(2014, 7, 19)};
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MFFExpected, scheduleMidMonth30MFF);
    ZonedDateTime[] scheduleMidMonth30MTF = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, true, false);
    ZonedDateTime[] scheduleMidMonth30MTFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 1, 19), DateUtils.getUTCDate(2014, 1, 19), DateUtils.getUTCDate(2014, 7, 19)};
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MTFExpected, scheduleMidMonth30MTF);
    ZonedDateTime[] scheduleMidMonth30MFT = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, false, true);
    ZonedDateTime[] scheduleMidMonth30MFTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 7, 19), DateUtils.getUTCDate(2014, 7, 19)};
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MFTExpected, scheduleMidMonth30MFT);
    ZonedDateTime[] scheduleMidMonth30MTT = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(m30), y1, true, true);
    ZonedDateTime[] scheduleMidMonth30MTTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 7, 19), DateUtils.getUTCDate(2014, 7, 19)};
    assertArrayEquals("Unadjusted schedule", scheduleMidMonth30MTTExpected, scheduleMidMonth30MTT);

    ZonedDateTime[] scheduleMonthEndDec1YFF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, false, false);
    ZonedDateTime[] scheduleMonthEndDec1YFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 12, 31)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, scheduleMonthEndDec1YFF);
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, false, false));
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, true, false));
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, false, true));
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndDec1YFFExpected, ScheduleCalculator.getUnadjustedDateSchedule(monthEndDec, monthEndDec.plus(y1), m6, true, true));

    ZonedDateTime[] scheduleMonthEndJan15MFF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, false, false);
    ZonedDateTime[] scheduleMonthEndJan15MFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2013, 4, 30)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MFFExpected, scheduleMonthEndJan15MFF);
    ZonedDateTime[] scheduleMonthEndJan15MTF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, true, false);
    ZonedDateTime[] scheduleMonthEndJan15MTFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2013, 1, 31), DateUtils.getUTCDate(2013, 4, 30)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MTFExpected, scheduleMonthEndJan15MTF);
    ZonedDateTime[] scheduleMonthEndJan15MFT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, false, true);
    ZonedDateTime[] scheduleMonthEndJan15MFTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 10, 30), DateUtils.getUTCDate(2013, 4, 30)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MFTExpected, scheduleMonthEndJan15MFT);
    ZonedDateTime[] scheduleMonthEndJan15MTT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndJan, monthEndJan.plus(m15), m6, true, true);
    ZonedDateTime[] scheduleMonthEndJan15MTTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 4, 30), DateUtils.getUTCDate(2012, 10, 30), DateUtils.getUTCDate(2013, 4, 30)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndJan15MTTExpected, scheduleMonthEndJan15MTT);

    ZonedDateTime[] scheduleMonthEndFeb15MFF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, false, false);
    ZonedDateTime[] scheduleMonthEndFeb15MFFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 29), DateUtils.getUTCDate(2013, 5, 29)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MFFExpected, scheduleMonthEndFeb15MFF);
    ZonedDateTime[] scheduleMonthEndFeb15MTF = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, true, false);
    ZonedDateTime[] scheduleMonthEndFeb15MTFExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 29), DateUtils.getUTCDate(2013, 2, 28), DateUtils.getUTCDate(2013, 5, 29)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MTFExpected, scheduleMonthEndFeb15MTF);
    ZonedDateTime[] scheduleMonthEndFeb15MFT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, false, true);
    ZonedDateTime[] scheduleMonthEndFeb15MFTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 11, 29), DateUtils.getUTCDate(2013, 5, 29)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MFTExpected, scheduleMonthEndFeb15MFT);
    ZonedDateTime[] scheduleMonthEndFeb15MTT = ScheduleCalculator.getUnadjustedDateSchedule(monthEndFeb, monthEndFeb.plus(m15), m6, true, true);
    ZonedDateTime[] scheduleMonthEndFeb15MTTExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 29), DateUtils.getUTCDate(2012, 11, 29), DateUtils.getUTCDate(2013, 5, 29)};
    assertArrayEquals("Unadjusted schedule", scheduleMonthEndFeb15MTTExpected, scheduleMonthEndFeb15MTT);
  }

  @Test
  /**
   * Tests the adjusted date schedule. Reviewed 23-Jan-2012.
   */
  public void adjustedDateSchedule1() {
    Period m6 = Period.ofMonths(6);
    Period y2 = Period.ofYears(2);
    Period y3 = Period.ofYears(3);
    ZonedDateTime midMonth = DateUtils.getUTCDate(2012, 1, 19);
    ZonedDateTime monthEndMarch = DateUtils.getUTCDate(2012, 3, 30);
    ZonedDateTime[] midMonthUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y2), m6, false, false);
    ZonedDateTime[] midMonthModFol = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, MOD_FOL, CALENDAR, false);
    ZonedDateTime[] midMonthModFolExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 1, 21), DateUtils.getUTCDate(2013, 7, 19),
        DateUtils.getUTCDate(2014, 1, 20)};
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFol);
    ZonedDateTime[] midMonthFol = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthFol);
    ZonedDateTime[] midMonthPre = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, PRE, CALENDAR, false);
    ZonedDateTime[] midMonthPreExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 19), DateUtils.getUTCDate(2013, 1, 18), DateUtils.getUTCDate(2013, 7, 19),
        DateUtils.getUTCDate(2014, 1, 17)};
    assertArrayEquals("Adjusted schedule", midMonthPreExpected, midMonthPre);
    ZonedDateTime[] midMonthEOM = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, MOD_FOL, CALENDAR, true); // Not natural to apply EOM when in mid month, nut this is only a test!
    ZonedDateTime[] midMonthEOMExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2013, 1, 31), DateUtils.getUTCDate(2013, 7, 31),
        DateUtils.getUTCDate(2014, 1, 31)};
    assertArrayEquals("Adjusted schedule", midMonthEOMExpected, midMonthEOM);
    ZonedDateTime[] endMarchUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(monthEndMarch, monthEndMarch.plus(y3), m6, false, false);
    ZonedDateTime[] endMarchModFol = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, MOD_FOL, CALENDAR, false);
    ZonedDateTime[] endMarchModFolExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29), DateUtils.getUTCDate(2013, 9, 30),
        DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 30)};
    assertArrayEquals("Adjusted schedule", endMarchModFolExpected, endMarchModFol);
    ZonedDateTime[] endMarchModFolEOM = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, MOD_FOL, CALENDAR, true);
    ZonedDateTime[] endMarchModFolExpectedEOM = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29), DateUtils.getUTCDate(2013, 9, 30),
        DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 31)};
    assertArrayEquals("Adjusted schedule", endMarchModFolExpectedEOM, endMarchModFolEOM);
    ZonedDateTime[] endMarchPre = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, PRE, CALENDAR, false);
    ZonedDateTime[] endMarchPreExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29), DateUtils.getUTCDate(2013, 9, 30),
        DateUtils.getUTCDate(2014, 3, 28), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 30)};
    assertArrayEquals("Adjusted schedule", endMarchPreExpected, endMarchPre);
    ZonedDateTime[] endMarchFol = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, FOL, CALENDAR, false);
    ZonedDateTime[] endMarchFolExpected = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 10, 1), DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 9, 30),
        DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 9, 30), DateUtils.getUTCDate(2015, 3, 30)};
    assertArrayEquals("Adjusted schedule", endMarchFolExpected, endMarchFol);
    ZonedDateTime[] endMarchFolEOM = ScheduleCalculator.getAdjustedDateSchedule(endMarchUnadjusted, FOL, CALENDAR, true);
    assertArrayEquals("Adjusted schedule", endMarchModFolExpectedEOM, endMarchFolEOM);
  }

  @Test
  /**
   * Tests the adjusted date schedule. Reviewed 30-Jan-2012.
   */
  public void adjustedDateSchedule2() {
    Period m6 = Period.ofMonths(6);
    Period y5 = Period.ofYears(5);
    ZonedDateTime midMonth = DateUtils.getUTCDate(2012, 1, 19);
    ZonedDateTime[] midMonthUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(midMonth, midMonth.plus(y5), m6, false, false);
    ZonedDateTime[] midMonthModFolExpected = ScheduleCalculator.getAdjustedDateSchedule(midMonthUnadjusted, MOD_FOL, CALENDAR, false);
    ZonedDateTime[] midMonthModFolDate = ScheduleCalculator.getAdjustedDateSchedule(midMonth, midMonth.plus(y5), m6, false, false, MOD_FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolDate);
    ZonedDateTime[] midMonthModFolTenor = ScheduleCalculator.getAdjustedDateSchedule(midMonth, y5, m6, false, false, MOD_FOL, CALENDAR, false);
    assertArrayEquals("Adjusted schedule", midMonthModFolExpected, midMonthModFolTenor);
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
    PeriodFrequency nullfrequency = null;
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), nullfrequency);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency2() {
    PeriodFrequency nullfrequency = null;
    ScheduleCalculator.getUnadjustedDateSchedule(DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), nullfrequency);
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
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, null, ALL, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    ScheduleCalculator.getAdjustedDateSchedule(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, new ModifiedFollowingBusinessDayConvention(), null, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    ScheduleCalculator.getTimes(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, null, NOW);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    ScheduleCalculator.getTimes(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 6, 1)}, new ThirtyEThreeSixty(), null);
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
            DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 1),
            DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
            DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), WEEKEND),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 6, 1),
            DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 7, 30), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
            DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2010, 12, 31)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new ModifiedFollowingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 2), DateUtils.getUTCDate(2010, 3, 2), DateUtils.getUTCDate(2010, 4, 2), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 2),
            DateUtils.getUTCDate(2010, 7, 2), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 2), DateUtils.getUTCDate(2010, 10, 4), DateUtils.getUTCDate(2010, 11, 2),
            DateUtils.getUTCDate(2010, 12, 2), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new FollowingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 2, 2), DateUtils.getUTCDate(2010, 3, 2), DateUtils.getUTCDate(2010, 4, 2), DateUtils.getUTCDate(2010, 5, 3), DateUtils.getUTCDate(2010, 6, 2),
            DateUtils.getUTCDate(2010, 7, 2), DateUtils.getUTCDate(2010, 8, 2), DateUtils.getUTCDate(2010, 9, 2), DateUtils.getUTCDate(2010, 10, 4), DateUtils.getUTCDate(2010, 11, 2),
            DateUtils.getUTCDate(2010, 12, 2), DateUtils.getUTCDate(2011, 1, 3)});
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(unadjusted, new PrecedingBusinessDayConvention(), FIRST),
        new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 29), DateUtils.getUTCDate(2010, 2, 26), DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 4, 30),
            DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 30), DateUtils.getUTCDate(2010, 8, 31), DateUtils.getUTCDate(2010, 9, 30),
            DateUtils.getUTCDate(2010, 10, 29), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31)});
    // End date is modified
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18)});
    // Check modified in modified following.
    ZonedDateTime settlementDateModified = DateUtils.getUTCDate(2011, 3, 31);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(settlementDateModified, ANNUITY_TENOR, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2012, 3, 30), DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29)});
    // End-of-month
    ZonedDateTime settlementDateEOM = DateUtils.getUTCDate(2011, 2, 28);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(settlementDateEOM, ANNUITY_TENOR, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 8, 31), DateUtils.getUTCDate(2013, 2, 28)});
    // Stub: short-last
    Period tenorLong = Period.ofMonths(27);
    assertDateArray(
        ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorLong, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18),
            DateUtils.getUTCDate(2013, 6, 17)});
    // Stub: long-last
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorLong, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, !SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 6, 17)});
    // Stub: very short period: short stub.
    Period tenorVeryShort = Period.ofMonths(3);
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorVeryShort, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 6, 17)});
    // Stub: very short period: long stub.
    assertDateArray(ScheduleCalculator.getAdjustedDateSchedule(SETTLEMENT_DATE, tenorVeryShort, PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, !SHORT_STUB),
        new ZonedDateTime[] {DateUtils.getUTCDate(2011, 6, 17)});
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
        return ((double) (secondDate.getMonthOfYear().getValue() - firstDate.getMonthOfYear().getValue())) / 12;
      }

      @Override
      public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
        return 0;
      }

    };
    final ZonedDateTime now = DateUtils.getUTCDate(2010, 1, 1);
    final ZonedDateTime dates[] = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1),
        DateUtils.getUTCDate(2010, 5, 1), DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), DateUtils.getUTCDate(2010, 9, 1),
        DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2010, 12, 1)};
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
  }
}
