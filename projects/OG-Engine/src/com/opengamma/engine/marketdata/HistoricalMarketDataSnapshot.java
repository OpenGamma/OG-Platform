/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot implements MarketDataSnapshot {

  private final HistoricalMarketDataSpecification _marketDataSpec;
  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final HistoricalMarketDataFieldResolver _fieldResolver;
  
  public HistoricalMarketDataSnapshot(HistoricalMarketDataSpecification marketDataSpec, HistoricalTimeSeriesSource timeSeriesSource, HistoricalMarketDataFieldResolver fieldResolver) {
    _marketDataSpec = marketDataSpec;
    _timeSeriesSource = timeSeriesSource;
    _fieldResolver = fieldResolver;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "HistoricalMarketDataSnapshot:"+getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    // TODO something better thought-out here
    return getMarketDataSpec().getSnapshotDate().atMidnight().atZone(TimeZone.UTC).toInstant();
  }

  @Override
  public void init() {
    // Nothing to do as we query on-the-fly.
  }
  
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    // Nothing to do as we query on-the-fly. Unavailable historical values will not become available by waiting, so
    // ignore the timeout.
  }

  @Override
  public Instant getSnapshotTime() {
    return getSnapshotTimeIndication();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    LocalDate date = getMarketDataSpec().getSnapshotDate();
    ExternalId identifier = requirement.getTargetSpecification().getIdentifier();
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        getFieldResolver().resolve(requirement.getValueName(), getMarketDataSpec().getTimeSeriesFieldResolverKey()),
        ExternalIdBundle.of(identifier),
        getMarketDataSpec().getTimeSeriesResolverKey(),
        date,
        true, 
        date, 
        true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      return null;
    }
    return hts.getTimeSeries().getValue(getMarketDataSpec().getSnapshotDate());
  }
  
  //-------------------------------------------------------------------------
  private HistoricalMarketDataSpecification getMarketDataSpec() {
    return _marketDataSpec;
  }
  
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  private HistoricalMarketDataFieldResolver getFieldResolver() {
    return _fieldResolver;
  }

}
