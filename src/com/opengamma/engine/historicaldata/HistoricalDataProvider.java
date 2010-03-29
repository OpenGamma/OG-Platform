/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import javax.time.calendar.LocalDate;

import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 *
 * @author yomi
 */
public interface HistoricalDataProvider {
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(DomainSpecificIdentifiers dsids, String dataSource, String dataProvider, String field);
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(DomainSpecificIdentifiers dsids, String dataSource, String dataProvider, String field, LocalDate start, LocalDate end);
}
