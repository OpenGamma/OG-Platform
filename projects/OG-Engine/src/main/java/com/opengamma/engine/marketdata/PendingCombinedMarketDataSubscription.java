/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the state relating to pending subscriptions over multiple live data providers.
 */
/*package*/ class PendingCombinedMarketDataSubscription {
  
  private final Set<MarketDataProvider> _pendingProviders;
  private final Collection<String> _failureMessages = new ArrayList<String>();
  private volatile boolean _overallFailure;
  
  /**
   * Enumerates the possible states while attempting to make a subscription over multiple live data providers.
   */
  public enum PendingCombinedSubscriptionState {
  
    /**
     * Waiting for one or more providers to respond to the subscription request.
     */
    AWAITING_RESPONSES,
    /**
     * The combined subscriptions were made successfully.
     */
    SUCCESS,
    /**
     * One or more subscriptions failed.
     */
    FAILURE
    
  }
  
  public PendingCombinedMarketDataSubscription(Collection<MarketDataProvider> pendingProviders) {
    _pendingProviders = new HashSet<MarketDataProvider>(pendingProviders);
  }
  
  public PendingCombinedSubscriptionState subscriptionSucceeded(MarketDataProvider provider) {
    synchronized (_pendingProviders) {
      return removePendingProvider(provider, true);
    }
  }
  
  public PendingCombinedSubscriptionState subscriptionFailed(MarketDataProvider provider, String msg) {
    synchronized (_pendingProviders) {
      _failureMessages.add(msg);
      return removePendingProvider(provider, false);
    }
  }
  
  public Collection<String> getFailureMessages() {
    return _failureMessages;
  }
  
  private PendingCombinedSubscriptionState removePendingProvider(MarketDataProvider provider, boolean success) {
    _pendingProviders.remove(provider);
    boolean finished = _pendingProviders.size() == 0;
    return incrementState(success, finished);
  }
  
  private PendingCombinedSubscriptionState incrementState(boolean success, boolean complete) {
    if (!success) {
      _overallFailure = true;
    }
    if (complete) {
      return _overallFailure
          ? PendingCombinedSubscriptionState.FAILURE
          : PendingCombinedSubscriptionState.SUCCESS;
    } else {
      return PendingCombinedSubscriptionState.AWAITING_RESPONSES;
    }
  }
  
}
