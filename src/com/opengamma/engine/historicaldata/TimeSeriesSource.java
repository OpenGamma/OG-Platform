/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import javax.time.calendar.LocalDate;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A source of Timeseries as accessed by the engine.
 * <p>
 * This interface provides a simple view of TimeSeries as needed by the engine.
 */
public interface TimeSeriesSource {
  /**
   * Finds a timeseries with all the available data points 
   * 
   * @param identifers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param field the dataField, not-null
   * @return the timeseries, empty if not found
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(IdentifierBundle identifers, String dataSource, String dataProvider, String field);
  
  /**
   * Finds a timeseries with data points between start and end dates
   * 
   * @param identifiers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param field the dataField, not-null
   * @param start the start date, if null will load the earliest date 
   * @param end the end date, if null will load the latest date
   * @return the timeseries, empty if not found
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start, LocalDate end);
  
  /**
   *  Finds a timeseries with all the available data points by identifier
   * @param uid the identifier, not-null
   * @return the timeseries, empty if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(UniqueIdentifier uid);

  /**
   *  Finds a timeseries with all the available data points by identifier
   * @param uid the identifier, not-null
   * @param start the start date, if null will load the earliest date 
   * @param end the end date, if null will load the latest date
   * @return the timeseries, empty if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(UniqueIdentifier uid, LocalDate start, LocalDate end);

  /**
   * Finds the Timeseries UID
   * 
   * @param identifiers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param field the dataField, not-null
   * @return the UID if found or null
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle identifiers, String dataSource, String dataProvider, String field);
  
}
