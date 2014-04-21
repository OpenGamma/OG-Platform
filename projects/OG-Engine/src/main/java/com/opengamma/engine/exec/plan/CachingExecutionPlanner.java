/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
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

  /* package */static final class CacheKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private DependencyGraph _graph;
    private long _functionInitId;
    private Set<ValueSpecification> _sharedValues;
    private Map<ValueSpecification, FunctionParameters> _parameters;

    public CacheKey(final DependencyGraph graph, final long functionInitId, final Set<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
      _graph = graph;
      _functionInitId = functionInitId;
      _sharedValues = new HashSet<ValueSpecification>(sharedValues);
      _parameters = new HashMap<ValueSpecification, FunctionParameters>(parameters);
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CacheKey)) {
        return false;
      }
      final CacheKey other = (CacheKey) o;
      if (_functionInitId != other._functionInitId) {
        return false;
      }
      if (!_sharedValues.equals(other._sharedValues)) {
        return false;
      }
      if (!_parameters.equals(other._parameters)) {
        return false;
      }
      return _graph.equals(other._graph);
    }

    @Override
    public int hashCode() {
      int hc = 0;
      hc += (hc << 4) + (int) (_functionInitId ^ (_functionInitId >>> 32));
      hc += (hc << 4) + _graph.hashCode();
      hc += (hc << 4) + _sharedValues.hashCode();
      hc += (hc << 4) + _parameters.hashCode();
      return hc;
    }

  }

  private final GraphExecutionPlanner _underlying;
  private final Cache _cache;

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
  public GraphExecutionPlan createPlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitId, final Set<ValueSpecification> sharedValues,
      final Map<ValueSpecification, FunctionParameters> parameters) {
    // NOTE: The logModeSource is not used as part of the key; this is wrong as the plan contains job items which embed the logging requirements
    s_logger.debug("Searching for cached execution plan for {}/{}", graph, functionInitId);
    CacheKey key = new CacheKey(graph, functionInitId, sharedValues, parameters);
    final Element element = _cache.get(key);
    if (element != null) {
      s_logger.debug("Cache hit");
      return ((GraphExecutionPlan) element.getObjectValue()).withCalculationConfiguration(graph.getCalculationConfigurationName());
    } else {
      s_logger.debug("Cache miss");
      final GraphExecutionPlan plan = _underlying.createPlan(graph, logModeSource, functionInitId, sharedValues, parameters);
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
