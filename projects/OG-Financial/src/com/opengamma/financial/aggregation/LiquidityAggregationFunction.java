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
public class LiquidityAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Liquidity (days to liquidate)";
  private static final String FIELD = "VOLUME";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_LIQUIDITY = "N/A";

  private HistoricalTimeSeriesSource _htsSource;
  
  public LiquidityAggregationFunction(HistoricalTimeSeriesSource htsSource) {
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
        Double volume = historicalTimeSeries.getTimeSeries().getLatestValue();
        double daysToLiquidate = volume / position.getQuantity().doubleValue();
        if (daysToLiquidate < 0.2) {
          return "< 0.2";
        } else if (daysToLiquidate < 0.5) {
          return "0.2 - 0.5";
        } else if (daysToLiquidate < 1.0) {
          return "0.5 - 1";
        } else if (daysToLiquidate < 3) {
          return "1 - 3";
        } else if (daysToLiquidate < 10) {
          return "3 - 10";
        } else {
          return "10+";
        }
      } else {
        return NO_LIQUIDITY;
      }
    } catch (UnsupportedOperationException ex) {
      return NO_LIQUIDITY;
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Arrays.asList("< 0.2", "0.2 - 0.5", "0.5 - 1", "1 - 3", "3 - 10", "10+");
  }
}
