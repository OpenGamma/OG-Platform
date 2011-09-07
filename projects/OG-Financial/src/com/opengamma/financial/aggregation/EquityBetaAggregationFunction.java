/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;

/**
 * Function to classify positions by Currency.
 *
 */
public class EquityBetaAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Beta";
  private static final String FIELD = "APPLIED_BETA";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_BETA = "N/A";

  private HistoricalTimeSeriesSource _htsSource;
  
  public EquityBetaAggregationFunction(HistoricalTimeSeriesSource htsSource) {
    _htsSource = htsSource;
  }
  
  @Override
  public String classifyPosition(Position position) {
    try {
      LocalDate yesterday = Clock.systemDefaultZone().yesterday();
      LocalDate oneWeekAgo = yesterday.minusDays(7);
      HistoricalTimeSeries historicalTimeSeries = _htsSource.getHistoricalTimeSeries(FIELD, position.getSecurity().getExternalIdBundle(), 
                                                                                     RESOLUTION_KEY, oneWeekAgo, true, yesterday, true);
      if (historicalTimeSeries != null && historicalTimeSeries.getTimeSeries() != null && !historicalTimeSeries.getTimeSeries().isEmpty()) {
        Double beta = historicalTimeSeries.getTimeSeries().getLatestValue();
        if (beta < 0.5) {
          return "< 0.5";
        } else if (beta < 0.75) {
          return "0.5 - 0.75";
        } else if (beta < 0.9) {
          return "0.75 - 0.9";
        } else if (beta < 1.25) {
          return "0.9 - 1.25";
        } else {
          return "> 1.25";
        }
      } else {
        return NO_BETA;
      }
    } catch (UnsupportedOperationException ex) {
      return NO_BETA;
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Arrays.asList("< 0.5", "0.5 - 0.75", "0.75 - 0.9", "0.9 - 1.25", "> 1.25");
  }
}
