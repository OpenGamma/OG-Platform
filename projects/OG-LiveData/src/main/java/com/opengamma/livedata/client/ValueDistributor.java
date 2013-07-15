/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;

/**
 * A stand-alone class which distributes live data values to all subscribed listeners.
 * <p>
 * This is separate from all live data client instances as it needs to be compact to be able to have efficient concurrency.
 */
public class ValueDistributor {

  /**
   * The map of specification to listeners.
   */
  private final ConcurrentMap<LiveDataSpecification, Set<LiveDataListener>> _listenersBySpec =
      new ConcurrentHashMap<LiveDataSpecification, Set<LiveDataListener>>();

  /**
   * Gets the current specifications.
   * 
   * @return the specifications, not null
   */
  public Set<LiveDataSpecification> getActiveSpecifications() {
    return new HashSet<LiveDataSpecification>(_listenersBySpec.keySet());
  }

  /**
   * Gets the number of current specifications.
   * 
   * @return the number of current specifications.
   */
  public int getActiveSpecificationCount() {
    return _listenersBySpec.size();
  }

  /**
   * Adds a listener.
   * 
   * @param fullyQualifiedSpecification the fully qualified specification, not null
   * @param listener the listener
   */
  public void addListener(LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener) {
    Set<LiveDataListener> freshListeners = new HashSet<LiveDataListener>();
    Set<LiveDataListener> actualListeners = _listenersBySpec.putIfAbsent(fullyQualifiedSpecification, freshListeners);
    if (actualListeners == null) {
      actualListeners = freshListeners;
    }
    synchronized (actualListeners) {
      actualListeners.add(listener);
    }
  }

  /**
   * Removes a listener.
   * 
   * @param fullyQualifiedSpecification the fully qualified specification, not null
   * @param listener the listener
   * @return true iff there are still active listeners
   */
  public boolean removeListener(LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener) {
    Set<LiveDataListener> actualListeners = _listenersBySpec.get(fullyQualifiedSpecification);
    if (actualListeners == null) {
      return false;
    }
    synchronized (actualListeners) {
      actualListeners.remove(listener);
      if (actualListeners.isEmpty()) {
        boolean removed = _listenersBySpec.remove(fullyQualifiedSpecification, actualListeners);
        if (removed) {
          return false;
        } else {
          // Someone else added a new one in addListener.
          return true;
        }
      } else {
        return true;
      }
    }
  }

  // TODO kirk 2009-09-29 -- This should be handed an executor service to
  // invoke the updates asynchronously.
  public void notifyListeners(LiveDataValueUpdateBean updateBean) {
    Set<LiveDataListener> listeners = _listenersBySpec.get(updateBean.getSpecification());
    if (listeners == null) {
      return;
    }
    for (LiveDataListener listener : listeners) {
      listener.valueUpdate(updateBean);
    }
  }

}
