
/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.sesame.cache.CacheInvalidator;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.graph.FunctionBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for creating instances of {@link View}.
 * This is one of the key classes of the calculation engine. The {@link #createView} methods take a view configuration
 * and returns a view that is ready to be executed.
 * <p>
 * Each view factory contains a cache which is shared by all views it creates. Each view requests a cache at the
 * start of a calculation cycle and uses it for the duration of the cycle. If {@link #clearCache()} is invoked
 * the cache in the view factory is replaced with a new, empty cache. When each view starts its next calculation
 * cycle it will request a cache and be given the new one. The previous cache is unchanged so any views that
 * are still using it are unaffected.
 */
public class ViewFactory implements CacheMonitor {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewFactory.class);

  private final ExecutorService _executor;
  private final AvailableOutputs _availableOutputs;
  private final AvailableImplementations _availableImplementations;
  private final EnumSet<FunctionService> _defaultServices;
  private final FunctionModelConfig _defaultConfig;
  private final FunctionBuilder _functionBuilder = new FunctionBuilder();

  /**
   * Reference to the current cache. When {@link #clearCache()} is called this reference is updated to point
   * to a new, empty cache. This means the new cache will be provided to views through {@link #_cacheProvider}
   * at the start of their next calculation cycle but any views using the existing cache wil be unaffected.
   */
  private final AtomicReference<Cache<MethodInvocationKey, Object>> _cacheRef;

  /**
   * Provides a cache to views. Views request a cache at the start of each calculation cycle and use it for
   * the duration of that cycle. This allows the cache in the view factory to change without any effect
   * on running views.
   */
  private final CacheProvider _cacheProvider = new CacheProvider() {
    @Override
    public Cache<MethodInvocationKey, Object> get() {
      return _cacheRef.get();
    }
  };

  /** For building new caches. A new cache is created whenever data in the existing cache becomes invalid. */
  private final CacheBuilder<Object, Object> _cacheBuilder;
  private final Optional<MetricRegistry> _metricRegistry;
  private final ComponentMap _componentMap;
  private final CacheInvalidator _cacheInvalidator;

  public ViewFactory(ExecutorService executor,
                     ComponentMap componentMap,
                     AvailableOutputs availableOutputs,
                     AvailableImplementations availableImplementations,
                     FunctionModelConfig defaultConfig,
                     EnumSet<FunctionService> defaultServices,
                     CacheBuilder<Object, Object> cacheBuilder,
                     CacheInvalidator cacheInvalidator,
                     Optional<MetricRegistry> metricRegistry) {
    _availableOutputs = ArgumentChecker.notNull(availableOutputs, "availableOutputs");
    _availableImplementations = ArgumentChecker.notNull(availableImplementations, "availableImplementations");
    _defaultServices = ArgumentChecker.notNull(defaultServices, "defaultServices");
    _defaultConfig = ArgumentChecker.notNull(defaultConfig, "defaultConfig");
    _executor = ArgumentChecker.notNull(executor, "executor");
    _cacheBuilder = ArgumentChecker.notNull(cacheBuilder, "cacheBuilder");
    _componentMap = ArgumentChecker.notNull(componentMap, "componentMap");
    _cacheInvalidator = ArgumentChecker.notNull(cacheInvalidator, "cacheInvalidator");
    // create an initial empty cache
    _cacheRef = new AtomicReference<>(_cacheBuilder.<MethodInvocationKey, Object>build());
    _metricRegistry = ArgumentChecker.notNull(metricRegistry, "metricRegistry");
  }

  /**
   * Currently the inputs must be instances of {@link PositionOrTrade} or {@link Security}.
   * This will be relaxed in future.
   * 
   * @param viewConfig  the configuration to use, not null
   * @param inputTypes  the types of the inputs to the calculations, e.g. trades, positions, securities
   * @return the view, not null
   */
  public View createView(ViewConfig viewConfig, Set<Class<?>> inputTypes) {
    return createView(viewConfig, _defaultServices, inputTypes);
  }

  /**
   * Currently the inputs must be instances of {@link PositionOrTrade} or {@link Security}.
   * This will be relaxed in future.
   *
   * @param viewConfig  the configuration to use, not null
   * @param inputTypes  the types of the inputs to the calculations, e.g. trades, positions, securities
   * @return the view, not null
   */
  public View createView(ViewConfig viewConfig, Class<?>... inputTypes) {
    return createView(viewConfig, _defaultServices, inputTypes);
  }

  /**
   * Currently the inputs must be instances of {@link PositionOrTrade} or {@link Security}.
   * This will be relaxed in future.
   * 
   * @param viewConfig  the configuration to use, not null
   * @param services  the services to run, not null
   * @param inputTypes  the types of the inputs to the calculations, e.g. trades, positions, securities
   * @return the view, not null
   */
  public View createView(ViewConfig viewConfig, EnumSet<FunctionService> services, Class<?>... inputTypes) {
    return createView(viewConfig, services, ImmutableSet.copyOf(inputTypes));
  }

  /**
   * Currently the inputs must be instances of {@link PositionOrTrade} or {@link Security}.
   * This will be relaxed in future.
   *
   * @param viewConfig  the configuration to use, not null
   * @param services  the services to run, not null
   * @param inputTypes  the types of the inputs to the calculations, e.g. trades, positions, securities
   * @return the view, not null
   */
  public View createView(ViewConfig viewConfig, EnumSet<FunctionService> services, Set<Class<?>> inputTypes) {
    return new View(viewConfig, _executor, _defaultConfig, _functionBuilder, services, _componentMap, inputTypes,
                    _availableOutputs, _availableImplementations, _cacheProvider, _cacheInvalidator, _metricRegistry);
  }

  /**
   * Clears all entries from the cache.
   * <p>
   * This doesn't affect the caches of any running views, it simply replaces the current cache with an empty one
   * so when each view starts its next cycle it gets the new cache.
   */
  public void clearCache() {
    s_logger.info("Clearing cache");
    _cacheRef.set(_cacheBuilder.<MethodInvocationKey, Object>build());
  }
}
