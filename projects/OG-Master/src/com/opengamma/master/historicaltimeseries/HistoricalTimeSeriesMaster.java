/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A general-purpose daily historical time-series master.
 * <p>
 * The time-series master consists of two parts - the information about the series
 * and the series data points themselves.
 * This separation provides the optimal storage scheme for the data.
 * It is necessary, as the versioning of the time-series data points is distinct
 * from the versioning of the information.
 */
@PublicSPI
public interface HistoricalTimeSeriesMaster extends AbstractMaster<HistoricalTimeSeriesInfoDocument> {
  // TODO: metadata

  /**
   * Searches for time-series matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request);

  /**
   * Queries the history of a single time-series.
   * <p>
   * The request must contain an object identifier to identify the time-series.
   * 
   * @param request  the history request, not null
   * @return the time-series history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request);

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series data points.
   * <p>
   * The main get request returns the document describing the time-series.
   * This method gets the series itself.
   * 
   * @param uniqueId  the time-series unique identifier, not null
   * @param fromDateInclusive  the inclusive start date of the points to remove, not null
   * @param toDateInclusive  the inclusive end date of the points to remove, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  ManageableHistoricalTimeSeries getTimeSeries(UniqueIdentifier uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive);

  /**
   * Gets the time-series data points.
   * <p>
   * The main get request returns the document describing the time-series.
   * This method gets the series itself.
   * 
   * @param objectId  the time-series object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @param fromDateInclusive  the inclusive start date of the points to remove, not null
   * @param toDateInclusive  the inclusive end date of the points to remove, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive);

  /**
   * Updates the time-series by adding new data points.
   * <p>
   * This is used to append new time-series data points.
   * The points must be after the latest current data point.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, an update does not prevent retrieval or correction of an earlier version.
   * 
   * @param objectId  the time-series object identifier, not null
   * @param series  the series to add, not null
   * @return the new time-series unique identifier, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  UniqueIdentifier updateTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series);

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
   * @param objectId  the time-series object identifier, not null
   * @param series  the series to correct to, no null values, not null
   * @return the unique identifier of the updated document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  UniqueIdentifier correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series);

  /**
   * Corrects the time-series by removing data points.
   * <p>
   * The correction applies as of the current instant.
   * <p>
   * A full master will store detailed historic information on documents.
   * Thus, a correction does not prevent retrieval or correction of an earlier version.
   * 
   * @param objectId  the time-series object identifier, not null
   * @param fromDateInclusive  the inclusive start date of the points to remove, not null
   * @param toDateInclusive  the inclusive end date of the points to remove, not null
   * @return the unique identifier of the updated document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  UniqueIdentifier removeTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive);

}
