/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Manages market data for a view process, taking care of subscriptions and producing snapshots.
 */
public class MarketDataManager implements MarketDataListener {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataManager.class);


  /**
   * Maximum number of subscriptions to make in a single request.
   */
  private static final int MAX_SUBSCRIPTION_BATCH_SIZE = 10000;

  /**
   * The current set of market data subscriptions.
   */
  private final Set<ValueSpecification> _marketDataSubscriptions = new HashSet<>();

  /**
   * Subscriptions which have been requested but not yet been satisfied by the market data provider.
   */
  private final Set<ValueSpecification> _pendingSubscriptions =
      Collections.newSetFromMap(new ConcurrentHashMap<ValueSpecification, Boolean>());

  /**
   * The listener to notify of market data changes.
   */
  private final MarketDataChangeListener _marketDataChangeListener;

  /**
   * The resolver to be used for market data providers.
   */
  private final MarketDataProviderResolver _marketDataProviderResolver;

  /**
   * The market data provider(s) for the current cycles.
   */
  private SnapshottingViewExecutionDataProvider _marketDataProvider;

  /**
   * Flag indicating the market data provider has changed and any nodes sourcing market
   * data into the dependency graph may now be invalid.
   */
  private boolean _marketDataProviderDirty;

  private final Lock _pendingSubscriptionsLock = new ReentrantLock();

  private static final ScheduledExecutorService s_submonitor = Executors.newScheduledThreadPool(1,
                                                                                                new NamedThreadPoolFactory(
                                                                                                    "Subscription Monitor"));

  {
    if (s_logger.isInfoEnabled()) {
      s_submonitor.execute(new Runnable() {

        @Override
        public void run() {
          s_logger.info("Pending subscriptions for '" + MarketDataManager.this + "': " + _pendingSubscriptions.size());
          if (!_pendingSubscriptions.isEmpty()) {
            logSpecs(_pendingSubscriptions);
          }
          s_submonitor.schedule(this, 15, TimeUnit.SECONDS);
        }

        private void logSpecs(Set<ValueSpecification> specs) {
          s_logger.info(specs.size() > 20 ? "First 20 pending: " : "All " + specs.size() + " pending: ");
          Iterator<ValueSpecification> iterator = specs.iterator();
          for (int i = 0; i < Ints.min(specs.size(), 20); i++) {
            s_logger.info(" - " + iterator.next());
          }
        }
      });
    }
  }


  public MarketDataManager(MarketDataChangeListener listener,
                           MarketDataProviderResolver marketDataProviderResolver) {
    _marketDataChangeListener = listener;
    _marketDataProviderResolver = marketDataProviderResolver;
  }

  public SnapshotManager createSnapshotManagerForCycle(UserPrincipal marketDataUser,
                                                       List<MarketDataSpecification> marketDataSpecifications) {

    if (_marketDataProvider == null || !_marketDataProvider.getSpecifications().equals(marketDataSpecifications)) {

      replaceMarketDataProvider(marketDataUser, marketDataSpecifications);
      if (_marketDataProvider == null) {
        throw new OpenGammaRuntimeException("Market data specifications " + marketDataSpecifications + "invalid");
      }
    }
    // Obtain the snapshot in case it is needed, but don't explicitly initialise it until the data is required
    return new SnapshotManager(_marketDataProvider.snapshot());
  }

  public SnapshottingViewExecutionDataProvider getMarketDataProvider() {
    return _marketDataProvider;
  }

  // MarketDataListener

  @Override
  public void subscriptionsSucceeded(final Collection<ValueSpecification> valueSpecifications) {
    removePendingSubscriptions(valueSpecifications);
    s_logger.info("{} subscription succeeded - {} pending subscriptions remaining", valueSpecifications.size(), _pendingSubscriptions.size());
  }

  @Override
  public void subscriptionFailed(final ValueSpecification valueSpecification, final String msg) {
    removePendingSubscription(valueSpecification);
    s_logger.info("Market data subscription to {} failed. This market data may be missing from computation cycles.",
                   valueSpecification);
    s_logger.info("{} pending subscriptions remaining", _pendingSubscriptions.size());
  }

  @Override
  public void subscriptionStopped(final ValueSpecification valueSpecification) {
  }

  @Override
  public void valuesChanged(Collection<ValueSpecification> specifications) {
    s_logger.info("Received change notification for {} specifications", specifications.size());
    _marketDataChangeListener.onMarketDataValuesChanged(specifications);
  }

  private void addMarketDataSubscriptions(final Set<ValueSpecification> requiredSubscriptions) {

    final OperationTimer timer = new OperationTimer(s_logger, "Adding {} market data subscriptions", requiredSubscriptions.size());

    _marketDataSubscriptions.addAll(requiredSubscriptions);
    _pendingSubscriptionsLock.lock();


    try {
      _pendingSubscriptions.addAll(requiredSubscriptions);

      Set<Set<ValueSpecification>> batches = partitionSet(requiredSubscriptions, MAX_SUBSCRIPTION_BATCH_SIZE);

      for (Set<ValueSpecification> batch : batches) {
        _marketDataProvider.subscribe(batch);
      }


      //try {
      //  synchronized (_pendingSubscriptions) {
      //    if (!_pendingSubscriptions.isEmpty()) {
      //      _pendingSubscriptions.wait();
      //    }
      //  }
      //} catch (final InterruptedException ex) {
      //  s_logger.info("Interrupted while waiting for subscription results.");
      //} finally {
      //  _pendingSubscriptions.clear();
      //}
    } finally {
      _pendingSubscriptionsLock.unlock();
    }

    timer.finished();
  }

  private <T> Set<Set<T>> partitionSet(Set<T> originalSet, int maxBatchSize) {

    ArgumentChecker.notNegativeOrZero(maxBatchSize, "maxBatchSize");
    int expectedNumberOfBatches = (originalSet.size() / maxBatchSize) + 1;
    Set<Set<T>> result = new HashSet<>(expectedNumberOfBatches);
    Set<T> batch = null;
    for (T item : originalSet) {
      if (batch == null) {
        batch = new HashSet<>(maxBatchSize);
        result.add(batch);
      }
      batch.add(item);
      if (batch.size() == maxBatchSize) {
        batch = null;
      }
    }

    s_logger.info("Partitioned original set of {} items into {} batches of max size {}", originalSet.size(), result.size(), maxBatchSize);

    return result;
  }

  private void removePendingSubscription(final ValueSpecification specification) {

    _pendingSubscriptionsLock.lock();
    try {
      if (_pendingSubscriptions.remove(specification)) {
        //notifyIfPendingSubscriptionsDone();
      }
    } finally {
      _pendingSubscriptionsLock.unlock();
    }
  }

  private void removePendingSubscriptions(final Collection<ValueSpecification> specifications) {

    _pendingSubscriptionsLock.lock();
    boolean removalPerformed;

    try {
      // Previously, this used removeAll, but as specifications may be a list, it was observed
      // that we may end up iterating over _pendingSubscriptions and calling contains() on
      // specifications, resulting in long wait times for a view to load (PLAT-3508)
      removalPerformed = false;
      for (ValueSpecification specification : specifications) {
        removalPerformed = _pendingSubscriptions.remove(specification) || removalPerformed;
      }
    } finally {
      _pendingSubscriptionsLock.unlock();
    }

    if (removalPerformed) {
      //notifyIfPendingSubscriptionsDone();
    }
  }

  private void notifyIfPendingSubscriptionsDone() {
    if (_pendingSubscriptions.isEmpty()) {
      synchronized (_pendingSubscriptions) {
        if (_pendingSubscriptions.isEmpty()) {
          _pendingSubscriptions.notifyAll();
        }
      }
    }
  }

  private void removeMarketDataSubscriptions() {
    removeMarketDataSubscriptions(_marketDataSubscriptions);
  }

  private void removeMarketDataSubscriptions(final Set<ValueSpecification> unusedSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} market data subscriptions", unusedSubscriptions.size());
    _marketDataProvider.unsubscribe(unusedSubscriptions);
    _marketDataSubscriptions.removeAll(unusedSubscriptions);
    timer.finished();
  }

  public void replaceMarketDataProviderIfRequired(UserPrincipal marketDataUser) {

    if (_marketDataProvider != null && !_marketDataProvider.getMarketDataUser().equals(marketDataUser)) {
      // [PLAT-3186] Not a huge overhead, but we could check compatability with the new specs and keep the same provider
      replaceMarketDataProvider(marketDataUser, _marketDataProvider.getSpecifications());
    }
  }

  private void replaceMarketDataProvider(UserPrincipal marketDataUser, List<MarketDataSpecification> specifications) {

    if (_marketDataProvider != null) {
      s_logger.info("Replacing market data provider between cycles");
    }
    removeMarketDataProvider();
    setMarketDataProvider(marketDataUser, specifications);
  }

  public void removeMarketDataProvider() {
    if (_marketDataProvider == null) {
      return;
    }
    removeMarketDataSubscriptions();
    _marketDataProvider.removeListener(this);
    _marketDataProvider = null;
    _marketDataProviderDirty = true;
  }

  private void setMarketDataProvider(UserPrincipal marketDataUser, final List<MarketDataSpecification> marketDataSpecs) {
    try {
      _marketDataProvider = new SnapshottingViewExecutionDataProvider(marketDataUser,
                                                                      marketDataSpecs, _marketDataProviderResolver);
    } catch (final Exception e) {
      s_logger.error("Failed to create data provider", e);
      _marketDataProvider = null;
    }
    if (_marketDataProvider != null) {
      _marketDataProvider.addListener(this);
    }
    _marketDataProviderDirty = true;
  }

  private void setMarketDataSubscriptions(final Set<ValueSpecification> requiredSubscriptions) {

    _pendingSubscriptionsLock.lock();
    try {
      final Set<ValueSpecification> currentSubscriptions = Sets.difference(_marketDataSubscriptions, _pendingSubscriptions).immutableCopy();
      final Set<ValueSpecification> unusedMarketData = Sets.difference(currentSubscriptions, requiredSubscriptions).immutableCopy();
      if (!unusedMarketData.isEmpty()) {
        s_logger.info("{} unused market data subscriptions", unusedMarketData.size());
        removeMarketDataSubscriptions(unusedMarketData);
      }
      final Set<ValueSpecification> newMarketData = Sets.difference(requiredSubscriptions, currentSubscriptions).immutableCopy();
      if (!newMarketData.isEmpty()) {
        s_logger.info("{} new market data requirements", newMarketData.size());
        addMarketDataSubscriptions(newMarketData);
      }
    } finally {
      _pendingSubscriptionsLock.unlock();
    }
  }

  /**
   * Shortcut method to get the availanbility provider from the market data provider.
   *
   * @return the market data availability provider, may be null.
   */
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _marketDataProvider != null ? _marketDataProvider.getAvailabilityProvider() : null;
  }

  public boolean isMarketDataProviderDirty() {
    return _marketDataProviderDirty;
  }

  public void markMarketDataProviderClean() {
    _marketDataProviderDirty = false;
  }


  public class SnapshotManager {

    private final MarketDataSnapshot _snapshot;
    private final Set<ValueSpecification> _cycleMarketDataRequirements = new HashSet<>();

    /**
     *
     * @param snapshot unititialized market data snapshot
     */
    public SnapshotManager(MarketDataSnapshot snapshot) {
      //
      _snapshot = snapshot;
    }

    public Instant getSnapshotTimeIndication() {
      return _snapshot.getSnapshotTimeIndication();
    }

    public void requestSubscriptions() {
      setMarketDataSubscriptions(_cycleMarketDataRequirements);
    }

    public void initialiseSnapshot() {
      _snapshot.init();
    }

    public void initialiseSnapshotWithSubscriptionResults() {
      requestSubscriptions();
      // REVIEW jonathan/andrew -- 2013-03-28 -- if the user wants to wait for market data, then assume they mean
      // it and wait as long as it takes. There are mechanisms for cancelling the job.
      _snapshot.init(_cycleMarketDataRequirements, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public Instant getSnapshotTime() {

      if (_snapshot == null) {
        throw new IllegalStateException("Snapshot has not yet been initialised");
      }
      return _snapshot.getSnapshotTime();
    }

    public MarketDataSnapshot getSnapshot() {
      return _snapshot;
    }

    public void addMarketDataRequirements(Set<ValueSpecification> marketDataRequirements) {

      _cycleMarketDataRequirements.addAll(marketDataRequirements);
    }
  }

  public interface MarketDataChangeListener {
    void onMarketDataValuesChanged(Collection<ValueSpecification> valueSpecifications);
  }
}
