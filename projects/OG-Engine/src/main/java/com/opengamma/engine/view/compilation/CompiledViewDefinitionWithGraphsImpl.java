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

import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyGraphExplorerImpl;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetReference;
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
   * @param viewDefinition the view definition, not null
   * @param graphs the dependency graphs, not null
   * @param resolutions the resolution mappings used to create the dependency graphs, not null
   * @param portfolio the portfolio, possibly null
   * @param functionInitId the function init ID that was used when creating the dependency graphs
   */
  public CompiledViewDefinitionWithGraphsImpl(final VersionCorrection versionCorrection, final ViewDefinition viewDefinition,
      final Collection<DependencyGraph> graphs, final Map<ComputationTargetReference, UniqueId> resolutions,
      final Portfolio portfolio, final long functionInitId) {
    this(versionCorrection, viewDefinition, portfolio, processCompiledCalculationConfigurations(graphs),
        processValidityRange(graphs), graphs, resolutions, functionInitId);
  }

  private CompiledViewDefinitionWithGraphsImpl(final VersionCorrection versionCorrection, final ViewDefinition viewDefinition, final Portfolio portfolio,
      final Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      final Pair<Instant, Instant> validityRange, final Collection<DependencyGraph> graphs, final Map<ComputationTargetReference, UniqueId> resolutions, final long functionInitId) {
    super(versionCorrection, viewDefinition, portfolio, compiledCalculationConfigurations, validityRange.getFirst(), validityRange.getSecond());
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

  @Override
  public CompiledViewDefinitionWithGraphs withResolverVersionCorrection(final VersionCorrection versionCorrection) {
    return new CompiledViewDefinitionWithGraphsImpl(this, versionCorrection);
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

  private static Collection<CompiledViewCalculationConfiguration> processCompiledCalculationConfigurations(final Collection<DependencyGraph> graphs) {
    ArgumentChecker.notNull(graphs, "graphs");
    final Collection<CompiledViewCalculationConfiguration> compiledViewCalculationConfigurations = new ArrayList<CompiledViewCalculationConfiguration>();
    for (DependencyGraph graph : graphs) {
      final CompiledViewCalculationConfigurationImpl compiledCalcConfig = new CompiledViewCalculationConfigurationImpl(graph);
      compiledViewCalculationConfigurations.add(compiledCalcConfig);
    }
    return compiledViewCalculationConfigurations;
  }

}
