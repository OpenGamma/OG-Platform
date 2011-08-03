/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import javax.time.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * Manager for receiving and handling events from masters.
 * <p>
 * Events will be sent when a document in a master is added, updated, removed or corrected.
 * <p>
 * This interface must be implemented in a thread-safe manner.
 */
@PublicSPI
public interface MasterChangeManager {

  /**
   * Adds a listener to the manager.
   * <p>
   * The listener will receive all events for the master.
   * 
   * @param listener  the listener to add, not null
   */
  void addChangeListener(MasterChangeListener listener);

  /**
   * Removed a listener from the manager.
   * <p>
   * The listener will cease receiving events for the master.
   * 
   * @param listener  the listener to remove, not null
   */
  void removeChangeListener(MasterChangeListener listener);

  /**
   * Fires an event when the master changes.
   * <p>
   * This method should only be called by a master.
   * 
   * @param type  the type of change, not null
   * @param beforeId  the unique identifier of the object after the change, may be null
   * @param afterId  the reference assigned to the listener, may be null
   * @param versionInstant  the reference assigned to the listener, may be null
   */
  void masterChanged(final MasterChangedType type, final UniqueId beforeId, final UniqueId afterId, final Instant versionInstant);

}
