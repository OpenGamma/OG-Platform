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
 * Provides bulk loading of historical data
 */
@PublicSPI
public interface TimeSeriesLoader {

  /**
   * Loads time-series from a data source like BLOOMBERG/REUTERS and add to {@link TimeSeriesMaster}.
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
   * Reloads the timeseries from a data source and updates {@link TimeSeriesMaster} with the new series
   * 
   * @param uniqueIdentifier the unique identifier, not null
   * @return true if the operation is successful otherwise false 
   */
  boolean updateTimeSeries(UniqueIdentifier uniqueIdentifier);
}
