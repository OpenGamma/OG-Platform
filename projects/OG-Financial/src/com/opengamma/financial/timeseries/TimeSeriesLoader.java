/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

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
   * Loads timeseries from datasource like BLOOMBERG/REUTERS and populate TimeSeriesMaster
   * 
   * @param identifiers the identifiers, not-null
   * @param dataProvider the dataProvider
   * @param dataField the datafield, not-null
   * @param startDate the start date of timeseries
   * @param endDate the end date of timeseries
   * @return the map of Identifier to UniqueIdentifier of loaded timeseries
   */
  Map<Identifier, UniqueIdentifier> loadTimeSeries(Set<Identifier> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate);

}
