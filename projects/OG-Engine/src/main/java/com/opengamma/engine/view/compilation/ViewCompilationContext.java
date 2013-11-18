/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Holds context relating to the partially-completed compilation of a view definition, for passing to different stages of the compilation.
 */
/* package */class ViewCompilationContext {

  private final ViewDefinition _viewDefinition;
  private final ViewCompilationServices _services;
  private final Collection<DependencyGraphBuilder> _builders;
  private final VersionCorrection _resolverVersionCorrection;
  private final Collection<DependencyGraph> _graphs;
  private final ConcurrentMap<ComputationTargetReference, UniqueId> _activeResolutions;
  private final CompiledFunctionResolver _functions;
  private final Collection<ResolutionRule> _rules;
  private final ComputationTargetResolver.AtVersionCorrection _targetResolver;
  private Set<UniqueId> _expiredResolutions;

  /* package */ViewCompilationContext(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices,
      final Instant valuationTime, final VersionCorrection resolverVersionCorrection, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
    _viewDefinition = viewDefinition;
    _services = compilationServices;
    _builders = new LinkedList<DependencyGraphBuilder>();
    _expiredResolutions = Sets.newSetFromMap(new ConcurrentHashMap<UniqueId, Boolean>());
    _functions = compilationServices.getFunctionResolver().compile(valuationTime);
    _rules = _functions.getAllResolutionRules();
    _targetResolver = TargetResolutionLogger.of(compilationServices.getFunctionCompilationContext().getRawComputationTargetResolver().atVersionCorrection(resolverVersionCorrection), resolutions,
        _expiredResolutions);
    for (final ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      _builders.add(createBuilder(calcConfig));
    }
    _resolverVersionCorrection = resolverVersionCorrection;
    _graphs = new ArrayList<DependencyGraph>(_builders.size());
    _activeResolutions = resolutions;
  }

  public DependencyGraphBuilder createBuilder(final ViewCalculationConfiguration calcConfig) {
    final DependencyGraphBuilder builder = _services.getDependencyGraphBuilder().newInstance();
    builder.setCalculationConfigurationName(calcConfig.getName());
    builder.setMarketDataAvailabilityProvider(_services.getMarketDataAvailabilityProvider());
    final FunctionCompilationContext compilationContext = _services.getFunctionCompilationContext().clone();
    compilationContext.setViewCalculationConfiguration(calcConfig);
    compilationContext.setComputationTargetResolver(_targetResolver);
    final Collection<ResolutionRule> transformedRules = calcConfig.getResolutionRuleTransform().transform(_rules);
    compilationContext.setComputationTargetResults(new ComputationTargetResults(transformedRules));
    final DefaultCompiledFunctionResolver functionResolver = new DefaultCompiledFunctionResolver(compilationContext, transformedRules);
    functionResolver.compileRules();
    builder.setFunctionResolver(functionResolver);
    compilationContext.init();
    builder.setCompilationContext(compilationContext);
    return builder;
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public ViewCompilationServices getServices() {
    return _services;
  }

  public CompiledFunctionResolver getCompiledFunctionResolver() {
    return _functions;
  }

  public Collection<DependencyGraphBuilder> getBuilders() {
    return _builders;
  }

  public Collection<DependencyGraph> getGraphs() {
    return _graphs;
  }

  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  public ConcurrentMap<ComputationTargetReference, UniqueId> getActiveResolutions() {
    return _activeResolutions;
  }

  public boolean hasExpiredResolutions() {
    return !_expiredResolutions.isEmpty();
  }

  public Set<UniqueId> takeExpiredResolutions() {
    final Set<UniqueId> result = _expiredResolutions;
    _expiredResolutions = Sets.newSetFromMap(new ConcurrentHashMap<UniqueId, Boolean>());
    return result;
  }

}
