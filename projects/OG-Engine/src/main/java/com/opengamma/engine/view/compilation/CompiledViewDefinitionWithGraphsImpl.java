/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyGraphExplorerImpl;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link CompiledViewDefinitionWithGraphs}.
 */
public class CompiledViewDefinitionWithGraphsImpl extends CompiledViewDefinitionImpl implements CompiledViewDefinitionWithGraphs {

  private final Map<String, DependencyGraphExplorer> _graphsByConfiguration;
  private final long _functionInitId;
  private final Map<ComputationTargetReference, UniqueId> _resolutions;

  /**
   * Constructs an instance.
   * 
   * @param context the view compilation context, not null
   * @param identifier the compilation identifier, not null
   * @param graphs the dependency graphs, not null
   * @param portfolio the portfolio, possibly null
   * @return the new instance, not null
   */
  public static CompiledViewDefinitionWithGraphsImpl of(final ViewCompilationContext context, final String identifier, final Collection<DependencyGraph> graphs, final Portfolio portfolio) {
    final Collection<CompiledViewCalculationConfiguration> calcConfigs = new ArrayList<>();
    Instant validFrom = null;
    Instant validTo = null;
    final CompiledFunctionResolver functions = context.getCompiledFunctionResolver();
    for (DependencyGraph graph : graphs) {
      calcConfigs.add(CompiledViewCalculationConfigurationImpl.of(graph));
      final Iterator<DependencyNode> itr = graph.nodeIterator();
      while (itr.hasNext()) {
        final CompiledFunctionDefinition function = functions.getFunction(itr.next().getFunction().getFunctionId());
        if (function != null) {
          Instant time = function.getEarliestInvocationTime();
          if (time != null) {
            if (validFrom != null) {
              if (validFrom.isBefore(time)) {
                validFrom = time;
              }
            } else {
              validFrom = time;
            }
          }
          time = function.getLatestInvocationTime();
          if (time != null) {
            if (validTo != null) {
              if (validTo.isAfter(time)) {
                validTo = time;
              }
            } else {
              validTo = time;
            }
          }
        }
      }
    }
    return new CompiledViewDefinitionWithGraphsImpl(context.getResolverVersionCorrection(), identifier, context.getViewDefinition(), graphs, context.getActiveResolutions(), portfolio, context
        .getServices().getFunctionCompilationContext().getFunctionInitId(), calcConfigs, validFrom, validTo);
  }

  public CompiledViewDefinitionWithGraphsImpl(VersionCorrection versionCorrection,
      String identifier,
      ViewDefinition viewDefinition,
      Collection<DependencyGraph> graphs,
      Map<ComputationTargetReference, UniqueId> resolutions,
      Portfolio portfolio,
      long functionInitId,
      Collection<CompiledViewCalculationConfiguration> compiledCalcConfigs,
      Instant validFrom,
      Instant validTo) {
    super(versionCorrection,
        identifier,
        viewDefinition,
        portfolio,
        compiledCalcConfigs,
        validFrom,
        validTo);
    ArgumentChecker.notNull(resolutions, "resolutions");
    _functionInitId = functionInitId;
    final Map<String, DependencyGraphExplorer> graphsByConfiguration = Maps.newHashMapWithExpectedSize(graphs.size());
    for (DependencyGraph graph : graphs) {
      graphsByConfiguration.put(graph.getCalculationConfigurationName(), new DependencyGraphExplorerImpl(graph));
    }
    _graphsByConfiguration = Collections.unmodifiableMap(graphsByConfiguration);
    _resolutions = Collections.unmodifiableMap(resolutions);
  }

  private CompiledViewDefinitionWithGraphsImpl(final CompiledViewDefinitionWithGraphsImpl copyFrom, final VersionCorrection versionCorrection) {
    super(copyFrom, versionCorrection);
    _graphsByConfiguration = copyFrom._graphsByConfiguration;
    _functionInitId = copyFrom._functionInitId;
    _resolutions = copyFrom._resolutions;
  }

  private CompiledViewDefinitionWithGraphsImpl(final CompiledViewDefinitionWithGraphsImpl copyFrom, final Map<String, DependencyGraphExplorer> graphsByConfig,
      final Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig, final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfig) {
    super(copyFrom.getResolverVersionCorrection(), copyFrom.getCompilationIdentifier(), copyFrom.getViewDefinition(), copyFrom.getPortfolio(), processCompiledCalculationConfigurations(
        copyFrom.getCompiledCalculationConfigurationsMap(), graphsByConfig, selectionsByConfig, paramsByConfig), copyFrom.getValidFrom(), copyFrom.getValidTo());
    _graphsByConfiguration = graphsByConfig;
    _functionInitId = copyFrom._functionInitId;
    _resolutions = copyFrom._resolutions;
  }

