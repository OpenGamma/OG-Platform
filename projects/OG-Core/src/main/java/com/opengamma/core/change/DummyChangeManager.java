/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;

/**
 * Implementation of {@link ChangeManager} to use when change notifications are not supported or never needed.
 */
public final class DummyChangeManager implements ChangeManager {

  /**
   * Singleton instance
   */
  public static final DummyChangeManager INSTANCE = new DummyChangeManager();

  /**
   * Hidden constructor.
   */
  private DummyChangeManager() {
  }

  @Override
  public void addChangeListener(ChangeListener listener) {
    // dummy manager does nothing
  }

  @Override
  public void removeChangeListener(ChangeListener listener) {
    // dummy manager does nothing
  }

  @Override
  public void entityChanged(ChangeType type, ObjectId oid, Instant versionFrom, Instant versionTo, Instant versionInstant) {
    // dummy manager does nothing
  }
}
