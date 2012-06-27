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
public class DefaultFunctionBlacklistPolicy implements FunctionBlacklistPolicy {

  private final String _name;
  private final UniqueId _uniqueId;
  private final long _defaultEntryActivationPeriod;
  private final Set<Entry> _entries;

  public DefaultFunctionBlacklistPolicy(final UniqueId uniqueId, final long defaultEntryActivationPeriod, final Collection<Entry> entries) {
    this(uniqueId, uniqueId.getValue(), defaultEntryActivationPeriod, entries);
  }

  public DefaultFunctionBlacklistPolicy(final UniqueId uniqueId, final String name, final long defaultEntryActivationPeriod, final Collection<Entry> entries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(entries, "entries");
    _uniqueId = uniqueId;
    _name = name;
    _defaultEntryActivationPeriod = defaultEntryActivationPeriod;
    _entries = Collections.unmodifiableSet(new HashSet<Entry>(entries));
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public long getDefaultEntryActivationPeriod() {
    return _defaultEntryActivationPeriod;
  }

  @Override
  public Set<Entry> getEntries() {
    return _entries;
  }

}
