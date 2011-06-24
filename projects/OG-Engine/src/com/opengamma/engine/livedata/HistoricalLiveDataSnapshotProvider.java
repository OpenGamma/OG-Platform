/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.UserPrincipal;

/**
 * Snapshot provider will always return the historical data on the date provided from the provided historical data source.
 */
public class HistoricalLiveDataSnapshotProvider extends AbstractLiveDataSnapshotProvider implements LiveDataAvailabilityProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalLiveDataSnapshotProvider.class);
  private static final long MILLIS_PER_DAY = 1000 * 3600 * 24;
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private String _dataSource;
  private String _dataProvider;
  private String _field;
  
  public HistoricalLiveDataSnapshotProvider(HistoricalTimeSeriesSource historicalTimeSeriesSource, String dataSource, String dataProvider, String field) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _field = field;
  }
  
  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueRequirement);
    addSubscription(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }

  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    LocalDate date = LocalDate.ofEpochDays(snapshot / MILLIS_PER_DAY);
    Identifier identifier = requirement.getTargetSpecification().getIdentifier();
    HistoricalTimeSeries hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(
        IdentifierBundle.of(identifier), 
        _dataSource, 
        _dataProvider, 
        _field, 
        date, 
        true, 
        date, 
        false);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      return null;
    }
    return hts.getTimeSeries().getValue(date);
  }

  @Override
  public void releaseSnapshot(long snapshot) {
  }

  @Override
  public long snapshot() {
    throw new UnsupportedOperationException("Cannot snapshot on a historical data snapshot provider");
  }
  
  @Override
  public long snapshot(long snapshot) {
    return snapshot;
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    Identifier identifier = requirement.getTargetSpecification().getIdentifier();
    HistoricalTimeSeries hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(IdentifierBundle.of(identifier), _dataSource, _dataProvider, _field);
    if (hts == null) {
      hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(IdentifierBundle.of(identifier), _dataSource, "CMPL", _field);
      if (hts == null) {
        hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(IdentifierBundle.of(identifier), _dataSource, "EXCH_XCME", _field);
      }
    }
    return (hts != null);
  }

}
