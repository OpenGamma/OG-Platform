/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.opengamma.util.tuple.Pair;

/**
 * Utility class for managing a subscription to blacklist rules
 */
/* package */abstract class BaseFunctionBlacklistRuleListener implements FunctionBlacklistRuleListener {

  private int _previousModification;

  protected void init(final int initialModificationCount, final Collection<FunctionBlacklistRule> initialRules) {
    _previousModification = initialModificationCount;
    replaceRules(initialRules);
  }

  /**
   * Queries the underlying for it's current modification count and rule set. If the current modification at the underlying is the same as the known modification count then it may return null.
   * 
   * @param modificationCount the known/expected modification count
   * @return the correct modification count and rule set or null if it is as expected
   */
  protected abstract Pair<Integer, ? extends Collection<FunctionBlacklistRule>> getUnderlyingRules(int modificationCount);

  protected abstract void replaceRules(Collection<FunctionBlacklistRule> rules);

  protected abstract void addRule(FunctionBlacklistRule rule);

  protected void addRules(final Collection<FunctionBlacklistRule> rules) {
    for (FunctionBlacklistRule rule : rules) {
      addRule(rule);
    }
  }

  protected abstract void removeRule(FunctionBlacklistRule rule);

  protected void removeRules(final Collection<FunctionBlacklistRule> rules) {
    for (FunctionBlacklistRule rule : rules) {
      removeRule(rule);
    }
  }

  protected synchronized void refresh() {
    Pair<Integer, ? extends Collection<FunctionBlacklistRule>> underlying = getUnderlyingRules(_previousModification);
    while (underlying != null) {
      _previousModification = underlying.getFirst();
      replaceRules(underlying.getSecond());
      underlying = getUnderlyingRules(_previousModification);
    }
  }

  private synchronized boolean validSequence(final int modificationCount, final ExecutorService defer) {
    if (_previousModification + 1 == modificationCount) {
      _previousModification = modificationCount;
      return true;
    } else {
      defer.submit(new Runnable() {
        @Override
        public void run() {
          refresh();
        }
      });
      return false;
    }
  }

  @Override
  public void ruleAdded(final int modificationCount, final FunctionBlacklistRule rule, final ExecutorService defer) {
    if (validSequence(modificationCount, defer)) {
      addRule(rule);
    }
  }

  @Override
  public void rulesAdded(final int modificationCount, final Collection<FunctionBlacklistRule> rules, final ExecutorService defer) {
    if (validSequence(modificationCount, defer)) {
      addRules(rules);
    }
  }

  @Override
  public void ruleRemoved(final int modificationCount, final FunctionBlacklistRule rule, final ExecutorService defer) {
    if (validSequence(modificationCount, defer)) {
      removeRule(rule);
    }
  }

  @Override
  public void rulesRemoved(final int modificationCount, final Collection<FunctionBlacklistRule> rules, final ExecutorService defer) {
    if (validSequence(modificationCount, defer)) {
      removeRules(rules);
    }
  }

}
