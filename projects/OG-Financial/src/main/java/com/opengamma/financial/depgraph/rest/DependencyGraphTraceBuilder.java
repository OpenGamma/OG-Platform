/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureAccumulator;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.worker.SnapshottingViewExecutionDataProvider;
import com.opengamma.financial.depgraph.provider.LocalDependencyGraphTraceProvider;
import com.opengamma.livedata.UserPrincipal;

/**
 * Helper class for {@link LocalDependencyGraphTraceProvider}.
 * 
 * Implements builder pattern with defaults.
 */
public class DependencyGraphTraceBuilder {

  private final DependencyGraphBuilderResourceContextBean _builderContext;

  public DependencyGraphTraceBuilder(DependencyGraphBuilderResourceContextBean builderContext) {
    _builderContext = builderContext;
  }

  /**
   * Builds the dependency graph trace with the configured params.
   * @param properties the properties to use
   * @return the built trace object
   */
  public DependencyGraphBuildTrace build(DependencyGraphTraceBuilderProperties properties) {
    final DependencyGraphBuilder builder = _builderContext.getDependencyGraphBuilderFactory().newInstance();
    builder.setCalculationConfigurationName(properties.getCalculationConfigurationName());
    final FunctionCompilationContext context = _builderContext.getFunctionCompilationContext().clone();
    final ViewDefinition definition = new ViewDefinition("Mock View", "Test");
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(definition, properties.getCalculationConfigurationName());
    calcConfig.setDefaultProperties(properties.getDefaultProperties());
    context.setViewCalculationConfiguration(calcConfig);
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(properties.getResolutionTime()));
    builder.setCompilationContext(context);
    final Collection<ResolutionRule> rules = _builderContext.getFunctionResolver().compile((properties.getValuationTime() != null) ? properties.getValuationTime() : Instant.now())
        .getAllResolutionRules();
    // TODO: allow transformation rules
    final DefaultCompiledFunctionResolver functions = new DefaultCompiledFunctionResolver(context, rules);
    functions.compileRules();
    builder.setFunctionResolver(functions);
    builder.setFunctionExclusionGroups(_builderContext.getFunctionExclusionGroups());
    // TODO this isn't used. is this OK?
    final UserPrincipal marketDataUser = UserPrincipal.getLocalUser();
    final MarketDataProviderResolver resolver = _builderContext.getMarketDataProviderResolver();
    List<MarketDataSpecification> marketData = properties.getMarketData();
    if (marketData == null || marketData.isEmpty()) {
      marketData = Collections.<MarketDataSpecification>singletonList(MarketData.live());
    }
    
    MarketDataAvailabilityProvider availabilityProvider = new SnapshottingViewExecutionDataProvider(marketDataUser, marketData, resolver).getAvailabilityProvider();
    builder.setMarketDataAvailabilityProvider(availabilityProvider);
    final ResolutionFailureAccumulator resolutionFailureAccumulator = new ResolutionFailureAccumulator();
    builder.setResolutionFailureListener(resolutionFailureAccumulator);
    builder.setDisableFailureReporting(false);
    for (final ValueRequirement requirement : properties.getRequirements()) {
      builder.addTarget(requirement);
    }
    DependencyGraph dependencyGraph = builder.getDependencyGraph();
    List<ResolutionFailure> resolutionFailures = resolutionFailureAccumulator.getResolutionFailures();

    DependencyGraphBuildTrace graphBuildTrace = DependencyGraphBuildTrace.of(
        dependencyGraph,
        builder.getExceptions(),
        resolutionFailures,
        builder.getValueRequirementMapping());

    return graphBuildTrace;

  }
}
