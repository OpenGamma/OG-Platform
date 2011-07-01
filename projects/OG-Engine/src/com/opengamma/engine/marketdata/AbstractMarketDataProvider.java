/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Abstract base class for {@link MarketDataProvider} implementations.
 */
public abstract class AbstractMarketDataProvider implements MarketDataProvider {
  
  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();
  
  @Override
  public void addListener(MarketDataListener listener) {
    _listeners.add(listener);
  }
  
  @Override
  public void removeListener(MarketDataListener listener) {
    _listeners.remove(listener);
  }

  //-------------------------------------------------------------------------
  protected void valueChanged(ValueRequirement requirement) {
    for (MarketDataListener listener : getListeners()) {
      listener.valueChanged(requirement);      
    }
  }
  
  protected void valueChanged(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      valueChanged(requirement);      
    }
  }
  
  protected void subscriptionSucceeded(ValueRequirement requirement) {
    for (MarketDataListener listener : getListeners()) {
      listener.subscriptionSucceeded(requirement);      
    }
  }
  
  protected void subscriptionSucceeded(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      subscriptionSucceeded(requirement);      
    }
  }
  
  protected void subscriptionFailed(ValueRequirement requirement, String msg) {
    for (MarketDataListener listener : getListeners()) {
      listener.subscriptionFailed(requirement, msg);      
    }
  }
  
  protected void subscriptionFailed(Collection<ValueRequirement> requirements, String msg) {
    for (ValueRequirement requirement : requirements) {
      subscriptionFailed(requirement, msg);      
    }
  }
  
  protected void subscriptionStopped(ValueRequirement requirement) {
    for (MarketDataListener listener : getListeners()) {
      listener.subscriptionStopped(requirement);      
    }
  }
  
  protected void subscriptionStopped(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      subscriptionStopped(requirement);      
    }
  }
  
  //-------------------------------------------------------------------------
  /** 
   * @return Collection will be unmodifiable. Iterating over it will not throw {link ConcurrentModificationException}.
   */
  protected Collection<MarketDataListener> getListeners() {
    return Collections.unmodifiableCollection(_listeners);
  }
  
}
