/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZoneOffset;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Test DateTimeDbTimeSeriesMaster.
 */
@Test
public class DateTimeDbTimeSeriesMasterTest extends DbTimeSeriesMasterTest<Date> {

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public DateTimeDbTimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @Override
  protected DbTimeSeriesMaster<Date> getTimeSeriesMaster(Map<String, String> namedSQLMap) {
    return new DateTimeDbTimeSeriesMaster(
        getDbSource(), 
        namedSQLMap,
        false);
  }
  
  @Override
  protected DoubleTimeSeries<Date> getTimeSeries(MapLocalDateDoubleTimeSeries tsMap) {
    return new ArrayDateTimeDoubleTimeSeries(tsMap.toDateTimeDoubleTimeSeries());
  }

  @Override
  protected DoubleTimeSeries<Date> getEmptyTimeSeries() {
    return new ArrayDateTimeDoubleTimeSeries();
  }

  @Override
  protected DoubleTimeSeries<Date> getTimeSeries(List<Date> dates, List<Double> values) {
    return new ArrayDateTimeDoubleTimeSeries(dates, values);
  }
  
  @Override
  protected LocalDate convert(Date date) {
    return DbDateUtils.fromSqlDate(new java.sql.Date(date.getTime()));
  }

  @Override
  protected Date convert(LocalDate date) {
    return DbDateUtils.toSqlDate(date);
  }

  @Override
  protected String print(Date date) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMillis(date.getTime()), ZoneOffset.UTC).toLocalDateTime().toString();
  }
  
}
