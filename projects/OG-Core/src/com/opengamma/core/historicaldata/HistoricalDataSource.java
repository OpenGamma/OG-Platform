/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaldata;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * A source of daily historical time-series as accessed by the engine.
 * <p>
 * The interface provides access to historical time-series data on a daily basis.
 * There may be other uses of time-series within the application, but this interface is
 * specifically focused on the requirement for daily data.
 */
@PublicSPI
public interface HistoricalDataSource {

  /**
   * Finds a time-series with all the available data points.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return  the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField);

  /**
   * Finds a time-series with all the available data points.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param currentDate  the current valid date for the identifier, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return  the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField);

  /**
   * Finds a time-series with data points between start and end dates.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   * Finds a time-series with data points between start and end dates.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param currentDate  the current date if applicable
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   * Finds a time-series with all the available data points.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param configDocName  the name of a configuration document to use for additional parameters
   * @return the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configDocName);

  /**
   * Finds a time-series with all the available data points.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param currentDate  the current date if applicable
   * @param configDocName  the name of a configuration document to use for additional parameters
   * @return the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String configDocName);

  /**
   * Finds a time-series with data points between start and end dates.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param configDocName  the name of a configuration document to use for additional parameters
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, String configDocName,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   * Finds a time-series with data points between start and end dates.
   * 
   * @param identifiers  the identifier bundle, not null
   * @param currentDate  the current date if applicable
   * @param configDocName  the name of a configuration document to use for additional parameters
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the (uid,time-series) pair, (null,empty) if not found, not null
   */
  Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   *  Finds a time-series with all the available data points by identifier.
   *  
   * @param uid  the unique identifier, not null
   * @return the time-series, empty if not found, not null
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid);

  /**
   * Finds a time-series with all data points between start and end date by identifier.
   * 
   * @param uid  the unique identifier, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the time-series, empty if not found, not null
   * @throws IllegalArgumentException if the identifier is invalid
   */
  LocalDateDoubleTimeSeries getHistoricalData(
      UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   * Finds multiple time-series for the same source, provider and field, with all data
   * points between start and end date. 
   * 
   * @param identifierSet  a set containing an identifier bundle for each time-series required, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return a map of each identifier bundle to the corresponding time-series, not null
   */
  Map<IdentifierBundle, Pair<UniqueIdentifier, LocalDateDoubleTimeSeries>> getHistoricalData(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

}
