/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import javax.time.Instant;

import com.opengamma.id.UniqueIdentifier;

/**
 * Implementation of {@link ChangeManager} to use when change notifications are not supported or never needed.
 */
public class DummyChangeManager implements ChangeManager {

  @Override
  public void addChangeListener(ChangeListener listener) {
  }

  @Override
  public void removeChangeListener(ChangeListener listener) {
  }

  @Override
  public void entityChanged(ChangeType type, UniqueIdentifier beforeId, UniqueIdentifier afterId, Instant versionInstant) {
    throw new UnsupportedOperationException(DummyChangeManager.class.getSimpleName() + " does not support change notifications");
  }

}
