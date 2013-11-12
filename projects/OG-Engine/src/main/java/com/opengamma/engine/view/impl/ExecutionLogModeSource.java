/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.util.tuple.Pair;

/**
 * Represents a queryable source of the execution log mode to apply for a value specification.
 */
public class ExecutionLogModeSource {

  private final ReentrantLock _lock = new ReentrantLock();
  private final Map<Pair<String, ValueSpecification>, Integer> _elevatedLogTargets = new HashMap<Pair<String, ValueSpecification>, Integer>();
  private final Map<String, Map<ValueSpecification, Integer>> _elevatedLogNodes = new ConcurrentHashMap<String, Map<ValueSpecification, Integer>>();
  private final Map<ValueSpecification, Integer> _elevatedLogSpecs = new ConcurrentHashMap<ValueSpecification, Integer>();
  private CompiledViewDefinitionWithGraphs _compiledViewDefinition;

  /**
   * Ensures at least a minimum level of logging output is present in the results for the given value specifications. Changes will take effect from the next computation cycle.
   * <p>
   * Each call to elevate the minimum level of logging output for a result must be paired with exactly one call to reduce the level of logging output, if required.
   * 
   * @param minimumLogMode the minimum log mode to ensure, not null
   * @param targets the targets affected, not null or empty
   */
  public void setMinimumLogMode(ExecutionLogMode minimumLogMode, Set<Pair<String, ValueSpecification>> targets) {
    // Synchronization ensures only one writer, while getLogMode is allowed to read from the ConcurrentHashMap
    // without further locking.
    switch (minimumLogMode) {
      case INDICATORS:
        _lock.lock();
        try {
          for (Pair<String, ValueSpecification> target : targets) {
            if (decrementRefCount(target, _elevatedLogTargets)) {
              removeElevatedNode(target);
            }
          }
        } finally {
          _lock.unlock();
        }
        break;
      case FULL:
        _lock.lock();
        try {
          for (Pair<String, ValueSpecification> target : targets) {
            if (incrementRefCount(target, _elevatedLogTargets)) {
              addElevatedNode(target);
            }
          }
        } finally {
          _lock.unlock();
        }
        break;
    }
  }

  /**
   * Gets the log mode for a dependency node.
   * 
   * @param calcConfig the calculation configuration, not null
   * @param output an output from the node, not null
   * @return the log mode, not null
   */
  public ExecutionLogMode getLogMode(final String calcConfig, final ValueSpecification output) {
    Map<ValueSpecification, Integer> modesByConfig = _elevatedLogNodes.get(calcConfig);
    if (modesByConfig == null) {
      return ExecutionLogMode.INDICATORS;
    }
    return modesByConfig.containsKey(output) ? ExecutionLogMode.FULL : ExecutionLogMode.INDICATORS;
  }

  //-------------------------------------------------------------------------
  /*package*/void viewDefinitionCompiled(CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    _lock.lock();
    try {
      _compiledViewDefinition = compiledViewDefinition;
      rebuildNodeLogModes();
    } finally {
      _lock.unlock();
    }
  }

  private void addElevatedNode(Pair<String, ValueSpecification> target) {
    // Must be called while holding the lock
    incrementNodeRefCount(getNodeProducing(target), getOrCreateModes(target.getFirst()));
  }

  private void incrementNodeRefCount(DependencyNode node, Map<ValueSpecification, Integer> refCounts) {
    if (node == null) {
      return;
    }
    int count = node.getOutputCount();
    for (int i = 0; i < count; i++) {
      incrementRefCount(node.getOutputValue(i), refCounts);
    }
    count = node.getInputCount();
    for (int i = 0; i < count; i++) {
      incrementNodeRefCount(node.getInputNode(i), refCounts);
    }
  }

  private void removeElevatedNode(Pair<String, ValueSpecification> target) {
    // Must be called while holding the lock
    decrementNodeRefCount(getNodeProducing(target), getOrCreateModes(target.getFirst()));
  }

  private void decrementNodeRefCount(DependencyNode node, Map<ValueSpecification, Integer> refCounts) {
    if (node == null) {
      return;
    }
    int count = node.getOutputCount();
    for (int i = 0; i < count; i++) {
      decrementRefCount(node.getOutputValue(i), refCounts);
    }
    count = node.getInputCount();
    for (int i = 0; i < count; i++) {
      decrementNodeRefCount(node.getInputNode(i), refCounts);
    }
  }

  private DependencyNode getNodeProducing(Pair<String, ValueSpecification> target) {
    if (_compiledViewDefinition == null) {
      return null;
    }
    String calcConfigName = target.getFirst();
    ValueSpecification valueSpec = target.getSecond();
    final DependencyGraphExplorer explorer;
    try {
      explorer = _compiledViewDefinition.getDependencyGraphExplorer(calcConfigName);
    } catch (Exception e) {
      // No graph available
      return null;
    }
    return explorer.getNodeProducing(valueSpec);
  }

  private void rebuildNodeLogModes() {
    // Must be called while holding the lock
    _elevatedLogNodes.clear();
    for (Pair<String, ValueSpecification> target : _elevatedLogTargets.keySet()) {
      addElevatedNode(target);
    }
  }

  private Map<ValueSpecification, Integer> getOrCreateModes(final String calcConfig) {
    Map<ValueSpecification, Integer> modesByConfig = _elevatedLogNodes.get(calcConfig);
    if (modesByConfig == null) {
      modesByConfig = new ConcurrentHashMap<ValueSpecification, Integer>();
      _elevatedLogNodes.put(calcConfig, modesByConfig);
    }
    return modesByConfig;
  }

  private static <T> boolean incrementRefCount(T key, Map<T, Integer> refMap) {
    Integer refCount = refMap.get(key);
    if (refCount == null) {
      refMap.put(key, 1);
      return true;
    } else {
      refMap.put(key, refCount + 1);
      return false;
    }
  }

  private static <T> boolean decrementRefCount(T key, Map<T, Integer> refMap) {
    Integer value = refMap.get(key);
    if (value == null) {
      return false;
    }
    if (value == 1) {
      refMap.remove(key);
      return true;
    } else {
      refMap.put(key, value - 1);
      return false;
    }
  }

}
