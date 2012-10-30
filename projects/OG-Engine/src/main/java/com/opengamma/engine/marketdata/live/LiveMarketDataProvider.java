/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link MarketDataProvider} for live data.
 */
public class LiveMarketDataProvider extends AbstractMarketDataProvider implements LiveDataListener {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataProvider.class);

  // Injected Inputs:
  private final LiveDataClient _liveDataClient;
  private final SecuritySource _securitySource;
  private final MarketDataAvailabilityProvider _availabilityProvider;
  private final LiveDataSpecificationLookup _liveDataLookup;

  // Runtime State:
  private final InMemoryLKVMarketDataProvider _underlyingProvider;
  private final MarketDataPermissionProvider _permissionProvider;
  private final Map<LiveDataSpecification, Set<ValueRequirement>> _liveDataSpec2ValueRequirements =
      new ConcurrentHashMap<LiveDataSpecification, Set<ValueRequirement>>();
  private final Set<ValueRequirement> _failedRequirements = new CopyOnWriteArraySet<ValueRequirement>();
  private final UserPrincipal _marketDataUser;

  public LiveMarketDataProvider(LiveDataClient liveDataClient,
                                MarketDataAvailabilityProvider availabilityProvider,
                                SecuritySource securitySource,
                                UserPrincipal marketDataUser) {
    this(liveDataClient,
         securitySource,
         availabilityProvider,
         new LiveMarketDataPermissionProvider(liveDataClient, securitySource),
         marketDataUser);
  }

  public LiveMarketDataProvider(LiveDataClient liveDataClient,
                                SecuritySource securitySource,
                                MarketDataAvailabilityProvider availabilityProvider,
                                MarketDataPermissionProvider permissionProvider,
                                UserPrincipal marketDataUser) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(availabilityProvider, "availabilityProvider");
    ArgumentChecker.notNull(permissionProvider, "permissionProvider");
    ArgumentChecker.notNull(marketDataUser, "marketDataUser");
    _liveDataClient = liveDataClient;
    _liveDataLookup = new LiveDataSpecificationLookup(securitySource, StandardRules.getOpenGammaRuleSetId());
    _securitySource = securitySource;
    _availabilityProvider = availabilityProvider;
    _underlyingProvider = new InMemoryLKVMarketDataProvider(securitySource);
    _permissionProvider = permissionProvider;
    _marketDataUser = marketDataUser;
  }

  private LiveDataSpecificationLookup getLiveDataSpecificationLookup() {
    return _liveDataLookup;
  }

  @Override
  public void subscribe(ValueRequirement valueRequirement) {
    subscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(Set<ValueRequirement> valueRequirements) {
    for (ValueRequirement valueRequirement : valueRequirements) {
      _failedRequirements.remove(valueRequirement); //Put these back to a waiting state so that we can try again
    }

    Set<LiveDataSpecification> liveDataSpecs = new HashSet<LiveDataSpecification>();
    for (ValueRequirement requirement : valueRequirements) {
      LiveDataSpecification liveDataSpec = getLiveDataSpecificationLookup().getRequiredLiveData(requirement);
      liveDataSpecs.add(liveDataSpec);
      registerLiveDataSpec(requirement, liveDataSpec);
    }
    _liveDataClient.subscribe(_marketDataUser, liveDataSpecs, this);
  }

  @Override
  public void unsubscribe(ValueRequirement valueRequirement) {
    unsubscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(Set<ValueRequirement> valueRequirements) {
    // TODO is there any reason not to unsubscribe from _liveDataClient
    //Set<LiveDataSpecification> specs = Sets.newHashSet();
    //for (ValueRequirement requirement : valueRequirements) {
    //  // TODO need a map from req to spec
    //}
    //_liveDataClient.unsubscribe(_marketDataUser, , this);
  }

  private void registerLiveDataSpec(ValueRequirement requirement, LiveDataSpecification liveDataSpec) {
    Set<ValueRequirement> requirementsForSpec = _liveDataSpec2ValueRequirements.get(liveDataSpec);
    if (requirementsForSpec == null) {
      requirementsForSpec = new HashSet<ValueRequirement>();
      _liveDataSpec2ValueRequirements.put(liveDataSpec, requirementsForSpec);
    }
    requirementsForSpec.add(requirement);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    // We don't look at the live data provider field at the moment
    return marketDataSpec instanceof LiveMarketDataSpecification;
  }

  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    return new LiveMarketDataSnapshot(_underlyingProvider.snapshot(marketDataSpec), this);
  }

  //-------------------------------------------------------------------------
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
      _failedRequirements.removeAll(valueRequirements); //We expect a valueUpdate call for this later
      s_logger.debug("Subscription made to {} resulted in fully qualified {}", subscriptionResult.getRequestedSpecification(), subscriptionResult.getFullyQualifiedSpecification());

      super.subscriptionSucceeded(valueRequirements);
    } else {
      _failedRequirements.addAll(valueRequirements);
      //TODO: could be more precise here, only those which weren't in _failedRequirements
      valuesChanged(valueRequirements); //PLAT-1429: wake up the init call

      if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.NOT_AUTHORIZED) {
        s_logger.warn("Subscription to {} failed because user is not authorised: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
      } else {
        s_logger.debug("Subscription to {} failed: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
      }

      super.subscriptionFailed(valueRequirements, subscriptionResult.getUserMessage());
    }
  }

  /* package */ boolean isFailed(ValueRequirement requirement) {
    return _failedRequirements.contains(requirement);
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
    FudgeMsg msg = valueUpdate.getFields();

    for (ValueRequirement valueRequirement : valueRequirements) {
      // We assume all market data can be represented as a Double. The request for the field as a Double also ensures
      // that we consistently provide a Double downstream, even if the value has been represented as a more efficient
      // type in the message.
      Double value = msg.getDouble(valueRequirement.getValueName());
      if (value == null) {
        continue;
      }
      _underlyingProvider.addValue(valueRequirement, value);
    }

    super.valuesChanged(valueRequirements);
  }

}
