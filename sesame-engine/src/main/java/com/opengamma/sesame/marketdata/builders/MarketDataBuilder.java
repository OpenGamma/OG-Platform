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

import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.result.Result;

/**
 * Builds market data, either single values or time series. Market data can be simple values, for example the
 * market price of a security, or complex structures derived from market data, for example calibrated curves.
 * <p>
 * Each builder can build a single type of market data, identified by an implementation of {@link MarketDataId}.
 * The builder declares what type of market data it can build using the {@link #getKeyType()} method.
 * <p>
 * Building is a two step process. First, builders are queried to find out what data they require to satisfy
 * a set of requirements. For example, a builder for curves will return requirements for the market data
 * values of its curve nodes. Once a builder's dependencies have been created, they are passed to the builder
 * and it builds and returns the market data.
 */
public interface MarketDataBuilder {

  /**
   * Returns the set of requirements for data needed to build the market data specified by the input requirement.
   * <p>
   * For example, if the input requirement specifies a curve, the returned set might contain requirements
   * for the market data values at the curve nodes.
   * <p>
   * If a builder can satisfy the requirement without needing any other input data (e.g. by directly
   * querying a market data provider) it will return an empty set.
   *
   * @param requirement the requirement
   * @param valuationTime the valuation time for which market data is required
   * @param suppliedData the data supplied by the user. This can be safely ignored. If this method returns
   *   requirements for data that is in the supplied data it is not a problem. It is included to allow
   *   market data builders to examine the data in case it contains requirements that are equivalent but
   *   not necessarily equal to the builders requirements. For example if an FX rate is in the supplied data
   *   that is the inverse of the rate required by the builder, the builder can use it instead of adding
   *   a new requirement.
   * @return requirements for the data needed to satisfy the requirement
   */
  Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                        ZonedDateTime valuationTime,
                                                        Set<? extends MarketDataRequirement> suppliedData);

  /**
   * Returns the set of requirements for data needed to build time series of market data specified by the
   * input requirement.
   * <p>
   * For example, if the input requirement specifies a series of curves, the returned set might contain requirements
   * for the market data values at the curve nodes.
   * <p>
   * If a builder can satisfy the requirement without needing any other input data (e.g. by directly
   * querying a market data provider) it will return an empty set.
   *
   * @param requirement the requirement
   * @param suppliedData the data supplied by the user. This can be safely ignored. If this method returns
   *   requirements for data that is in the supplied data it is not a problem. It is included to allow
   *   market data builders to examine the data in case it contains requirements that are equivalent but
   *   not necessarily equal to the builders requirements. For example if an FX rate is in the supplied data
   *   that is the inverse of the rate required by the builder, the builder can use it.
   * @return requirements for the data needed to satisfy the requirement
   */
  Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                       Set<MarketDataId<?>> suppliedData);

  /**
   * Builds single values of market data.
   * <p>
   * The return value contains an entry for every requirement in the input set. If any market data can't be
   * built the map will contain a failure result.
   *
   * @param marketDataBundle bundle containing the market data required to build the values
   * @param valuationTime the valuation time used when building the market data
   * @param requirements identifies the market data that should be built
   * @param marketDataSource source of raw market data
   * @return a map of market data values keyed by requirement. If a value can't be built the map will contain
   *   an entry with a failure result.
   */
  Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                           ZonedDateTime valuationTime,
                                                           Set<SingleValueRequirement> requirements,
                                                           MarketDataSource marketDataSource);

  /**
   * Builds time series of market data.
   * <p>
   * The return value contains an entry for every requirement in the input set. If any market data can't be
   * built the map will contain a failure result.
   *
   * @param marketDataBundle bundle containing the market data required to build the values
   * @param requirements identifies the market data that should be built
   * @param marketDataSource source of raw market data
   * @return a map of market data values keyed by ID. If a value can't be built the map will contain
   *   an entry with a failure result.
   */
  Map<TimeSeriesRequirement, Result<DateTimeSeries<LocalDate, ?>>> buildTimeSeries(MarketDataBundle marketDataBundle,
                                                                                   Set<TimeSeriesRequirement> requirements,
                                                                                   MarketDataSource marketDataSource);

  /**
   * Returns the type of market data ID this builder can handle.
   *
   * @return the type of market data ID this builder can handle
   */
  Class<? extends MarketDataId> getKeyType();
}
