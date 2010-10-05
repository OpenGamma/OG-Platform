/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import javax.time.calendar.LocalDate;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * A source of Timeseries as accessed by the engine.
 * <p>
 * This interface provides a simple view of TimeSeries as needed by the engine.
 */
@PublicSPI
public interface HistoricalDataSource {
  /**
   * Finds a timeseries with all the available data points 
   * 
   * @param identifiers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param dataField the dataField, not-null
   * @return  the (uid, timeseries) pair, (null,empty) if not found
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField);

  /**
   * Finds a timeseries with data points between start and end dates
   * <p>
   * Datapoint for both start and end date are included
   * 
   * @param identifiers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param field the dataField, not-null
   * @param start the start date, if null will load the earliest date 
   * @param end the end date, if null will load the latest date
   * @return  the (uid, timeseries) pair, (null,empty) if not found
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start, LocalDate end);

  /**
   * Finds a timeseries with data points between start and end dates
   * 
   * @param identifiers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param field the dataField, not-null
   * @param start the start date, if null will load the earliest date 
   * @param inclusiveStart whether or not the start date is included in the result.
   * @param end the end date, if null will load the latest date
   * @param exclusiveEnd whether or not the end date is included in the result.
   * @return the (uid, timeseries) pair, (null,empty) if not found
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start, boolean inclusiveStart,
      LocalDate end, boolean exclusiveEnd);

  /**
   * Finds a timeseries with all the available data points 
   * <p>
   * The {@link TimeSeriesResolver} provides the default meta data for lookup
   * 
   * @param identifiers the identifier bundle, not-null
   * @return the (uid, timeseries) pair, (null,empty) if not found
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers);

  /**
   * Finds a timeseries with data points between start and end dates
   * <p>
   * The {@link TimeSeriesResolver} provides the default meta data for lookup
   * Datapoints for both start and end dates are included
   * 
   * @param identifiers the identifier bundle, not-null
   * @param start the start date, if null will load the earliest date 
   * @param end the end date, if null will load the latest date
   * @return the (uid, timeseries) pair, (null,empty) if not found
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, LocalDate end);
  
 
  /**
   * Finds a timeseries with data points between start and end dates
   * <p>
   * The {@link TimeSeriesResolver} provides the default meta data for lookup
   * 
   * @param identifiers the identifier bundle, not-null
   * @param start the start date, if null will load the earliest date 
   * @param inclusiveStart whether or not the start date is included in the result.
   * @param end the end date, if null will load the latest date
   * @param exclusiveEnd whether or not the end date is included in the result.
   * @return the (uid, timeseries) pair, (null,empty) if not found
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   *  Finds a timeseries with all the available data points by identifier
   *  
   * @param uid the identifier, not-null
   * @return the timeseries, empty if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid);

  /**
   *  Finds a timeseries with all data points between start and end date included by identifier
   *  
   * @param uid the identifier, not-null
   * @param start the start date, if null will load the earliest date 
   * @param end the end date, if null will load the latest date
   * @return the timeseries, empty if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, LocalDate end);
  
  /**
   * Finds a timeseries with all data points between start and end date by identifier
   * 
   * @param uid the identifier, not-null
   * @param start the start date, if null will load the earliest date 
   * @param inclusiveStart whether or not the start date is included in the result.
   * @param end the end date, if null will load the latest date
   * @param exclusiveEnd whether or not the end date is included in the result.
   * @return the timeseries, empty if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

}
