/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * Loader for obtaining historical time-series.
 * <p>
 * A historical time-series is the representation of a value over time,
 * such as the price of an equity.
 * The series is typically obtained from a major data source.
 */
@PublicSPI
public interface HistoricalTimeSeriesLoader {

  /**
   * Loads a time-series from a data source.
   * <p>
   * This will typically update an associated {@code HistoricalTimeSeriesMaster}.
   * 
   * @param identifiers  the identifiers, not null
   * @param dataProvider  the data provider, null should default to a sensible value
   * @param dataField  the data field, not null
   * @param startDate  the start date of time-series, null should default to a sensible value
   * @param endDate  the end date of time-series, null should default to a sensible value
   * @return the map of Identifier to UniqueId of loaded time-series, not null
   */
  Map<Identifier, UniqueId> addTimeSeries(
      Set<Identifier> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate);

  /**
   * Updates the time-series with the latest data from a data source.
   * <p>
   * This will typically update an associated {@code HistoricalTimeSeriesMaster}.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return true if the operation is successful otherwise false 
   */
  boolean updateTimeSeries(UniqueId uniqueId);

}
