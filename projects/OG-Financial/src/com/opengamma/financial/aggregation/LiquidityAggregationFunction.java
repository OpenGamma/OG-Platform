/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
  private boolean _useAttributes;
  
  private static final String MORE_THAN_10_0 = "F) 10+";
  private static final String FROM_3_0_TO_10_0 = "E) 3 - 10";
  private static final String FROM_1_0_TO_3_0 = "D) 1 - 3";
  private static final String FROM_0_5_TO_1_0 = "C) 0.5 - 1";
  private static final String FROM_0_2_TO_0_5 = "B) 0.2 - 0.5";
  private static final String LESS_THAN_0_2 = "A) < 0.2";
  private static final String NAME = "Liquidity";
  private static final String FIELD = "VOLUME";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_LIQUIDITY = "N/A";
  
  private HistoricalTimeSeriesSource _htsSource;
  
  public LiquidityAggregationFunction(HistoricalTimeSeriesSource htsSource) {
    this(htsSource, true);
  }
  
  public LiquidityAggregationFunction(HistoricalTimeSeriesSource htsSource, boolean useAttributes) {
    _htsSource = htsSource;
    _useAttributes = useAttributes;
  }
  
  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NO_LIQUIDITY;
      }
    } else {
      try {
        LocalDate yesterday = Clock.systemDefaultZone().yesterday();
        LocalDate oneWeekAgo = yesterday.minusDays(7);
        HistoricalTimeSeries historicalTimeSeries = _htsSource.getHistoricalTimeSeries(FIELD, position.getSecurity().getExternalIdBundle(), 
                                                                                       RESOLUTION_KEY, oneWeekAgo, true, yesterday, true);
        if (historicalTimeSeries != null && historicalTimeSeries.getTimeSeries() != null && !historicalTimeSeries.getTimeSeries().isEmpty()) {
          Double volume = historicalTimeSeries.getTimeSeries().getLatestValue();
          double daysToLiquidate = (volume / position.getQuantity().doubleValue()) * 0.1;
          if (daysToLiquidate < 0.2) {
            return LESS_THAN_0_2;
          } else if (daysToLiquidate < 0.5) {
            return FROM_0_2_TO_0_5;
          } else if (daysToLiquidate < 1.0) {
            return FROM_0_5_TO_1_0;
          } else if (daysToLiquidate < 3) {
            return FROM_1_0_TO_3_0;
          } else if (daysToLiquidate < 10) {
            return FROM_3_0_TO_10_0;
          } else {
            return MORE_THAN_10_0;
          }
        } else {
          return NO_LIQUIDITY;
        }
      } catch (UnsupportedOperationException ex) {
        return NO_LIQUIDITY;
      }
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Arrays.asList(LESS_THAN_0_2, FROM_0_2_TO_0_5, FROM_0_5_TO_1_0, FROM_1_0_TO_3_0, FROM_3_0_TO_10_0, MORE_THAN_10_0, NO_LIQUIDITY);
  }
}
