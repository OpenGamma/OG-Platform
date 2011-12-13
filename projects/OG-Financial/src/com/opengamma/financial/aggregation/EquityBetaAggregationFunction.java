/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.util.tuple.Pair;

/**
 * Function to classify positions by Currency.
 *
 */
public class EquityBetaAggregationFunction implements AggregationFunction<String> {
  private boolean _useAttributes;
  private boolean _includeEmptyCategories;
  
  private static final String MORE_THAN_1_25 = "E) > 1.25";
  private static final String FROM_0_9_TO_1_25 = "D) 0.9 - 1.25";
  private static final String FROM_0_75_TO_0_9 = "C) 0.75 - 0.9";
  private static final String FROM_0_5_TO_0_75 = "B) 0.5 - 0.75";
  private static final String LESS_THAN_0_5 = "A) < 0.5";
  private static final String NAME = "Beta";
  private static final String FIELD = "APPLIED_BETA";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_BETA = "N/A";

  private HistoricalTimeSeriesSource _htsSource;
  
  public EquityBetaAggregationFunction(HistoricalTimeSeriesSource htsSource, boolean useAttributes, boolean includeEmptyCategories) {
    _htsSource = htsSource;
    _useAttributes = useAttributes;
    _includeEmptyCategories = includeEmptyCategories;
  }

  public EquityBetaAggregationFunction(HistoricalTimeSeriesSource htsSource, boolean useAttributes) {
    this(htsSource, useAttributes, true);
  }
  
  public EquityBetaAggregationFunction(HistoricalTimeSeriesSource htsSource) {
    this(htsSource, true);
  }
  
  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NO_BETA;
      }
    } else {
      try {
        Pair<LocalDate, Double> results = _htsSource.getLatestDataPoint(FIELD, null, RESOLUTION_KEY);
        if (results != null && results.getFirst() != null && results.getSecond() != null) {
          Double beta = results.getSecond();
          if (beta < 0.5) {
            return LESS_THAN_0_5;
          } else if (beta < 0.75) {
            return FROM_0_5_TO_0_75;
          } else if (beta < 0.9) {
            return FROM_0_75_TO_0_9;
          } else if (beta < 1.25) {
            return FROM_0_9_TO_1_25;
          } else {
            return MORE_THAN_1_25;
          }
        } else {
          return NO_BETA;
        }
      } catch (UnsupportedOperationException ex) {
        return NO_BETA;
      }
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    if (_includeEmptyCategories) {
      return Arrays.asList(LESS_THAN_0_5, FROM_0_5_TO_0_75, FROM_0_75_TO_0_9, FROM_0_9_TO_1_25, MORE_THAN_1_25, NO_BETA);
    } else {
      return Collections.emptyList();
    }
  }
}
