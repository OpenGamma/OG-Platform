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
import com.opengamma.engine.marketdata.MarketDataTargetResolver;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalMarketDataSnapshot.class);

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final Instant _snapshotInstant;
  private final LocalDate _snapshotDate;
  private final String _timeSeriesResolverKey;
  private final MarketDataTargetResolver _targetResolver;
  
  /**
   * Creates a market data snapshot based on historical time-series data.
   * 
   * @param timeSeriesSource  the time-series source, not null
   * @param snapshotInstant  the snapshot instant to report to the engine, not null
   * @param snapshotDate  the date of the required value, null for the latest
   * @param timeSeriesResolverKey  the time series resolver key, null for default
   * @param targetResolver  the market data target resolver, not null
   */
  public HistoricalMarketDataSnapshot(HistoricalTimeSeriesSource timeSeriesSource,
                                      Instant snapshotInstant,
                                      LocalDate snapshotDate,
                                      String timeSeriesResolverKey,
                                      MarketDataTargetResolver targetResolver) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    ArgumentChecker.notNull(snapshotInstant, "snapshotInstant");
    _timeSeriesSource = timeSeriesSource;
    _snapshotInstant = snapshotInstant;
    _snapshotDate = snapshotDate;
    _timeSeriesResolverKey = timeSeriesResolverKey;
    _targetResolver = targetResolver;
  }
  
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "HistoricalMarketDataSnapshot:" + getSnapshotTime());
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
  public ComputedValue query(ValueRequirement requirement) {
    String valueName = requirement.getValueName();
    ExternalIdBundle identifiers = _targetResolver.getExternalIdBundle(requirement);
    if (identifiers == null) {
      s_logger.warn("Unable to resolve requirement {} to an external ID bundle", requirement);
      return null;
    }
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        valueName,
        identifiers,
        _snapshotDate,
        _timeSeriesResolverKey,
        _snapshotDate,
        true, 
        _snapshotDate, 
        true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      s_logger.info("No time-series for {}, {}", identifiers, valueName);
      return null;
    }
    return new ComputedValue(MarketDataUtils.createMarketDataValue(requirement), _snapshotDate != null ? hts.getTimeSeries().getValue(_snapshotDate) : hts.getTimeSeries()
        .getLatestValue());
  }

  //-------------------------------------------------------------------------  
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

}
