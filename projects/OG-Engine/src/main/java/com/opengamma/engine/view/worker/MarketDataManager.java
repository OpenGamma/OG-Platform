/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.MarketDataListener;
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
   * How long do wait for a subscription response to come back before giving up on it. Note that
   * retries may well have occurred depnding on the value of {@link #SUBSCRIPTION_RETRY_DURATION}
   */
  public static final Duration SUBSCRIPTION_ABANDONMENT_DURATION = Duration.ofMinutes(15);

  /**
   * How long to wait before for a subscription response to come back before retrying a subscription.
   * Note that the scheduler runs at this period too.
   */
  public static final Duration SUBSCRIPTION_RETRY_DURATION = Duration.ofMinutes(5);

  /**
   * The current set of market data subscriptions.
   */
  private final Set<ValueSpecification> _marketDataSubscriptions = new HashSet<>();

  /**
   * Subscriptions which have been requested but not yet been satisfied by the market data
   * provider. The time indicates when the subscription was first requested.
   */
  private final Map<ValueSpecification, ZonedDateTime> _pendingSubscriptions = new ConcurrentHashMap<>();

  /**
   * Lock for safely adding and removing items from the {@link #_marketDataSubscriptions} and
   * {@link #_pendingSubscriptions} collections.
   */
  private final Lock _subscriptionsLock = new ReentrantLock();

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

  private static final ScheduledExecutorService s_submonitor = Executors.newScheduledThreadPool(1, new NamedThreadPoolFactory("Subscription Monitor"));

  /**
   * Create the manager for the market data.
   *
   * @param listener the listener for market data changes, not null
   * @param marketDataProviderResolver the provider resolver, not null
   */
  public MarketDataManager(MarketDataChangeListener listener,
                           MarketDataProviderResolver marketDataProviderResolver) {

    ArgumentChecker.notNull(listener, "listener");
    ArgumentChecker.notNull(marketDataProviderResolver, "marketDataProviderResolver");
    _marketDataChangeListener = listener;
    _marketDataProviderResolver = marketDataProviderResolver;
    s_submonitor.scheduleAtFixedRate(createSubscriptionMonitorLogging(), 0, 15, TimeUnit.SECONDS);
    s_submonitor.scheduleAtFixedRate(createSubscriptionMonitor(), 0, SUBSCRIPTION_RETRY_DURATION.get(ChronoUnit.SECONDS), TimeUnit.SECONDS);
  }


  private Runnable createSubscriptionMonitorLogging() {
    return new Runnable() {

      @Override
      public void run() {

        if (s_logger.isInfoEnabled()) {
          _subscriptionsLock.lock();

          try {
            s_logger.info("Pending subscriptions for {}: {}", MarketDataManager.this, _pendingSubscriptions.size());

            if (!_pendingSubscriptions.isEmpty()) {

              s_logger.info(_pendingSubscriptions.size() > 20 ? "First 20 pending: " : "All {} pending: ", _pendingSubscriptions.size());
              for (Map.Entry<ValueSpecification, ZonedDateTime> entry : Iterables.limit(_pendingSubscriptions.entrySet(), 20)) {
                s_logger.info(" - {} in cache since: {}", entry.getKey(), entry.getValue());
              }
            }
          } finally {
            _subscriptionsLock.unlock();
          }
        }
      }
    };
  }

  private Runnable createSubscriptionMonitor() {
    return new Runnable() {

      @Override
      public void run() {

        _subscriptionsLock.lock();

        try {

          ZonedDateTime abandonLimit = ZonedDateTime.now().minus(SUBSCRIPTION_ABANDONMENT_DURATION);
          ZonedDateTime retryLimit = ZonedDateTime.now().minus(SUBSCRIPTION_RETRY_DURATION);

          Set<ValueSpecification> toAbandon = new HashSet<>();
          Set<ValueSpecification> toRetry = new HashSet<>();

          for (Map.Entry<ValueSpecification, ZonedDateTime> entry : _pendingSubscriptions.entrySet()) {

            if (entry.getValue().isBefore(abandonLimit)) {
              toAbandon.add(entry.getKey());
            } else if (entry.getValue().isBefore(retryLimit)) {
              toRetry.add(entry.getKey());
            }
            // else it's on the pending list but we're still waiting for it to come back
            // so do nothing
          }

          if (!toAbandon.isEmpty()) {
            s_logger.warn("Giving up waiting on {} subscriptions - maybe they don't exist", toAbandon.size());
            s_logger.info("No longer waiting for subscriptions: {}", toAbandon);
            removePendingSubscriptions(toAbandon);
          }

          if (!toRetry.isEmpty()) {
            s_logger.info("Retrying {} subscriptions as no responses received yet", toRetry.size());
            s_logger.info("Retrying subscriptions: {}", toRetry);
            makeSubscriptionRequest(toRetry);
          }
        } finally {
          _subscriptionsLock.unlock();
        }
      }
    };
  }

  /**
   * Create a snapshot manager for use in the current cycle.
   *
   * @param marketDataUser the market data user, not null
   * @param marketDataSpecifications the market data required for the cycle (and hence
   * to be included in any snapshot created), not null
   * @return new snapshot manager, not null
   */
  public SnapshotManager createSnapshotManagerForCycle(UserPrincipal marketDataUser,
                                                       List<MarketDataSpecification> marketDataSpecifications) {

    ArgumentChecker.notNull(marketDataUser, "marketDataUser");
    ArgumentChecker.notNull(marketDataSpecifications, "marketDataSpecifications");
    
    marketDataUser = ensureUserNameNonEmpty(marketDataUser);
    
    _subscriptionsLock.lock();
    try {
      if (_marketDataProvider == null || !_marketDataProvider.getSpecifications().equals(marketDataSpecifications)) {

        replaceMarketDataProvider(marketDataUser, marketDataSpecifications);
        if (_marketDataProvider == null) {
          throw new OpenGammaRuntimeException("Market data specifications " + marketDataSpecifications + "invalid");
        }
      }
      // Obtain the snapshot in case it is needed, but don't explicitly initialise it until the data is required
      return new SnapshotManager(_marketDataProvider.snapshot(), this);
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  /**
   * Return the current market data provider.
   *
   * @return the current market data provider, may be null
   */
  public SnapshottingViewExecutionDataProvider getMarketDataProvider() {

    _subscriptionsLock.lock();
    try {
      return _marketDataProvider;
    } finally {
      _subscriptionsLock.unlock();
    }
  }

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

    // Lock has been obtained by calling method
    _marketDataSubscriptions.addAll(requiredSubscriptions);
    for (ValueSpecification subscription : requiredSubscriptions) {
      _pendingSubscriptions.put(subscription, ZonedDateTime.now());
    }

    makeSubscriptionRequest(requiredSubscriptions);
    timer.finished();
  }

  private void makeSubscriptionRequest(Set<ValueSpecification> requiredSubscriptions) {

    for (Set<ValueSpecification> batch : partitionSet(requiredSubscriptions, MAX_SUBSCRIPTION_BATCH_SIZE)) {
      _marketDataProvider.subscribe(batch);
    }
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

    _subscriptionsLock.lock();
    try {
      _pendingSubscriptions.remove(specification);
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  private void removePendingSubscriptions(final Collection<ValueSpecification> specifications) {

    // Previously, this method used _pendingSubscriptions.removeAll, but as specifications may
    // be a list and the JDK may invert iteration order, it was observed that we may end up
    // terating over _pendingSubscriptions and calling contains() on specifications, resulting
    // in long wait times for a view to load (PLAT-3508)

    _subscriptionsLock.lock();

    try {
      for (ValueSpecification specification : specifications) {
        _pendingSubscriptions.remove(specification);
      }
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  private void removeMarketDataSubscriptions(final Set<ValueSpecification> unusedSubscriptions) {

    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} market data subscriptions", unusedSubscriptions.size());

    _subscriptionsLock.lock();
    try {
      _marketDataProvider.unsubscribe(unusedSubscriptions);
      _marketDataSubscriptions.removeAll(unusedSubscriptions);
    } finally {
      _subscriptionsLock.unlock();
    }
    timer.finished();
  }

  /**
   * Replace the market data provider if required. It will be replaced if it is not already
   * setup or if the user has changed.
   *
   * @param marketDataUser the market data user, not null
   */
  public void replaceMarketDataProviderIfRequired(UserPrincipal marketDataUser) {

    ArgumentChecker.notNull(marketDataUser, "marketDataUser");
    
    marketDataUser = ensureUserNameNonEmpty(marketDataUser);
    
    _subscriptionsLock.lock();
    try {
      if (_marketDataProvider != null && !_marketDataProvider.getMarketDataUser().equals(marketDataUser)) {
        // [PLAT-3186] Not a huge overhead, but we could check compatability with the new specs and keep the same provider
        replaceMarketDataProvider(marketDataUser, _marketDataProvider.getSpecifications());
      }
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  private void replaceMarketDataProvider(UserPrincipal marketDataUser, List<MarketDataSpecification> specifications) {

    // Lock held by calling methods
    if (_marketDataProvider != null) {
      s_logger.info("Replacing market data provider between cycles");
    }
    removeMarketDataProvider();
    setMarketDataProvider(marketDataUser, specifications);
  }

  /**
   * Removes the current market data provider and all market data subscriptions.
   */
  public void removeMarketDataProvider() {

    _subscriptionsLock.lock();
    try {
      if (_marketDataProvider == null) {
        return;
      }
      removeMarketDataSubscriptions(_marketDataSubscriptions);
      _marketDataProvider.removeListener(this);
      _marketDataProvider = null;
      _marketDataProviderDirty = true;
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  private void setMarketDataProvider(UserPrincipal marketDataUser, final List<MarketDataSpecification> marketDataSpecs) {

    // Lock held by calling methods
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

  /**
   * Request subscriptions for required market data. The request is checked against current
   * subscriptions ensuring subscriptions are only sent for new requests. Any previously
   * requested subscriptions that are no longer required will be removed.
   *
   * @param requiredSubscriptions the required subscriptions, not null
   */
  public void requestMarketDataSubscriptions(final Set<ValueSpecification> requiredSubscriptions) {

    ArgumentChecker.notNull(requiredSubscriptions, "requiredSubscriptions");

    _subscriptionsLock.lock();
    try {
      final Set<ValueSpecification> currentSubscriptions = Sets.difference(_marketDataSubscriptions, _pendingSubscriptions.keySet()).immutableCopy();
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
      _subscriptionsLock.unlock();
    }
  }

  /**
   * Shortcut method to get the availanbility provider from the market data provider.
   *
   * @return the market data availability provider, may be null.
   */
  public MarketDataAvailabilityProvider getAvailabilityProvider() {

    _subscriptionsLock.lock();
    try {
      return _marketDataProvider != null ? _marketDataProvider.getAvailabilityProvider() : null;
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  /**
   * Indicates if the current market data provider is dirty and thus any nodes sourcing market
   * data into the dependency graph may now be invalid.
   *
   * @return true if the market data provider is dirty
   */
  public boolean isMarketDataProviderDirty() {
    _subscriptionsLock.lock();
    try {
      return _marketDataProviderDirty;
    } finally {
      _subscriptionsLock.unlock();
    }
  }

  /**
   * Marks the current market data provider as clean.
   */
  public void markMarketDataProviderClean() {
    _subscriptionsLock.lock();
    try {
      _marketDataProviderDirty = false;
    } finally {
      _subscriptionsLock.unlock();
    }
  }
  
  /**
   * Switches {@link UserPrincipal#getTestUser()}  for the given userPrincipal if
   * {@link UserPrincipal#getUserName()} is null or empty.
   * @param userPrincipal the object to check
   * @return the resolved object
   */
  private UserPrincipal ensureUserNameNonEmpty(UserPrincipal userPrincipal) {
    String userName = userPrincipal.getUserName();
    if (userName == null || "".equals(userName)) {
      UserPrincipal testUser = UserPrincipal.getTestUser();
      s_logger.info("UserName undefined for {}. Will use test user {} instead.", userPrincipal, UserPrincipal.getTestUser());
      return testUser;
    } else {
      return userPrincipal;
    }
    
  }
}
