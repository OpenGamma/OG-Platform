/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * Loader for obtaining a time-series.
 * <p>
 * A time-series is a representation of a value over time, such as the price of an equity.
 * The series is typically obtained from a major data source.
 */
@PublicSPI
public interface HistoricalDataLoader {

  /**
   * Loads a time-series from a data source.
   * <p>
   * This will typically update an associated {@code TimeSeriesMaster}.
   * 
   * @param identifiers  the identifiers, not null
   * @param dataProvider  the data provider, null should default to a sensible value
   * @param dataField  the data field, not null
   * @param startDate  the start date of time-series, null should default to a sensible value
   * @param endDate  the end date of time-series, null should default to a sensible value
   * @return the map of Identifier to UniqueIdentifier of loaded time-series, not null
   */
  Map<Identifier, UniqueIdentifier> addTimeSeries(
      Set<Identifier> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate);

  /**
   * Updates the time-series with the latest data from a data source.
   * <p>
   * This will typically update an associated {@code TimeSeriesMaster}.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return true if the operation is successful otherwise false 
   */
  boolean updateTimeSeries(UniqueIdentifier uniqueId);

}