  @Override
  public CompiledViewDefinitionWithGraphs withResolverVersionCorrection(final VersionCorrection versionCorrection) {
    return new CompiledViewDefinitionWithGraphsImpl(this, versionCorrection);
  }

  @Override
  public CompiledViewDefinitionWithGraphs withMarketDataManipulationSelections(final Map<String, DependencyGraph> newGraphsByConfig,
      final Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig, final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfig) {
    final Map<String, DependencyGraphExplorer> graphsByConfig = Maps.newHashMap(_graphsByConfiguration);
    for (Map.Entry<String, DependencyGraph> graph : newGraphsByConfig.entrySet()) {
      graphsByConfig.put(graph.getKey(), new DependencyGraphExplorerImpl(graph.getValue()));
    }
    return new CompiledViewDefinitionWithGraphsImpl(this, graphsByConfig, selectionsByConfig, paramsByConfig);
  }

  @Override
  public Collection<DependencyGraphExplorer> getDependencyGraphExplorers() {
    return _graphsByConfiguration.values();
  }

  public static Collection<DependencyGraph> getDependencyGraphs(final CompiledViewDefinitionWithGraphs viewDefinition) {
    final Collection<DependencyGraphExplorer> explorers = viewDefinition.getDependencyGraphExplorers();
    final List<DependencyGraph> graphs = new ArrayList<DependencyGraph>(explorers.size());
    for (DependencyGraphExplorer explorer : explorers) {
      graphs.add(explorer.getWholeGraph());
    }
    return graphs;
  }

  /**
   * Gets the function init ID that was used when creating the dependency graphs
   * 
   * @return the function init ID that was used when creating the dependency graphs
   * @deprecated this needs to go
   */
  @Deprecated
  public long getFunctionInitId() {
    return _functionInitId;
  }

  @Override
  public Map<ComputationTargetReference, UniqueId> getResolvedIdentifiers() {
    return _resolutions;
  }

  @Override
  public DependencyGraphExplorer getDependencyGraphExplorer(final String calcConfigName) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    final DependencyGraphExplorer dependencyGraph = _graphsByConfiguration.get(calcConfigName);
    if (dependencyGraph == null) {
      throw new DataNotFoundException("The calculation configuration name " + calcConfigName + " does not exist in the view definition");
    }
    return dependencyGraph;
  }

  private static Collection<CompiledViewCalculationConfiguration> processCompiledCalculationConfigurations(
      final Map<String, CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      final Map<String, DependencyGraphExplorer> graphsByConfiguration,
      final Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig,
      final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfig) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    ArgumentChecker.notNull(selectionsByConfig, "selectionsByGraph");
    final Collection<CompiledViewCalculationConfiguration> compiledViewCalculationConfigurations = new ArrayList<>();
    for (Map.Entry<String, DependencyGraphExplorer> entry : graphsByConfiguration.entrySet()) {
      String configName = entry.getKey();
      DependencyGraph graph = entry.getValue().getWholeGraph();
      CompiledViewCalculationConfiguration cvcc = createCalculationConfiguration(configName, graph, selectionsByConfig, paramsByConfig, compiledCalculationConfigurations);
      compiledViewCalculationConfigurations.add(cvcc);
    }
    return compiledViewCalculationConfigurations;
  }

  private static CompiledViewCalculationConfiguration createCalculationConfiguration(String configName, DependencyGraph graph,
      Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig, Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfig,
      Map<String, CompiledViewCalculationConfiguration> compiledCalculationConfigurations) {
    // There is a market data selection in place, create a new configuration with it
    if (selectionsByConfig.containsKey(configName)) {
      // Function params will be a subset of the selections, so it is possible we have no entry
      Map<DistinctMarketDataSelector, FunctionParameters> marketDataSelectionFunctionParameters = paramsByConfig.containsKey(configName) ? paramsByConfig.get(configName) : ImmutableMap
          .<DistinctMarketDataSelector, FunctionParameters>of();
      return CompiledViewCalculationConfigurationImpl.of(graph, selectionsByConfig.get(configName), marketDataSelectionFunctionParameters);
    } else if (compiledCalculationConfigurations.containsKey(configName)) {
      // No market data selection, but we have a compiled one already so use it
      return compiledCalculationConfigurations.get(configName);
    } else {
      // Create a new configuration just from the graph
      return CompiledViewCalculationConfigurationImpl.of(graph);
    }
  }

}
