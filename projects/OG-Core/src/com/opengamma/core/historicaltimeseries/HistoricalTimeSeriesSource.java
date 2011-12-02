/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A source of daily historical time-series as accessed by the engine.
 * <p>
 * The interface provides access to historical time-series data on a daily basis.
 * There may be other uses of time-series within the application, but this interface is
 * specifically focused on the requirement for daily data.
 * <p>
 * This interface provides a simple view of the time-series as needed by the engine.
 * This may be backed by a full-featured time-series master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
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
  HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId);

  /**
   * Finds a specific time-series by unique identifier.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param start  the start date, null will load the earliest date 
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the time-series, null if not found
   * @throws IllegalArgumentException if the unique identifier is invalid
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

  //-------------------------------------------------------------------------
  /**
   * Finds a time-series from identifierBundle, source, provider and field.
   * <p>
   * The validity date for identifiers is set to today's date.
   * This returns all the available data points.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField);

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
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField);

  /**
   * Finds a time-series from identifierBundle, source, provider and field.
   * <p>
   * The validity date for identifiers is set to today's date.
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date 
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

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
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

  //-------------------------------------------------------------------------
  /**
   * Finds a time-series from identifierBundle using configuration.
   * <p>
   * The validity date for identifiers is set to today's date.
   * This returns all the available data points.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey);

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
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey);

  /**
   * Finds a time-series from identifierBundle using configuration.
   * <p>
   * The validity date for identifiers is set to today's date.
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date 
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

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
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

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
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return a map of each supplied identifier bundle to the corresponding time-series, not null
   */
  Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

  //-------------------------------------------------------------------------
  
  /**
   * Returns summary information for the specified time series.
   * The information includes the earliest and latest data points and their dates.
   * @param uniqueId  the unique id of the historic time series
   * @return          a HistoricalTimeSeriesSummary filled in with the summary information
   */
  HistoricalTimeSeriesSummary getSummary(UniqueId uniqueId);

  /**
   * Returns summary information for the specified time series.
   * The information includes the earliest and latest data points and their dates.
   * @param objectId  the time-series object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @return          a HistoricalTimeSeriesSummary filled in with the summary information
   */
  HistoricalTimeSeriesSummary getSummary(ObjectIdentifiable objectId, VersionCorrection versionCorrection);  

//  /**
//   * Fills in and returns a ManageableHistoricalTimeSeries without fetching any data points
//   * 
//   * @param uniqueId  the time-series unique identifier, not null
//   * @return A ManageableHistoricalTimeSeries object, without data points
//   */
////  ManageableHistoricalTimeSeries getTimeSeriesWithoutDataPoints(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive);
//
//  /**
//   * Fills in and returns a ManageableHistoricalTimeSeries without fetching any data points
//   * 
//   * @param objectId  the time-series object identifier, not null
//   * @param versionCorrection  the version-correction locator to search at, not null
//   * @return the current state of the document, may be an update of the input document, not null
//   * @throws IllegalArgumentException if the identifier is invalid
//   * @throws DataNotFoundException if there is no document with that unique identifier
//   */
////  ManageableHistoricalTimeSeries getTimeSeriesWithoutDataPoints(ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive);
//
//  /**
//   * Gets the time series data points (without a ManageableHistoricalTimeSeries object)
//   * 
//   * @param uniqueId  the time-series unique identifier, not null
//   * @param fromDateInclusive  the inclusive start date of the points to get, null for far past
//   * @param toDateInclusive  the inclusive end date of the points to get, null for far future
//   * @return a series of data points
//   * @throws IllegalArgumentException if the identifier is invalid
//   * @throws DataNotFoundException if there is no document with that unique identifier
//   */
////  LocalDateDoubleTimeSeries getTimeSeriesDataPoints(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive);
//  
//  /**
//   * Gets the time series data points
//   * 
//   * @param objectId  the time-series object identifier, not null
//   * @param versionCorrection  the version-correction locator to search at, not null
//   * @param fromDateInclusive  the inclusive start date of the points to get, null for far past
//   * @param toDateInclusive  the inclusive end date of the points to get, null for far future
//   * @return A series of data points
//   * @throws IllegalArgumentException if the identifier is invalid
//   * @throws DataNotFoundException if there is no document with that unique identifier
//   */
////  LocalDateDoubleTimeSeries getTimeSeriesDataPoints(ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive);
//  
//  /**
//   * Gets the date of the earliest data point for the specified time-series.
//   * @param uniqueId  the unique identifier, not null
//   * @return  the earliest data point's date
//   */
//  LocalDate getEarliestDate(UniqueId uniqueId);
//  
//  /**
//   * Gets the date of the latest data point for the specified time-series.
//   * @param uniqueId  the unique identifier, not null
//   * @return  the latest data point's date
//   */
//  LocalDate getLatestDate(UniqueId uniqueId);
//
//  /**
//   * Gets the earliest data point for the specified time-series.
//   * @param uniqueId  the unique identifier, not null
//   * @return  the earliest data point value
//   */
//  Double getEarliestValue(UniqueId uniqueId);
//
//  /**
//   * Gets the latest data point for the specified time-series.
//   * @param uniqueId  the unique identifier, not null
//   * @return  the latest data point value
//   */
//  Double getLatestValue(UniqueId uniqueId);

}

