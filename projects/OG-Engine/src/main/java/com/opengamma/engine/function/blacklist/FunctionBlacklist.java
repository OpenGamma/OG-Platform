/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Set;

/**
 * Readable interface to a blacklist resource. An instance is typically obtained from a {@link FunctionBlacklistProvider}. A blacklist resource may be shared and be used/updated by multiple agents. A
 * query interface may perform a best efforts caching and best efforts routing of updates to the resource.
 */
public interface FunctionBlacklist {

  /**
   * Returns the symbolic name of the blacklist.
   * 
   * @return the name, not null
   */
  String getName();

  /**
   * Returns the current rule set.
   * 
   * @return the rule set
   */
  Set<FunctionBlacklistRule> getRules();

  /**
   * Returns the modification count for the rule set. The count will increase by 1 each time an event occurs (that would normally notify the listeners). Observing a change in the modification count
   * greater than this between notifications means a notification was missed. Observing a change around a call to getRules means the rule set has changed and the values may not have been correct.
   * 
   * @return the modification count
   */
  int getModificationCount();

  /**
   * Adds a listener to be notified when the underlying rule set this is querying has changed.
   * 
   * @param listener the listener to register, not null
   */
  void addRuleListener(FunctionBlacklistRuleListener listener);

  /**
   * Removes a previously registered listener.
   * 
   * @param listener the listener to remove, not null
   */
  void removeRuleListener(FunctionBlacklistRuleListener listener);

}
