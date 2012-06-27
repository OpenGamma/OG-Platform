/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * Partial implementation of a {@link ManageableFunctionBlacklist} interface.
 */
public abstract class AbstractManageableFunctionBlacklist extends AbstractFunctionBlacklist implements ManageableFunctionBlacklist {

  private long _defaultTTL;

  protected AbstractManageableFunctionBlacklist(final String name, final ExecutorService executorService, long defaultTTL) {
    super(name, executorService);
    _defaultTTL = defaultTTL;
  }

  /**
   * Sets the default time to live for a new blacklist rule.
   * 
   * @param timeToLive the time to live in seconds
   */
  public synchronized void setDefaultTTL(final long timeToLive) {
    _defaultTTL = timeToLive;
  }

  /**
   * Queries the default time to live for a new blacklist rule.
   * 
   * @return the time to live in seconds
   */
  public synchronized long getDefaultTTL() {
    return _defaultTTL;
  }

  @Override
  public void addBlacklistRule(final FunctionBlacklistRule rule) {
    addBlacklistRule(rule, getDefaultTTL());
  }

  @Override
  public void addBlacklistRules(final Collection<FunctionBlacklistRule> rules) {
    for (FunctionBlacklistRule rule : rules) {
      addBlacklistRule(rule);
    }
  }

  @Override
  public void addBlacklistRules(final Collection<FunctionBlacklistRule> rules, long timeToLive) {
    for (FunctionBlacklistRule rule : rules) {
      addBlacklistRule(rule, timeToLive);
    }
  }

  @Override
  public void removeBlacklistRules(final Collection<FunctionBlacklistRule> rules) {
    for (FunctionBlacklistRule rule : rules) {
      removeBlacklistRule(rule);
    }
  }

}
