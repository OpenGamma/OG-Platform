/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Determines which nodes in a graph have changed. A node has 'changed' if and only 
 * if its subtree contains a node for which PreviousLiveDataInput != CurrentLiveDataInput.
 * Note that this excludes changes due to passage of the system clock. 
 */
public class LiveDataDeltaCalculator {

  private final DependencyGraph _graph;
  private final ViewComputationCache _cache;
  private final ViewComputationCache _previousCache;

  private final Set<DependencyNode> _changedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _unchangedNodes = new HashSet<DependencyNode>();

  private boolean _done; // = false

  /**
   * For the delta calculation to be meaningful, the caches should be populated with LiveData 
   * inputs required to compute the given dependency graph. 
   * See {@link DependencyNode#getRequiredLiveData()}
   * and {@link ViewComputationCache#getValue(ValueSpecification)}.
   * 
   * @param graph Dependency graph
   * @param cache Contains CurrentLiveDataInputs (for the given graph)
   * @param previousCache Contains PreviousLiveDataInputs (for the given graph)
   */
  public LiveDataDeltaCalculator(DependencyGraph graph, ViewComputationCache cache, ViewComputationCache previousCache) {
    ArgumentChecker.notNull(graph, "Graph");
    ArgumentChecker.notNull(cache, "Cache");
    ArgumentChecker.notNull(previousCache, "Previous cache");
    _graph = graph;
    _cache = cache;
    _previousCache = previousCache;
  }

  public Set<DependencyNode> getChangedNodes() {
    if (!_done) {
      throw new IllegalStateException("Call computeDelta() first");
    }

    return Collections.unmodifiableSet(_changedNodes);
  }

  public Set<DependencyNode> getUnchangedNodes() {
    if (!_done) {
      throw new IllegalStateException("Call computeDelta() first");
    }

    return Collections.unmodifiableSet(_unchangedNodes);
  }

  public void computeDelta() {
    if (_done) {
      throw new IllegalStateException("Cannot determine delta twice");
    }

    for (DependencyNode rootNode : _graph.getRootNodes()) {
      computeDelta(rootNode);
    }

    _done = true;
  }

  private boolean computeDelta(DependencyNode node) {
    if (_changedNodes.contains(node)) {
      return true;
    }
    if (_unchangedNodes.contains(node)) {
      return false;
    }

    boolean hasChanged = false;
    for (DependencyNode inputNode : node.getInputNodes()) {
      // if any children changed, this node automatically requires recomputation.
      hasChanged |= computeDelta(inputNode);
    }

    if (!hasChanged) {
      // if no children changed, the node may still require recomputation
      // due to market data changes affecting the function of the node.
      Pair<ValueRequirement, ValueSpecification> liveData = node.getRequiredMarketData();
      if (liveData != null) {
        // Market data is always in the shared cache
        Object oldValue = _previousCache.getValue(liveData.getSecond(), CacheSelectHint.allShared());
        Object newValue = _cache.getValue(liveData.getSecond(), CacheSelectHint.allShared());
        if (!ObjectUtils.equals(oldValue, newValue)) {
          hasChanged = true;
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
