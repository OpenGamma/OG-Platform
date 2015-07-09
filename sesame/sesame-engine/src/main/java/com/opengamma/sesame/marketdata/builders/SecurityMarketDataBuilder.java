/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SecurityId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Market data builder for handling {@link SecurityId}.
 * <p>
 * This delegates to {@link RawMarketDataBuilder} for looking up the data.
 */
public class SecurityMarketDataBuilder implements MarketDataBuilder {

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    RawId<?> rawId = rawId(requirement);
    return ImmutableSet.<MarketDataRequirement>of(SingleValueRequirement.of(rawId, requirement.getMarketDataTime()));
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {

    RawId<?> rawId = rawId(requirement);
    LocalDateRange dateRange = requirement.getMarketDataTime().getDateRange();
    return ImmutableSet.<MarketDataRequirement>of(TimeSeriesRequirement.of(rawId, dateRange));
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource,
                                                                  CyclePerturbations cyclePerturbations) {
    ImmutableMap.Builder<SingleValueRequirement, Result<?>> results = ImmutableMap.builder();

    for (SingleValueRequirement requirement : requirements) {
      RawId<?> rawId = rawId(requirement);
      results.put(requirement, marketDataBundle.get(rawId, rawId.getMarketDataType()));
    }
    return results.build();
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource,
      CyclePerturbations cyclePerturbations) {

    ImmutableMap.Builder<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> results =
        ImmutableMap.builder();

    for (TimeSeriesRequirement requirement : requirements) {
      RawId<?> rawId = rawId(requirement);
      LocalDateRange dateRange = requirement.getMarketDataTime().getDateRange();
      @SuppressWarnings({"unchecked", "rawtypes" })
      Result<DateTimeSeries<LocalDate, ?>> result =
          (Result) marketDataBundle.get(rawId, rawId.getMarketDataType(), dateRange);
      results.put(requirement, result);
    }
    return results.build();
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return SecurityId.class;
  }

  private static RawId rawId(MarketDataRequirement requirement) {
    SecurityId<?, ?> id = (SecurityId<?, ?>) requirement.getMarketDataId();
    return RawId.of(id.getId(), id.getMarketDataType(), id.getFieldName());
  }
}
