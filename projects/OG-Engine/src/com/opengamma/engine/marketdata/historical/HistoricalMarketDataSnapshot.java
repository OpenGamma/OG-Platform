/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalMarketDataSnapshot.class);

  private final HistoricalMarketDataSpecification _marketDataSpec;
  private final HistoricalTimeSeriesSource _timeSeriesSource;
  
  public HistoricalMarketDataSnapshot(HistoricalMarketDataSpecification marketDataSpec, HistoricalTimeSeriesSource timeSeriesSource) {
    _marketDataSpec = marketDataSpec;
    _timeSeriesSource = timeSeriesSource;
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    // TODO something better thought-out here
    return getMarketDataSpec().getSnapshotDate().atMidnight().atZone(TimeZone.UTC).toInstant();
  }

  @Override
  public Instant getSnapshotTime() {
    return getSnapshotTimeIndication();
  }
  
  @Override
  public Object query(ValueRequirement requirement) {
    final LocalDate date = getMarketDataSpec().getSnapshotDate();
    String valueName = requirement.getValueName();
    ExternalIdBundle identifiers = ExternalIdBundle.of(requirement.getTargetSpecification().getIdentifier());
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        valueName,
        identifiers,
        getMarketDataSpec().getTimeSeriesResolverKey(),
        date,
        true, 
        date, 
        true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      s_logger.info("No time-series for {}, {}", identifiers, valueName);
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

}
