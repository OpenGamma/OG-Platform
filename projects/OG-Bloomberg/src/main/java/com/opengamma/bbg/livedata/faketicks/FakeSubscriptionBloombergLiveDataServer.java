/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.livedata.BloombergLiveDataServer;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A live data server which fakes out Bloomberg subscriptions for some tickers.
 * Other tickers will be subscribed as normal.
 */
public class FakeSubscriptionBloombergLiveDataServer extends StandardLiveDataServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(FakeSubscriptionBloombergLiveDataServer.class);

  /**
   * Timer period.
   */
  private static final long PERIOD_MILLIS = Duration.ofHours(24).toMillis();

  /**
   * The subscriptions that have been made.
   */
  private final ConcurrentMap<String, Object> _subscriptions = new ConcurrentHashMap<String, Object>();
  /**
   * The cache of values.
   */
  private final Cache _snapshotValues;
  /**
   * The timer.
   */
  private Timer _timer;
  /**
   * The underlying server.
   */
  private final BloombergLiveDataServer _underlying;
  /**
   * The external identifier scheme that this live data server handles. This must match the scheme that the underlying
   * live data server handles.
   */
  private final ExternalScheme _uniqueIdDomain;

  /**
   * Creates an instance.
   * <p>
   * The distribution specification resolver, entitlement checker and market data sender factory
   * are set by the constructor based on the underlying server.
   * 
   * @param underlying  the underlying server, not null
   * @param uniqueIdDomain  the external identifier scheme that this live data server handles, not null
   * @param cacheManager  the cache manager, not null
   */
  public FakeSubscriptionBloombergLiveDataServer(BloombergLiveDataServer underlying, ExternalScheme uniqueIdDomain, CacheManager cacheManager) {
    super(cacheManager);
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(uniqueIdDomain, "uniqueIdDomain");
    _underlying = underlying;
    _uniqueIdDomain = uniqueIdDomain;
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    setDistributionSpecificationResolver(getDistributionSpecificationResolver(underlying.getDistributionSpecificationResolver()));
    setEntitlementChecker(underlying.getEntitlementChecker());
    setMarketDataSenderFactory(underlying.getMarketDataSenderFactory());
    
    String snapshotCacheName = "FakeSubscriptionBloombergLiveDataServer.SnapshotValues";
    EHCacheUtils.addCache(cacheManager, snapshotCacheName);
    _snapshotValues = EHCacheUtils.getCacheFromManager(cacheManager, snapshotCacheName);
  }

  private DistributionSpecificationResolver getDistributionSpecificationResolver(final DistributionSpecificationResolver underlying) {
    return new FakeDistributionSpecificationResolver(underlying);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doConnect() {
    _timer = new Timer(FakeSubscriptionBloombergLiveDataServer.class.getSimpleName(), true);
    
    _timer.schedule(new TimerTask() {
      
      @Override
      public void run() {
        updateAll();
      }
    }, PERIOD_MILLIS, PERIOD_MILLIS);
  }

  private void updateAll() {
    Set<String> idsToUpdate = _subscriptions.keySet();
    s_logger.info("Requerying {} in order to fake ticks", idsToUpdate);
    Map<String, FudgeMsg> doSnapshot = doUnderlyingSnapshot(idsToUpdate);
    for (Entry<String, FudgeMsg> entry : doSnapshot.entrySet()) {
      liveDataReceived(entry.getKey(), entry.getValue());
    }
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Unique IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Map<String, FudgeMsg> result = new HashMap<String, FudgeMsg>();
    Set<String> uidsToQuery = new HashSet<String>();
    for (String uid : uniqueIds) {
      Element cached = _snapshotValues.get(uid);
      if (cached != null && cached.getObjectValue() != null) {
        CachedPerSecuritySnapshotResult cachedResult = (CachedPerSecuritySnapshotResult) cached.getObjectValue();
        result.put(uid, cachedResult._fieldData);
      } else {
        uidsToQuery.add(uid);
      }
    }
    if (uidsToQuery.isEmpty()) {
      return result;
    }
    
    Map<String, FudgeMsg> underlyingResult = doUnderlyingSnapshot(uidsToQuery);
    result.putAll(underlyingResult);
    return result;
  }

  private Map<String, FudgeMsg> doUnderlyingSnapshot(Set<String> uidsToQuery) {
    Map<String, FudgeMsg> result = _underlying.doSnapshot(uidsToQuery);
    
    for (Entry<String, FudgeMsg> entry : result.entrySet()) {
      //In the case of a race there may already be an entry here, but it's consistent
      String uid = entry.getKey();
      _snapshotValues.put(new Element(uid, new CachedPerSecuritySnapshotResult(entry.getValue())));
    }
    
    _snapshotValues.flush();
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Cached value.
   */
  private static class CachedPerSecuritySnapshotResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private FudgeMsg _fieldData;

    private static class Inner implements Serializable {
      private static final long serialVersionUID = -4661981447809146669L;
      private byte[] _data;
    }

    public CachedPerSecuritySnapshotResult(FudgeMsg value) {
      _fieldData = value;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
      if (_fieldData == null) {
        out.writeObject(new Inner());
        return;
      }

      byte[] bytes = OpenGammaFudgeContext.getInstance().toByteArray(_fieldData);
      Inner wrapper = new Inner();
      wrapper._data = bytes;
      out.writeObject(wrapper);
      out.flush();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      Inner wrapper = (Inner) in.readObject();
      byte[] bytes = wrapper._data;
      if (bytes == null) {
        _fieldData = null;
      } else {
        FudgeMsgEnvelope envelope = OpenGammaFudgeContext.getInstance().deserialize(bytes);
        _fieldData = envelope.getMessage();
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doDisconnect() {
    final CountDownLatch timerCancelledLatch = new CountDownLatch(1);
    _timer.schedule(new TimerTask() {
      
      @Override
      public void run() {
        s_logger.info("Cancelling fake subscriptions");
        _timer.cancel(); // NOTE doing this here  "absolutely guarantees that the ongoing task execution is the last task execution"
        timerCancelledLatch.countDown();
      }
    }, 0);
    try {
      s_logger.info("Waiting for fake subscriptions to stop");
      timerCancelledLatch.await();
    } catch (InterruptedException ex) {
      throw new OpenGammaRuntimeException("Interrupted whilst disconnecting", ex);
    }
    s_logger.info("Fake subscriptions to stopped");
    _timer = null;
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Unique IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Map<String, Object> subscriptions = new HashMap<String, Object>();
    for (String uniqueId : uniqueIds) {
      s_logger.info("Faking subscription to {}", uniqueId);
      subscriptions.put(uniqueId, uniqueId);
    }
    
    _subscriptions.putAll(subscriptions);
    return subscriptions;
  }

  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    ArgumentChecker.notNull(subscriptionHandles, "Subscription handles");
    if (subscriptionHandles.isEmpty()) {
      return;
    }
    
    for (Object subscriptionHandle : subscriptionHandles) {
      s_logger.info("Removing fake subscription to {}", subscriptionHandle);
      _subscriptions.remove(subscriptionHandle);
    }
  }

  @Override
  public ExternalScheme getUniqueIdDomain() {
    return _uniqueIdDomain;
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    return true;
  }

  /**
   * Gets the reference data provider from the underlying.
   * 
   * @return the reference data provider, not null
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _underlying.getReferenceDataProvider();
  }

}
