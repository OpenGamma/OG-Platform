/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.UniqueId;

import javax.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class TestChangeManager implements ChangeManager, ChangeProvider {

  private final List<ChangeListener> _listeners = new CopyOnWriteArrayList<ChangeListener>();

  @Override
  public void addChangeListener(ChangeListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(ChangeListener listener) {
    throw new UnsupportedOperationException("removeChangeListener not implemented");
  }

  @Override
  public void entityChanged(ChangeType type, UniqueId beforeId, UniqueId afterId, Instant versionInstant) {
    ChangeEvent event = new ChangeEvent(type, beforeId, afterId, versionInstant);
    for (ChangeListener listener : _listeners) {
      listener.entityChanged(event);
    }
  }

  @Override
  public ChangeManager changeManager() {
    return this;
  }
}
