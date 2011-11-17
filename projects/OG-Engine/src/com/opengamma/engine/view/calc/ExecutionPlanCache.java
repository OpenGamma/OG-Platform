/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Caches meta data taken from a graph fragment graph sufficient to construct a fragment
 * graph quickly for a recently processed graph.
 */
/* package */class ExecutionPlanCache {

  private static final Logger s_logger = LoggerFactory.getLogger(ExecutionPlanCache.class);

  private static final String CACHE_NAME = "executionPlans";

  /**
   * Tests two dependency nodes for equality. Two nodes are the same if they
   * are for the same parameterized function on the same target, taking the same input
   * values and producing the same output values.
   */
  protected static final class DependencyNodeKey {

    private final ComputationTargetSpecification _target;
    private final String _functionId;
    private final FunctionParameters _functionParameters;
    private final Set<ValueSpecification> _inputs;
    private final Set<ValueSpecification> _outputs;

    public DependencyNodeKey(final DependencyNode node) {
      _target = node.getComputationTarget().toSpecification();
      _functionId = node.getFunction().getFunction().getFunctionDefinition().getUniqueId();
      _functionParameters = node.getFunction().getParameters();
      _inputs = node.getInputValues();
      _outputs = node.getOutputValues();
    }

    @Override
    public int hashCode() {
      int hc = _target.hashCode();
      hc += (hc << 4) + _functionId.hashCode();
      hc += (hc << 4) + _functionParameters.hashCode();
      hc += (hc << 4) + _inputs.hashCode();
      hc += (hc << 4) + _outputs.hashCode();
      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof DependencyNodeKey)) {
        return false;
      }
      final DependencyNodeKey other = (DependencyNodeKey) o;
      return _target.equals(other._target)
          && _functionId.equals(other._functionId)
          && _functionParameters.equals(other._functionParameters)
          && _inputs.equals(other._inputs)
          && _outputs.equals(other._outputs);
    }

  }

  /**
   * Tests if two dependency graphs are the same. Graphs are the same if they produce the
   * same terminal outputs and contain nodes which apply the same parameterized functions
   * to the same target with the same input values and produce the same output values.  
   */
  protected static final class DependencyGraphKey {

    private final long _functionInitId;
    private final Set<ValueSpecification> _terminals;
    private final Set<DependencyNodeKey> _nodes;

    public DependencyGraphKey(final DependencyGraph graph, final long functionInitId) {
      _functionInitId = functionInitId;
      _terminals = new HashSet<ValueSpecification>(graph.getTerminalOutputSpecifications());
      final Set<DependencyNode> nodes = graph.getDependencyNodes();
      _nodes = Sets.newHashSetWithExpectedSize(nodes.size());
      for (DependencyNode node : nodes) {
        _nodes.add(new DependencyNodeKey(node));
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof DependencyGraphKey)) {
        return false;
      }
      final DependencyGraphKey key = (DependencyGraphKey) o;
      if (_functionInitId != key._functionInitId) {
        return false;
      }
      if (!_terminals.equals(key._terminals)) {
        return false;
      }
      return _nodes.equals(key._nodes);
    }

    @Override
    public int hashCode() {
      int hc = 0;
      hc += (hc << 4) + (int) (_functionInitId ^ (_functionInitId >>> 32));
      hc += (hc << 4) + _terminals.hashCode();
      hc += (hc << 4) + _nodes.hashCode();
      return hc;
    }

  }

  private final Cache _cache;

  /**
   * Building the "key" object can be costly. If the graph is still in memory, then we can keep a previous key
   * around. The current behavior of view processes and executors is that graphs do not get modified once they
   * are constructed and being used. If this changes then we will have a problem at execution as the older plan
   * will match.
   */
  private final Map<DependencyGraph, DependencyGraphKey> _identityLookup = new MapMaker().weakKeys().makeMap();

  public ExecutionPlanCache(final CacheManager manager, final int cacheSize) {
    if (cacheSize > 0) {
      EHCacheUtils.addCache(manager, CACHE_NAME, cacheSize, MemoryStoreEvictionPolicy.LRU, false, null, true, 1800, 300, false, 0, null);
      _cache = EHCacheUtils.getCacheFromManager(manager, CACHE_NAME);
    } else {
      _cache = null;
    }
  }

  public synchronized void clear() {
    if (_cache != null) {
      s_logger.info("Clearing execution plan cache of {} items", _cache.getSize());
      _cache.removeAll();
    }
  }

  public ExecutionPlan getCachedPlan(final DependencyGraph graph, final long functionInitId) {
    if (_cache != null) {
      s_logger.debug("Searching for cached execution plan for {}/{}", graph, functionInitId);
      DependencyGraphKey key = _identityLookup.get(graph);
      if ((key == null) || (key._functionInitId != functionInitId)) {
        s_logger.debug("Identity lookup miss");
        key = new DependencyGraphKey(graph, functionInitId);
        _identityLookup.put(graph, key);
      }
      final Element element = _cache.get(key);
      if (element != null) {
        s_logger.debug("Cache hit");
        return (ExecutionPlan) element.getObjectValue();
      } else {
        s_logger.debug("Cache miss");
        return null;
      }
    } else {
      return null;
    }
  }

  public void cachePlan(final DependencyGraph graph, final long functionInitId, final ExecutionPlan plan) {
    if (_cache != null) {
      s_logger.info("Caching execution plan for {}/{}", graph, functionInitId);
      DependencyGraphKey key = _identityLookup.get(graph);
      if ((key == null) || (key._functionInitId != functionInitId)) {
        s_logger.debug("Identity lookup miss");
        key = new DependencyGraphKey(graph, functionInitId);
        _identityLookup.put(graph, key);
      }
      _cache.put(new Element(key, plan));
    }
  }

  // TODO [ENG-269] If the function costs change significantly, invalidate the execution plan cache.

}
