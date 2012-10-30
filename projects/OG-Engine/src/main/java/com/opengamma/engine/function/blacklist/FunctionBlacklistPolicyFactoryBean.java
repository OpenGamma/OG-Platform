/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy.Entry;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for constructing basic {@link FunctionBlacklistPolicy} instances.
 */
public class FunctionBlacklistPolicyFactoryBean extends SingletonFactoryBean<FunctionBlacklistPolicy> {

  private static final AtomicInteger s_nextName = new AtomicInteger(1);

  private UniqueId _uniqueId;
  private String _name = Integer.toString(s_nextName.getAndIncrement());
  private int _defaultEntryActivationPeriod = 3600;
  private int _wildcard;
  private int _function;
  private int _parameterizedFunction;
  private int _partialNode;
  private int _buildNode;
  private int _executionNode;
  private Collection<Entry> _entries;

  // TODO: also allow this bean to be used to create a policy by loading one from a source

  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  public void setUniqueId(final UniqueId uniqueId) {
    _uniqueId = uniqueId;
  }

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  public int getDefaultEntryActivationPeriod() {
    return _defaultEntryActivationPeriod;
  }

  public void setDefaultEntryActivationPeriod(final int defaultEntryActivationPeriod) {
    _defaultEntryActivationPeriod = defaultEntryActivationPeriod;
  }

  private boolean is(final int entry) {
    return entry != 0;
  }

  private int set(final int entry, final boolean flag) {
    if (flag) {
      if (entry == 0) {
        return -1;
      } else {
        return entry;
      }
    } else {
      return 0;
    }
  }

  public boolean isWildcard() {
    return is(_wildcard);
  }

  public void setWildcard(final boolean wildcard) {
    _wildcard = set(_wildcard, wildcard);
  }

  public int getWildcardActivationPeriod() {
    return _wildcard;
  }

  public void setWildcardActivationPeriod(final int wildcard) {
    _wildcard = wildcard;
  }

  public boolean isFunction() {
    return is(_function);
  }

  public void setFunction(final boolean function) {
    _function = set(_function, function);
  }

  public int getFunctionActivationPeriod() {
    return _function;
  }

  public void setFunctionActivationPeriod(final int function) {
    _function = function;
  }

  public boolean isParameterizedFunction() {
    return is(_parameterizedFunction);
  }

  public void setParameterizedFunction(final boolean parameterizedFunction) {
    _parameterizedFunction = set(_parameterizedFunction, parameterizedFunction);
  }

  public int getParameterizedFunctionActivationPeriod() {
    return _parameterizedFunction;
  }

  public void setParameterizedFunctionActivationPeriod(final int parameterizedFunction) {
    _parameterizedFunction = parameterizedFunction;
  }

  public boolean isPartialNode() {
    return is(_partialNode);
  }

  public void setPartialNode(final boolean partialNode) {
    _partialNode = set(_partialNode, partialNode);
  }

  public int getPartialNodeActivationPeriod() {
    return _partialNode;
  }

  public void setPartialNodeActivationPeriod(final int partialNode) {
    _partialNode = partialNode;
  }

  public boolean isBuildNode() {
    return is(_buildNode);
  }

  public void setBuildNode(final boolean buildNode) {
    _buildNode = set(_buildNode, buildNode);
  }

  public int getBuildNodeActivationPeriod() {
    return _buildNode;
  }

  public void setBuildNodeActivationPeriod(final int buildNode) {
    _buildNode = buildNode;
  }

  public boolean isExecutionNode() {
    return is(_executionNode);
  }

  public void setExecutionNode(final boolean executionNode) {
    _executionNode = set(_executionNode, executionNode);
  }

  public int getExecutionNodeActivationPeriod() {
    return _executionNode;
  }

  public void setExecutionNodeActivationPeriod(final int executionNode) {
    _executionNode = executionNode;
  }

  public Collection<Entry> getEntries() {
    return _entries;
  }

  public void setEntries(final Collection<Entry> entries) {
    _entries = new ArrayList<Entry>(entries);
  }

  private void create(final Collection<Entry> target, Entry entry, final int flag) {
    if (flag == 0) {
      return;
    }
    if (flag > 0) {
      entry = entry.activationPeriod(flag);
    }
    target.add(entry);
  }

  private Collection<Entry> createEntries() {
    final Collection<Entry> entries = new LinkedList<Entry>();
    if (getEntries() != null) {
      entries.addAll(getEntries());
    }
    create(entries, Entry.WILDCARD, getWildcardActivationPeriod());
    create(entries, Entry.FUNCTION, getFunctionActivationPeriod());
    create(entries, Entry.PARAMETERIZED_FUNCTION, getParameterizedFunctionActivationPeriod());
    create(entries, Entry.PARTIAL_NODE, getPartialNodeActivationPeriod());
    create(entries, Entry.BUILD_NODE, getBuildNodeActivationPeriod());
    create(entries, Entry.EXECUTION_NODE, getExecutionNodeActivationPeriod());
    return entries;
  }

  @Override
  protected FunctionBlacklistPolicy createObject() {
    if (getUniqueId() == null) {
      ArgumentChecker.notNullInjected(getName(), "name");
      setUniqueId(UniqueId.of("com.opengamma.engine.function.blacklist", getName()));
    }
    if (getName() == null) {
      setName(getUniqueId().getValue());
    }
    return new DefaultFunctionBlacklistPolicy(getUniqueId(), getName(), getDefaultEntryActivationPeriod(), createEntries());
  }

}
