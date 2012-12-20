/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of a {@link FunctionBlacklistPolicy}.
 */
public class DefaultFunctionBlacklistPolicy extends AbstractFunctionBlacklistPolicy {

  private final Set<Entry> _entries;

  public DefaultFunctionBlacklistPolicy(final UniqueId uniqueId, final int defaultEntryActivationPeriod, final Collection<Entry> entries) {
    this(uniqueId, uniqueId.getValue(), defaultEntryActivationPeriod, entries);
  }

  public DefaultFunctionBlacklistPolicy(final UniqueId uniqueId, final String name, final int defaultEntryActivationPeriod, final Collection<Entry> entries) {
    super(uniqueId, name, defaultEntryActivationPeriod);
    ArgumentChecker.notNull(entries, "entries");
    _entries = Collections.unmodifiableSet(new HashSet<Entry>(entries));
  }

  @Override
  public Set<Entry> getEntries() {
    return _entries;
  }

}
