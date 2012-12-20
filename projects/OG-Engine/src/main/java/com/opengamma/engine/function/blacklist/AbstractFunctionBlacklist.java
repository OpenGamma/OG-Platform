/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link FunctionBlacklist}.
 */
public abstract class AbstractFunctionBlacklist implements FunctionBlacklist {

  private final String _name;
  private final Set<FunctionBlacklistRuleListener> _listeners = Collections.newSetFromMap(new ConcurrentHashMap<FunctionBlacklistRuleListener, Boolean>());
  private final AtomicInteger _modificationCount = new AtomicInteger();
  private final ExecutorService _executorService;

  /**
   * Creates a new blacklist.
   * 
   * @param name the name of the blacklist, not null
   * @param executorService the executor service to use for deferred listener actions, not null
   */
  public AbstractFunctionBlacklist(final String name, final ExecutorService executorService) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(executorService, "executorService");
    _name = name;
    _executorService = executorService;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getModificationCount() {
    return _modificationCount.get();
  }

  private Set<FunctionBlacklistRuleListener> getListeners() {
    return _listeners;
  }

  private ExecutorService getExecutorService() {
    return _executorService;
  }

  /**
   * Increments the modification count and notifies any listeners of the change. This should be called after the data structure has been updated.
   * 
   * @param rule the new rule
   */
  protected void notifyAddRule(final FunctionBlacklistRule rule) {
    final int mc = _modificationCount.incrementAndGet();
    for (FunctionBlacklistRuleListener listener : getListeners()) {
      listener.ruleAdded(mc, rule, getExecutorService());
    }
  }

  /**
   * Increments the modification count and notifies any listeners of the change. This should be called after the data structure has been updated.
   * 
   * @param rules the new rules
   */
  protected void notifyAddRules(final Collection<FunctionBlacklistRule> rules) {
    final int mc = _modificationCount.incrementAndGet();
    for (FunctionBlacklistRuleListener listener : getListeners()) {
      listener.rulesAdded(mc, rules, getExecutorService());
    }
  }

  /**
   * Increments the modification count and notifies any listeners of the change. This should be called after the data structure has been updated.
   * 
   * @param rule the removed rule
   */
  protected void notifyRemoveRule(final FunctionBlacklistRule rule) {
    final int mc = _modificationCount.incrementAndGet();
    for (FunctionBlacklistRuleListener listener : getListeners()) {
      listener.ruleRemoved(mc, rule, getExecutorService());
    }
  }

  /**
   * Increments the modification count and notifies any listeners of the change. This should be called after the data structure has been updated.
   * 
   * @param rules the removed rules
   */
  protected void notifyRemoveRules(final Collection<FunctionBlacklistRule> rules) {
    final int mc = _modificationCount.incrementAndGet();
    for (FunctionBlacklistRuleListener listener : getListeners()) {
      listener.rulesRemoved(mc, rules, getExecutorService());
    }
  }

  @Override
  public void addRuleListener(final FunctionBlacklistRuleListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeRuleListener(final FunctionBlacklistRuleListener listener) {
    _listeners.remove(listener);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getName() + "]";
  }

}
