/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.time.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyGraphExplorerImpl;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of {@link CompiledViewDefinitionWithGraphs}.
 */
public class CompiledViewDefinitionWithGraphsImpl extends CompiledViewDefinitionImpl implements CompiledViewDefinitionWithGraphs {

  private final Map<String, DependencyGraph> _graphsByConfiguration;
  private final long _functionInitId;
  private final Map<ComputationTargetReference, UniqueId> _resolutions;

  /**
   * Constructs an instance.
   *
   * @param viewDefinition the view definition, not null
   * @param graphsByConfiguration the dependency graphs by calculation configuration name, not null
   * @param resolutions the resolution mappings used to create the dependency graphs, not null
   * @param portfolio the portfolio, possibly null
   * @param functionInitId the function init ID that was used when creating the dependency graphs
   */
  public CompiledViewDefinitionWithGraphsImpl(final ViewDefinition viewDefinition,
      final Map<String, DependencyGraph> graphsByConfiguration, final Map<ComputationTargetReference, UniqueId> resolutions,
      final Portfolio portfolio, final long functionInitId) {
    this(viewDefinition, portfolio, processCompiledCalculationConfigurations(graphsByConfiguration),
        processValidityRange(graphsByConfiguration), graphsByConfiguration, resolutions, functionInitId);
  }

  private CompiledViewDefinitionWithGraphsImpl(final ViewDefinition viewDefinition, final Portfolio portfolio, final Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      final Pair<Instant, Instant> validityRange, final Map<String, DependencyGraph> graphsByConfiguration, final Map<ComputationTargetReference, UniqueId> resolutions, final long functionInitId) {
    super(viewDefinition, portfolio, compiledCalculationConfigurations, validityRange.getFirst(), validityRange.getSecond());
    ArgumentChecker.notNull(resolutions, "resolutions");
    _functionInitId = functionInitId;
    _graphsByConfiguration = Collections.unmodifiableMap(graphsByConfiguration);
    _resolutions = Collections.unmodifiableMap(resolutions);
  }

  //--------------------------------------------------------------------------
  /**
   * Gets a map of dependency graphs by configuration name.
   *
   * @return an unmodifiable map of dependency graphs by configuration name, not null
   */
  public Map<String, DependencyGraph> getDependencyGraphsByConfiguration() {
    return _graphsByConfiguration;
  }

  /**
   * Gets the dependency graphs for every calculation configuration.
   *
   * @return  an unmodifiable collection of the dependency graphs for every calculation configuration, not null
   */
  public Collection<DependencyGraph> getAllDependencyGraphs() {
    return _graphsByConfiguration.values();
  }

  /**
   * Gets the dependency graph for a specified calculation configuration.
   *
   * @param calcConfigName  the calculation configuration name, not null
   * @return the dependency graph for the specified calculation configuration, or null if no dependency graph
   *         was found.
   */
  public DependencyGraph getDependencyGraph(final String calcConfigName) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    return _graphsByConfiguration.get(calcConfigName);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the function init ID that was used when creating the dependency graphs
   *
   * @return the function init ID that was used when creating the dependency graphs
   */
  public long getFunctionInitId() {
    return _functionInitId;
  }

  /**
   * Gets the object and external identifiers that were resolved as part of the view compilation. The graphs contained in this instance are only valid when the mapping returned here holds. For
   * example, a different version/correction used for target resolution might make one or more references resolve to a different target specification. Anything using the original specification will
   * not longer be valid.
   *
   * @return the map of target references containing object identifiers (unversioned unique identifiers) or external identifiers to the resolved unique identifiers
   */
  public Map<ComputationTargetReference, UniqueId> getResolvedIdentifiers() {
    return _resolutions;
  }

  @Override
  public DependencyGraphExplorer getDependencyGraphExplorer(final String calcConfigName) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    final DependencyGraph dependencyGraph = getDependencyGraph(calcConfigName);
    if (dependencyGraph == null) {
      throw new OpenGammaRuntimeException("The calculation configuration name " + calcConfigName + " does not exist in the view definition");
    }
    return new DependencyGraphExplorerImpl(dependencyGraph);
  }

  //--------------------------------------------------------------------------
  private static Pair<Instant, Instant> processValidityRange(final Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Instant earliest = null;
    Instant latest = null;
    for (final DependencyGraph graph : graphsByConfiguration.values()) {
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

  private static Collection<CompiledViewCalculationConfiguration> processCompiledCalculationConfigurations(final Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    final Collection<CompiledViewCalculationConfiguration> compiledViewCalculationConfigurations = new ArrayList<CompiledViewCalculationConfiguration>();
    for (final Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      final DependencyGraph depGraph = entry.getValue();
      final CompiledViewCalculationConfigurationImpl compiledCalcConfig = new CompiledViewCalculationConfigurationImpl(depGraph);
      compiledViewCalculationConfigurations.add(compiledCalcConfig);
    }
    return compiledViewCalculationConfigurations;
  }

}
