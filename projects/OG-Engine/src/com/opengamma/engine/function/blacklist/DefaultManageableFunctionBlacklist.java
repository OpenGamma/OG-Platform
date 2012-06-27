/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Basic implementation of {@link ManageableFunctionBlacklist} using a {@link DefaultFunctionBlacklistQuery} and {@link FunctionBlacklistRuleSet} to maintain the data.
 */
public class DefaultManageableFunctionBlacklist extends AbstractManageableFunctionBlacklist {

  private static final long DEFAULT_TTL = 60 * 60 * 6; // Blacklist for 6-hours

  private final FunctionBlacklistRuleSet _rules;

  public DefaultManageableFunctionBlacklist(final String name, final ScheduledExecutorService scheduler) {
    this(name, scheduler, DEFAULT_TTL);
  }

  /**
   * Creates a new updateable blacklist.
   * 
   * @param name the name of the blacklist
   * @param scheduler the scheduler to use for blacklist housekeeping
   * @param defaultTTL the default time to live
   */
  public DefaultManageableFunctionBlacklist(final String name, final ScheduledExecutorService scheduler, final long defaultTTL) {
    super(name, scheduler, defaultTTL);
    _rules = new FunctionBlacklistRuleSet(scheduler, defaultTTL) {

      @Override
      protected void onAdd(final FunctionBlacklistRule rule) {
        // Notify any listeners
        DefaultManageableFunctionBlacklist.this.notifyAddRule(rule);
      }

      @Override
      protected void onRemove(final FunctionBlacklistRule rule) {
        // Notify any listeners
        DefaultManageableFunctionBlacklist.this.notifyRemoveRule(rule);
      }

    };
  }

  @Override
  public FunctionBlacklistRuleSet getRules() {
    return _rules;
  }

  @Override
  public void addBlacklistRule(final FunctionBlacklistRule rule, final long timeToLive) {
    getRules().add(rule, timeToLive);
  }

  @Override
  public void removeBlacklistRule(final FunctionBlacklistRule rule) {
    getRules().remove(rule);
  }

}
