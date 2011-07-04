/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata;

import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose daily historical time-series master.
 * <p>
 * The time-series master provides a uniform view over the database.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface HistoricalTimeSeriesMaster {

  /**
   * Value for unknown data provider in the database.
   */
  String UNKNOWN_PROVIDER = "UNKNOWN";
  /**
   * Value for Unknown observation time in the database.
   */
  String UNKNOWN_OBSERVATION_TIME = "UNKNOWN";

  /**
   * Gets all the identifiers.
   * 
   * @return the list of identifiers, not null
   */
  List<IdentifierBundleWithDates> getAllIdentifiers();

  /**
   * Searches for time-series matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HistoricalTimeSeriesSearchResult search(HistoricalTimeSeriesSearchRequest request);

  /**
   * Gets a time-series by unique identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  HistoricalTimeSeriesDocument get(UniqueIdentifier uniqueId);

  /**
   * Gets a time-series document controlling the amount of data returned.
   * <p>
   * This returns a single time-series document by unique identifier.
   * As the time-series is potentially large, the request allows the returned
   * data points to be filtered.
   * 
   * @param request  the batch data request, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  HistoricalTimeSeriesDocument get(HistoricalTimeSeriesGetRequest request);

  /**
   * Adds a time-series to the data store.
   * <p>
   * The unique identifier will be set in the response.
   * 
   * @param document  the document, not null
   * @return the added document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HistoricalTimeSeriesDocument add(HistoricalTimeSeriesDocument document);

  /**
   * Updates a time-series in the data store.
   * <p>
   * The specified document must contain the unique identifier.
   * 
   * @param document  the document, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no time-series with that unique identifier
   */
  HistoricalTimeSeriesDocument update(HistoricalTimeSeriesDocument document);

  /**
   * Removes a time-series from the data store.
   * 
   * @param uniqueId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void remove(final UniqueIdentifier uniqueId);

  /**
   * Searches for time-series matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  HistoricalTimeSeriesHistoryResult searchHistoric(HistoricalTimeSeriesHistoryRequest request);

  //-------------------------------------------------------------------------
  /**
   * Gets a data point by unique identifier.
   * <p> 
   * The dataPoint UID is of the format {@code HistoricalTimeSeriesUID-YYYYMMDD}.
   * 
   * @param dataPointId  the data point unique identifier, not null
   * @return the data point document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  DataPointDocument getDataPoint(UniqueIdentifier dataPointId);

  /**
   * Adds a data point to an existing time-series in the data store.
   * 
   * @param document  the data point document, not null
   * @return the added document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  DataPointDocument addDataPoint(DataPointDocument document);

  /**
   * Updates a data point in the data store.
   * <p>
   * The specified document must contain the value and the unique identifier.
   * 
   * @param document  the data point document, not null
   * @return the updated data point document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  DataPointDocument updateDataPoint(DataPointDocument document);

  /**
   * Removes a data point from a time-series in the data store.
   * <p> 
   * The dataPoint UID is of the format {@code HistoricalTimeSeriesUID-YYYYMMDD}.
   * 
   * @param dataPointId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void removeDataPoint(final UniqueIdentifier dataPointId);

  //-------------------------------------------------------------------------
  /**
   * Append data points to an existing time-series.
   * <p>
   * This is a bulk update method to add multiple data points to an existing time-series.
   * The document must contain the unique identifier for the time-series to append to.
   * 
   * @param document  the time series document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void appendTimeSeries(HistoricalTimeSeriesDocument document);

  /**
   * Removes all data points before the given date.
   * <p>
   * This is a bulk update method to remove multiple data points from an existing time-series.
   * 
   * @param historicalTimeSeriesId  the historical time-series to operate on, not null
   * @param firstDateToRetain  remove all data points before this date, not null
   */
  void removeDataPoints(UniqueIdentifier historicalTimeSeriesId, LocalDate firstDateToRetain);

  //-------------------------------------------------------------------------
  /**
   * Searches for a time-series unique identifier matching specific criteria.
   * 
   * @param securityBundle  the security identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @return the unique identifier, null if not found
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField);

  /**
   * Searches for a time-series unique identifier matching specific criteria.
   * 
   * @param securityBundle  the security identifier bundle, not null
   * @param identifierValidityDate  the date on which identifiers must be valid, null for no restriction
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @return the unique identifier, null if not found
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField);

}
