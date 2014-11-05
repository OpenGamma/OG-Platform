/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Builder for constructing instances of {@link MapMarketDataBundle}.
 */
public class MapMarketDataBundleBuilder {

  /** The time for which the market data is valid. */
  private final MarketDataTime _time;

  /** Single market data values, keyed by their requirement. */
  private final Map<MarketDataRequirement, Object> _marketData = new HashMap<>();

  /** Time series of values, keyed by the ID of the value. */
  private final Map<MarketDataId, DateTimeSeries<LocalDate, ?>> _timeSeries = new HashMap<>();

  /** The valuation time for calculations using the bundle's data. */
  private ZonedDateTime _valuationTime;

  private MapMarketDataBundleBuilder(MarketDataTime time) {
    _time = ArgumentChecker.notNull(time, "time");
  }

  MapMarketDataBundleBuilder(MarketDataTime time,
                             Map<MarketDataRequirement, Object> marketData,
                             Map<MarketDataId, DateTimeSeries<LocalDate, ?>> timeSeries) {
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _time = ArgumentChecker.notNull(time, "time");
    _marketData.putAll(marketData);
    _timeSeries.putAll(timeSeries);
  }

  /**
   * Creates an instance whose single data values are valid indefinitely.
   */
  public MapMarketDataBundleBuilder() {
    this(MarketDataTime.VALUATION_TIME);
  }

  /**
   * Creates an instance whose single data values are valid on a specific date.
   *
   * @param date the date on which the data is valid
   */
  public MapMarketDataBundleBuilder(LocalDate date) {
    this(MarketDataTime.of(date));
  }

  /**
   * Creates an instance whose single data values are valid at a specific time.
   *
   * @param time the time at which the data is valid
   */
  public MapMarketDataBundleBuilder(ZonedDateTime time) {
    this(MarketDataTime.of(time));
  }

  // TODO rename add -> addSingleValue and addTimeSeries

  /**
   * Adds a single value of data to the builder.
   *
   * @param id ID of the data
   * @param marketDataItem the data
   * @return this builder
   */
  public MapMarketDataBundleBuilder add(MarketDataId id, Object marketDataItem) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(marketDataItem, "marketDataItem");

    MarketDataRequirement requirement = SingleValueRequirement.of(id, _time);
    _marketData.put(requirement, marketDataItem);
    return this;
  }

  /**
   * Adds a single value of data to the builder that is valid for a single day.
   *
   * @param id ID of the data
   * @param marketDataItem the data
   * @param marketDataDate the date for which the data is valid
   * @return this builder
   */
  public MapMarketDataBundleBuilder add(MarketDataId id, Object marketDataItem, LocalDate marketDataDate) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(marketDataItem, "marketDataItem");
    ArgumentChecker.notNull(marketDataDate, "marketDataDate");

    _marketData.put(SingleValueRequirement.of(id, marketDataDate), marketDataItem);
    return this;
  }

  /**
   * Adds a single value of data to the builder that is valid for a specific time.
   *
   * @param id ID of the data
   * @param marketDataItem the data
   * @param marketDataTime the time at which the data is valid
   * @return this builder
   */
  public MapMarketDataBundleBuilder add(MarketDataId id, Object marketDataItem, ZonedDateTime marketDataTime) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(marketDataItem, "marketDataItem");
    ArgumentChecker.notNull(marketDataTime, "marketDataTime");

    _marketData.put(SingleValueRequirement.of(id, marketDataTime), marketDataItem);
    return this;
  }

  /**
   * Adds a time series to the builder.
   *
   * @param id ID of the data
   * @param timeSeries the data
   * @return this builder
   */
  public MapMarketDataBundleBuilder add(MarketDataId id, DateTimeSeries<LocalDate, ?> timeSeries) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(timeSeries, "timeSeries");

    _timeSeries.put(id, timeSeries);
    return this;
  }

  /**
   * Adds single values to the builder in bulk.
   *
   * @param items the data
   * @return this builder
   */
  public MapMarketDataBundleBuilder addSingleValues(Map<SingleValueRequirement, Object> items) {
    ArgumentChecker.notNull(items, "items");
    _marketData.putAll(items);
    return this;
  }

  /**
   * Adds time series to the builder in bulk.
   *
   * @param timeSeries the time series
   * @return this builder
   */
  public MapMarketDataBundleBuilder addTimeSeries(Map<MarketDataId, DateTimeSeries<LocalDate, ?>> timeSeries) {
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _timeSeries.putAll(timeSeries);
    return this;
  }

  /**
   * Sets the valuation time that should be used for calculations using this bundle's data.
   *
   * @param valuationTime the valuation time for calculations using this bundle's data
   * @return this builder
   * @deprecated this method is not guaranteed to be permanent
   */
  @Deprecated
  public MapMarketDataBundleBuilder valuationTime(ZonedDateTime valuationTime) {
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    return this;
  }

  /**
   * Builds a market data bundle from the data in this builder.
   * <p>
   * Changes to this builder after calling {@code build()} won't affect the data in the bundle. This builder
   * can continue to be used after calling {@code build()} and can build an unlimited number of bundles.
   *
   * @return a market data bundle containing the data in this builder
   */
  public MapMarketDataBundle build() {
    return new MapMarketDataBundle(_time, _marketData, _timeSeries, _valuationTime);
  }
}
