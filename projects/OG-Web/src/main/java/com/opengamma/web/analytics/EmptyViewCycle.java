/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ComputationCacheResponse;
import com.opengamma.engine.view.cycle.ComputationCycleQuery;
import com.opengamma.engine.view.cycle.ComputationResultsResponse;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleState;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.tuple.Pair;

/**
 * {@link ViewCycle} implementation that acts as a placeholder when a calculation cycle hasn't completed and there isn't a cycle available. This is cleaner than using a null cycle reference and being
 * forced to do a null check everywhere it's used. Only a single instance of this class should ever exist.
 */
/*package*/final class EmptyViewCycle implements ViewCycle {

  /** Reference to the empty cycle. */
  /* package */static final EngineResourceReference<ViewCycle> REFERENCE = new EmptyViewCycleReference();
  /** Single empty cycle instance. */
  /* package */static final ViewCycle INSTANCE = new EmptyViewCycle();

  /** Empty set of analytics results. */
  private static final InMemoryViewComputationResultModel EMPTY_RESULTS = new InMemoryViewComputationResultModel();
  /** Empty response for a cache lookup. */
  private static final ComputationCacheResponse EMPTY_CACHE_RESPONSE;
  /** Empty response for a results lookup. */
  private static final ComputationResultsResponse EMPTY_RESULTS_RESPONSE;

  static {
    EMPTY_CACHE_RESPONSE = new ComputationCacheResponse();
    EMPTY_CACHE_RESPONSE.setResults(ImmutableList.<Pair<ValueSpecification, Object>>of());
    EMPTY_RESULTS_RESPONSE = new ComputationResultsResponse();
    EMPTY_RESULTS_RESPONSE.setResults(ImmutableMap.<ValueSpecification, ComputedValueResult>of());
  }

  private final CompiledViewDefinitionWithGraphs _compiledViewDef = new EmptyViewDefinition();

  private EmptyViewCycle() {
  }

  @Override
  public UniqueId getUniqueId() {
    throw new UnsupportedOperationException("getUniqueId not supported");
  }

  @Override
  public UniqueId getViewProcessId() {
    throw new UnsupportedOperationException("getViewProcessId not supported");
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException("getName not supported");
  }

  @Override
  public ViewCycleState getState() {
    throw new UnsupportedOperationException("getState not supported");
  }

  @Override
  public Duration getDuration() {
    return Duration.ZERO;
  }
  
  @Override
  public ViewCycleExecutionOptions getExecutionOptions() {
    throw new UnsupportedOperationException("getExecutionOptions not supported");
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    return _compiledViewDef;
  }

  @Override
  public ViewComputationResultModel getResultModel() {
    return EMPTY_RESULTS;
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(ComputationCycleQuery computationCacheQuery) {
    return EMPTY_CACHE_RESPONSE;
  }

  @Override
  public ComputationResultsResponse queryResults(ComputationCycleQuery query) {
    return EMPTY_RESULTS_RESPONSE;
  }

  private static final class EmptyViewCycleReference implements EngineResourceReference<ViewCycle> {

    private EmptyViewCycleReference() {
    }

    @Override
    public ViewCycle get() {
      return EmptyViewCycle.INSTANCE;
    }

    @Override
    public void release() {
      // do nothing
    }
  }

  private static class EmptyViewDefinition implements CompiledViewDefinitionWithGraphs {

    private final DependencyGraphExplorer _dependencyGraphExplorer = new EmptyDependencyGraphExplorer();

    @Override
    public VersionCorrection getResolverVersionCorrection() {
      throw new UnsupportedOperationException("getResolverVersionCorrection not implemented");
    }

    @Override
    public String getCompilationIdentifier() {
      throw new UnsupportedOperationException("getCompilationIdentifier not implemented");
    }

    @Override
    public CompiledViewDefinitionWithGraphs withResolverVersionCorrection(VersionCorrection resolverVersionCorrection) {
      throw new UnsupportedOperationException("withResolverVersionCorrection not implemented");
    }

    @Override
    public ViewDefinition getViewDefinition() {
      throw new UnsupportedOperationException("getViewDefinition not implemented");
    }

    @Override
    public Portfolio getPortfolio() {
      throw new UnsupportedOperationException("getPortfolio not implemented");
    }

    @Override
    public CompiledViewCalculationConfiguration getCompiledCalculationConfiguration(String viewCalculationConfiguration) {
      throw new UnsupportedOperationException("getCompiledCalculationConfiguration not implemented");
    }

    @Override
    public Collection<CompiledViewCalculationConfiguration> getCompiledCalculationConfigurations() {
      throw new UnsupportedOperationException("getCompiledCalculationConfigurations not implemented");
    }

    @Override
    public CompiledViewDefinitionWithGraphs withMarketDataManipulationSelections(
        Map<String, DependencyGraph> graphsByConfiguration,
        Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfiguration,
        Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfiguration) {
      throw new UnsupportedOperationException("withMarketDataManipulationSelections not implemented");
    }

    @Override
    public Map<String, CompiledViewCalculationConfiguration> getCompiledCalculationConfigurationsMap() {
      throw new UnsupportedOperationException("getCompiledCalculationConfigurationsMap not implemented");
    }

    @Override
    public Set<ValueSpecification> getMarketDataRequirements() {
      throw new UnsupportedOperationException("getMarketDataRequirements not implemented");
    }

    @Override
    public Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesRequirements() {
      throw new UnsupportedOperationException("getTerminalValuesRequirements not implemented");
    }

    @Override
    public Set<ComputationTargetSpecification> getComputationTargets() {
      throw new UnsupportedOperationException("getComputationTargets not implemented");
    }

    @Override
    public Instant getValidFrom() {
      throw new UnsupportedOperationException("getValidFrom not implemented");
    }

    @Override
    public Instant getValidTo() {
      throw new UnsupportedOperationException("getValidTo not implemented");
    }

    @Override
    public Collection<DependencyGraphExplorer> getDependencyGraphExplorers() {
      throw new UnsupportedOperationException("getDependencyGraphExplorers not implemented");
    }

    @Override
    public DependencyGraphExplorer getDependencyGraphExplorer(String calcConfig) {
      return _dependencyGraphExplorer;
    }

    @Override
    public Map<ComputationTargetReference, UniqueId> getResolvedIdentifiers() {
      throw new UnsupportedOperationException("getResolvedIdentifiers not implemented");
    }
  }

  private static class EmptyDependencyGraphExplorer implements DependencyGraphExplorer {

    @Override
    public String getCalculationConfigurationName() {
      return "Empty";
    }

    @Override
    public DependencyGraph getWholeGraph() {
      throw new UnsupportedOperationException("getWholeGraph not implemented");
    }

    @Override
    public DependencyGraphExplorer getSubgraphProducing(ValueSpecification output) {
      return null;
    }

    @Override
    public DependencyNode getNodeProducing(ValueSpecification output) {
      return null;
    }

    @Override
    public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs() {
      return Collections.emptyMap();
    }

    @Override
    public Set<ComputationTargetSpecification> getComputationTargets() {
      return Collections.emptySet();
    }

  }

}
