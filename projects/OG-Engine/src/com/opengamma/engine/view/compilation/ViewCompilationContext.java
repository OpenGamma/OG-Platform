/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.tuple.Pair;

/**
 * Holds context relating to the partially-completed compilation of a view definition, for passing to different stages
 * of the compilation.
 */
public class ViewCompilationContext {

  private final ViewDefinition _viewDefinition;
  private final ViewCompilationServices _services;
  private final Map<String, Pair<DependencyGraphBuilder, Set<ValueRequirement>>> _configurations;

  @SuppressWarnings("unchecked")
  /* package */ViewCompilationContext(ViewDefinition viewDefinition, ViewCompilationServices compilationServices, Instant valuationTime) {
    _viewDefinition = viewDefinition;
    _services = compilationServices;
    final Map<String, Pair<DependencyGraphBuilder, Set<ValueRequirement>>> configurations = new HashMap<String, Pair<DependencyGraphBuilder, Set<ValueRequirement>>>();
    final Collection<ResolutionRule> rules = compilationServices.getFunctionResolver().compile(valuationTime).getAllResolutionRules();
    for (String configName : viewDefinition.getAllCalculationConfigurationNames()) {
      final DependencyGraphBuilder builder = compilationServices.getDependencyGraphBuilder().newInstance();
      builder.setCalculationConfigurationName(configName);
      builder.setMarketDataAvailabilityProvider(compilationServices.getMarketDataAvailabilityProvider());
      builder.setTargetResolver(compilationServices.getComputationTargetResolver());
      final FunctionCompilationContext compilationContext = compilationServices.getFunctionCompilationContext().clone();
      final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(configName);
      compilationContext.setViewCalculationConfiguration(calcConfig);
      final Collection<ResolutionRule> transformedRules = calcConfig.getResolutionRuleTransform().transform(rules);
      compilationContext.setComputationTargetResults(new ComputationTargetResults(transformedRules, compilationContext, compilationServices.getComputationTargetResolver()));
      builder.setFunctionResolver(new DefaultCompiledFunctionResolver(compilationContext, transformedRules));
      builder.setCompilationContext(compilationContext);
      configurations.put(configName, (Pair<DependencyGraphBuilder, Set<ValueRequirement>>) (Pair<?, ?>) Pair.of(builder, new HashSet<ValueRequirement>()));
    }
    _configurations = configurations;
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public ViewCompilationServices getServices() {
    return _services;
  }

  public DependencyGraphBuilder getBuilder(final String calcConfig) {
    return _configurations.get(calcConfig).getFirst();
  }

  public Set<ValueRequirement> getValueRequirements(final String calcConfig) {
    return _configurations.get(calcConfig).getSecond();
  }

  public Collection<Pair<DependencyGraphBuilder, Set<ValueRequirement>>> getBuilders() {
    return _configurations.values();
  }

}
