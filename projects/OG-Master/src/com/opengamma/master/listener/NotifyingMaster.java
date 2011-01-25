/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

/**
 * Trait added to those masters that can send events whenever they are changed.
 * <p>
 * This trait allows listeners to be registered for the events.
 */
public interface NotifyingMaster {

  /**
   * Adds a change listener to the master.
   * <p>
   * This will add a change listener to the master which will be called with events.
   * 
   * @param listener  the change listener, not null
   */
  void addChangeListener(MasterChangeListener listener);

  /**
   * Removes a change listener from the master.
   * <p>
   * This will remove a change listener from the master.
   * 
   * @param listener  the change listener, not null
   */
  void removeChangeListener(MasterChangeListener listener);

}
