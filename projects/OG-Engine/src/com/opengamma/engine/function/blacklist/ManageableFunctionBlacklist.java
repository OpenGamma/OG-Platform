/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

/**
 * Read/write interface to a blacklist resource. An update interface is typically obtained from a {@link ManageableFunctionBlacklistProvider}. A blacklist resource may be shared and be used/updated by
 * multiple agents. The order of updates is not guaranteed to other agents querying the list as they may be performing best efforts caching.
 */
public interface ManageableFunctionBlacklist extends FunctionBlacklist {

  /**
   * Adds a rule to the blacklist with a default time to live. If there is already a matching rule its time to live must be replaced with the new value.
   * 
   * @param rule the rule to add, not null
   */
  void addBlacklistRule(FunctionBlacklistRule rule);

  /**
   * Adds a rule to the blacklist with a specific time to live. If there is already a matching rule its time to live must be replaced with the new value.
   * 
   * @param rule the rule to add, not null
   * @param timeToLive the time to live in seconds
   */
  void addBlacklistRule(FunctionBlacklistRule rule, int timeToLive);

  /**
   * Adds one or more rules to the blacklist with a default time to live. If there are already matching rules their time to live must be replaced with the new value.
   * 
   * @param rules the rules to add, not null
   */
  void addBlacklistRules(Collection<FunctionBlacklistRule> rules);

  /**
   * Adds one or more rules to the blacklist with a specific time to live. If there are already matching rules their time to live must be replaced with the new value.
   * 
   * @param rules the rules to add, not null
   * @param timeToLive the time to live in seconds
   */
  void addBlacklistRules(Collection<FunctionBlacklistRule> rules, int timeToLive);

  /**
   * Removes a rule from the blacklist.
   * 
   * @param rule the rule to remove
   */
  void removeBlacklistRule(FunctionBlacklistRule rule);

  /**
   * Removes multiple rules from the blacklist.
   * 
   * @param rules the rules to remove
   */
  void removeBlacklistRules(Collection<FunctionBlacklistRule> rules);

}
