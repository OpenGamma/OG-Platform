/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.time.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Manager for receiving and handling events from masters.
 * <p>
 * Events will be sent when a document in a master is added, updated, removed or corrected.
 * <p>
 * This class is mutable and thread-safe using concurrent collections.
 */
@PublicSPI
public class BasicMasterChangeManager implements MasterChangeManager {

  /**
   * The listeners.
   */
  private final CopyOnWriteArraySet<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  /**
   * Creates a manager.
   */
  public BasicMasterChangeManager() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a listener to the manager.
   * <p>
   * The listener will receive all events for the master.
   * <p>
   * This method is not intended to be overridden.
   * 
   * @param listener  the listener to add, not null
   */
  @Override
  public void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /**
   * Removed a listener from the manager.
   * <p>
   * The listener will cease receiving events for the master.
   * <p>
   * This method is not intended to be overridden.
   * 
   * @param listener  the listener to remove, not null
   */
  @Override
  public void removeChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }

  /**
   * Gets a copy of the list of listeners.
   * <p>
   * This method is not intended to be overridden.
   * 
   * @return the list of listeners, not null
   */
  protected List<MasterChangeListener> getListeners() {
    return new ArrayList<MasterChangeListener>(_listeners);
  }

  //-------------------------------------------------------------------------
  /**
   * Handles an event when the master changes.
   * <p>
   * This method should only be called by a master.
   * 
   * @param type  the type of change, not null
   * @param beforeId  the unique identifier of the object after the change, may be null
   * @param afterId  the reference assigned to the listener, may be null
   * @param versionInstant  the reference assigned to the listener, may be null
   */
  @Override
  public void masterChanged(final MasterChangedType type, final UniqueId beforeId, final UniqueId afterId, final Instant versionInstant) {
    MasterChanged event = new MasterChanged(type, beforeId, afterId, versionInstant);
    handleMasterChanged(event);
  }

  /**
   * Handles an event when the master changes.
   * <p>
   * This implementation calls {@link #fireMasterChanged(MasterChanged)} directly.
   * An overriding method may use a more advanced mechanism to handle the event.
   * 
   * @param event  the event that occurred, not null
   */
  protected void handleMasterChanged(final MasterChanged event) {
    fireMasterChanged(event);
  }

  /**
   * Fires an event to the local listeners when the master changes.
   * <p>
   * This implementation loops around the stored listeners and calls them in
   * serial on the calling thread.
   * 
   * @param event  the event that occurred, not null
   */
  protected void fireMasterChanged(final MasterChanged event) {
    for (MasterChangeListener listener : _listeners) {
      listener.masterChanged(event);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a debugging string for the manager.
   * 
   * @return the debugging string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
