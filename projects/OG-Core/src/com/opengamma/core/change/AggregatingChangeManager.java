/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import com.opengamma.util.ArgumentChecker;

import java.util.List;

/**
 * Manager for aggregating entity change events from multiple underlying change managers.
 * <p>
 * Events are sent when an entity is added, updated, removed or corrected.
 * <p>
 * This class is mutable and thread-safe using concurrent collections.
 */
public class AggregatingChangeManager extends BasicChangeManager implements ChangeListener {

  public AggregatingChangeManager() {
  }

  public AggregatingChangeManager(List<ChangeProvider> changeProviders) {
    ArgumentChecker.notNull(changeProviders, "changeProviders");
    for (ChangeProvider changeProvider : changeProviders) {
      addChangeManager(changeProvider.changeManager());
    }
  }

  public void addChangeManager(ChangeManager changeManager) {
    changeManager.addChangeListener(this);
  }
      
  public void removeChangeManager(ChangeManager changeManager) {
    changeManager.removeChangeListener(this);
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    // Forward on the event to the local listeners
    handleEntityChanged(event);
  }

}
