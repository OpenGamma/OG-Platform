/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import com.opengamma.id.ObjectId;

/**
 * Manage for object change listeners.
 */
public interface ObjectChangeListenerManager {

  /**
   * Adds a listener to the source.
   * <p>
   * The listener will receive events for the source which change the result of:
   * <code>
   * get(objectId);
   * </code>
   *
   * @param objectId  the identifier to register interest in, not null
   * @param listener  the listener to add, not null
   */
  void addChangeListener(ObjectId objectId, ObjectChangeListener listener);

  /**
   * Removes a listener from the source.
   * <p>
   * The listener will cease receiving events for the identifier.
   *
   * @param objectId  the identifier to unregister interest in, not null
   * @param listener  the listener to remove, not null
   */
  void removeChangeListener(ObjectId objectId, ObjectChangeListener listener);

}
