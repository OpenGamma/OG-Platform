/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;

/**
 * Change manager that passes listeners to multiple underlying managers.
 * <p>
 * Unlike an {@link AggregatingChangeManager} no events can be generated locally for its immediate listeners.
 */
public class PassthroughChangeManager implements ChangeManager {

  private final List<ChangeManager> _underlying = new CopyOnWriteArrayList<ChangeManager>();

  /**
   * Creates an instance.
   */
  public PassthroughChangeManager() {
  }

  /**
   * Creates an instance.
   *
   * @param changeProviders the underlying change providers, not null and not containing null
   */
  public PassthroughChangeManager(final Iterable<? extends ChangeProvider> changeProviders) {
    for (final ChangeProvider changeProvider : changeProviders) {
      addChangeManager(changeProvider.changeManager());
    }
  }

  /**
   * Creates an instance.
   *
   * @param changeProviders the underlying change providers, not null and not containing null
   */
  public PassthroughChangeManager(final ChangeProvider... changeProviders) {
    for (final ChangeProvider changeProvider : changeProviders) {
      addChangeManager(changeProvider.changeManager());
    }
  }

  /**
   * Adds the manager as an underlying. Once added as an underlying, the manager cannot be removed. Any requests to add or remove a listener made on this instance will be passed to all underlying
   * change managers.
   *
   * @param changeManager the change manager to add, not null
   */
  public void addChangeManager(final ChangeManager changeManager) {
    _underlying.add(changeManager);
  }

  // ChangeManager

  @Override
  public void addChangeListener(final ChangeListener listener) {
    for (final ChangeManager underlying : _underlying) {
      underlying.addChangeListener(listener);
    }
  }

  @Override
  public void removeChangeListener(final ChangeListener listener) {
    for (final ChangeManager underlying : _underlying) {
      underlying.removeChangeListener(listener);
    }
  }

  @Override
  public void entityChanged(final ChangeType type, final ObjectId oid, final Instant versionFrom, final Instant versionTo, final Instant versionInstant) {
    throw new UnsupportedOperationException();
  }

}
