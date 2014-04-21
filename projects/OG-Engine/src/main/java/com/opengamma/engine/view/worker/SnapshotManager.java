/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Manages a snapshot for use within the current view cycle.
 */
public class SnapshotManager {

  /**
   * The snapshot being managed.
   */
  private final MarketDataSnapshot _snapshot;

  /**
   * The market data requirements for the current cycle.
   */
  private final Set<ValueSpecification> _cycleMarketDataRequirements = new HashSet<>();

  /**
   * The market data manager which can be used to make subscription requests.
   */
  private final MarketDataManager _marketDataManager;

  /**
   * Create the snapshot manager with an unitialized snapshot.
   *
   * @param snapshot unititialized market data snapshot, not null
   * @param marketDataManager the market data manager, not null
   */
  public SnapshotManager(MarketDataSnapshot snapshot, MarketDataManager marketDataManager) {
    ArgumentChecker.notNull(snapshot, "snapshot");
    ArgumentChecker.notNull(marketDataManager, "marketDataManager");
    _snapshot = snapshot;
    _marketDataManager = marketDataManager;
  }

  public Instant getSnapshotTimeIndication() {
    return _snapshot.getSnapshotTimeIndication();
  }

  /**
   * Request the market data subscriptions required for this snapshot. Note that will be a
   * delay between this method returning and the data actually being available in the snapshot.
   */
  public void requestSubscriptions() {
    _marketDataManager.requestMarketDataSubscriptions(_cycleMarketDataRequirements);
  }

  /**
   * Initialize the snapshot, using whatever market data values are currently available.
   */
  public void initialiseSnapshot() {
    _snapshot.init();
  }

  /**
   * Initialise the snapshot, only returning once it has been initialized with all
   * required market data results. In order to achieve this, all required subscriptions
   * will be requested.
   * 
   * @param timeoutMillis  the timeout in milliseconds
   */
  public void initialiseSnapshotWithSubscriptionResults(long timeoutMillis) {
    requestSubscriptions();
    _snapshot.init(_cycleMarketDataRequirements, timeoutMillis, TimeUnit.MILLISECONDS);
  }

  public Instant getSnapshotTime() {
    return _snapshot.getSnapshotTime();
  }

  /**
   * Get the underlying snapshot.
   *
   * @return the dnapshot, not null
   */
  public MarketDataSnapshot getSnapshot() {
    return _snapshot;
  }

  /**
   * Add the market data requirements for this view cycle.
   *
   * @param marketDataRequirements the market data requirements, not null
   */
  public void addMarketDataRequirements(Set<ValueSpecification> marketDataRequirements) {
    ArgumentChecker.notNull(marketDataRequirements, "marketDataRequirements");
    _cycleMarketDataRequirements.addAll(marketDataRequirements);
  }
}
