/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.client.LiveDataClient;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataSnapshotProviderImpl implements LiveDataSnapshotProvider, LiveDataListener 
{
  private static final Logger s_logger = LoggerFactory.getLogger(LiveDataSnapshotProviderImpl.class);
  
  // Injected Inputs:
  private final LiveDataClient _liveDataClient;
  private final FudgeContext _fudgeContext;
  private final SecurityMaster _securityMaster;
  
  // Runtime State:
  private final InMemoryLKVSnapshotProvider _underlyingProvider = new InMemoryLKVSnapshotProvider();
  private final Map<LiveDataSpecification, Set<ValueRequirement>> _liveDataSpec2ValueRequirements =
    new ConcurrentHashMap<LiveDataSpecification, Set<ValueRequirement>>();
  
  public LiveDataSnapshotProviderImpl(LiveDataClient liveDataClient, SecurityMaster secMaster) {
    this(liveDataClient, secMaster, new FudgeContext());
  }
  
  public LiveDataSnapshotProviderImpl(LiveDataClient liveDataClient, SecurityMaster secMaster, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(liveDataClient, "Live Data Client");
    ArgumentChecker.notNull(secMaster, "Security master");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _liveDataClient = liveDataClient;
    _securityMaster = secMaster;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlyingProvider
   */
  public InMemoryLKVSnapshotProvider getUnderlyingProvider() {
    return _underlyingProvider;
  }

  /**
   * @return the secMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void addSubscription(String userName, ValueRequirement requirement) {
    addSubscription(userName, Collections.singleton(requirement));    
  }
  
  @Override
  public void addSubscription(String userName, Set<ValueRequirement> valueRequirements) {
    Set<LiveDataSpecification> liveDataSpecs = new HashSet<LiveDataSpecification>();
    for (ValueRequirement requirement : valueRequirements) {
      LiveDataSpecification liveDataSpec = constructRequirementLiveDataSpecification(requirement);
      liveDataSpecs.add(liveDataSpec);
      Set<ValueRequirement> requirementsForSpec = _liveDataSpec2ValueRequirements.get(liveDataSpec);
      if(requirementsForSpec == null) {
        requirementsForSpec = new HashSet<ValueRequirement>();
        _liveDataSpec2ValueRequirements.put(liveDataSpec, requirementsForSpec);
      }
      requirementsForSpec.add(requirement);
      
      _liveDataClient.subscribe(userName, liveDataSpec, this);
    }
    //_liveDataClient.subscribe(userName, liveDataSpecs, this);
  }
  
  /**
   * @param requirement
   * @return
   */
  private LiveDataSpecification constructRequirementLiveDataSpecification(
      ValueRequirement requirement) {
    switch(requirement.getTargetSpecification().getType()) {
    case PRIMITIVE:
      // Just use the identifier as given.
      return new LiveDataSpecification(_liveDataClient.getDefaultNormalizationRuleSetId(), requirement.getTargetSpecification().getIdentifier());
    case SECURITY:
      Security security = getSecurityMaster().getSecurity(requirement.getTargetSpecification().getUniqueIdentifier());
      if (security == null) {
        throw new OpenGammaRuntimeException("Unknown security in configured security master: " + requirement.getTargetSpecification().getIdentifier());
      }
      // Package up the other identifiers
      return new LiveDataSpecification(_liveDataClient.getDefaultNormalizationRuleSetId(), security.getIdentifiers());
    default:
      throw new OpenGammaRuntimeException("Unhandled requirement type for live data client: " + requirement);
    }
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

  @Override
  public void subscriptionResultReceived(
      LiveDataSubscriptionResponse subscriptionResult) {
    if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
      
      Set<ValueRequirement> valueRequirements = _liveDataSpec2ValueRequirements.get(subscriptionResult.getRequestedSpecification());
      if (valueRequirements == null) {
        s_logger.warn("Received subscription result for which no corresponding set of value requirements was found: {}", subscriptionResult);
        return;
      }
      
      _liveDataSpec2ValueRequirements.remove(subscriptionResult.getRequestedSpecification());
      _liveDataSpec2ValueRequirements.put(subscriptionResult.getFullyQualifiedSpecification(), valueRequirements);
      
      s_logger.info("Subscription made to {}", subscriptionResult.getRequestedSpecification());
    
    } else {
      s_logger.error("Subscription to {} failed: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);      
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
      ComputedValue value = new ComputedValue(new ValueSpecification(valueRequirement), msg);
      getUnderlyingProvider().addValue(value);
    }
  }
  
}
