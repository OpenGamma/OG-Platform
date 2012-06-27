/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.tuple.Pair;

/**
 * Provides remote access to a {@link FunctionBlacklist}.
 */
public class RemoteFunctionBlacklist extends AbstractFunctionBlacklist {

  private final class Listener extends BaseFunctionBlacklistRuleListener {

    @Override
    protected Pair<Integer, ? extends Collection<FunctionBlacklistRule>> getUnderlyingRules(final int modificationCount) {
      final FudgeMsg msg = getProvider().refresh(getName(), modificationCount);
      if (msg.isEmpty()) {
        return null;
      }
      final FudgeDeserializer fdc = new FudgeDeserializer(getProvider().getFudgeContext());
      return Pair.of(msg.getInt(DataFunctionBlacklistResource.MODIFICATION_COUNT_FIELD), getRules(fdc, msg.getMessage(DataFunctionBlacklistResource.RULES_FIELD)));
    }

    @Override
    protected synchronized void replaceRules(final Collection<FunctionBlacklistRule> rules) {
      final List<FunctionBlacklistRule> newRules = new ArrayList<FunctionBlacklistRule>(rules.size());
      final Set<FunctionBlacklistRule> oldRules = new HashSet<FunctionBlacklistRule>(_rules);
      for (FunctionBlacklistRule rule : rules) {
        if (_rules.contains(rule)) {
          oldRules.remove(rule);
          continue;
        }
        newRules.add(rule);
      }
      addRules(newRules);
      removeRules(oldRules);
    }

    @Override
    protected synchronized void addRule(final FunctionBlacklistRule rule) {
      _rules.add(rule);
      notifyAddRule(rule);
    }

    @Override
    protected synchronized void addRules(final Collection<FunctionBlacklistRule> rules) {
      _rules.addAll(rules);
      notifyAddRules(rules);
    }

    @Override
    protected synchronized void removeRule(final FunctionBlacklistRule rule) {
      _rules.remove(rule);
      notifyRemoveRule(rule);
    }

    @Override
    protected synchronized void removeRules(final Collection<FunctionBlacklistRule> rules) {
      _rules.removeAll(rules);
      notifyRemoveRules(rules);
    }

  }

  private final RemoteFunctionBlacklistProvider _provider;
  private final Set<FunctionBlacklistRule> _rules = new HashSet<FunctionBlacklistRule>();
  private final Listener _listener = new Listener();

  private static Collection<FunctionBlacklistRule> getRules(final FudgeDeserializer fdc, final FudgeMsg rulesField) {
    final List<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(rulesField.getNumFields());
    for (FudgeField rule : rulesField) {
      rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, rule));
    }
    return rules;
  }

  public RemoteFunctionBlacklist(final FudgeDeserializer fdc, FudgeMsg info, final RemoteFunctionBlacklistProvider provider) {
    super(info.getString(DataFunctionBlacklistResource.NAME_FIELD), provider.getBackgroundTasks());
    _provider = provider;
    _listener.init(info.getInt(DataFunctionBlacklistResource.MODIFICATION_COUNT_FIELD), getRules(fdc, info.getMessage(DataFunctionBlacklistResource.RULES_FIELD)));
    // TODO: subscribe to JMS topics to receive updates to the rules in this blacklist & call methods on the listener to handle them
    _listener.refresh();
  }

  protected RemoteFunctionBlacklistProvider getProvider() {
    return _provider;
  }

  @Override
  public Set<FunctionBlacklistRule> getRules() {
    synchronized (_listener) {
      return new HashSet<FunctionBlacklistRule>(_rules);
    }
  }

}
