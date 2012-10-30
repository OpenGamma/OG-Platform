/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link LiveDataClient} that delegates all calls to other
 * clients, keyed by the external ID scheme of the items to be loaded.
 * Where requests are made that may be satisfied by any of the underlying clients,
 * the actual underlying client that will be chosen is non-deterministic.
 */
public class DelegatingLiveDataClient implements LiveDataClient {
  private static final Logger s_logger = LoggerFactory.getLogger(DelegatingLiveDataClient.class);
  private final Map<String, LiveDataClient> _underlyingClients = new ConcurrentSkipListMap<String, LiveDataClient>();
  private LiveDataClient _defaultClient;
  
  public void addUnderlyingClient(String idScheme, LiveDataClient liveDataClient) {
    ArgumentChecker.notNull(idScheme, "idScheme");
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    _underlyingClients.put(idScheme, liveDataClient);
  }
  
  public void setDefaultClient(LiveDataClient defaultClient) {
    _defaultClient = defaultClient;
  }
  
  protected LiveDataClient identifyUnderlying(LiveDataSpecification specification) {
    ExternalIdBundle idBundle = specification.getIdentifiers();
    
    for (ExternalId id : idBundle.getExternalIds()) {
      LiveDataClient underlying = _underlyingClients.get(id.getScheme().getName());
      if (underlying != null) {
        s_logger.debug("Delegating {} to {}", specification, underlying);
        return underlying;
      }
    }
    
    if (_defaultClient != null) {
      return _defaultClient;
    }
    
    throw new OpenGammaRuntimeException("No underlying client configured to handle " + specification);
  }
  
  protected Map<LiveDataClient, List<LiveDataSpecification>> splitCollection(Collection<LiveDataSpecification> specifications) {
    Map<LiveDataClient, List<LiveDataSpecification>> result = new HashMap<LiveDataClient, List<LiveDataSpecification>>();
    for (LiveDataSpecification specification : specifications) {
      LiveDataClient underlying = identifyUnderlying(specification);
      List<LiveDataSpecification> perUnderlyingSpecs = result.get(underlying);
      if (perUnderlyingSpecs == null) {
        perUnderlyingSpecs = new LinkedList<LiveDataSpecification>();
        result.put(underlying, perUnderlyingSpecs);
      }
      perUnderlyingSpecs.add(specification);
    }
    return result;
  }

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    LiveDataClient underlying = identifyUnderlying(requestedSpecification);
    return underlying.isEntitled(user, requestedSpecification);
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataClient, List<LiveDataSpecification>> split = splitCollection(requestedSpecifications);
    
    Map<LiveDataSpecification, Boolean> result = new HashMap<LiveDataSpecification, Boolean>();
    
    for (Map.Entry<LiveDataClient, List<LiveDataSpecification>> entry : split.entrySet()) {
      Map<LiveDataSpecification, Boolean> perUnderlyingResult = entry.getKey().isEntitled(user, entry.getValue());
      if (perUnderlyingResult != null) {
        result.putAll(perUnderlyingResult);
      }
    }
    
    return result;
  }

  @Override
  public void subscribe(UserPrincipal user, LiveDataSpecification requestedSpecification, LiveDataListener listener) {
    LiveDataClient underlying = identifyUnderlying(requestedSpecification);
    underlying.subscribe(user, requestedSpecification, listener);
  }

  @Override
  public void subscribe(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, LiveDataListener listener) {
    Map<LiveDataClient, List<LiveDataSpecification>> split = splitCollection(requestedSpecifications);
    for (Map.Entry<LiveDataClient, List<LiveDataSpecification>> entry : split.entrySet()) {
      entry.getKey().subscribe(user, entry.getValue(), listener);
    }
  }

  @Override
  public void unsubscribe(UserPrincipal user, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener) {
    LiveDataClient underlying = identifyUnderlying(fullyQualifiedSpecification);
    underlying.unsubscribe(user, fullyQualifiedSpecification, listener);
  }

  @Override
  public void unsubscribe(UserPrincipal user, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener) {
    Map<LiveDataClient, List<LiveDataSpecification>> split = splitCollection(fullyQualifiedSpecifications);
    for (Map.Entry<LiveDataClient, List<LiveDataSpecification>> entry : split.entrySet()) {
      entry.getKey().unsubscribe(user, entry.getValue(), listener);
    }
  }

  @Override
  public LiveDataSubscriptionResponse snapshot(UserPrincipal user, LiveDataSpecification requestedSpecification, long timeout) {
    LiveDataClient underlying = identifyUnderlying(requestedSpecification);
    return underlying.snapshot(user, requestedSpecification, timeout);
  }

  @Override
  public Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, long timeout) {
    Map<LiveDataClient, List<LiveDataSpecification>> split = splitCollection(requestedSpecifications);
    List<LiveDataSubscriptionResponse> snapshots = new ArrayList<LiveDataSubscriptionResponse>(requestedSpecifications.size());
    for (Map.Entry<LiveDataClient, List<LiveDataSpecification>> entry : split.entrySet()) {
      snapshots.addAll(entry.getKey().snapshot(user, entry.getValue(), timeout));
    }
    return snapshots;
  }

  @Override
  public String getDefaultNormalizationRuleSetId() {
    // REVIEW kirk 2012-08-17 -- This probably isn't the best behavior here.
    if (_underlyingClients.isEmpty()) {
      return null;
    }
    return _underlyingClients.values().iterator().next().getDefaultNormalizationRuleSetId();
  }

  @Override
  public void close() {
    for (LiveDataClient underlying : _underlyingClients.values()) {
      underlying.close();
    }
  }

}
