/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Partial implementation of a {@link ManageableFunctionBlacklist} interface.
 */
public abstract class AbstractManageableFunctionBlacklist extends AbstractFunctionBlacklist implements ManageableFunctionBlacklist {

  private final AtomicInteger _update = new AtomicInteger();
  private int _defaultTTL;

  protected AbstractManageableFunctionBlacklist(final String name, final ExecutorService executorService, int defaultTTL) {
    super(name, executorService);
    _defaultTTL = defaultTTL;
  }

  /**
   * Sets the default time to live for a new blacklist rule.
   * 
   * @param timeToLive the time to live in seconds
   */
  public synchronized void setDefaultTTL(final int timeToLive) {
    _defaultTTL = timeToLive;
  }

  /**
   * Queries the default time to live for a new blacklist rule.
   * 
   * @return the time to live in seconds
   */
  public synchronized int getDefaultTTL() {
    return _defaultTTL;
  }

  /**
   * Marks the start of an update operation. Re-entry to updates is counted so that bulk operations can be identified. Subclasses could also use this to acquire a lock and release it in
   * {@link #endUpdate}.
   * 
   * @return true if this is the first call, false if this is a re-entry
   */
  protected boolean beginUpdate() {
    return _update.getAndIncrement() == 0;
  }

  /**
   * Marks the end of an update operation. Re-entry to updates is counted so that bulk operations can be identifier. Subclasses could also use this to release any locks acquired by
   * {@link #beginUpdate}.
   * 
   * @return true if this was the last call, false if there is still re-entry
   */
  protected boolean endUpdate() {
    return _update.decrementAndGet() == 0;
  }

  @Override
  public void addBlacklistRule(final FunctionBlacklistRule rule) {
    addBlacklistRule(rule, getDefaultTTL());
  }

  @Override
  public void addBlacklistRules(final Collection<FunctionBlacklistRule> rules) {
    beginUpdate();
    try {
      for (FunctionBlacklistRule rule : rules) {
        addBlacklistRule(rule);
      }
    } finally {
      endUpdate();
    }
  }

  @Override
  public void addBlacklistRules(final Collection<FunctionBlacklistRule> rules, int timeToLive) {
    beginUpdate();
    try {
      for (FunctionBlacklistRule rule : rules) {
        addBlacklistRule(rule, timeToLive);
      }
    } finally {
      endUpdate();
    }
  }

  @Override
  public void removeBlacklistRules(final Collection<FunctionBlacklistRule> rules) {
    beginUpdate();
    try {
      for (FunctionBlacklistRule rule : rules) {
        removeBlacklistRule(rule);
      }
    } finally {
      endUpdate();
    }
  }

}
