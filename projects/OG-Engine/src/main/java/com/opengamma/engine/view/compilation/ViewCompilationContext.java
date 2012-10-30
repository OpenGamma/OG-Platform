/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.time.Instant;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * Holds context relating to the partially-completed compilation of a view definition, for passing to different stages
 * of the compilation.
 */
public class ViewCompilationContext {

  private final ViewDefinition _viewDefinition;
  private final ViewCompilationServices _services;
  private final Map<String, DependencyGraphBuilder> _configurationGraphs;
  private final VersionCorrection _resolverVersionCorrection;

  /* package */ViewCompilationContext(ViewDefinition viewDefinition, ViewCompilationServices compilationServices, Instant valuationTime, VersionCorrection resolverVersionCorrection) {
    _viewDefinition = viewDefinition;
    _services = compilationServices;
    final Map<String, DependencyGraphBuilder> configurationGraphs = new HashMap<String, DependencyGraphBuilder>();
    final Collection<ResolutionRule> rules = compilationServices.getFunctionResolver().compile(valuationTime).getAllResolutionRules();
    final ComputationTargetResolver.AtVersionCorrection resolver = compilationServices.getFunctionCompilationContext().getRawComputationTargetResolver().atVersionCorrection(resolverVersionCorrection);
    for (String configName : viewDefinition.getAllCalculationConfigurationNames()) {
      final DependencyGraphBuilder builder = compilationServices.getDependencyGraphBuilder().newInstance();
      builder.setCalculationConfigurationName(configName);
      builder.setMarketDataAvailabilityProvider(compilationServices.getMarketDataAvailabilityProvider());
      final FunctionCompilationContext compilationContext = compilationServices.getFunctionCompilationContext().clone();
      final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(configName);
      compilationContext.setViewCalculationConfiguration(calcConfig);
      compilationContext.setComputationTargetResolver(resolver);
      final Collection<ResolutionRule> transformedRules = calcConfig.getResolutionRuleTransform().transform(rules);
      compilationContext.setComputationTargetResults(new ComputationTargetResults(transformedRules, compilationContext));
      final DefaultCompiledFunctionResolver functionResolver = new DefaultCompiledFunctionResolver(compilationContext, transformedRules);
      functionResolver.compileRules();
      builder.setFunctionResolver(functionResolver);
      builder.setCompilationContext(compilationContext);
      configurationGraphs.put(configName, builder);
    }
    _configurationGraphs = configurationGraphs;
    _resolverVersionCorrection = resolverVersionCorrection;
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public ViewCompilationServices getServices() {
    return _services;
  }

  public DependencyGraphBuilder getBuilder(final String calcConfig) {
    return _configurationGraphs.get(calcConfig);
  }

  public Collection<DependencyGraphBuilder> getBuilders() {
    return Collections.unmodifiableCollection(_configurationGraphs.values());
  }

  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

}
