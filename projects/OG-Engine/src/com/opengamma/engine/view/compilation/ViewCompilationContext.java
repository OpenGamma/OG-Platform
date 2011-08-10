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

import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;

/**
 * Holds context relating to the partially-completed compilation of a view definition, for passing to different stages
 * of the compilation.
 */
public class ViewCompilationContext {

  private final ViewDefinition _viewDefinition;
  private final ViewCompilationServices _services;
  private final Map<String, DependencyGraphBuilder> _builders;

  /* package */ViewCompilationContext(ViewDefinition viewDefinition, ViewCompilationServices compilationServices, Instant valuationTime) {
    _viewDefinition = viewDefinition;
    _services = compilationServices;
    _builders = generateBuilders(viewDefinition, compilationServices, valuationTime);
  }

  // --------------------------------------------------------------------------
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public ViewCompilationServices getServices() {
    return _services;
  }

  public Map<String, DependencyGraphBuilder> getBuilders() {
    return Collections.unmodifiableMap(_builders);
  }

  // --------------------------------------------------------------------------
  private Map<String, DependencyGraphBuilder> generateBuilders(ViewDefinition viewDefinition, ViewCompilationServices compilationServices, Instant valuationTime) {
    Map<String, DependencyGraphBuilder> result = new HashMap<String, DependencyGraphBuilder>();
    final CompiledFunctionResolver functionResolver = compilationServices.getFunctionResolver().compile(valuationTime);
    final Collection<ResolutionRule> rules = functionResolver.getAllResolutionRules();
    final DependencyGraphBuilderFactory builderFactory = new DependencyGraphBuilderFactory();
    for (String configName : viewDefinition.getAllCalculationConfigurationNames()) {
      final DependencyGraphBuilder builder = builderFactory.newInstance();
      builder.setCalculationConfigurationName(configName);
      builder.setMarketDataAvailabilityProvider(compilationServices.getMarketDataAvailabilityProvider());
      builder.setTargetResolver(compilationServices.getComputationTargetResolver());
      final FunctionCompilationContext compilationContext = compilationServices.getFunctionCompilationContext().clone();
      final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(configName);
      compilationContext.setViewCalculationConfiguration(calcConfig);
      final Collection<ResolutionRule> transformedRules = calcConfig.getResolutionRuleTransform().transform(rules);
      if (transformedRules == rules) {
        // No transformation applied; use the default resolver
        builder.setFunctionResolver(functionResolver);
      } else {
        // Create a new resolver with the transformed rules
        builder.setFunctionResolver(new DefaultCompiledFunctionResolver(compilationContext, transformedRules));
      }
      builder.setCompilationContext(compilationContext);
      result.put(configName, builder);
    }
    return result;
  }

}
