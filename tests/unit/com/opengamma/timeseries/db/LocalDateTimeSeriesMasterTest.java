/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.timeseries.TimeSeriesMaster;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * 
 */
public class LocalDateTimeSeriesMasterTest extends TimeSeriesMasterTest<LocalDate> {
  
  public LocalDateTimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  @Override
  protected TimeSeriesMaster<LocalDate> getTimeSeriesMaster(Map<String, String> namedSQLMap) {
    return new LocalDateRowStoreTimeSeriesMaster(
        getTransactionManager(), 
        namedSQLMap,
        false);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getTimeSeries(MapLocalDateDoubleTimeSeries tsMap) {
    return new ArrayLocalDateDoubleTimeSeries(tsMap);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getEmptyTimeSeries() {
    return new ArrayLocalDateDoubleTimeSeries();
  }

  @Override
  protected DoubleTimeSeries<LocalDate> getTimeSeries(List<LocalDate> dates, List<Double> values) {
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  @Override
  protected LocalDate convert(LocalDate date) {
    return date;
  }

  @Override
  protected String print(LocalDate date) {
    return DateUtil.printYYYYMMDD(date);
  }
  
}
