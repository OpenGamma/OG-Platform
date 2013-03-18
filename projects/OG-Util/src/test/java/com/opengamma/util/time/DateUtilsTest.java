/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;
import static org.threeten.bp.Month.MARCH;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Test DateUtils.
 */
@Test(groups = TestGroup.UNIT)
public class DateUtilsTest {
  private static final double EPS = 1e-9;

  public void testDifferenceInYears() {
    final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2001, 1, 1, 0, 0), ZoneOffset.UTC);
    try {
      DateUtils.getDifferenceInYears((Instant) null, endDate.toInstant());
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtils.getDifferenceInYears(startDate.toInstant(), (Instant) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final double leapYearDays = 366;
    assertEquals(DateUtils.getDifferenceInYears(startDate.toInstant(), endDate.toInstant()) * DateUtils.DAYS_PER_YEAR / leapYearDays, 1, EPS);
    try {
      DateUtils.getDifferenceInYears(null, endDate.toInstant(), leapYearDays);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtils.getDifferenceInYears(startDate.toInstant(), null, leapYearDays);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(DateUtils.getDifferenceInYears(startDate.toInstant(), endDate.toInstant(), leapYearDays), 1, EPS);
  }

  public void testDateOffsetWithYearFraction() {
    final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2001, 1, 1, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime offsetDateWithFinancialYearDefinition = ZonedDateTime.of(LocalDateTime.of(2002, 1, 1, 6, 0), ZoneOffset.UTC);
    final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2002, 1, 1, 0, 0), ZoneOffset.UTC);
    final double daysPerYear = 365;
    try {
      DateUtils.getDateOffsetWithYearFraction((Instant) null, 1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtils.getDateOffsetWithYearFraction((ZonedDateTime) null, 1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtils.getDateOffsetWithYearFraction((Instant) null, 1, daysPerYear);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DateUtils.getDateOffsetWithYearFraction((ZonedDateTime) null, 1, daysPerYear);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate.toInstant(), 1), offsetDateWithFinancialYearDefinition.toInstant());
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate, 1), offsetDateWithFinancialYearDefinition);
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate.toInstant(), 1, daysPerYear), endDate.toInstant());
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate, 1, daysPerYear), endDate);
  }

  public void testUTCDate() {
    final int year = 2009;
    final int month = 9;
    final int day = 1;
    ZonedDateTime date = DateUtils.getUTCDate(year, month, day);
    assertEquals(date.getYear(), year);
    assertEquals(date.getMonthValue(), month);
    assertEquals(date.getDayOfMonth(), day);
    assertEquals(date.getHour(), 0);
    assertEquals(date.getMinute(), 0);
    assertEquals(date.getZone(), ZoneOffset.UTC);
    final int hour = 6;
    final int minutes = 31;
    date = DateUtils.getUTCDate(year, month, day, hour, minutes);
    assertEquals(date.getYear(), year);
    assertEquals(date.getMonthValue(), month);
    assertEquals(date.getDayOfMonth(), day);
    assertEquals(date.getHour(), hour);
    assertEquals(date.getMinute(), minutes);
    assertEquals(date.getZone(), ZoneOffset.UTC);
  }

  public void testDateInTimeZone() {
    // TODO don't know how to create time zones
  }

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

  public void testDaysBetween() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2008, 1, 1);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2009, 1, 1);
    assertEquals(DateUtils.getDaysBetween(startDate, false, endDate, false), 365);
    assertEquals(DateUtils.getDaysBetween(startDate, true, endDate, false), 366);
    assertEquals(DateUtils.getDaysBetween(startDate, false, endDate, true), 366);
    assertEquals(DateUtils.getDaysBetween(startDate, true, endDate, true), 367);
    assertEquals(DateUtils.getDaysBetween(startDate, endDate), 366);
  }
  
  public void testPrintYYYYMMDD() {
    final int year = 2009;
    final int month = 9;
    final int day = 1;
    ZonedDateTime date = DateUtils.getUTCDate(year, month, day);
    assertEquals("20090901", DateUtils.printYYYYMMDD(date));
    try {
      DateUtils.printYYYYMMDD(null);
      fail();
    } catch (final IllegalArgumentException e) {
      //Expected
    }
  }
  
  public void testPrintMMDD() {
    LocalDate test = LocalDate.of(2010, 1, 1);
    assertEquals("01-01", DateUtils.printMMDD(test));
    try {
      DateUtils.printMMDD(null);
      fail();
    } catch (final IllegalArgumentException e) {
      //Expected
    }
    
  }
  
  public void testPreviousWeekDay() {
    LocalDate sun = LocalDate.of(2009, 11, 8);
    LocalDate sat = LocalDate.of(2009, 11, 7);
    LocalDate fri = LocalDate.of(2009, 11, 6);
    LocalDate thur = LocalDate.of(2009, 11, 5);
    LocalDate wed = LocalDate.of(2009, 11, 4);
    LocalDate tue = LocalDate.of(2009, 11, 3);
    LocalDate mon = LocalDate.of(2009, 11, 2);
    LocalDate lastFri = LocalDate.of(2009, 10, 30);
    
    assertEquals(fri, DateUtils.previousWeekDay(sun));
    assertEquals(fri, DateUtils.previousWeekDay(sat));
    assertEquals(thur, DateUtils.previousWeekDay(fri));
    assertEquals(wed, DateUtils.previousWeekDay(thur));
    assertEquals(tue, DateUtils.previousWeekDay(wed));
    assertEquals(mon, DateUtils.previousWeekDay(tue));
    assertEquals(lastFri, DateUtils.previousWeekDay(mon));
  }
  
  public void testNextWeekDay() {
    LocalDate sun = LocalDate.of(2009, 11, 8);
    LocalDate sat = LocalDate.of(2009, 11, 7);
    LocalDate fri = LocalDate.of(2009, 11, 6);
    LocalDate thur = LocalDate.of(2009, 11, 5);
    LocalDate wed = LocalDate.of(2009, 11, 4);
    LocalDate tue = LocalDate.of(2009, 11, 3);
    LocalDate mon = LocalDate.of(2009, 11, 2);
    LocalDate nextMon = LocalDate.of(2009, 11, 9);
    
    assertEquals(nextMon, DateUtils.nextWeekDay(sun));
    assertEquals(nextMon, DateUtils.nextWeekDay(sat));
    assertEquals(nextMon, DateUtils.nextWeekDay(fri));
    assertEquals(fri, DateUtils.nextWeekDay(thur));
    assertEquals(thur, DateUtils.nextWeekDay(wed));
    assertEquals(wed, DateUtils.nextWeekDay(tue));
    assertEquals(tue, DateUtils.nextWeekDay(mon));
  }

  public void testToLocalDate() {
    LocalDate D20100328 = LocalDate.of(2010, MARCH, 28);
    LocalDate localDate = DateUtils.toLocalDate(20100328);
    assertEquals(D20100328, localDate);
  }

}
