/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * Holds context relating to the partially-completed compilation of a view definition, for passing to different stages of the compilation.
 */
public class ViewCompilationContext {

  private final ViewDefinition _viewDefinition;
  private final ViewCompilationServices _services;
  private final Collection<DependencyGraphBuilder> _builders;
  private final VersionCorrection _resolverVersionCorrection;
  private final Collection<DependencyGraph> _graphs;

  /* package */ViewCompilationContext(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices,
      final Instant valuationTime, final VersionCorrection resolverVersionCorrection) {
    _viewDefinition = viewDefinition;
    _services = compilationServices;
    _builders = new LinkedList<DependencyGraphBuilder>();
    final Collection<ResolutionRule> rules = compilationServices.getFunctionResolver().compile(valuationTime).getAllResolutionRules();
    final ComputationTargetResolver.AtVersionCorrection resolver = compilationServices.getFunctionCompilationContext().getRawComputationTargetResolver().atVersionCorrection(resolverVersionCorrection);
    for (final String configName : viewDefinition.getAllCalculationConfigurationNames()) {
      final DependencyGraphBuilder builder = compilationServices.getDependencyGraphBuilder().newInstance();
      builder.setCalculationConfigurationName(configName);
      builder.setMarketDataAvailabilityProvider(compilationServices.getMarketDataAvailabilityProvider());
      final FunctionCompilationContext compilationContext = compilationServices.getFunctionCompilationContext().clone();
      final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(configName);
      compilationContext.setViewCalculationConfiguration(calcConfig);
      compilationContext.setComputationTargetResolver(resolver);
      final Collection<ResolutionRule> transformedRules = calcConfig.getResolutionRuleTransform().transform(rules);
      compilationContext.setComputationTargetResults(new ComputationTargetResults(transformedRules));
      final DefaultCompiledFunctionResolver functionResolver = new DefaultCompiledFunctionResolver(compilationContext, transformedRules);
      functionResolver.compileRules();
      builder.setFunctionResolver(functionResolver);
      compilationContext.init();
      builder.setCompilationContext(compilationContext);
      _builders.add(builder);
    }
    _resolverVersionCorrection = resolverVersionCorrection;
    _graphs = new ArrayList<DependencyGraph>(_builders.size());
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public ViewCompilationServices getServices() {
    return _services;
  }

  public Collection<DependencyGraphBuilder> getBuilders() {
    return _builders;
  }

  public Collection<DependencyGraph> getGraphs() {
    return _graphs;
  }

  public void addGraph(final DependencyGraph graph) {
    _graphs.add(graph);
  }

  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

}
