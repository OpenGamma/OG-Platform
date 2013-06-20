/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy.Entry;
import com.opengamma.util.ArgumentChecker;

/**
 * Default application of a {@link FunctionBlacklistPolicy} to a {@link ManageableFunctionBlacklist} interface.
 */
public class DefaultFunctionBlacklistMaintainer implements FunctionBlacklistMaintainer {

  private final FunctionBlacklistPolicy _policy;
  private final ManageableFunctionBlacklist _update;

  public DefaultFunctionBlacklistMaintainer(final FunctionBlacklistPolicy policy, final ManageableFunctionBlacklist update) {
    ArgumentChecker.notNull(policy, "policy");
    ArgumentChecker.notNull(update, "update");
    _policy = policy;
    _update = update;
  }

  protected FunctionBlacklistPolicy getPolicy() {
    return _policy;
  }

  protected ManageableFunctionBlacklist getUpdate() {
    return _update;
  }

  protected FunctionBlacklistRule createRule(final Entry entry, final CalculationJobItem item) {
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    if (entry.isMatchFunctionIdentifier()) {
      rule.setFunctionIdentifier(item.getFunctionUniqueIdentifier());
    }
    if (entry.isMatchFunctionParameters()) {
      rule.setFunctionParameters(item.getFunctionParameters());
    }
    if (entry.isMatchTarget()) {
      rule.setTarget(item.getComputationTargetSpecification());
    }
    if (entry.isMatchInputs()) {
      rule.setInputs(Arrays.asList(item.getInputs()));
    }
    if (entry.isMatchOutputs()) {
      rule.setOutputs(Arrays.asList(item.getOutputs()));
    }
    return rule;
  }

  @Override
  public void failedJobItem(final CalculationJobItem item) {
    final Collection<Entry> entries = getPolicy().getEntries();
    for (Entry entry : entries) {
      getUpdate().addBlacklistRule(createRule(entry, item), entry.getActivationPeriod(getPolicy()));
    }
  }

  @Override
  public void failedJobItems(final Collection<CalculationJobItem> items) {
    final Collection<Entry> entries = getPolicy().getEntries();
    final ArrayList<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(items.size());
    for (Entry entry : entries) {
      rules.clear();
      for (CalculationJobItem item : items) {
        rules.add(createRule(entry, item));
      }
      getUpdate().addBlacklistRules(rules, entry.getActivationPeriod(getPolicy()));
    }
  }

}
