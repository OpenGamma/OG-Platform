/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;

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
public interface HistoricalTimeSeriesSource extends ChangeProvider {

  // By Unique Id
  
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

  /**
   * Finds a specific time-series by unique identifier.
   * <p>
   * This returns a subset of the data points filtered by the dates provided, up to the limit
   * specified by maxPoints:
   * +ve maxPoints returns at most maxPoints data points counting forwards from the earliest date
   * -ve maxPoints returns at most -maxPoints data points counting backwards from the latest date
   * 
   * @param uniqueId  the unique identifier, not null
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @param maxPoints the maximum number of points to be returned
   * @return the time-series, null if not found
   * @throws IllegalArgumentException if the unique identifier is invalid
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints);

  // By Unique Id - latest data point methods
  
  /**
   * Returns the latest data point from the specified time series.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return  a pair containing the latest data point value and its date
   */
  Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId);

  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);
  
  //-------------------------------------------------------------------------
  
  // By External Id/Field/Source/Provider - without identifierValidityDate
  
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
   * The validity date for identifiers is set to today's date.
   * This returns a subset of the data points filtered by the dates provided and limited to the
   * specified maximum number of points:
   * +ve maxPoints returns at most maxPoints data points counting forwards from the earliest date
   * -ve maxPoints returns at most -maxPoints data points counting backwards from the latest date
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @param maxPoints the maximum number of points to be returned
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints);

  // By External Id/Field/Source/Provider - With identifierValidityDate

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
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource,
      String dataProvider, String dataField);

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
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource,
      String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

  /**
   * Finds a time-series from identifierBundle, source, provider and field checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns a subset of the data points filtered by the dates provided and limited to the
   * specified maximum number of points:
   * +ve maxPoints returns at most maxPoints data points counting forwards from the earliest date
   * -ve maxPoints returns at most -maxPoints data points counting backwards from the latest date
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
   * @param maxPoints the maximum number of points to be returned
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource,
      String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints);

  // By External Id/Field/Source/Provider - latest data point methods

  /**
   * Returns the latest data point from the specified time series.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource,
      String dataProvider, String dataField);

  /**
   * Returns the latest data point from the specified date range in the time series.
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
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource,
      String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

  /**
   * Returns the latest data point from the specified time series.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return  a pair containing the latest data point value and its date
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField);

  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param identifierBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);

  //-------------------------------------------------------------------------
  
  // By External Id/Field/Resolution key - without identifierValidityDate
  
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
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey);

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
   * Finds a time-series from identifierBundle using configuration.
   * <p>
   * The validity date for identifiers is set to today's date.
   * This returns a subset of the data points filtered by the dates provided and limited to the
   * specified maximum number of points:
   * +ve maxPoints returns at most maxPoints data points counting forwards from the earliest date
   * -ve maxPoints returns at most -maxPoints data points counting backwards from the latest date
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @param maxPoints the maximum number of points to be returned
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints);

  // By External Id/Field/Resolution key - with identifierValidityDate

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

  /**
   * Finds a time-series from identifierBundle using configuration checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns a subset of the data points filtered by the dates provided and limited to the
   * specified maximum number of points:
   * +ve maxPoints returns at most maxPoints data points counting forwards from the earliest date
   * -ve maxPoints returns at most -maxPoints data points counting backwards from the latest date
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @param maxPoints the maximum number of points to be returned
   * @return the historical time-series, null if not found
   */
  HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints);

  // By External Id/Field/Resolution key - Latest data point methods

  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey);
  
  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd);
  
  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey);
  
  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param dataField  the type of data required, see {@code HistoricalTimeSeriesFields}, not null
   * @param identifierBundle  the identifier bundle to retrieve a time-series for, not null
   * @param identifierValidityDate  the date that the identifier must be valid on, null to use all identifierBundle
   * @param resolutionKey  the key to resolve the correct time-series, null to use default rules
   * @param start  the start date, null will load the earliest date
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return  a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
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
   * Returns the external id bundle associated with the time series identified by the specified unique id.
   * @param uniqueId the unique id of the time series in question
   * @return the bundle of external ids associated with the time series, or null if it doesn't exist
   */
  ExternalIdBundle getExternalIdBundle(UniqueId uniqueId);

}

