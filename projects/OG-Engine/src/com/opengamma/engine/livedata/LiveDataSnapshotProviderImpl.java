/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LiveDataSnapshotProviderImpl extends AbstractLiveDataSnapshotProvider implements LiveDataListener {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(LiveDataSnapshotProviderImpl.class);

  // Injected Inputs:
  private final LiveDataClient _liveDataClient;
  private final FudgeContext _fudgeContext;
  private final SecuritySource _securitySource;

  // Runtime State:
  private final InMemoryLKVSnapshotProvider _underlyingProvider = new InMemoryLKVSnapshotProvider();
  private final Map<LiveDataSpecification, Set<ValueRequirement>> _liveDataSpec2ValueRequirements =
    new ConcurrentHashMap<LiveDataSpecification, Set<ValueRequirement>>();

  public LiveDataSnapshotProviderImpl(LiveDataClient liveDataClient, SecuritySource securitySource) {
    this(liveDataClient, securitySource, new FudgeContext());
  }

  public LiveDataSnapshotProviderImpl(LiveDataClient liveDataClient, SecuritySource securitySource, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _liveDataClient = liveDataClient;
    _securitySource = securitySource;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying snapshot provider.
   * @return the underlying provider, not null
   */
  public InMemoryLKVSnapshotProvider getUnderlyingProvider() {
    return _underlyingProvider;
  }

  /**
   * Gets the source of securities.
   * @return the source of securities, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the Fudge context.
   * @return the fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement requirement) {
    addSubscription(user, Collections.singleton(requirement));    
  }
  
  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    Set<LiveDataSpecification> liveDataSpecs = new HashSet<LiveDataSpecification>();
    for (ValueRequirement requirement : valueRequirements) {
      LiveDataSpecification liveDataSpec = requirement.getRequiredLiveData(getSecuritySource());
      liveDataSpecs.add(liveDataSpec);
      registerLiveDataSpec(requirement, liveDataSpec);
    }
    _liveDataClient.subscribe(user, liveDataSpecs, this);
  }

  /**
   * @param requirement
   * @param liveDataSpec
   */
  protected void registerLiveDataSpec(ValueRequirement requirement, LiveDataSpecification liveDataSpec) {
    Set<ValueRequirement> requirementsForSpec = _liveDataSpec2ValueRequirements.get(liveDataSpec);
    if (requirementsForSpec == null) {
      requirementsForSpec = new HashSet<ValueRequirement>();
      _liveDataSpec2ValueRequirements.put(liveDataSpec, requirementsForSpec);
    }
    requirementsForSpec.add(requirement);
  }
  
  // Protected for unit testing.
  protected Map<LiveDataSpecification, Set<ValueRequirement>> getRequirementsForSubscriptionIds() {
    return Collections.unmodifiableMap(_liveDataSpec2ValueRequirements);
  }

  @Override
  public Object querySnapshot(long snapshot,
      ValueRequirement requirement) {
    return getUnderlyingProvider().querySnapshot(snapshot, requirement);
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    getUnderlyingProvider().releaseSnapshot(snapshot);
  }

  @Override
  public long snapshot() {
    return getUnderlyingProvider().snapshot();
  }
  
  public long snapshot(long snapshot) {
    return getUnderlyingProvider().snapshot(snapshot);
  }

  @Override
  public void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult) {
    Set<ValueRequirement> valueRequirements = _liveDataSpec2ValueRequirements.remove(subscriptionResult.getRequestedSpecification());
    if (valueRequirements == null) {
      s_logger.warn("Received subscription result for which no corresponding set of value requirements was found: {}", subscriptionResult);
      s_logger.debug("Current pending subscriptions: {}", _liveDataSpec2ValueRequirements);
      return;
    }
    if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
      _liveDataSpec2ValueRequirements.put(subscriptionResult.getFullyQualifiedSpecification(), valueRequirements);
      s_logger.info("Subscription made to {} resulted in fully qualified {}", subscriptionResult.getRequestedSpecification(), subscriptionResult.getFullyQualifiedSpecification());
      super.subscriptionSucceeded(valueRequirements);
    } else {
      s_logger.error("Subscription to {} failed: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
      super.subscriptionFailed(valueRequirements, subscriptionResult.getUserMessage());
    }
  }

  @Override
  public void subscriptionStopped(
      LiveDataSpecification fullyQualifiedSpecification) {
    // This shouldn't really happen because there's no removeSubscription() method on this class...
    s_logger.warn("Subscription stopped " + fullyQualifiedSpecification);
  }

  @Override
  public void valueUpdate(LiveDataValueUpdate valueUpdate) {
    s_logger.debug("Update received {}", valueUpdate);
    
    Set<ValueRequirement> valueRequirements = _liveDataSpec2ValueRequirements.get(valueUpdate.getSpecification());
    if (valueRequirements == null) {
      s_logger.warn("Received value update for which no corresponding set of value requirements was found: {}", valueUpdate.getSpecification());
      return;            
    }
    
    s_logger.debug("Corresponding value requirements are {}", valueRequirements);
    FudgeFieldContainer msg = valueUpdate.getFields();
    
    for (ValueRequirement valueRequirement : valueRequirements) {
      // We assume all market data can be represented as a Double. The request for the field as a Double also ensures
      // that we consistently provide a Double downstream, even if the value has been represented as a more efficient
      // type in the message.
      Double value = msg.getDouble(valueRequirement.getValueName());
      if (value == null) {
        continue;
      }
      getUnderlyingProvider().addValue(valueRequirement, value);
    }
    
    super.valueChanged(valueRequirements);
  }
  
}
