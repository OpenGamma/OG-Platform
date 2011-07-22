/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A source of daily historical time-series as accessed by the engine.
 * <p>
 * The interface provides access to historical time-series data on a daily basis.
 * There may be other uses of time-series within the application, but this interface is
 * specifically focused on the requirement for daily data.
 * <p>
 * This interface provides a simple view of the time-series as needed by the engine.
 * This may be backed by a full-featured time-series master, or by a much simpler data structure.
 */
@PublicSPI
public interface HistoricalTimeSeriesSource {

  /**
   * Finds a specific time-series by unique identifier.
   * <p>
   * This returns all the available data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the time-series, null if not found
   * @throws IllegalArgumentException if the unique identifier is invalid
   */
  HistoricalTimeSeries getHistoricalTimeSeries(UniqueIdentifier uniqueId);

  /**
   * Finds a specific time-series by unique identifier.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the time-series, null if not found
   * @throws IllegalArgumentException if the unique identifier is invalid
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueIdentifier uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  //-------------------------------------------------------------------------
  /**
   * Finds a time-series from identifierBundle, source, provider and field.
   * <p>
   * This returns all the available data points.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, String dataSource, String dataProvider, String dataField);

  /**
   * Finds a time-series from identifierBundle, source, provider and field checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns all the available data points.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField);

  /**
   * Finds a time-series from identifierBundle, source, provider and field.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   * Finds a time-series from identifierBundle, source, provider and field checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  //-------------------------------------------------------------------------
  /**
   * Finds a time-series from identifierBundle using configuration.
   * <p>
   * This returns all the available data points.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(String dataField, IdentifierBundle identifierBundle, String resolutionKey);

  /**
   * Finds a time-series from identifierBundle using configuration checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns all the available data points.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey);

  /**
   * Finds a time-series from identifierBundle using configuration.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  /**
   * Finds a time-series from identifierBundle using configuration checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

  //-------------------------------------------------------------------------
  /**
   * Finds multiple time-series for the same source, provider and field, with all data
   * points between start and end date. 
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param identifierSet  a set containing an identifier bundle for each time-series required, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return a map of each supplied identifier bundle to the corresponding time-series, not null
   */
  Map<IdentifierBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd);

}
