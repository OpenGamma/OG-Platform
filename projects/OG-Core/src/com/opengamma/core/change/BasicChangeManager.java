/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.time.Instant;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Manager for receiving and handling entity change events.
 * <p>
 * Events are sent when an entity is added, updated, removed or corrected.
 * <p>
 * This class is mutable and thread-safe using concurrent collections.
 */
@PublicSPI
public class BasicChangeManager implements ChangeManager {

  /**
   * The listeners.
   */
  private final CopyOnWriteArraySet<ChangeListener> _listeners = new CopyOnWriteArraySet<ChangeListener>();

  /**
   * Creates a manager.
   */
  public BasicChangeManager() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a listener to the manager.
   * <p>
   * The listener will receive all events.
   * <p>
   * This method is not intended to be overridden.
   * 
   * @param listener  the listener to add, not null
   */
  @Override
  public void addChangeListener(ChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /**
   * Removed a listener from the manager.
   * <p>
   * The listener will cease receiving events.
   * <p>
   * This method is not intended to be overridden.
   * 
   * @param listener  the listener to remove, not null
   */
  @Override
  public void removeChangeListener(ChangeListener listener) {
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
  protected List<ChangeListener> getListeners() {
    return new ArrayList<ChangeListener>(_listeners);
  }

  //-------------------------------------------------------------------------
  /**
   * Handles an event when an entity changes.
   * <p>
   * This method should only be called by the owner of the change manager.
   * 
   * @param type  the type of change, not null
   * @param beforeId  the unique identifier of the entity before the change, may be null
   * @param afterId  the unique identifier of the entity after the change, may be null
   * @param versionInstant  the instant at which the change is recorded as happening, not null
   */
  @Override
  public void entityChanged(final ChangeType type, final UniqueIdentifier beforeId, final UniqueIdentifier afterId, final Instant versionInstant) {
    ChangeEvent event = new ChangeEvent(type, beforeId, afterId, versionInstant);
    handleEntityChanged(event);
  }

  /**
   * Handles an event when an entity changes.
   * <p>
   * This implementation calls {@link #fireEntityChanged(ChangeEvent)} directly.
   * An overriding method may use a more advanced mechanism to handle the event.
   * 
   * @param event  the event that occurred, not null
   */
  protected void handleEntityChanged(final ChangeEvent event) {
    fireEntityChanged(event);
  }

  /**
   * Fires an event to the local listeners when an entity changes.
   * <p>
   * This implementation loops around the stored listeners and calls them in
   * serial on the calling thread.
   * 
   * @param event  the event that occurred, not null
   */
  protected void fireEntityChanged(final ChangeEvent event) {
    for (ChangeListener listener : _listeners) {
      listener.entityChanged(event);
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
