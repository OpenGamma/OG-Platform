/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalMarketDataSnapshot.class);

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final Instant _snapshotInstant;
  private final LocalDate _snapshotDate;
  private final String _timeSeriesFieldResolverKey;
  
  public HistoricalMarketDataSnapshot(HistoricalTimeSeriesSource timeSeriesSource, Instant snapshotInstant, LocalDate snapshotDate, String timeSeriesFieldResolverKey) {
    _timeSeriesSource = timeSeriesSource;
    _snapshotInstant = snapshotInstant;
    _snapshotDate = snapshotDate;
    _timeSeriesFieldResolverKey = timeSeriesFieldResolverKey;
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotInstant;
  }

  @Override
  public Instant getSnapshotTime() {
    return getSnapshotTimeIndication();
  }
  
  @Override
  public Object query(ValueRequirement requirement) {
    String valueName = requirement.getValueName();
    ExternalIdBundle identifiers = ExternalIdBundle.of(requirement.getTargetSpecification().getIdentifier());
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        valueName,
        identifiers,
        _timeSeriesFieldResolverKey,
        _snapshotDate,
        true, 
        _snapshotDate, 
        true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      s_logger.info("No time-series for {}, {}", identifiers, valueName);
      return null;
    }
    return hts.getTimeSeries().getValue(_snapshotDate);
  }

  //-------------------------------------------------------------------------  
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

}
