/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeProvider;

import org.testng.annotations.Test;

/**
 * Test DbDateUtils.
 */
@SuppressWarnings("deprecation")
@Test
public class DbDateUtilsTest {

  //-------------------------------------------------------------------------
  public void test_toSqlTimestamp() {
    Instant instant = Instant.now();
    Timestamp ts = DbDateUtils.toSqlTimestamp(instant);
    assertEquals(instant.toEpochMillisLong(), ts.getTime());
    assertEquals(instant.getNanoOfSecond(), ts.getNanos());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_toSqlTimestamp_TimeProvider_null() {
    DbDateUtils.toSqlTimestamp((TimeProvider) null);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_toSqlTimestamp_InstantProvider_null() {
    DbDateUtils.toSqlTimestamp((InstantProvider) null);
  }

  public void test_fromSqlTimestamp() {
    Timestamp ts = new Timestamp(123456789L);
    ts.setNanos(789654321);
    Instant instant = DbDateUtils.fromSqlTimestamp(ts);
    assertEquals(ts.getTime(), instant.toEpochMillisLong());
    assertEquals(ts.getNanos(), instant.getNanoOfSecond());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromSqlTimestamp_null() {
    DbDateUtils.fromSqlTimestamp(null);
  }

  public void test_fromSqlTimestamp_max() {
    assertEquals(DbDateUtils.MAX_INSTANT, DbDateUtils.fromSqlTimestamp(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  public void test_fromSqlTimestampNullFarFuture() {
    Timestamp ts = new Timestamp(123456789L);
    ts.setNanos(789654321);
    Instant instant = DbDateUtils.fromSqlTimestampNullFarFuture(ts);
    assertEquals(ts.getTime(), instant.toEpochMillisLong());
    assertEquals(ts.getNanos(), instant.getNanoOfSecond());
  }

  public void test_fromSqlTimestampNullFarFuture_max() {
    assertNull(DbDateUtils.fromSqlTimestampNullFarFuture(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  //-------------------------------------------------------------------------
  public void test_toSqlDateTime() {
    assertEquals(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7), DbDateUtils.toSqlDateTime(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_toSqlDateTime_null() {
    DbDateUtils.toSqlDateTime(null);
  }

  public void test_fromSqlDateTime() {
    assertEquals(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7), DbDateUtils.fromSqlDateTime(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromSqlDateTimep_null() {
    DbDateUtils.fromSqlDateTime(null);
  }

  public void test_fromSqlDateTime_max() {
    assertEquals(LocalDateTime.of(9999, 12, 31, 23, 59, 59, 0), DbDateUtils.fromSqlDateTime(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  public void test_fromSqlDateTimeNullFarFuture() {
    assertEquals(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7), DbDateUtils.fromSqlDateTimeNullFarFuture(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7)));
  }

  public void test_fromSqlDateTimeNullFarFuture_max() {
    assertNull(DbDateUtils.fromSqlDateTimeNullFarFuture(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  //-------------------------------------------------------------------------
  public void test_toSqlDate() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDate(LocalDate.of(2005, 11, 12)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_toSqlDate_null() {
    DbDateUtils.toSqlDate(null);
  }

  public void test_toSqlDateNullFarFuture() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDateNullFarFuture(LocalDate.of(2005, 11, 12)));
    assertEquals(DbDateUtils.MAX_SQL_DATE, DbDateUtils.toSqlDateNullFarFuture(null));
  }

  public void test_toSqlDateNullFarPast() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDateNullFarPast(LocalDate.of(2005, 11, 12)));
    assertEquals(DbDateUtils.MIN_SQL_DATE, DbDateUtils.toSqlDateNullFarPast(null));
  }

  public void test_fromSqlDate() {
    assertEquals(LocalDate.of(2005, 11, 12), DbDateUtils.fromSqlDate(new Date(2005 - 1900, 11 - 1, 12)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromSqlDate_null() {
    DbDateUtils.fromSqlDate(null);
  }

  //-------------------------------------------------------------------------
  public void test_toSqlTime() {
    assertEquals(new Time(12, 34, 56), DbDateUtils.toSqlTime(LocalTime.of(12, 34, 56)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_toSqlTime_null() {
    DbDateUtils.toSqlTime(null);
  }

  public void test_fromSqlTime() {
    assertEquals(LocalTime.of(12, 34, 56), DbDateUtils.fromSqlTime(new Time(12, 34, 56)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromSqlTime_time_null() {
    DbDateUtils.fromSqlTime((Time) null);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromSqlTime_timestamp_null() {
    DbDateUtils.fromSqlTime((Timestamp) null);
  }

}
