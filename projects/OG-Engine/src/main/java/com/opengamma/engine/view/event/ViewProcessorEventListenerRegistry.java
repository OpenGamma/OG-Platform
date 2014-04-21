/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.event;

import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.id.UniqueId;

/**
 * Registered listeners for registering and unregistering ViewProcessorEventListener and sending notifications to
 * registrants.
 *  <p>
 * There is one of these per ViewProcessor. It is a composite listener.
 */
public class ViewProcessorEventListenerRegistry implements ViewProcessorEventListener {

  /**
   * The set of listeners.
   */
  private final CopyOnWriteArraySet<ViewProcessorEventListener> _listeners = new CopyOnWriteArraySet<ViewProcessorEventListener>();

  @Override
  public void notifyViewProcessAdded(UniqueId viewProcessId) {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessAdded(viewProcessId);
    }
  }

  @Override
  public void notifyViewAutomaticallyStarted(UniqueId viewProcessId, String autoStartName) {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewAutomaticallyStarted(viewProcessId, autoStartName);
    }
  }

  @Override
  public void notifyViewProcessRemoved(UniqueId viewProcessId) {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessRemoved(viewProcessId);
    }
  }
  
  @Override
  public void notifyViewClientAdded(UniqueId viewClientId) {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewClientAdded(viewClientId);
    }
  }

  @Override
  public void notifyViewClientRemoved(UniqueId viewClientId) {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewClientRemoved(viewClientId);
    }
  }
  
  /**
   * Adds a listener to the notification service. No guarantee is made that listeners will be
   * notified in the order they were added.
   *
   * @param viewProcessorEventListener the listener to add. Can be null, in which case nothing happens
   * @return true if the listener is being added and was not already added
   */
  public final boolean registerListener(ViewProcessorEventListener viewProcessorEventListener) {
    if (viewProcessorEventListener == null) {
      return false;
    }
    return _listeners.add(viewProcessorEventListener);
  }

  /**
   * Removes a listener from the notification service.
   *
   * @param viewProcessorEventListener the listener to remove
   * @return true if the listener was present
   */
  public final boolean unregisterListener(ViewProcessorEventListener viewProcessorEventListener) {
    return _listeners.remove(viewProcessorEventListener);
  }

  @Override
  public void notifyViewProcessorStarted() {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessorStarted();
    }
  }

  @Override
  public void notifyViewProcessorStopped() {
    for (ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessorStopped();
    }
  }

}
