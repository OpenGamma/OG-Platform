/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Determines which nodes in a graph have changed. A node has 'changed' if and only if its subtree contains a node for which PreviousLiveDataInput != CurrentLiveDataInput. Note that this excludes
 * changes due to passage of the system clock.
 */
public class LiveDataDeltaCalculator {

  private final DependencyGraph _graph;
  private final ViewComputationCache _cache;
  private final ViewComputationCache _previousCache;
  private final Set<ValueSpecification> _changedSpecifications;

  private final Set<DependencyNode> _changedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _unchangedNodes = new HashSet<DependencyNode>();

  private boolean _done; // = false

  /**
   * For the delta calculation to be meaningful, the caches should be populated with LiveData inputs required to compute the given dependency graph. See {@link DependencyNode#getRequiredLiveData()}
   * and {@link ViewComputationCache#getValue(ValueSpecification)}.
   * 
   * @param graph Dependency graph
   * @param cache Contains CurrentLiveDataInputs (for the given graph)
   * @param previousCache Contains PreviousLiveDataInputs (for the given graph)
   * @param dirtySpecifications Value specifications that are to be considered "changed"
   */
  public LiveDataDeltaCalculator(final DependencyGraph graph, final ViewComputationCache cache, final ViewComputationCache previousCache, final Set<ValueSpecification> dirtySpecifications) {
    ArgumentChecker.notNull(graph, "Graph");
    ArgumentChecker.notNull(cache, "Cache");
    ArgumentChecker.notNull(previousCache, "Previous cache");
    ArgumentChecker.notNull(dirtySpecifications, "dirtySpecifications");
    _graph = graph;
    _cache = cache;
    _previousCache = previousCache;
    _changedSpecifications = dirtySpecifications.isEmpty() ? null : dirtySpecifications;
  }

  public Set<DependencyNode> getChangedNodes() {
    if (!_done) {
      throw new IllegalStateException("Call computeDelta() first");
    }
    return _changedNodes;
  }

  public Set<DependencyNode> getUnchangedNodes() {
    if (!_done) {
      throw new IllegalStateException("Call computeDelta() first");
    }
    return _unchangedNodes;
  }

  public void computeDelta() {
    if (_done) {
      throw new IllegalStateException("Cannot determine delta twice");
    }
    final int count = _graph.getRootCount();
    for (int i = 0; i < count; i++) {
      computeDelta(_graph.getRootNode(i));
    }
    _done = true;
  }

  private boolean computeDelta(final DependencyNode node) {
    if (_changedNodes.contains(node)) {
      return true;
    }
    if (_unchangedNodes.contains(node)) {
      return false;
    }
    boolean hasChanged = false;
    int count = node.getInputCount();
    if (count == 0) {
      if (MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        // This is a graph leaf, but market data changes may affect the function of the node.
        count = node.getOutputCount();
        for (int i = 0; i < count; i++) {
          final ValueSpecification liveData = node.getOutputValue(i);
          // Market data is always in the shared cache
          final Object oldValue = _previousCache.getValue(liveData, CacheSelectHint.allShared());
          final Object newValue = _cache.getValue(liveData, CacheSelectHint.allShared());
          if (!ObjectUtils.equals(oldValue, newValue)) {
            hasChanged = true;
            break;
          }
        }
      }
    } else {
      for (int i = 0; i < count; i++) {
        // if any children changed, this node requires recalculation
        hasChanged |= computeDelta(node.getInputNode(i));
      }
      if (!hasChanged && (_changedSpecifications != null)) {
        count = node.getOutputCount();
        for (int i = 0; i < count; i++) {
          if (_changedSpecifications.contains(node.getOutputValue(i))) {
            hasChanged = true;
            break;
          }
        }
      }
    }
    if (hasChanged) {
      _changedNodes.add(node);
    } else {
      _unchangedNodes.add(node);
    }
    return hasChanged;
  }
}
