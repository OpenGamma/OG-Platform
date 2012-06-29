/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory function blacklist implementation. Any time a named blacklist is requested an empty one will be constructed if this has not already been done so.
 */
public class InMemoryFunctionBlacklistProvider implements ManageableFunctionBlacklistProvider {
  
  private final ConcurrentMap<String, ManageableFunctionBlacklist> _blacklists = new ConcurrentHashMap<String, ManageableFunctionBlacklist>();
  private final ScheduledExecutorService _executor;

  /**
   * Creates a new blacklist implementation.
   * 
   * @param executor the executor service to use for housekeeping, not null
   */
  public InMemoryFunctionBlacklistProvider(final ScheduledExecutorService executor) {
    ArgumentChecker.notNull(executor, "executor");
    _executor = executor;
  }

  protected ScheduledExecutorService getExecutor() {
    return _executor;
  }

  protected ManageableFunctionBlacklist createBlacklist(final String identifier) {
    return new DefaultManageableFunctionBlacklist(identifier, getExecutor());
  }

  @Override
  public ManageableFunctionBlacklist getBlacklist(final String identifier) {
    ManageableFunctionBlacklist blacklist = _blacklists.get(identifier);
    if (blacklist == null) {
      blacklist = createBlacklist(identifier);
      final ManageableFunctionBlacklist existing = _blacklists.putIfAbsent(identifier, blacklist);
      if (existing != null) {
        blacklist = existing;
      }
    }
    return blacklist;
  }

}
