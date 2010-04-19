/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import javax.time.calendar.LocalDate;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 *
 * @author yomi
 */
public interface HistoricalDataProvider {
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(IdentifierBundle dsids, String dataSource, String dataProvider, String field);
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(IdentifierBundle dsids, String dataSource, String dataProvider, String field, LocalDate start, LocalDate end);
}
