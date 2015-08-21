/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.sesame.InflationProviderBundle;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;



/**
 * Market data builder for inflation curve bundles.
 */
public class InflationMulticurveMarketDataBuilder implements MarketDataBuilder {


  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    return ImmutableSet.of();
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {
    return ImmutableSet.of();
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource,
                                                                  CyclePerturbations cyclePerturbations) {
    ImmutableMap.Builder<SingleValueRequirement, Result<?>> resultsBuilder = ImmutableMap.builder();
    for (SingleValueRequirement requirement : requirements) {
      Result<Object> failure = Result.failure(FailureStatus.NOT_APPLICABLE,
                                              "Building of inflation curves isn't supported for requirement {}",
                                              requirement);
      resultsBuilder.put(requirement, failure);
    }
    return resultsBuilder.build();
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(MarketDataBundle marketDataBundle,
                                                                                                    Set<TimeSeriesRequirement> requirements,
                                                                                                    MarketDataSource marketDataSource,
                                                                                                    CyclePerturbations cyclePerturbations) {
    ImmutableMap.Builder<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> resultsBuilder = ImmutableMap.builder();
    for (TimeSeriesRequirement requirement : requirements) {
      Result<? extends DateTimeSeries<LocalDate, ?>> failure = Result.failure(FailureStatus.NOT_APPLICABLE,
                                                                              "Building of inflation curves isn't supported for requirement {}",
                                                                              requirement);
      resultsBuilder.put(requirement, failure);
    }
    return resultsBuilder.build();
  }

  @Override
  public Class<? extends MarketDataId<InflationProviderBundle>> getKeyType() {
    return InflationMulticurveId.class;
  }
}
