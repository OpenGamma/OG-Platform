/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;

/**
 * A time-series master implementation stores daily points using a {@code LocalDate}.
 */
public class LocalDateDbTimeSeriesMaster extends DbTimeSeriesMaster implements TimeSeriesMaster {

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database information, not null
   * @param namedSQLMap  the named SQL map, not null
   * @param isTriggerSupported  whether trigger is supported
   */
  public LocalDateDbTimeSeriesMaster(
      DbSource dbSource, Map<String, String> namedSQLMap, boolean isTriggerSupported) {
    super(dbSource, namedSQLMap, isTriggerSupported);
  }

  //-------------------------------------------------------------------------
  protected String getDataPointTableName() {
    return "tss_data_point";
  }

  @Override
  protected String getDataPointDeltaTableName() {
    return "tss_data_point_delta";
  }

  @Override
  protected int getSqlDateType() {
    return Types.DATE;
  }

  @Override
  protected Object getSqlDate(LocalDate date) {
    return DbDateUtils.toSqlDate(date);
  }

  @Override
  protected LocalDate getDate(ResultSet rs, String column) throws SQLException {
    java.sql.Date date = rs.getDate(column);
    if (date == null) {
      return null;
    }
    return DbDateUtils.fromSqlDate(date);
  }

  @Override
  protected LocalDateDoubleTimeSeries getTimeSeries(List<LocalDate> dates, List<Double> values) {
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  @Override
  protected MutableLocalDateDoubleTimeSeries getMutableTimeSeries(LocalDateDoubleTimeSeries timeSeries) {
    return new MapLocalDateDoubleTimeSeries(timeSeries);
  }

  @Override
  protected LocalDate getDate(String date) {
    return DateUtil.toLocalDate(date);
  }

  @Override
  protected String printDate(LocalDate date) {
    return DateUtil.printYYYYMMDD(date);
  }

}
