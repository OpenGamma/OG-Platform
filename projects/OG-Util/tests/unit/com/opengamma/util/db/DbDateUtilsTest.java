/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeProvider;

import org.junit.Test;

/**
 * Test DbDateUtils.
 */
@SuppressWarnings("deprecation")
public class DbDateUtilsTest {

  //-------------------------------------------------------------------------
  @Test
  public void test_toSqlTimestamp() {
    Instant instant = Instant.now();
    Timestamp ts = DbDateUtils.toSqlTimestamp(instant);
    assertEquals(instant.toEpochMillisLong(), ts.getTime());
    assertEquals(instant.getNanoOfSecond(), ts.getNanos());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_toSqlTimestamp_TimeProvider_null() {
    DbDateUtils.toSqlTimestamp((TimeProvider) null);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_toSqlTimestamp_InstantProvider_null() {
    DbDateUtils.toSqlTimestamp((InstantProvider) null);
  }

  @Test
  public void test_fromSqlTimestamp() {
    Timestamp ts = new Timestamp(123456789L);
    ts.setNanos(789654321);
    Instant instant = DbDateUtils.fromSqlTimestamp(ts);
    assertEquals(ts.getTime(), instant.toEpochMillisLong());
    assertEquals(ts.getNanos(), instant.getNanoOfSecond());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_fromSqlTimestamp_null() {
    DbDateUtils.fromSqlTimestamp(null);
  }

  @Test
  public void test_fromSqlTimestamp_max() {
    assertEquals(DbDateUtils.MAX_INSTANT, DbDateUtils.fromSqlTimestamp(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  @Test
  public void test_fromSqlTimestampNullFarFuture() {
    Timestamp ts = new Timestamp(123456789L);
    ts.setNanos(789654321);
    Instant instant = DbDateUtils.fromSqlTimestampNullFarFuture(ts);
    assertEquals(ts.getTime(), instant.toEpochMillisLong());
    assertEquals(ts.getNanos(), instant.getNanoOfSecond());
  }

  @Test
  public void test_fromSqlTimestampNullFarFuture_max() {
    assertNull(DbDateUtils.fromSqlTimestampNullFarFuture(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toSqlDateTime() {
    assertEquals(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7), DbDateUtils.toSqlDateTime(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_toSqlDateTime_null() {
    DbDateUtils.toSqlDateTime(null);
  }

  @Test
  public void test_fromSqlDateTime() {
    assertEquals(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7), DbDateUtils.fromSqlDateTime(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_fromSqlDateTimep_null() {
    DbDateUtils.fromSqlDateTime(null);
  }

  @Test
  public void test_fromSqlDateTime_max() {
    assertEquals(LocalDateTime.of(9999, 12, 31, 23, 59, 59, 0), DbDateUtils.fromSqlDateTime(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  @Test
  public void test_fromSqlDateTimeNullFarFuture() {
    assertEquals(LocalDateTime.of(2005, 11, 7, 12, 34, 56, 7), DbDateUtils.fromSqlDateTimeNullFarFuture(new Timestamp(2005 - 1900, 11 - 1, 7, 12, 34, 56, 7)));
  }

  @Test
  public void test_fromSqlDateTimeNullFarFuture_max() {
    assertNull(DbDateUtils.fromSqlDateTimeNullFarFuture(DbDateUtils.MAX_SQL_TIMESTAMP));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toSqlDate() {
    assertEquals(new Date(2005 - 1900, 11 - 1, 12), DbDateUtils.toSqlDate(LocalDate.of(2005, 11, 12)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_toSqlDate_null() {
    DbDateUtils.toSqlDate(null);
  }

  @Test
  public void test_fromSqlDate() {
    assertEquals(LocalDate.of(2005, 11, 12), DbDateUtils.fromSqlDate(new Date(2005 - 1900, 11 - 1, 12)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_fromSqlDate_null() {
    DbDateUtils.fromSqlDate(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toSqlTime() {
    assertEquals(new Time(12, 34, 56), DbDateUtils.toSqlTime(LocalTime.of(12, 34, 56)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_toSqlTime_null() {
    DbDateUtils.toSqlTime(null);
  }

  @Test
  public void test_fromSqlTime() {
    assertEquals(LocalTime.of(12, 34, 56), DbDateUtils.fromSqlTime(new Time(12, 34, 56)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_fromSqlTime_time_null() {
    DbDateUtils.fromSqlTime((Time) null);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_fromSqlTime_timestamp_null() {
    DbDateUtils.fromSqlTime((Timestamp) null);
  }

}
