/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.opengamma.engine.depgraph.DependencyGraphExplorerImpl;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

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
   * @param versionCorrection the resolver version/correction, not null
   * @param identifier the compilation identifier, not null
   * @param viewDefinition the view definition, not null
   * @param graphs the dependency graphs, not null
   * @param resolutions the resolution mappings used to create the dependency graphs, not null
   * @param portfolio the portfolio, possibly null
   * @param functionInitId the function init ID that was used when creating the dependency graphs
   */
  public CompiledViewDefinitionWithGraphsImpl(VersionCorrection versionCorrection,
                                              String identifier,
                                              ViewDefinition viewDefinition,
                                              Collection<DependencyGraph> graphs,
                                              Map<ComputationTargetReference, UniqueId> resolutions,
                                              Portfolio portfolio,
                                              long functionInitId) {
    this(versionCorrection,
         identifier,
         viewDefinition,
         graphs,
         resolutions,
         portfolio,
         functionInitId,
         processCompiledCalculationConfigurations(graphs),
         processValidityRange(graphs)
    );
  }

  /**
   * Constructs an instance.
   *
   * @param versionCorrection the resolver version/correction, not null
   * @param identifier the compilation identifier, not null
   * @param viewDefinition the view definition, not null
   * @param graphs the dependency graphs, not null
   * @param resolutions the resolution mappings used to create the dependency graphs, not null
   * @param portfolio the portfolio, possibly null
   * @param functionInitId the function init ID that was used when creating the dependency graphs
   * @param compiledCalcConfigs The compiled calculation configurations
   */
  public CompiledViewDefinitionWithGraphsImpl(VersionCorrection versionCorrection,
                                               String identifier,
                                               ViewDefinition viewDefinition,
                                               Collection<DependencyGraph> graphs,
                                               Map<ComputationTargetReference, UniqueId> resolutions,
                                               Portfolio portfolio,
                                               long functionInitId,
                                               Collection<CompiledViewCalculationConfiguration> compiledCalcConfigs) {
    this(versionCorrection,
         identifier,
         viewDefinition,
         graphs,
         resolutions,
         portfolio,
         functionInitId,
         compiledCalcConfigs,
         processValidityRange(graphs));
  }

  private CompiledViewDefinitionWithGraphsImpl(VersionCorrection versionCorrection,
                                               String identifier,
                                               ViewDefinition viewDefinition,
                                               Collection<DependencyGraph> graphs,
                                               Map<ComputationTargetReference, UniqueId> resolutions,
                                               Portfolio portfolio,
                                               long functionInitId,
                                               Collection<CompiledViewCalculationConfiguration> compiledCalcConfigs,
                                               Pair<Instant, Instant> validityRange) {
    super(versionCorrection,
          identifier,
          viewDefinition,
          portfolio,
          compiledCalcConfigs,
          validityRange.getFirst(),
          validityRange.getSecond());
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

  private CompiledViewDefinitionWithGraphsImpl(
      CompiledViewDefinitionWithGraphsImpl copyFrom,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByGraph,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByGraph) {
    super(copyFrom.getResolverVersionCorrection(),
          copyFrom.getCompilationIdentifier(),
          copyFrom.getViewDefinition(),
          copyFrom.getPortfolio(),
          processCompiledCalculationConfigurations(copyFrom.getCompiledCalculationConfigurationsMap(),
                                                   copyFrom._graphsByConfiguration,
                                                   selectionsByGraph,
                                                   paramsByGraph),
          copyFrom.getValidFrom(),
          copyFrom.getValidTo());
    _graphsByConfiguration = copyFrom._graphsByConfiguration;
    _functionInitId = copyFrom._functionInitId;
    _resolutions = copyFrom._resolutions;
  }

  @Override
  public CompiledViewDefinitionWithGraphs withResolverVersionCorrection(final VersionCorrection versionCorrection) {
    return new CompiledViewDefinitionWithGraphsImpl(this, versionCorrection);
  }

  @Override
  public CompiledViewDefinitionWithGraphs withMarketDataManipulationSelections(
      Map<DependencyGraph, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByGraph,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByGraph) {
    return new CompiledViewDefinitionWithGraphsImpl(this, selectionsByGraph, paramsByGraph);
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

  //--------------------------------------------------------------------------
  private static Pair<Instant, Instant> processValidityRange(final Collection<DependencyGraph> graphs) {
    ArgumentChecker.notNull(graphs, "graphs");
    Instant earliest = null;
    Instant latest = null;
    for (final DependencyGraph graph : graphs) {
      for (final DependencyNode node : graph.getDependencyNodes()) {
        Instant time = node.getFunction().getFunction().getEarliestInvocationTime();
        if (time != null) {
          if (earliest != null) {
            if (earliest.isBefore(time)) {
              earliest = time;
            }
          } else {
            earliest = time;
          }
        }
        time = node.getFunction().getFunction().getLatestInvocationTime();
        if (time != null) {
          if (latest != null) {
            if (latest.isAfter(time)) {
              latest = time;
            }
          } else {
            latest = time;
          }
        }
      }
    }
    return Pair.of(earliest, latest);
  }

  private static Collection<CompiledViewCalculationConfiguration> processCompiledCalculationConfigurations(
      Map<String, CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      Map<String, DependencyGraphExplorer> graphsByConfiguration,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByGraph,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByGraph) {

    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    ArgumentChecker.notNull(selectionsByGraph, "selectionsByGraph");

    final Collection<CompiledViewCalculationConfiguration> compiledViewCalculationConfigurations = new ArrayList<>();

    for (Map.Entry<String, DependencyGraphExplorer> entry : graphsByConfiguration.entrySet()) {

      String configName = entry.getKey();
      DependencyGraph graph = entry.getValue().getWholeGraph();
      CompiledViewCalculationConfiguration cvcc = createCalculationConfiguration(configName, graph, selectionsByGraph, paramsByGraph, compiledCalculationConfigurations);
      compiledViewCalculationConfigurations.add(cvcc);
    }

    return compiledViewCalculationConfigurations;
  }

  private static CompiledViewCalculationConfiguration createCalculationConfiguration(
      String configName,
      DependencyGraph graph,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByGraph,
      Map<DependencyGraph, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByGraph,
      Map<String, CompiledViewCalculationConfiguration> compiledCalculationConfigurations) {

    // There is a market data selection in place, create a new configuration with it
    if (selectionsByGraph.containsKey(graph)) {

      // Function params will be a subset of the selections, so it is possible we have no entry
      Map<DistinctMarketDataSelector, FunctionParameters> marketDataSelectionFunctionParameters =
          paramsByGraph.containsKey(graph) ?
              paramsByGraph.get(graph) :
              ImmutableMap.<DistinctMarketDataSelector, FunctionParameters>of();

      return new CompiledViewCalculationConfigurationImpl(
          graph, selectionsByGraph.get(graph), marketDataSelectionFunctionParameters);

    } else if (compiledCalculationConfigurations.containsKey(configName)) {
      // No market data selection, but we have a compiled one already so use it
      return compiledCalculationConfigurations.get(configName);
    } else {
      // Create a new configuration just from the graph
      return new CompiledViewCalculationConfigurationImpl(graph);
    }
  }

  private static Collection<CompiledViewCalculationConfiguration> processCompiledCalculationConfigurations(
      final Collection<DependencyGraph> graphs) {
    ArgumentChecker.notNull(graphs, "graphs");
    final Collection<CompiledViewCalculationConfiguration> compiledViewCalculationConfigurations = new ArrayList<>();
    for (DependencyGraph graph : graphs) {
      final CompiledViewCalculationConfigurationImpl compiledCalcConfig = new CompiledViewCalculationConfigurationImpl(graph);
      compiledViewCalculationConfigurations.add(compiledCalcConfig);
    }
    return compiledViewCalculationConfigurations;
  }

}
