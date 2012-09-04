/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.firehose.FireHoseLiveData.DataStateListener;
import com.opengamma.livedata.firehose.FireHoseLiveData.ValueUpdateListener;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.livedata.server.distribution.EmptyMarketDataSenderFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * OpenGamma Live Data Server implementation built on top of a {@link FireHoseLiveData} implementation.
 */
public class FireHoseLiveDataServer extends AbstractLiveDataServer {

  private static final Logger s_logger = LoggerFactory.getLogger(FireHoseLiveDataServer.class);
  private static final ExecutorService s_executorService = Executors.newCachedThreadPool(new NamedThreadPoolFactory("FireHoseLiveDataServer"));

  private final ExternalScheme _uniqueIdDomain;
  private final FireHoseLiveData _fireHose;
  private final Set<String> _waitingFor = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  private final BlockingQueue<Subscription> _received = new LinkedBlockingQueue<Subscription>();
  private TerminatableJob _dispatcher;
  private long _marketDataTimeout = 30000000000L;

  public FireHoseLiveDataServer(final ExternalScheme uniqueIdDomain, final FireHoseLiveData fireHose) {
    super(EHCacheUtils.createCacheManager());
    ArgumentChecker.notNull(uniqueIdDomain, "uniqueIdDomain");
    ArgumentChecker.notNull(fireHose, "fireHose");
    _uniqueIdDomain = uniqueIdDomain;
    _fireHose = fireHose;
    _fireHose.setValueUpdateListener(new ValueUpdateListener() {
      @Override
      public void updatedValue(final String uniqueId, final FudgeMsg msg) {
        if (_waitingFor.contains(uniqueId)) {
          synchronized (FireHoseLiveDataServer.this) {
            FireHoseLiveDataServer.this.notifyAll();
          }
        }
        final Subscription subscription = getSubscription(uniqueId);
        if (subscription != null) {
          @SuppressWarnings("unchecked")
          final AtomicReference<FudgeMsg> pointer = (AtomicReference<FudgeMsg>) subscription.getHandle();
          if (pointer != null) {
            final FudgeMsg previous = pointer.getAndSet(msg);
            if (previous == null) {
              _received.add(subscription);
            } else {
              s_logger.debug("Dropped previous message for {}", uniqueId);
            }
          } else {
            s_logger.info("Subscription {} created but no handle available for live data dispatch", subscription);
          }
        }
      }
    });
    _fireHose.setDataStateListener(new DataStateListener() {
      @Override
      public void valuesRefreshed() {
        synchronized (FireHoseLiveDataServer.this) {
          FireHoseLiveDataServer.this.notifyAll();
        }
      }
    });
  }

  protected FireHoseLiveData getFireHose() {
    return _fireHose;
  }

  protected static ExecutorService getExecutorService() {
    return s_executorService;
  }

  protected void setMarketDataTimeout(final long timeout, final TimeUnit unit) {
    _marketDataTimeout = unit.toNanos(timeout);
  }

  protected long getMarketDataTimeout() {
    return _marketDataTimeout;
  }

  protected synchronized void waitForMarketData(final Collection<String> identifiers) {
    s_logger.debug("Waiting for market data on {}", identifiers);
    _waitingFor.addAll(identifiers);
    try {
      final long completionTime = System.nanoTime() + getMarketDataTimeout();
      while (!getFireHose().isMarketDataComplete()) {
        boolean pending = false;
        for (String identifier : identifiers) {
          if (getFireHose().getLatestValue(identifier) == null) {
            s_logger.debug("Identifier {} pending", identifier);
            pending = true;
            break;
          }
        }
        if (!pending) {
          s_logger.info("Requested market data available");
          break;
        }
        final long timeout = completionTime - System.nanoTime();
        if (timeout < 1000000L) {
          s_logger.info("Timeout exceeded waiting for market data");
          break;
        }
        try {
          wait(timeout / 1000000L);
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("Interrupted", e);
        }
      }
    } finally {
      _waitingFor.removeAll(identifiers);
    }
  }

  @Override
  protected Map<String, Object> doSubscribe(final Collection<String> uniqueIds) {
    s_logger.debug("Subscribing to {}", uniqueIds);
    final Map<String, Object> result = Maps.newHashMapWithExpectedSize(uniqueIds.size());
    for (String identifier : uniqueIds) {
      result.put(identifier, new AtomicReference<FudgeMsg>());
    }
    return result;
  }
  
  @Override
  protected void subscriptionDone(Set<String> uniqueIds) {
    for (String identifier : uniqueIds) {
      final FudgeMsg msg = getFireHose().getLatestValue(identifier);
      if (msg != null) {
        liveDataReceived(identifier, msg);
      }
    }
  }

  @Override
  protected void doUnsubscribe(final Collection<Object> subscriptionHandles) {
    // No-op; don't maintain or forward any subscription state
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(final Collection<String> uniqueIds) {
    final Map<String, FudgeMsg> result = Maps.newHashMapWithExpectedSize(uniqueIds.size());
    Collection<String> failures = null;
    for (String identifier : uniqueIds) {
      final FudgeMsg msg = getFireHose().getLatestValue(identifier);
      if (msg == null) {
        if (failures == null) {
          failures = new LinkedList<String>();
        }
        failures.add(identifier);
      } else {
        result.put(identifier, msg);
      }
    }
    if (failures != null) {
      waitForMarketData(failures);
      final Iterator<String> itr = failures.iterator();
      while (itr.hasNext()) {
        final String identifier = itr.next();
        final FudgeMsg msg = getFireHose().getLatestValue(identifier);
        if (msg != null) {
          result.put(identifier, msg);
          itr.remove();
        }
      }
      if (!failures.isEmpty()) {
        throw new OpenGammaRuntimeException("Couldn't snapshot " + failures);
      }
    }
    return result;
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return _uniqueIdDomain;
  }

  @Override
  protected void doConnect() {
    getFireHose().start();
    _dispatcher = new TerminatableJob() {
      @Override
      protected void runOneCycle() {
        final Subscription subscription;
        try {
          subscription = _received.take();
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("Interrupted", e);
        }
        @SuppressWarnings("unchecked")
        final AtomicReference<FudgeMsg> pointer = (AtomicReference<FudgeMsg>) subscription.getHandle();
        if (pointer != null) {
          final FudgeMsg msg = pointer.getAndSet(null);
          if (msg != null) {
            liveDataReceived(subscription.getSecurityUniqueId(), msg);
          } else {
            s_logger.warn("NULL Fudge message found for {}", subscription.getSecurityUniqueId());
          }
        } else {
          s_logger.info("Poison message found in queue");
        }
      }
    };
    getExecutorService().submit(_dispatcher);
  }

  @Override
  protected void doDisconnect() {
    getFireHose().stop();
    // Terminate the dispatch thread
    _dispatcher.terminate();
    _dispatcher = null;
    // Poke it with a NULL message to release the "take" method
    _received.add(new Subscription("", new EmptyMarketDataSenderFactory(), getLkvStoreProvider()));
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(final Subscription subscription) {
    return false;
  }

}
