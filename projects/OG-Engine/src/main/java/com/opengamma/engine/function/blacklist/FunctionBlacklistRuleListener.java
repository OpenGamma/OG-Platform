/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * Callback interface for receiving notifications about rules that have been added or removed from a blacklist. Actions should be minimal as they may be blocking the caller which triggered the update.
 * If a complex action is required, or one that will make calls while holding monitors that could otherwise cause deadlocks, the supplied executor should be used to defer it.
 */
public interface FunctionBlacklistRuleListener {

  /**
   * Called when a rule has been added.
   * 
   * @param modificationCount the current modification count - should be an increment of the previous notification
   * @param rule the new rule
   * @param defer the executor to use for a deferred action
   */
  void ruleAdded(int modificationCount, FunctionBlacklistRule rule, ExecutorService defer);

  /**
   * Called when multiple rules have been added.
   * 
   * @param modificationCount the current modification count - should be an increment of the previous notification
   * @param rules the new rules
   * @param defer the executor to use for a deferred action
   */
  void rulesAdded(int modificationCount, Collection<FunctionBlacklistRule> rules, ExecutorService defer);

  /**
   * Called when a rule has been removed.
   * 
   * @param modificationCount the current modification count - should be an increment of the previous notification
   * @param rule the removed rule
   * @param defer the executor to use for a deferred action
   */
  void ruleRemoved(int modificationCount, FunctionBlacklistRule rule, ExecutorService defer);

  /**
   * Called when multiple rules have been removed.
   * 
   * @param modificationCount the current modification count - should be an increment of the previous notification
   * @param rules the removed rules
   * @param defer the executor to use for a deferred action
   */
  void rulesRemoved(int modificationCount, Collection<FunctionBlacklistRule> rules, ExecutorService defer);

}
