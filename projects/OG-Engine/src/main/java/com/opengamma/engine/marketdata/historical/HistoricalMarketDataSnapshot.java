/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueSpecification;
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

  /**
   * Creates a market data snapshot based on historical time-series data. The data provider will have created value specifications which reference the resolved time series.
   * 
   * @param timeSeriesSource the time-series source, not null
   * @param snapshotInstant the snapshot instant to report to the engine, not null
   * @param snapshotDate the date of the required value, null for the latest
   */
  public HistoricalMarketDataSnapshot(final HistoricalTimeSeriesSource timeSeriesSource, final Instant snapshotInstant, final LocalDate snapshotDate) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    ArgumentChecker.notNull(snapshotInstant, "snapshotInstant");
    _timeSeriesSource = timeSeriesSource;
    _snapshotInstant = snapshotInstant;
    _snapshotDate = snapshotDate;
  }

  @Override
  public UniqueId getUniqueId() {
    // REVIEW 2013-02-04 Andrew -- This is not an appropriate unique identifier.
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "HistoricalMarketDataSnapshot:" + getSnapshotTime());
  }
  
  @Override
  public boolean isInitialized() {
    return true;
  }
  
  @Override
  public boolean isEmpty() {
    return false;
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
  public Object query(final ValueSpecification specification) {
    final UniqueId htsIdentifier = specification.getTargetSpecification().getUniqueId();
    final HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(htsIdentifier, _snapshotDate, true, _snapshotDate, true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      s_logger.info("No time-series for {}", specification);
      return null;
    }
    final Double value = (_snapshotDate != null) ? hts.getTimeSeries().getValue(_snapshotDate) : hts.getTimeSeries().getLatestValue();
    if (value == null) {
      return null;
    }
    final String normalization = specification.getProperty(AbstractHistoricalMarketDataProvider.NORMALIZATION_PROPERTY);
    if (normalization != null) {
      return HistoricalTimeSeriesAdjustment.parse(normalization).adjust(value);
    } else {
      return value;
    }
  }

  //-------------------------------------------------------------------------
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

}
