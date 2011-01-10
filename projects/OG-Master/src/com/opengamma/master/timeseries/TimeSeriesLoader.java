/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Provides bulk loading of historical data
 */
public interface TimeSeriesLoader {

  /**
   * Loads a time-series from a data source like BLOOMBERG/REUTERS in order to populate the master.
   * 
   * @param identifiers  the identifiers, not null
   * @param dataProvider  the data provider, null should default to a sensible value
   * @param dataField  the data field, not null
   * @param startDate  the start date of time-series, null should default to a sensible value
   * @param endDate  the end date of time-series, null should default to a sensible value
   * @return the map of Identifier to UniqueIdentifier of loaded time-series, not null
   */
  Map<Identifier, UniqueIdentifier> loadTimeSeries(
      Set<Identifier> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate);

}
