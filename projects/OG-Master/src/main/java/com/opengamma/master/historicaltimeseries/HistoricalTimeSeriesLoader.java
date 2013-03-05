/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A tool for loading time-series into a master.
 * <p>
 * A historical time-series is the representation of a value over time,
 * such as the price of an equity.
 * The series is typically obtained from a major data source.
 * <p>
 * The time-series loader provides the functionality to load new time-series into the system.
 * The loaded time-series will be placed into a master.
 * <p>
 * Implementations will check the master before loading to ensure that the same
 * time-series is not loaded twice.
 */
@PublicSPI
public interface HistoricalTimeSeriesLoader {

  /**
   * Loads a time-series from a data source.
   * <p>
   * The securities are specified by an external identifier.
   * The result is keyed by the same identifier.
   * A missing entry in the result occurs if the time-series information could not be found
   * 
   * @param identifiers  the identifiers, not null
   * @param dataProvider  the data provider, null should default to a sensible value
   * @param dataField  the data field, not null
   * @param startDate  the start date of time-series, null should default to a sensible value
   * @param endDate  the end date of time-series, null should default to a sensible value
   * @return the map of external to unique identifier of loaded time-series, not null
   */
  Map<ExternalId, UniqueId> loadTimeSeries(
      Set<ExternalId> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate);

  /**
   * Gets one or more security information objects from the underlying data source.
   * <p>
   * This is the underlying operation.
   * All other load methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the security information result, not null
   * @throws RuntimeException if a problem occurs
   */
  HistoricalTimeSeriesLoaderResult loadTimeSeries(HistoricalTimeSeriesLoaderRequest request);

  /**
   * Updates the time-series with the latest data from a data source.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return true if the operation is successful otherwise false 
   */
  boolean updateTimeSeries(UniqueId uniqueId);

}
