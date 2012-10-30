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
   *
   * <code>
   * get(uniqueId);
   * </code>
   *
   * @param listener  the listener to add, not null
   * @param oid the identifier to register interest in
   * */
  void addChangeListener(ObjectId oid, ObjectChangeListener listener);

  /**
   * Removes a listener from the source.
   * <p>
   * The listener will cease receiving events for this {@link com.opengamma.id.UniqueId} on the source
   *
   * @param listener  the listener to remove, not null
   * @param oid the identifier to unregister interest in
   * */
  void removeChangeListener(ObjectId oid, ObjectChangeListener listener);

}
