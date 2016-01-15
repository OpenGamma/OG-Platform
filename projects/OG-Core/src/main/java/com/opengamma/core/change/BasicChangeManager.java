/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.id.ObjectId;
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
public class BasicChangeManager implements ChangeManager, Serializable {

  private static final Logger s_logger = LoggerFactory.getLogger(BasicChangeManager.class);
  
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
    return Lists.newArrayList(_listeners);
  }

  //-------------------------------------------------------------------------

  @Override
  public void entityChanged(ChangeType type, ObjectId oid, Instant versionFrom, Instant versionTo, Instant versionInstant) {
    ChangeEvent event = new ChangeEvent(type, oid, versionFrom, versionTo, versionInstant);
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
      try {
        listener.entityChanged(event);
      } catch (Exception e) {
        s_logger.error("Error while calling listener " + listener + " on entity changed", e);
      }
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
