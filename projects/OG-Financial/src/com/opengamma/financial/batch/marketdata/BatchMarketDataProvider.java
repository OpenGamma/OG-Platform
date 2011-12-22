/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.marketdata;

import java.util.Set;

import javax.time.Duration;
import javax.time.Instant;

import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.historical.HistoricalMarketDataProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.permission.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.batch.BatchJobRun;
import com.opengamma.financial.batch.BatchRunMaster;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

// REVIEW jonathan 2011-06-30 -- as usual, this has been added simply to make batch work against the new market data
// API. It is not an illustration of the best practice for implementing a custom MarketDataProvider, and has only been
// added in the knowledge that the batch framework is due for a complete rewrite.
//
// Anyone rewriting the batch framework should use a custom subclass of MarketDataSpecification which includes all the
// information needed to locate the market data either from the batch database or time series source. A batch run
// should not need to construct an entire custom engine instance per cycle! The custom MarketDataSpecification type
// should be registered with the demo view processor so that batch runs can be performed against this remote engine,
// and can be performed in their entirety in a single execution sequence.

/**
 * A market data provider for batch runs.
 */
public class BatchMarketDataProvider implements MarketDataProvider, MarketDataAvailabilityProvider {

  /**
   * The run for which snapshots are being provided.
   */
  private final BatchJobRun _run;
  /**
   * The batch master.
   */
  private final BatchRunMaster _batchRunMaster;
  /**
   * The provider of previously-used market data from the batch database.
   */
  private final InMemoryLKVMarketDataProvider _batchDbProvider;
  /**
   * The provider of historical market data.
   */
  private final HistoricalMarketDataProvider _historicalMarketDataProvider;

  /**
   * Creates an instance.
   * 
   * @param run  the run data, not null
   * @param batchRunMaster  the batch master, not null
   * @param batchDbProvider  the provider of previously-used market data from the batch database, not null 
   * @param historicalMarketDataProvider  the provider of historical market data, not null
   */
  public BatchMarketDataProvider(
      BatchJobRun run,
      BatchRunMaster batchRunMaster,
      InMemoryLKVMarketDataProvider batchDbProvider,
      HistoricalMarketDataProvider historicalMarketDataProvider) {
    ArgumentChecker.notNull(run, "run");
    ArgumentChecker.notNull(batchRunMaster, "batchMaster");
    ArgumentChecker.notNull(batchDbProvider, "batchDbMarketDataProvider");
    _run = run;
    _batchRunMaster = batchRunMaster;
    _batchDbProvider = batchDbProvider;
    _historicalMarketDataProvider = historicalMarketDataProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public void addListener(MarketDataListener listener) {
    // Subscriptions irrelevant for batch
  }

  @Override
  public void removeListener(MarketDataListener listener) {
    // Subscriptions irrelevant for batch
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    // Subscriptions irrelevant for batch but unless the execution options are configured not to trigger on live data
    // then the view process will hang waiting for the subscriptions to succeed.
  }

  @Override
  public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // Subscriptions irrelevant for batch but unless the execution options are configured not to trigger on live data
    // then the view process will hang waiting for the subscriptions to succeed.
  }

  @Override
  public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    // Subscriptions irrelevant for batch
  }

  @Override
  public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // Subscriptions irrelevant for batch
  }
  
  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return this;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return new PermissiveMarketDataPermissionProvider();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    // Not going to be executing more than an single cycle at a time so will never be called
    return true;
  }
  
  @Override
  public BatchMarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    InMemoryLKVMarketDataSnapshot batchDbSnapshot = _batchDbProvider.snapshot(marketDataSpec);
    MarketDataSnapshot historicalSnapshot;
    if (_historicalMarketDataProvider != null) {
      HistoricalMarketDataSpecification historicalMarketDataSpec = (HistoricalMarketDataSpecification) marketDataSpec; 
      historicalSnapshot = _historicalMarketDataProvider.snapshot(historicalMarketDataSpec);
    } else {
      historicalSnapshot = null;
    }
    
    return new BatchMarketDataSnapshot(_run.getSnapshotId(), _batchRunMaster, _batchDbProvider, batchDbSnapshot, historicalSnapshot);
  }
  
  @Override
  public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
    return Duration.between(fromInstant, toInstant);
  }

  @Override
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    final MarketDataAvailability batch = _batchDbProvider.getAvailability(requirement);
    if (batch == MarketDataAvailability.AVAILABLE) {
      return batch;
    }
    if (_historicalMarketDataProvider != null) {
      final MarketDataAvailability historical = _historicalMarketDataProvider.getAvailability(requirement);
      if (historical != MarketDataAvailability.NOT_AVAILABLE) {
        return historical;
      }
    }
    return batch;
  }

}
