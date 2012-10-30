/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of {@link ManageableFunctionBlacklist} using a {@link FunctionBlacklistRuleSet} to maintain the data.
 */
public class DefaultManageableFunctionBlacklist extends AbstractManageableFunctionBlacklist {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultManageableFunctionBlacklist.class);
  private static final int DEFAULT_TTL = 3600; // Blacklist for 1-hour

  private final FunctionBlacklistRuleSet _rules;
  private List<FunctionBlacklistRule> _added;
  private List<FunctionBlacklistRule> _removed;

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
  public DefaultManageableFunctionBlacklist(final String name, final ScheduledExecutorService scheduler, final int defaultTTL) {
    super(name, scheduler, defaultTTL);
    _rules = new FunctionBlacklistRuleSet(scheduler, defaultTTL) {

      @Override
      protected void onAdd(final FunctionBlacklistRule rule) {
        s_logger.debug("{} added to {}", rule, DefaultManageableFunctionBlacklist.this);
        final boolean direct = beginUpdate();
        try {
          if (direct) {
            // Notify any listeners
            DefaultManageableFunctionBlacklist.this.notifyAddRule(rule);
          } else {
            // Listeners will be notified as a bulk operation when the re-entry ends
            synchronized (DefaultManageableFunctionBlacklist.this) {
              if (_added == null) {
                _added = new LinkedList<FunctionBlacklistRule>();
              }
              _added.add(rule);
            }
          }
        } finally {
          endUpdate();
        }
      }

      @Override
      protected void onRemove(final FunctionBlacklistRule rule) {
        s_logger.debug("{} removed from {}", rule, DefaultManageableFunctionBlacklist.this);
        final boolean direct = beginUpdate();
        try {
          if (direct) {
            // Notify any listeners
            DefaultManageableFunctionBlacklist.this.notifyRemoveRule(rule);
          } else {
            // Listeners will be notified as a bulk operation when the re-entry ends
            synchronized (DefaultManageableFunctionBlacklist.this) {
              if (_removed == null) {
                _removed = new LinkedList<FunctionBlacklistRule>();
              }
              _removed.add(rule);
            }
          }
        } finally {
          endUpdate();
        }
      }

    };
  }

  @Override
  protected boolean endUpdate() {
    if (!super.endUpdate()) {
      return false;
    }
    final List<FunctionBlacklistRule> added, removed;
    synchronized (this) {
      added = _added;
      _added = null;
      removed = _removed;
      _removed = null;
    }
    if (added != null) {
      if (added.size() > 1) {
        notifyAddRules(added);
      } else {
        notifyAddRule(added.get(0));
      }
    }
    if (removed != null) {
      if (removed.size() > 1) {
        notifyRemoveRules(removed);
      } else {
        notifyRemoveRule(removed.get(0));
      }
    }
    return true;
  }

  @Override
  public FunctionBlacklistRuleSet getRules() {
    return _rules;
  }

  @Override
  public void addBlacklistRule(final FunctionBlacklistRule rule, final int timeToLive) {
    getRules().add(rule, timeToLive);
  }

  @Override
  public void removeBlacklistRule(final FunctionBlacklistRule rule) {
    getRules().remove(rule);
  }

}
