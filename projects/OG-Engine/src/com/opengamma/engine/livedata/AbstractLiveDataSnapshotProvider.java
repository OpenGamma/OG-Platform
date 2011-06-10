/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 */
public abstract class AbstractLiveDataSnapshotProvider implements LiveDataSnapshotProvider {
  
  private final CopyOnWriteArraySet<LiveDataSnapshotListener> _listeners = new CopyOnWriteArraySet<LiveDataSnapshotListener>();

  @Override
  public void addListener(LiveDataSnapshotListener listener) {
    _listeners.add(listener);
  }
  
  @Override
  public void removeListener(LiveDataSnapshotListener listener) {
    _listeners.remove(listener);
  }
  
  /** 
   * @return Collection will be unmodifiable. Iterating over it will not throw {@code ConcurrentModificationException}.
   */
  public Collection<LiveDataSnapshotListener> getListeners() {
    return Collections.unmodifiableCollection(_listeners);
  }
  
  protected void valueChanged(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : getListeners()) {
      listener.valueChanged(requirement);      
    }
  }
  
  protected void valueChanged(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      valueChanged(requirement);      
    }
  }
  
  protected void subscriptionSucceeded(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : getListeners()) {
      listener.subscriptionSucceeded(requirement);      
    }
  }
  
  protected void subscriptionSucceeded(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      subscriptionSucceeded(requirement);      
    }
  }
  
  protected void subscriptionFailed(ValueRequirement requirement, String msg) {
    for (LiveDataSnapshotListener listener : getListeners()) {
      listener.subscriptionFailed(requirement, msg);      
    }
  }
  
  protected void subscriptionFailed(Collection<ValueRequirement> requirements, String msg) {
    for (ValueRequirement requirement : requirements) {
      subscriptionFailed(requirement, msg);      
    }
  }
  
  protected void subscriptionStopped(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : getListeners()) {
      listener.subscriptionStopped(requirement);      
    }
  }
  
  protected void subscriptionStopped(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      subscriptionStopped(requirement);      
    }
  }
  
  
  @Override
  public boolean hasStructuredData() {
    return false;
  }

  @Override
  public Object querySnapshot(long snapshot, StructuredMarketDataKey marketDataKey) {
    return null;
  }
}
