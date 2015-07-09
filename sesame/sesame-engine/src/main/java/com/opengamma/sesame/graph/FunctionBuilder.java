/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.sesame.config.EngineUtils;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FunctionService;
import com.opengamma.util.ArgumentChecker;

/**
 * Builds function objects from the {@link FunctionModelNode} instances representing them in the function model.
 */
public final class FunctionBuilder implements FunctionIdProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionBuilder.class);

  /** Produces IDs for functions that are unique across the system. */
  private static final AtomicInteger s_functionIds = new AtomicInteger();

  /** Whether identical nodes should share IDs and therefore share cache values. */
  private final boolean _enableCacheSharing;

  /**
   * Map of function identities to function IDs.
   * <p>
   * The keys are the identity hash codes of the function instances and therefore identify a specific
   * function instance. The values are generated function IDs. The idea is that two functions with
   * identical nodes are logically identical and will share an ID. That ID is used in the cache key so
   * identical functions running in different views can share data.
   */
  private final Map<Integer, FunctionId> _functionsToIds = new ConcurrentHashMap<>();

  /**
   * Map of model nodes to function IDs.
   * <p>
   * When a new function is created this map is checked to see if a function has previously been created from an
   * identical node. If it has then the two functions are identical and can share values in the cache.
   * They are allocated the same ID so their cache keys are equal for the same set of arguments.
   */
  private final Map<FunctionModelNode, FunctionId> _nodesToFunctionIds = new HashMap<>();

  /**
   * Creates a new function builder which builds functions that can share cached values between views.
   * <p>
   * For caching to work the service {@link FunctionService#CACHING} must also be included in the set of services
   * when creating the view.
   */
  public FunctionBuilder() {
    _enableCacheSharing = true;
  }

  /**
   * Creates a new function builder.
   *
   * @param enableCacheSharing whether identical nodes should share IDs and therefore share cache values between
   *   different views. For caching to work the service {@link FunctionService#CACHING} must also be
   *   included in the set of services when creating the view.
   */
  public FunctionBuilder(boolean enableCacheSharing) {
    _enableCacheSharing = enableCacheSharing;
  }

  synchronized Object create(FunctionModelNode node, ComponentMap componentMap) {
    checkValid(node);
    // TODO detect cycles in the graph
    // TODO cache this info if it proves expensive to do it over and over for the same classes
    boolean cacheable =
        node instanceof InterfaceNode &&
            (EngineUtils.hasMethodAnnotation(((InterfaceNode) node).getImplementationType(), Cacheable.class) ||
                (EngineUtils.hasMethodAnnotation(((InterfaceNode) node).getType(), Cacheable.class)));

    List<Object> dependencies = Lists.newArrayListWithCapacity(node.getDependencies().size());

    for (FunctionModelNode dependentNode : node.getDependencies()) {
      dependencies.add(create(dependentNode, componentMap));
    }
    Object function = node.create(componentMap, dependencies, this);

    if (cacheable) {
      FunctionId existingFunctionId = _nodesToFunctionIds.get(node);

      if (_enableCacheSharing && existingFunctionId != null) {
        s_logger.debug("Using existing function ID {} for node {}", existingFunctionId, node.prettyPrint(false));
        _functionsToIds.put(System.identityHashCode(function), existingFunctionId);
      } else {
        FunctionId newFunctionId = FunctionId.of(s_functionIds.getAndIncrement());
        s_logger.debug("Creating new function ID {} for node {}", newFunctionId, node.prettyPrint(false));
        _functionsToIds.put(System.identityHashCode(function), newFunctionId);
        _nodesToFunctionIds.put(node, newFunctionId);
      }
    }
    return function;
  }

  private static void checkValid(FunctionModelNode node) {
    if (!node.isValid()) {
      throw new GraphBuildException("Can't build functions from an invalid graph\n" + node.prettyPrint(false) + "\n",
                                    node.getExceptions());
    }
  }

  @Override
  public FunctionId getFunctionId(Object obj) {
    ArgumentChecker.notNull(obj, "obj");
    FunctionId id = _functionsToIds.get(System.identityHashCode(obj));

    if (id == null) {
      throw new IllegalArgumentException("No ID for function " + obj);
    }
    return id;
  }
}
