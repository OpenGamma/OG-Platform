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

import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSnapshotSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot implements MarketDataSnapshot {

  private final HistoricalMarketDataSnapshotSpecification _snapshotSpec;
  private final HistoricalTimeSeriesSource _timeSeriesSource;
  
  public HistoricalMarketDataSnapshot(HistoricalMarketDataSnapshotSpecification snapshotSpec, HistoricalTimeSeriesSource timeSeriesSource) {
    _snapshotSpec = snapshotSpec;
    _timeSeriesSource = timeSeriesSource;
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    // TODO something better thought-out here
    return getSnapshotSpec().getSnapshotDate().atMidnight().atZone(TimeZone.UTC).toInstant();
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
  public boolean hasStructuredData() {
    return false;
  }

  @Override
  public Object query(ValueRequirement requirement) {
    LocalDate date = getSnapshotSpec().getSnapshotDate();
    Identifier identifier = requirement.getTargetSpecification().getIdentifier();
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        IdentifierBundle.of(identifier), 
        getSnapshotSpec().getDataSource(), 
        getSnapshotSpec().getDataProvider(), 
        getSnapshotSpec().getDataField(), 
        date, 
        true, 
        date, 
        false);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      return null;
    }
    return hts.getTimeSeries().getValue(getSnapshotSpec().getSnapshotDate());
  }

  @Override
  public Object query(StructuredMarketDataKey marketDataKey) {
    return null;
  }
  
  //-------------------------------------------------------------------------
  private HistoricalMarketDataSnapshotSpecification getSnapshotSpec() {
    return _snapshotSpec;
  }
  
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

}
