/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.core.historicaltimeseries.impl;

import org.threeten.bp.LocalDate;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

//TODO: Make ExternalId & UniqueId handling more consistent

/**
 * Interface representing a source of simulation series data.
 * Similar to a {@link HistoricalTimeSeriesSource} but allowing multiple series
 * to co-exist for the same id each corresponding to the series that should apply on a specific date.
 */
public interface SimulationSeriesSource extends ChangeProvider {

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
   * Clone this source and fix the simulation date.
   * @param date the simulation date to fix to
   * @return a copy of this source with the simulation date fixed
   */
  SimulationSeriesSource withSimulationDate(LocalDate date);

  /**
   * Gets the currentSimulationExecutionDate.
   * @return the currentSimulationExecutionDate
   */
  LocalDate getCurrentSimulationExecutionDate();

  /**
   * Update series point
   *
   * @param uniqueId series unique id
   * @param simulationExecutionDate the simulation date
   * @param valueDate the series point date
   * @param value the value to update to
   */
  void updateTimeSeriesPoint(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDate valueDate, double value);

  /**
   * Update a series with the given points
   *
   * @param uniqueId series unique id
   * @param simulationExecutionDate the simulation date
   * @param timeseries the simulation series
   */
  void updateTimeSeries(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDateDoubleTimeSeries timeseries);

  /**
   * Replace a series.
   *
   * @param uniqueId series unique id
   * @param simulationExecutionDate the simulation date
   * @param timeSeries the simulation series
   */
  void replaceTimeSeries(UniqueId uniqueId, LocalDate simulationExecutionDate, LocalDateDoubleTimeSeries timeSeries);

  /**
   * Clear all series for a given date
   *
   * @param simulationExecutionDate the simulation date
   */
  void clearExecutionDate(LocalDate simulationExecutionDate);

}
