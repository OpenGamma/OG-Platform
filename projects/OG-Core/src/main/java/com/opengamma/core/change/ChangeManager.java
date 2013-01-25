/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.util.PublicSPI;

/**
 * Manager for receiving and handling entity change events.
 * <p>
 * Events are sent when an entity is added, updated or removed.
 * <p>
 * This interface must be implemented in a thread-safe manner.
 */
@PublicSPI
public interface ChangeManager {

  /**
   * Adds a listener to the manager.
   * <p>
   * The listener will receive all events from the manager.
   * 
   * @param listener  the listener to add, not null
   */
  void addChangeListener(ChangeListener listener);

  /**
   * Removes a listener from the manager.
   * <p>
   * The listener will cease receiving events from the manager.
   * 
   * @param listener  the listener to remove, not null
   */
  void removeChangeListener(ChangeListener listener);

  /**
   * Handles an entity change event.
   * <p>
   * This method should only be called by the owner of the change manager.
   * It is invoked whenever an entity has been successfully changed.
   * 
   * @param type  the type of change, not null
   * @param oid  the object id of the entity, not null
   * @param versionFrom  the begining of a timespan of the change of the entity, not null
   * @param versionTo  the end of a timespan of the change of the entity, may be null
   * @param versionInstant  the instant at which the change is recorded as happening, not null
   */
  void entityChanged(final ChangeType type, final ObjectId oid, final Instant versionFrom, final Instant versionTo, final Instant versionInstant);

}
