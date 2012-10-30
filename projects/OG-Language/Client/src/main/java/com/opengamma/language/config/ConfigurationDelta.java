/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Represents a 'delta' between two sets of configuration items.
 */
public final class ConfigurationDelta {

  private final Set<ConfigurationItem> _added;
  private final Set<ConfigurationItem> _removed;

  private ConfigurationDelta(final Set<ConfigurationItem> added, final Set<ConfigurationItem> removed) {
    _added = added;
    _removed = removed;
  }

  public static ConfigurationDelta of(final Collection<ConfigurationItem> first, final Collection<ConfigurationItem> second) {
    final Set<ConfigurationItem> firstSet = (first != null) ? new HashSet<ConfigurationItem>(first) : Collections.<ConfigurationItem>emptySet();
    final Set<ConfigurationItem> secondSet = (second != null) ? new HashSet<ConfigurationItem>(second) : Collections.<ConfigurationItem>emptySet();
    return new ConfigurationDelta(Sets.difference(secondSet, firstSet), Sets.difference(firstSet, secondSet));
  }

  public static ConfigurationDelta empty() {
    return of(null, null);
  }

  public Set<ConfigurationItem> getAdded() {
    return _added;
  }

  public Set<ConfigurationItem> getRemoved() {
    return _removed;
  }

  public boolean hasChanged() {
    return !getAdded().isEmpty() || !getRemoved().isEmpty();
  }

}
