/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZoneOffset;

import com.opengamma.master.timeseries.DateTimeTimeSeriesMaster;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MapDateTimeDoubleTimeSeries;

/**
 * A time-series master implementation stores intraday points using a {@code java.util.Date}.
 */
public class DateTimeDbTimeSeriesMaster extends DbTimeSeriesMaster<Date> implements DateTimeTimeSeriesMaster {

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database information, not null
   * @param namedSQLMap  the named SQL map, not null
   * @param isTriggerSupported  whether trigger is supported
   */
  public DateTimeDbTimeSeriesMaster(
      DbSource dbSource, Map<String, String> namedSQLMap, boolean isTriggerSupported) {
    super(dbSource, namedSQLMap, isTriggerSupported);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String getDataPointTableName() {
    return "tss_intraday_data_point";
  }

  @Override
  protected String getDataPointDeltaTableName() {
    return "tss_intraday_data_point_delta";
  }

  @Override
  protected int getSqlDateType() {
    return Types.TIMESTAMP;
  }

  @Override
  protected Object getSqlDate(Date date) {
    return new java.sql.Timestamp(date.getTime());
  }

  @Override
  protected Date getDate(ResultSet rs, String column) throws SQLException {
    java.sql.Timestamp date = rs.getTimestamp(column);
    return date;
  }

  @Override
  protected DoubleTimeSeries<Date> getTimeSeries(List<Date> dates, List<Double> values) {
    return new ArrayDateTimeDoubleTimeSeries(dates, values);
  }

  @Override
  protected MutableDoubleTimeSeries<Date> getMutableTimeSeries(DoubleTimeSeries<Date> timeSeries) {
    return new MapDateTimeDoubleTimeSeries(timeSeries);
  }

  @Override
  protected Date getDate(String date) {
    return new Date(OffsetDateTime.of(LocalDateTime.parse(date), ZoneOffset.UTC).toInstant().toEpochMillisLong());
  }

  @Override
  protected String printDate(Date date) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMillis(date.getTime()), ZoneOffset.UTC).toLocalDateTime().toString();
  }

}
