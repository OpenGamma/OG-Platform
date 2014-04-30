/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.metric.MetricProducer;

/**
 * A base class that handles all the in-memory requirements for a {@link LiveDataClient} implementation.
 */
@PublicAPI
public abstract class AbstractLiveDataClient implements LiveDataClient, MetricProducer {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractLiveDataClient.class);
  // Injected Inputs:
  private long _heartbeatPeriod = Heartbeater.DEFAULT_PERIOD;
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  // Running State:
  private final ValueDistributor _valueDistributor = new ValueDistributor();
  private final Timer _timer = new Timer("LiveDataClient Timer");
  private Heartbeater _heartbeater;
  private final Lock _subscriptionLock = new ReentrantLock();

  private final ReentrantReadWriteLock _pendingSubscriptionLock = new ReentrantReadWriteLock();
  private final ReadLock _pendingSubscriptionReadLock = _pendingSubscriptionLock.readLock();
  private final WriteLock _pendingSubscriptionWriteLock = _pendingSubscriptionLock.writeLock();
  private final Multimap<LiveDataSpecification, SubscriptionHandle> _fullyQualifiedSpec2PendingSubscriptions = HashMultimap.create();

  /**
   * This is the reverse of _fullyQualifiedSpec2PendingSubscriptions
   */
  // REVIEW simon 2012-02-20 -- I suspect that these could just be a BiMap, but it's not obvious from the current implementation
  private final Multimap<SubscriptionHandle, LiveDataSpecification> _specsByHandle = HashMultimap.create();

  private Meter _inboundTickMeter;

  @Override
  public synchronized void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    _inboundTickMeter = summaryRegistry.meter(namePrefix + ".ticks.count");
    // REVIEW kirk 2013-04-22 -- This might be better as a Counter.
    summaryRegistry.register(namePrefix + ".subscriptions.count", new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return getValueDistributor().getActiveSpecificationCount();
      }
    });
  }

  public void setHeartbeatMessageSender(ByteArrayMessageSender messageSender) {
    ArgumentChecker.notNull(messageSender, "Message Sender");
    _heartbeater = new Heartbeater(_valueDistributor, new HeartbeatSender(messageSender, getFudgeContext()), getTimer(), getHeartbeatPeriod());
  }

  @Override
  public void close() {
    _timer.cancel();
  }

  /**
   * @return the heartbeater
   */
  public Heartbeater getHeartbeater() {
    return _heartbeater;
  }

  /**
   * @return the timer
   */
  public Timer getTimer() {
    return _timer;
  }

  /**
   * @return the heartbeatPeriod
   */
  public long getHeartbeatPeriod() {
    return _heartbeatPeriod;
  }

  /**
   * @param heartbeatPeriod the heartbeatPeriod to set
   */
  public void setHeartbeatPeriod(long heartbeatPeriod) {
    _heartbeatPeriod = heartbeatPeriod;
  }

  /**
   * @return the valueDistributor
   */
  public ValueDistributor getValueDistributor() {
    return _valueDistributor;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * @param fudgeContext the fudgeContext to set
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  /**
   * Obtain <em>a copy of</em> the active subscription specifications. For concurrency reason this will return a new copy on each call.
   * 
   * @return a copy of the Active Fully-Qualified Subscription Specifications
   */
  public Set<LiveDataSpecification> getActiveSubscriptionSpecifications() {
    return getValueDistributor().getActiveSpecifications();
  }

  @Override
  public void subscribe(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, LiveDataListener listener) {

    ArrayList<SubscriptionHandle> subscriptionHandles = new ArrayList<SubscriptionHandle>();
    for (LiveDataSpecification requestedSpecification : requestedSpecifications) {
      SubscriptionHandle subHandle = new SubscriptionHandle(user, SubscriptionType.NON_PERSISTENT, requestedSpecification, listener);
      subscriptionHandles.add(subHandle);
    }

    if (!subscriptionHandles.isEmpty()) {
      handleSubscriptionRequest(subscriptionHandles);
    }
  }

  @Override
  public void subscribe(UserPrincipal user, LiveDataSpecification requestedSpecification, LiveDataListener listener) {
    subscribe(user, Collections.singleton(requestedSpecification), listener);
  }

  private abstract class SnapshotListener implements LiveDataListener {

    private final Collection<LiveDataSubscriptionResponse> _responses = new ArrayList<LiveDataSubscriptionResponse>();
    private final AtomicInteger _responsesOutstanding;

    public SnapshotListener(int expectedNumberOfResponses) {
      _responsesOutstanding = new AtomicInteger(expectedNumberOfResponses);
    }

    @Override
    public void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult) {
      _responses.add(subscriptionResult);
      if (_responsesOutstanding.decrementAndGet() <= 0) {
        notifyResponses();
      }
    }

    @Override
    public void subscriptionResultsReceived(Collection<LiveDataSubscriptionResponse> subscriptionResults) {
      _responses.addAll(subscriptionResults);
      if (_responsesOutstanding.addAndGet(-subscriptionResults.size()) <= 0) {
        notifyResponses();
      }
    }

    @Override
    public void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification) {
      // should never go here      
      throw new UnsupportedOperationException();
    }

    @Override
    public void valueUpdate(LiveDataValueUpdate valueUpdate) {
      // should never go here
      throw new UnsupportedOperationException();
    }

    protected boolean responsesOutstanding() {
      return _responsesOutstanding.get() > 0;
    }

    protected abstract void notifyResponses();

    public Collection<LiveDataSubscriptionResponse> getResponses() {
      return _responses;
    }

  }

  private final class SynchronousSnapshotListener extends SnapshotListener {

    public SynchronousSnapshotListener(final int expectedNumberOfResponses) {
      super(expectedNumberOfResponses);
    }

    @Override
    protected synchronized void notifyResponses() {
      notifyAll();
    }

    public synchronized boolean waitForResponses(final long timeout) throws InterruptedException {
      final long delayUntil = System.nanoTime() + timeout * 1_000_000L;
      while (responsesOutstanding()) {
        final long delay = delayUntil - System.nanoTime();
        if (delay < 1_000_000L) {
          return !responsesOutstanding();
        }
        wait(delay / 1_000_000L);
      }
      return true;
    }

  }

  private final class AsynchronousSnapshotListener extends SnapshotListener {

    private PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>> _callback;

    public AsynchronousSnapshotListener(final int expectedNumberOfResponses, final PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>> callback) {
      super(expectedNumberOfResponses);
      _callback = callback;
    }

    @Override
    protected synchronized void notifyResponses() {
      final PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>> callback;
      synchronized (this) {
        callback = _callback;
        _callback = null;
      }
      if (callback != null) {
        callback.success(getResponses());
      }
    }

    public void waitForResponses(final long timeout) {
      _timer.schedule(new TimerTask() {
        @Override
        public void run() {
          final PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>> callback;
          synchronized (this) {
            callback = _callback;
            _callback = null;
          }
          if (callback != null) {
            callback.success(null);
          }
        }
      }, timeout);
    }

  }

  @Override
  public Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, long timeout) {
    ArgumentChecker.notNull(user, "User");
    ArgumentChecker.notNull(requestedSpecifications, "Live Data specifications");
    if (requestedSpecifications.isEmpty()) {
      return Collections.emptySet();
    }
    final SynchronousSnapshotListener listener = new SynchronousSnapshotListener(requestedSpecifications.size());
    ArrayList<SubscriptionHandle> subscriptionHandles = new ArrayList<SubscriptionHandle>();
    for (LiveDataSpecification requestedSpecification : requestedSpecifications) {
      SubscriptionHandle subHandle = new SubscriptionHandle(user, SubscriptionType.SNAPSHOT, requestedSpecification, listener);
      subscriptionHandles.add(subHandle);
    }
    handleSubscriptionRequest(subscriptionHandles);
    boolean success;
    try {
      success = listener.waitForResponses(timeout);
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Thread interrupted when obtaining snapshot");
    }
    if (success) {
      return listener.getResponses();
    } else {
      throw new OpenGammaRuntimeException("Timeout " + timeout + "ms reached when obtaining snapshot of " + requestedSpecifications.size() + " handles");
    }
  }

  /**
   * Asynchronous form of {@link #snapshot(UserPrincipal, Collection<LiveDataSpecification>, long)}.
   * 
   * @param user see {@link #snapshot(UserPrincipal, Collection<LiveDataSpecification>, long)}
   * @param requestedSpecifications see {@link #snapshot(UserPrincipal, Collection<LiveDataSpecification>, long)}
   * @param timeout see {@link #snapshot(UserPrincipal, Collection<LiveDataSpecification>, long)}
   * @param callback receives the result of execution
   */
  protected void snapshot(final UserPrincipal user, final Collection<LiveDataSpecification> requestedSpecifications, final long timeout,
      final PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>> callback) {
    ArgumentChecker.notNull(user, "User");
    ArgumentChecker.notNull(requestedSpecifications, "Live Data specifications");
    if (requestedSpecifications.isEmpty()) {
      callback.success(Collections.<LiveDataSubscriptionResponse>emptySet());
      return;
    }
    final AsynchronousSnapshotListener listener = new AsynchronousSnapshotListener(requestedSpecifications.size(),
        new PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>>() {

          @Override
          public void success(final Collection<LiveDataSubscriptionResponse> result) {
            if (result != null) {
              callback.success(result);
            } else {
              callback.failure(new OpenGammaRuntimeException("Timeout " + timeout + "ms reached when obtaining snapshot of " + requestedSpecifications.size() + " handles"));
            }
          }

          @Override
          public void failure(final Throwable error) {
            callback.failure(error);
          }

        });
    ArrayList<SubscriptionHandle> subscriptionHandles = new ArrayList<SubscriptionHandle>();
    for (LiveDataSpecification requestedSpecification : requestedSpecifications) {
      SubscriptionHandle subHandle = new SubscriptionHandle(user, SubscriptionType.SNAPSHOT, requestedSpecification, listener);
      subscriptionHandles.add(subHandle);
    }
    handleSubscriptionRequest(subscriptionHandles);
    listener.waitForResponses(timeout);
  }

  @Override
  public LiveDataSubscriptionResponse snapshot(UserPrincipal user, LiveDataSpecification requestedSpecification, long timeout) {

    Collection<LiveDataSubscriptionResponse> snapshots = snapshot(user, Collections.singleton(requestedSpecification), timeout);

    if (snapshots.size() != 1) {
      throw new OpenGammaRuntimeException("One snapshot request should return 1 snapshot, was " + snapshots.size());
    }

    return snapshots.iterator().next();
  }

  /**
   * @param subHandle Not null, not empty
   */
  protected abstract void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandle);

  protected void subscriptionStartingToReceiveTicks(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    _pendingSubscriptionWriteLock.lock();
    try {
      _fullyQualifiedSpec2PendingSubscriptions.put(response.getFullyQualifiedSpecification(), subHandle);
      _specsByHandle.put(subHandle, response.getFullyQualifiedSpecification());
    } finally {
      _pendingSubscriptionWriteLock.unlock();
    }
  }

  protected void subscriptionRequestSatisfied(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    _pendingSubscriptionWriteLock.lock();
    try {
      // Atomically (to valueUpdate callers) turn the pending subscription into a full subscription.
      // REVIEW jonathan 2010-12-01 -- rearranged this so that the internal _subscriptionLock is not being held while
      // releasing ticks to listeners, which is a recipe for deadlock.
      removePendingSubscription(subHandle);
      subHandle.releaseTicksOnHold();
      _subscriptionLock.lock();
      try {
        getValueDistributor().addListener(response.getFullyQualifiedSpecification(), subHandle.getListener());
      } finally {
        _subscriptionLock.unlock();
      }
    } finally {
      _pendingSubscriptionWriteLock.unlock();
    }
  }

  protected void subscriptionRequestFailed(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    removePendingSubscription(subHandle);
  }

  protected void removePendingSubscription(SubscriptionHandle subHandle) {
    _pendingSubscriptionWriteLock.lock();
    try {
      Collection<LiveDataSpecification> specs = _specsByHandle.removeAll(subHandle);
      for (LiveDataSpecification liveDataSpecification : specs) {
        _fullyQualifiedSpec2PendingSubscriptions.remove(liveDataSpecification, subHandle);
      }
    } finally {
      _pendingSubscriptionWriteLock.unlock();
    }
  }

  @Override
  public void unsubscribe(UserPrincipal user, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener) {
    for (LiveDataSpecification fullyQualifiedSpecification : fullyQualifiedSpecifications) {
      s_logger.info("Unsubscribing by {} to {} delivered to {}", new Object[] {user, fullyQualifiedSpecification, listener });
      boolean unsubscribeToSpec = false;
      _subscriptionLock.lock();
      try {
        boolean stillActiveSubs = getValueDistributor().removeListener(fullyQualifiedSpecification, listener);
        if (!stillActiveSubs) {
          unsubscribeToSpec = true;
        }
      } finally {
        _subscriptionLock.unlock();
      }

      // REVIEW kirk 2009-09-29 -- Potential race condition with multiple
      // subscribers and unsubscribers here.... do something about it?
      if (unsubscribeToSpec) {
        cancelPublication(fullyQualifiedSpecification);
      }
      listener.subscriptionStopped(fullyQualifiedSpecification);
    }
  }

  @Override
  public void unsubscribe(UserPrincipal user, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener) {
    unsubscribe(user, Collections.singleton(fullyQualifiedSpecification), listener);
  }

  protected abstract void cancelPublication(LiveDataSpecification fullyQualifiedSpecification);

  @Override
  public String getDefaultNormalizationRuleSetId() {
    return StandardRules.getOpenGammaRuleSetId();
  }

  protected void valueUpdate(LiveDataValueUpdateBean update) {

    if (_inboundTickMeter != null) {
      _inboundTickMeter.mark();
    }
    s_logger.debug("{}", update);

    _pendingSubscriptionReadLock.lock();
    try {
      Collection<SubscriptionHandle> pendingSubscriptions = _fullyQualifiedSpec2PendingSubscriptions.get(update.getSpecification());
      for (SubscriptionHandle pendingSubscription : pendingSubscriptions) {
        pendingSubscription.addTickOnHold(update);
      }
    } finally {
      _pendingSubscriptionReadLock.unlock();
    }
    getValueDistributor().notifyListeners(update);
  }

}
