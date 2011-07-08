/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A general-purpose daily historical time-series master.
 * <p>
 * The time-series master provides a uniform view over the database.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface HistoricalTimeSeriesMaster extends AbstractMaster<HistoricalTimeSeriesDocument> {
  // TODO: metadata

  /**
   * Searches for time-series matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HistoricalTimeSeriesSearchResult search(HistoricalTimeSeriesSearchRequest request);

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
   * Queries the history of a single time-series.
   * <p>
   * The request must contain an object identifier to identify the time-series.
   * 
   * @param request  the history request, not null
   * @return the time-series history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HistoricalTimeSeriesHistoryResult history(HistoricalTimeSeriesHistoryRequest request);

  //-------------------------------------------------------------------------
//  /**
//  * Gets the time-series data points without the document.
//  * <p>
//  * The main get request returns the document describing the time-series.
//  * This request gets the series itself.
//  * 
//  * @param objectId  the time-series object identifier, not null
//  * @param fromDateInclusive  the inclusive start date of the points to remove, not null
//  * @param toDateInclusive  the inclusive end date of the points to remove, not null
//  * @param versionCorrection  the version-correction locator to search at, not null
//  * @return the current state of the document, may be an update of the input document, not null
//  * @throws IllegalArgumentException if the identifier is invalid
//  * @throws DataNotFoundException if there is no document with that unique identifier
//  */
//  HistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive);

  /**
   * Updates the time-series by appending new data points.
   * <p>
   * This is used to append new time-series data points.
   * The specified request must contain the unique identifier.
   * If the identifier has a version it must be the latest version.
   * Only the unique identifier is returned.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, an update does not prevent retrieval or correction of an earlier version.
   * 
   * @param uniqueId  the update request, not null
   * @param series  the series to append, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  UniqueIdentifier updateDataPoints(UniqueIdentifier uniqueId, LocalDateDoubleTimeSeries series);

  /**
   * Corrects the time-series by removing data points.
   * <p>
   * This takes each point in the specified series and applies it on top of the existing data.
   * If the date already has a value, the value is corrected if different.
   * If the date is not currently present, it is added.
   * The addition occurs as though the original was added at the base version instant,
   * which is different to just adding a point using {@link #updateDataPoints}.
   * The correction applies as of the current instant.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, a correction does not prevent retrieval or correction of an earlier version.
   * 
   * @param uniqueId  the unique identifier of the time-series, not null
   * @param series  the series to correct to, no null values, not null
   * @return the unique identifier of the updated document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  UniqueIdentifier correctDataPoints(UniqueIdentifier uniqueId, LocalDateDoubleTimeSeries series);

  /**
   * Corrects the time-series by removing data points.
   * <p>
   * The correction applies as of the current instant.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, a correction does not prevent retrieval or correction of an earlier version.
   * 
   * @param uniqueId  the unique identifier of the time-series, not null
   * @param fromDateInclusive  the inclusive start date of the points to remove, not null
   * @param toDateInclusive  the inclusive end date of the points to remove, not null
   * @return the unique identifier of the updated document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  UniqueIdentifier correctRemoveDataPoints(UniqueIdentifier uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive);

}
