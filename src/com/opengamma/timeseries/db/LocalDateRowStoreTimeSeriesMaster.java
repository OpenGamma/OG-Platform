/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.opengamma.timeseries.LocalDateTimeSeriesMaster;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * 
 */
public class LocalDateRowStoreTimeSeriesMaster extends RowStoreTimeSeriesMaster<LocalDate> implements LocalDateTimeSeriesMaster {
  
  public LocalDateRowStoreTimeSeriesMaster(DataSourceTransactionManager transactionManager, 
      Map<String, String> namedSQLMap,
      boolean isTriggerSupported) {
    super(transactionManager, namedSQLMap, isTriggerSupported);
  }
  
  @Override
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
    return DateUtil.toSqlDate(date);
  }
    
  @Override
  protected LocalDate getDate(ResultSet rs, String column) throws SQLException {
    java.sql.Date date = rs.getDate(column);
    return DateUtil.fromSqlDate(date);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getTimeSeries(List<LocalDate> dates, List<Double> values) {
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  @Override
  protected MutableDoubleTimeSeries<LocalDate> getMutableTimeSeries(DoubleTimeSeries<LocalDate> timeSeries) {
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
