/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.bbg.livedata.AbstractBloombergLiveDataServer;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.replay.BloombergTick;
import com.opengamma.bbg.replay.BloombergTickReceiver;
import com.opengamma.bbg.replay.BloombergTicksReplayer;
import com.opengamma.bbg.replay.BloombergTicksReplayer.Mode;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;

/**
 * Exposes a window of recorded Bloomberg data in an infinite loop to simulate a live data server. 
 */
public class RecordedBloombergLiveDataServer extends AbstractBloombergLiveDataServer {
  
  private final BloombergTicksReplayer _tickReplayer;
  private final ReferenceDataProvider _referenceDataProvider;
  
  /**
   * Used to keep track of subscriptions ourself in a way that can be queried efficiently, every time a tick is
   * replayed, to see whether it is required.
   */
  private final ConcurrentMap<String, Object> _subscriptions = new ConcurrentHashMap<String, Object>();
  
  /**
   * Creates an instance, parsing the given times from ISO-8601 strings.
   *  
   * @param rootTickPath  the recorded ticks directory
   * @param dataStart  the tick start time
   * @param dataEnd  the tick end time
   * @param referenceDataProvider  a source of reference data
   * @param cacheManager  the cache manager, not null
   */
  public RecordedBloombergLiveDataServer(String rootTickPath, String dataStart, String dataEnd, ReferenceDataProvider referenceDataProvider, CacheManager cacheManager) {
    this(rootTickPath, ZonedDateTime.parse(dataStart), ZonedDateTime.parse(dataEnd), referenceDataProvider, cacheManager);
  }
  
  /**
   * Creates an instance.
   * 
   * @param rootTickPath  the recorded ticks directory
   * @param dataStart  the tick start time
   * @param dataEnd  the tick end time
   * @param referenceDataProvider  a source of reference data
   * @param cacheManager  the cache manager, not null
   */
  public RecordedBloombergLiveDataServer(String rootTickPath, ZonedDateTime dataStart, ZonedDateTime dataEnd, ReferenceDataProvider referenceDataProvider, CacheManager cacheManager) {
    super(cacheManager);
    BloombergTickReceiver tickReceiver = new BloombergTickReceiver() {
      @Override
      public void tickReceived(BloombergTick msg) {
        RecordedBloombergLiveDataServer.this.tickReceived(msg.getBuid(), msg.getFields());
      }
    };
    
    setEntitlementChecker(new LiveDataEntitlementChecker() {
      @Override
      public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
        Map<LiveDataSpecification, Boolean> results = new HashMap<LiveDataSpecification, Boolean>();
        for (LiveDataSpecification requestedSpec : requestedSpecifications) {
          results.put(requestedSpec, true);
        }
        return results;
      }
      
      @Override
      public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
        return true;
      }
    });
    
    _tickReplayer = new BloombergTicksReplayer(Mode.ORIGINAL_LATENCY, rootTickPath, tickReceiver, dataStart, dataEnd, true, Collections.<String>emptySet());
    _referenceDataProvider = referenceDataProvider;
  }

  @Override
  protected void doConnect() {
    _tickReplayer.start();
  }

  @Override
  protected void doDisconnect() {
    _tickReplayer.stop();
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    Map<String, Object> subscriptions = new HashMap<String, Object>();
    for (String uniqueId : uniqueIds) {
      subscriptions.put(uniqueId, uniqueId);
    }
    _subscriptions.putAll(subscriptions);
    return subscriptions;
  }

  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    for (Object subscriptionHandle : subscriptionHandles) {
      _subscriptions.remove(subscriptionHandle);
    }
  }
  
  @Override
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }
  
  private void tickReceived(String buid, FudgeMsg fields) {
    if (_subscriptions.containsKey(buid)) {
      liveDataReceived(buid, fields);
    }
  }

}
