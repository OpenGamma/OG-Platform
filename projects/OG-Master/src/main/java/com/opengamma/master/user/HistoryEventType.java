/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

/**
 * The type of a history event.
 * <p>
 * See {@link HistoryEvent} for more details.
 */
public enum HistoryEventType {

  /**
   * The object was added to the master.
   */
  ADDED,
  /**
   * The object was changed in the master.
   */
  CHANGED,
  /**
   * The object was removed from the master.
   */
  REMOVED,

}
