/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Change manager that aggregates entity change events from multiple underlying managers.
 * <p>
 * Events are sent when an entity is added, updated, removed or corrected.
 * <p>
 * This class is mutable and thread-safe using concurrent collections.
 */
public class AggregatingChangeManager extends BasicChangeManager implements ChangeListener {

  /**
   * Creates an instance.
   */
  public AggregatingChangeManager() {
  }

  /**
   * Creates an instance.
   * 
   * @param changeProviders  the change providers to aggregate, not null
   */
  public AggregatingChangeManager(List<ChangeProvider> changeProviders) {
    ArgumentChecker.notNull(changeProviders, "changeProviders");
    for (ChangeProvider changeProvider : changeProviders) {
      addChangeManager(changeProvider.changeManager());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds the manager to those aggregated.
   * <p>
   * This simply adds this instance as a listener to the specified manager.
   * 
   * @param changeManager  the change manager to add, not null
   */
  public void addChangeManager(ChangeManager changeManager) {
    changeManager.addChangeListener(this);
  }
      
  /**
   * Removes the manager from those aggregated.
   * <p>
   * This simply removes this instance as a listener from the specified manager.
   * 
   * @param changeManager  the change manager to add, not null
   */
  public void removeChangeManager(ChangeManager changeManager) {
    changeManager.removeChangeListener(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public void entityChanged(ChangeEvent event) {
    // Forward on the event to the local listeners
    handleEntityChanged(event);
  }

}
