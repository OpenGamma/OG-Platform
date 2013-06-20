/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureAccumulator;
import com.opengamma.engine.depgraph.ResolutionFailureGatherer;
import com.opengamma.engine.depgraph.SimpleResolutionFailureVisitor;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;

/**
 * Helper class for {@link DependencyGraphBuilderResource}.
 * 
 * Implements builder pattern with defaults.
 */
public class DependencyGraphTraceBuilder {

  private final DependencyGraphBuilderResourceContextBean _builderContext;
  
  private final String _calculationConfigurationName;
  private final Instant _valuationTime;
  private final VersionCorrection _resolutionTime;
  private final ValueProperties _defaultProperties;
  private final Collection<ValueRequirement> _requirements;
  private final MarketDataSpecification _marketData;

  public DependencyGraphTraceBuilder(DependencyGraphBuilderResourceContextBean builderContext) {
    _builderContext = builderContext;
    _calculationConfigurationName = "Default";
    _valuationTime = null;
    _resolutionTime = VersionCorrection.LATEST;
    _defaultProperties = ValueProperties.none();
    _requirements = Collections.emptyList();
    _marketData = MarketData.live();
  }

  private DependencyGraphTraceBuilder(DependencyGraphTraceBuilder other) {
    _builderContext = other.getBuilderContext();
    _calculationConfigurationName = other.getCalculationConfigurationName();
    _valuationTime = other.getValuationTime();
    _resolutionTime = other.getResolutionTime();
    _defaultProperties = other.getDefaultProperties();
    _requirements = other.getRequirements();
    _marketData = other.getMarketData();
  }
  
  public DependencyGraphBuilderResourceContextBean getBuilderContext() {
    return _builderContext;
  }

  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  public Instant getValuationTime() {
    return _valuationTime;
  }

  public VersionCorrection getResolutionTime() {
    return _resolutionTime;
  }

  public ValueProperties getDefaultProperties() {
    return _defaultProperties;
  }

  public Collection<ValueRequirement> getRequirements() {
    return _requirements;
  }

  public MarketDataSpecification getMarketData() {
    return _marketData;
  }
  
  public DependencyGraphTraceBuilder calculationConfigurationName(final String calculationConfigurationName) {
    return new DependencyGraphTraceBuilder(this) {
      public String getCalculationConfigurationName() {
        return calculationConfigurationName;
      }
    };
  }

  public DependencyGraphTraceBuilder resolutionTime(final VersionCorrection resolutionTime) {
    return new DependencyGraphTraceBuilder(this) {
      @Override
      public VersionCorrection getResolutionTime() {
        return resolutionTime;
      }
    };
  }

  public DependencyGraphTraceBuilder valuationTime(final Instant valuationTime) {
    return new DependencyGraphTraceBuilder(this) {
      @Override
      public Instant getValuationTime() {
        return valuationTime;
      }
    };
  }

  public DependencyGraphTraceBuilder defaultProperties(final ValueProperties defaultProperties) {
    return new DependencyGraphTraceBuilder(this) {
      public ValueProperties getDefaultProperties() {
        return defaultProperties;
      };
    };
  }

  public DependencyGraphTraceBuilder addRequirement(ValueRequirement requirement) {
    final Collection<ValueRequirement> currentRequirements = new ArrayList<>(getRequirements());
    currentRequirements.add(requirement);
    return requirements(currentRequirements);
  }

  
  public DependencyGraphTraceBuilder requirements(final Collection<ValueRequirement> requirements) {
    return new DependencyGraphTraceBuilder(this) {
      @Override
      public Collection<ValueRequirement> getRequirements() {
        return requirements;
      }
    };
  }

  public DependencyGraphTraceBuilder marketData(final MarketDataSpecification marketData) {
    return new DependencyGraphTraceBuilder(this) {
      @Override
      public MarketDataSpecification getMarketData() {
        return marketData;
      }
    };
  }
  
  
  /**
   * Builds the dependency graph trace with the configured params.
   * @return the built trace object
   */
  public DependencyGraphBuildTrace build() {
    final DependencyGraphBuilder builder = getBuilderContext().getDependencyGraphBuilderFactory().newInstance();
    builder.setCalculationConfigurationName(_calculationConfigurationName);
    final FunctionCompilationContext context = getBuilderContext().getFunctionCompilationContext().clone();
    final ViewDefinition definition = new ViewDefinition("Mock View", "Test");
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(definition, getCalculationConfigurationName());
    calcConfig.setDefaultProperties(getDefaultProperties());
    context.setViewCalculationConfiguration(calcConfig);
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(getResolutionTime()));
    builder.setCompilationContext(context);
    final Collection<ResolutionRule> rules = getBuilderContext().getFunctionResolver().compile((getValuationTime() != null) ? getValuationTime() : Instant.now()).getAllResolutionRules();
    // TODO: allow transformation rules
    final DefaultCompiledFunctionResolver functions = new DefaultCompiledFunctionResolver(context, rules);
    functions.compileRules();
    builder.setFunctionResolver(functions);
    builder.setFunctionExclusionGroups(getBuilderContext().getFunctionExclusionGroups());
    // TODO this isn't used. is this OK?
    // TODO it's a bit nasty to build a MarketDataProvider just to get its availability provider
    final UserPrincipal marketDataUser = UserPrincipal.getLocalUser();
    final MarketDataProviderResolver resolver = getBuilderContext().getMarketDataProviderResolver();
    final MarketDataProvider marketDataProvider = resolver.resolve(marketDataUser, getMarketData());
    builder.setMarketDataAvailabilityProvider(marketDataProvider.getAvailabilityProvider(getMarketData()));
    final ResolutionFailureAccumulator resolutionFailureAccumulator = new ResolutionFailureAccumulator();
    builder.setResolutionFailureListener(resolutionFailureAccumulator);
    builder.setDisableFailureReporting(false);
    for (final ValueRequirement requirement : getRequirements()) {
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
