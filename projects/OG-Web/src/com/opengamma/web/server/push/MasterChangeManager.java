/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.rest.MasterType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Dispatches notifications to listeners when data changes in a master.
 */
/* package */ class MasterChangeManager {

  /** Listeners for changes in data in a master */
  private final Set<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  /**
   * Creates a new instance that will receive change events from the change providers and dispatch them to its
   * listeners.
   * @param changeProviders Providers of change events from masters keyed by the type of the master which
   * produces their events.
   */
  public MasterChangeManager(Map<MasterType, ChangeProvider> changeProviders) {
    for (Map.Entry<MasterType, ChangeProvider> entry : changeProviders.entrySet()) {
      final MasterType masterType = entry.getKey();
      ChangeProvider changeProvider = entry.getValue();
      changeProvider.changeManager().addChangeListener(new ChangeListener() {
        @Override
        public void entityChanged(ChangeEvent event) {
          MasterChangeManager.this.entityChanged(masterType);
        }
      });
    }
  }

  /* package */ void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /* package */ void removeChangeListener(MasterChangeListener listener) {
    _listeners.remove(listener);
  }

  private void entityChanged(MasterType masterType) {
    for (MasterChangeListener listener : _listeners) {
      listener.masterChanged(masterType);
    }
  }
}
