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
 *
 */
public class MapMarketDataBundleBuilder {

  private final MarketDataTime _time;

  private final Map<MarketDataRequirement, Object> _marketData = new HashMap<>();

  private final Map<MarketDataId, DateTimeSeries<LocalDate, ?>> _timeSeries = new HashMap<>();

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

  public MapMarketDataBundleBuilder(ZonedDateTime time) {
    this(MarketDataTime.of(time));
  }

  public MapMarketDataBundleBuilder() {
    this(MarketDataTime.VALUATION_TIME);
  }

  public MapMarketDataBundleBuilder(LocalDate date) {
    this(MarketDataTime.of(date));
  }

  // TODO rename add -> addSingleValue and addTimeSeries

  public MapMarketDataBundleBuilder add(MarketDataId id, Object marketDataItem) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(marketDataItem, "marketDataItem");

    MarketDataRequirement requirement = SingleValueRequirement.of(id, _time);
    _marketData.put(requirement, marketDataItem);
    return this;
  }

  public MapMarketDataBundleBuilder add(MarketDataId id, Object marketDataItem, LocalDate marketDataDate) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(marketDataItem, "marketDataItem");
    ArgumentChecker.notNull(marketDataDate, "marketDataDate");

    _marketData.put(SingleValueRequirement.of(id, marketDataDate), marketDataItem);
    return this;
  }

  public MapMarketDataBundleBuilder add(MarketDataId id, Object marketDataItem, ZonedDateTime marketDataTime) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(marketDataItem, "marketDataItem");
    ArgumentChecker.notNull(marketDataTime, "marketDataTime");

    _marketData.put(SingleValueRequirement.of(id, marketDataTime), marketDataItem);
    return this;
 }

  public MapMarketDataBundleBuilder add(MarketDataId id, DateTimeSeries<LocalDate, ?> timeSeries) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(timeSeries, "timeSeries");

    _timeSeries.put(id, timeSeries);
    return this;
  }

  public MapMarketDataBundleBuilder addSingleValues(Map<SingleValueRequirement, Object> items) {
    ArgumentChecker.notNull(items, "items");
    _marketData.putAll(items);
    return this;
  }

  public MapMarketDataBundleBuilder addTimeSeries(Map<MarketDataId, DateTimeSeries<LocalDate, ?>> timeSeries) {
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _timeSeries.putAll(timeSeries);
    return this;
  }

  public MapMarketDataBundle build() {
    return new MapMarketDataBundle(_time, _marketData, _timeSeries);
  }
}
