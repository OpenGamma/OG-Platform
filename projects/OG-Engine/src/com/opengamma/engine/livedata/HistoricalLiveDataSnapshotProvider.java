/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Snapshot provider will always return the historical data on the date provided from the provided historical data source.
 */
public class HistoricalLiveDataSnapshotProvider extends AbstractLiveDataSnapshotProvider implements LiveDataAvailabilityProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalLiveDataSnapshotProvider.class);
  private static final long MILLIS_PER_DAY = 1000 * 3600 * 24;
  private HistoricalDataSource _historicalDataSource;
  private String _dataSource;
  private String _dataProvider;
  private String _field;
  
  public HistoricalLiveDataSnapshotProvider(HistoricalDataSource historicalDataSource, String dataSource, String dataProvider, String field) {
    _historicalDataSource = historicalDataSource;
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
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> historicalData = _historicalDataSource.getHistoricalData(
        IdentifierBundle.of(identifier), 
        _dataSource, 
        _dataProvider, 
        _field, 
        date, 
        true, 
        date, 
        false);
    if ((historicalData == null) || (historicalData.getValue().isEmpty())) {
      return null;
    }
    return historicalData.getValue().getValue(date);
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
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> historicalData = _historicalDataSource.getHistoricalData(IdentifierBundle.of(identifier), _dataSource, _dataProvider, _field);
    if (historicalData == null || historicalData.getKey() == null) {
      historicalData = _historicalDataSource.getHistoricalData(IdentifierBundle.of(identifier), _dataSource, "CMPL", _field);
    }
    if (historicalData == null || historicalData.getKey() == null) {
      historicalData = _historicalDataSource.getHistoricalData(IdentifierBundle.of(identifier), _dataSource, "EXCH_XCME", _field);
    }
    if (historicalData != null) {
      //System.err.println("isAvailable(" + identifier + ", " + _dataSource + ", " + _dataProvider + ", " + _field + ") = " + (historicalData.getKey() != null));
      return historicalData.getKey() != null;
    } else {
      //System.err.println("isAvailable(" + identifier + ", " + _dataSource + ", " + _dataProvider + ", " + _field + ") = false (no data at all)");
      return false;
    }
    
  }

}
