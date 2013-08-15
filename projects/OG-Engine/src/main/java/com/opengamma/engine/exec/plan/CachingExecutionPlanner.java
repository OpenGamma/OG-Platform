/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Caches the plans produced by other execution planners.
 */
public class CachingExecutionPlanner implements GraphExecutionPlanner {

  // NOTE: This class has been created for completeness, to preserve the previous behaviours of ExecutionPlanCache, even though those behaviours are unlikely to be correct.

  private static final Logger s_logger = LoggerFactory.getLogger(CachingExecutionPlanner.class);

  private static final String CACHE_NAME = "executionPlans";

  /**
   * Tests two dependency nodes for equality. Two nodes are the same if they are for the same parameterized function on the same target, taking the same input values and producing the same output
   * values.
   */
  protected static final class DependencyNodeKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ComputationTargetSpecification _target;
    private final String _functionId;
    private final FunctionParameters _functionParameters;
    private final Set<ValueSpecification> _inputs;
    private final Set<ValueSpecification> _outputs;

    public DependencyNodeKey(final DependencyNode node) {
      _target = node.getComputationTarget();
      _functionId = node.getFunction().getFunction().getFunctionDefinition().getUniqueId();
      _functionParameters = node.getFunction().getParameters();
      _inputs = node.getInputValues();
      _outputs = new HashSet<ValueSpecification>(node.getOutputValues());
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
   * Tests if two dependency graphs are the same. Graphs are the same if they produce the same terminal outputs and contain nodes which apply the same parameterized functions to the same target with
   * the same input values and produce the same output values.
   */
  protected static final class DependencyGraphKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private long _functionInitId;
    private Set<ValueSpecification> _terminals;
    private Map<DependencyNodeKey, DependencyNode> _nodes;

    public DependencyGraphKey(final DependencyGraph graph, final long functionInitId) {
      _functionInitId = functionInitId;
      _terminals = new HashSet<ValueSpecification>(graph.getTerminalOutputSpecifications());
      final Set<DependencyNode> nodes = graph.getDependencyNodes();
      _nodes = Maps.newHashMapWithExpectedSize(nodes.size());
      for (final DependencyNode node : nodes) {
        _nodes.put(new DependencyNodeKey(node), node);
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
      return _nodes.keySet().equals(key._nodes.keySet());
    }

    @Override
    public int hashCode() {
      int hc = 0;
      hc += (hc << 4) + (int) (_functionInitId ^ (_functionInitId >>> 32));
      hc += (hc << 4) + _terminals.hashCode();
      hc += (hc << 4) + _nodes.keySet().hashCode();
      return hc;
    }

    public Map<DependencyNodeKey, DependencyNode> getNodes() {
      return _nodes;
    }

    private void writeObject(final ObjectOutputStream out) throws Exception {
      out.writeLong(_functionInitId);
      out.writeObject(_terminals);
      out.writeInt(_nodes.size());
      for (final DependencyNodeKey key : _nodes.keySet()) {
        out.writeObject(key);
      }
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws Exception {
      _functionInitId = in.readLong();
      _terminals = (Set<ValueSpecification>) in.readObject();
      final int nodes = in.readInt();
      _nodes = Maps.newHashMapWithExpectedSize(nodes);
      for (int i = 0; i < nodes; i++) {
        _nodes.put((DependencyNodeKey) in.readObject(), null);
      }
    }

  }

  private final GraphExecutionPlanner _underlying;
  private final Cache _cache;

  /**
   * Building the "key" object can be costly. If the graph is still in memory, then we can keep a previous key around. The current behavior of view processes and executors is that graphs do not get
   * modified once they are constructed and being used. If this changes then we will have a problem at execution as the older plan will match.
   */
  // TODO: The above comment is wrong. Graph structures do change.
  private final Map<DependencyGraph, DependencyGraphKey> _identityLookup = new MapMaker().weakKeys().makeMap();

  /**
   * Constructs an instance.
   * 
   * @param underlying the underlying execution planner, not null
   * @param manager the cache manager from which to obtain the execution plan cache not null
   */
  public CachingExecutionPlanner(final GraphExecutionPlanner underlying, final CacheManager manager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(manager, "manager");
    _underlying = underlying;
    EHCacheUtils.addCache(manager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(manager, CACHE_NAME);
  }

  public synchronized void invalidate() {
    if (_cache != null) {
      s_logger.info("Clearing execution plan cache of {} items", _cache.getSize());
      _cache.removeAll();
    }
  }

  // GraphExecutionPlanner

  @Override
  public GraphExecutionPlan createPlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitId) {
    // NOTE: The logModeSource is not used as part of the key; this is wrong as the plan contains job items which embed the logging requirements
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
      return ((GraphExecutionPlan) element.getObjectValue()).withCalculationConfiguration(graph.getCalculationConfigurationName());
    } else {
      s_logger.debug("Cache miss");
      final GraphExecutionPlan plan = _underlying.createPlan(graph, logModeSource, functionInitId);
      if (plan != null) {
        _cache.put(new Element(key, plan));
      }
      return plan;
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _cache.getCacheManager().removeCache(CACHE_NAME);
  }

  // TODO [ENG-269] If the function costs change significantly, invalidate the execution plan cache.

}
