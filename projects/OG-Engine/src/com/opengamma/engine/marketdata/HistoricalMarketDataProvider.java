/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.permission.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Snapshot provider will always return the historical data on the date provided from the provided historical data source.
 */
public class HistoricalMarketDataProvider extends AbstractMarketDataProvider implements MarketDataAvailabilityProvider {
  
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalMarketDataProvider.class);
  
  private final HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _dataField;
  private final MarketDataPermissionProvider _permissionProvider;
  
  public HistoricalMarketDataProvider(HistoricalTimeSeriesSource historicalTimeSeriesSource, String dataSource, String dataProvider, String field) {
    ArgumentChecker.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(field, "field");
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = field;
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    subscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // Nothing to do, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }
  
  @Override
  public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    unsubscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // Nothing to do
    s_logger.debug("Removed subscriptions from {}", valueRequirements);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return this;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof HistoricalMarketDataSpecification)) {
      return false;
    }
    HistoricalMarketDataSpecification historicalSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return getDataProvider().equals(historicalSpec.getDataProvider())
        && getDataSource().equals(historicalSpec.getDataSource())
        && getDataField().equals(historicalSpec.getDataField());
  }
  
  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    HistoricalMarketDataSpecification historicalSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return new HistoricalMarketDataSnapshot(historicalSpec, getTimeSeriesSource());
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    Identifier identifier = requirement.getTargetSpecification().getIdentifier();
    HistoricalTimeSeries hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(IdentifierBundle.of(identifier), getDataSource(), getDataProvider(), getDataField());
    if (hts == null) {
      hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(IdentifierBundle.of(identifier), getDataSource(), "CMPL", getDataField());
      if (hts == null) {
        hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(IdentifierBundle.of(identifier), getDataSource(), "EXCH_XCME", getDataField());
      }
    }
    return (hts != null);
  }
  
  //-------------------------------------------------------------------------
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }
  
  public String getDataSource() {
    return _dataSource;
  }
  
  public String getDataProvider() {
    return _dataProvider;
  }
  
  public String getDataField() {
    return _dataField;
  }

}
