/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

/**
 * 
 * 
 * @author emcleod
 */
public class DateUtilTest {
  private static final double EPS = 1e-9;

  @Test
  public void testDifferenceInYears() {
    final ZonedDateTime startDate = ZonedDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT, TimeZone.UTC);
    final ZonedDateTime endDate = ZonedDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.MIDNIGHT, TimeZone.UTC);
    try {
      DateUtil.getDifferenceInYears((InstantProvider)null, endDate);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtil.getDifferenceInYears(startDate, (InstantProvider)null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final double leapYearDays = 366;
    assertEquals(DateUtil.getDifferenceInYears((InstantProvider)startDate, endDate) * DateUtil.DAYS_PER_YEAR / leapYearDays, 1, EPS);
    try {
      DateUtil.getDifferenceInYears(null, endDate, leapYearDays);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtil.getDifferenceInYears(startDate, null, leapYearDays);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(DateUtil.getDifferenceInYears(startDate, endDate, leapYearDays), 1, EPS);
  }

  @Test
  public void testDateOffsetWithYearFraction() {
    final ZonedDateTime startDate = ZonedDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.MIDNIGHT, TimeZone.UTC);
    final ZonedDateTime offsetDateWithFinancialYearDefinition = ZonedDateTime.of(LocalDate.of(2002, 1, 1), LocalTime.of(6, 0), TimeZone.UTC);
    final ZonedDateTime endDate = ZonedDateTime.of(LocalDate.of(2002, 1, 1), LocalTime.MIDNIGHT, TimeZone.UTC);
    final double daysPerYear = 365;
    try {
      DateUtil.getDateOffsetWithYearFraction((InstantProvider) null, 1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtil.getDateOffsetWithYearFraction((ZonedDateTime) null, 1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtil.getDateOffsetWithYearFraction((InstantProvider) null, 1, daysPerYear);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtil.getDateOffsetWithYearFraction((ZonedDateTime) null, 1, daysPerYear);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(DateUtil.getDateOffsetWithYearFraction(startDate.toInstant(), 1), offsetDateWithFinancialYearDefinition.toInstant());
    assertEquals(DateUtil.getDateOffsetWithYearFraction(startDate, 1), offsetDateWithFinancialYearDefinition);
    assertEquals(DateUtil.getDateOffsetWithYearFraction(startDate.toInstant(), 1, daysPerYear), endDate.toInstant());
    assertEquals(DateUtil.getDateOffsetWithYearFraction(startDate, 1, daysPerYear), endDate);
  }

  @Test
  public void testUTCDate() {
    final int year = 2009;
    final int month = 9;
    final int day = 1;
    ZonedDateTime date = DateUtil.getUTCDate(year, month, day);
    assertEquals(date.getYear(), year);
    assertEquals(date.getMonthOfYear().getValue(), month);
    assertEquals(date.getDayOfMonth(), day);
    assertEquals(date.getHourOfDay(), 0);
    assertEquals(date.getMinuteOfHour(), 0);
    assertEquals(date.getZone(), TimeZone.UTC);
    final int hour = 6;
    final int minutes = 31;
    date = DateUtil.getUTCDate(year, month, day, hour, minutes);
    assertEquals(date.getYear(), year);
    assertEquals(date.getMonthOfYear().getValue(), month);
    assertEquals(date.getDayOfMonth(), day);
    assertEquals(date.getHourOfDay(), hour);
    assertEquals(date.getMinuteOfHour(), minutes);
    assertEquals(date.getZone(), TimeZone.UTC);
  }

  @Test
  public void testDateInTimeZone() {
    // TODO don't know how to create time zones
  }

  @Test
  public void testExactDaysBetween() {
    // TODO don't know how to create time zones
    // final ZonedDateTime startDate = DateUtil.getDateInTimeZone(2000, 1, 1, 0,
    // 0, "Europe/London");
    // final ZonedDateTime endDate = DateUtil.getDateInTimeZone(2001, 1, 1, 0,
    // 0, "Europe/Paris");
    // try {
    // DateUtil.getExactDaysBetween(null, endDate);
    // fail();
    // } catch (final IllegalArgumentException e) {
    // Expected
    // }
    // try {
    // DateUtil.getExactDaysBetween(startDate, null);
    // fail();
    // } catch (final IllegalArgumentException e) {
    // Expected
    // }
  }

  @Test
  public void testDaysBetween() {
    final ZonedDateTime startDate = DateUtil.getUTCDate(2008, 1, 1);
    final ZonedDateTime endDate = DateUtil.getUTCDate(2009, 1, 1);
    assertEquals(DateUtil.getDaysBetween(startDate, false, endDate, false), 365);
    assertEquals(DateUtil.getDaysBetween(startDate, true, endDate, false), 366);
    assertEquals(DateUtil.getDaysBetween(startDate, false, endDate, true), 366);
    assertEquals(DateUtil.getDaysBetween(startDate, true, endDate, true), 367);
    assertEquals(DateUtil.getDaysBetween(startDate, endDate), 366);
  }
  
  @Test
  public void testPrintYYYYMMDD() {
    final int year = 2009;
    final int month = 9;
    final int day = 1;
    ZonedDateTime date = DateUtil.getUTCDate(year, month, day);
    assertEquals("20090901", DateUtil.printYYYYMMDD(date));
    try {
      DateUtil.printYYYYMMDD(null);
      fail();
    } catch (final IllegalArgumentException e) {
      //Expected
    }
  }
  
  @Test
  public void testPreviousWeekDay() {
    LocalDate sun = LocalDate.of(2009, 11, 8);
    LocalDate sat = LocalDate.of(2009, 11, 7);
    LocalDate fri = LocalDate.of(2009, 11, 6);
    LocalDate thur = LocalDate.of(2009, 11, 5);
    LocalDate wed = LocalDate.of(2009, 11, 4);
    LocalDate tue = LocalDate.of(2009, 11, 3);
    LocalDate mon = LocalDate.of(2009, 11, 2);
    LocalDate lastFri = LocalDate.of(2009, 10, 30);
    
    assertEquals(fri, DateUtil.previousWeekDay(sun));
    assertEquals(fri, DateUtil.previousWeekDay(sat));
    assertEquals(thur, DateUtil.previousWeekDay(fri));
    assertEquals(wed, DateUtil.previousWeekDay(thur));
    assertEquals(tue, DateUtil.previousWeekDay(wed));
    assertEquals(mon, DateUtil.previousWeekDay(tue));
    assertEquals(lastFri, DateUtil.previousWeekDay(mon));
  }
  
  @Test
  public void testNextWeekDay() {
    LocalDate sun = LocalDate.of(2009, 11, 8);
    LocalDate sat = LocalDate.of(2009, 11, 7);
    LocalDate fri = LocalDate.of(2009, 11, 6);
    LocalDate thur = LocalDate.of(2009, 11, 5);
    LocalDate wed = LocalDate.of(2009, 11, 4);
    LocalDate tue = LocalDate.of(2009, 11, 3);
    LocalDate mon = LocalDate.of(2009, 11, 2);
    LocalDate nextMon = LocalDate.of(2009, 11, 9);
    
    assertEquals(nextMon, DateUtil.nextWeekDay(sun));
    assertEquals(nextMon, DateUtil.nextWeekDay(sat));
    assertEquals(nextMon, DateUtil.nextWeekDay(fri));
    assertEquals(fri, DateUtil.nextWeekDay(thur));
    assertEquals(thur, DateUtil.nextWeekDay(wed));
    assertEquals(wed, DateUtil.nextWeekDay(tue));
    assertEquals(tue, DateUtil.nextWeekDay(mon));
  }

  @Test
  public void testToLocalDate() {
    LocalDate D20100328 = LocalDate.of(2010, MonthOfYear.MARCH, 28);
    LocalDate localDate = DateUtil.toLocalDate(20100328);
    assertEquals(D20100328, localDate);
  }

}
