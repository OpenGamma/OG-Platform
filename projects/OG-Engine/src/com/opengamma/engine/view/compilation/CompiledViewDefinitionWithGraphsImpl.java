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
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of {@link CompiledViewDefinitionWithGraphs}.
 */
public class CompiledViewDefinitionWithGraphsImpl extends CompiledViewDefinitionImpl implements CompiledViewDefinitionWithGraphs {

  private final Map<String, DependencyGraph> _graphsByConfiguration;
  private final long _functionInitId;

  /**
   * Constructs an instance.
   * 
   * @param viewDefinition  the view definition, not null
   * @param graphsByConfiguration  the dependency graphs by calculation configuration name, not null
   * @param portfolio  the portfolio, possibly null
   * @param functionInitId  the function init ID that was used when creating the dependency graphs
   */
  public CompiledViewDefinitionWithGraphsImpl(ViewDefinition viewDefinition,
      Map<String, DependencyGraph> graphsByConfiguration, Portfolio portfolio, long functionInitId) {
    this(viewDefinition, portfolio, processCompiledCalculationConfigurations(graphsByConfiguration),
        processValidityRange(graphsByConfiguration), graphsByConfiguration, functionInitId);
  }
  
  private CompiledViewDefinitionWithGraphsImpl(ViewDefinition viewDefinition, Portfolio portfolio,
      Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      Pair<Instant, Instant> validityRange, Map<String, DependencyGraph> graphsByConfiguration, long functionInitId) {
    super(viewDefinition, portfolio, compiledCalculationConfigurations, validityRange.getFirst(), validityRange.getSecond());
    _functionInitId = functionInitId;
    _graphsByConfiguration = Collections.unmodifiableMap(graphsByConfiguration);
  }
  
  //--------------------------------------------------------------------------
  /**
   * Gets a map of dependency graphs by configuration name.
   * 
   * @return an unmodifiable map of dependency graphs by configuration name, not null
   */
  public Map<String, DependencyGraph> getDependencyGraphsByConfiguration() {
    return Collections.unmodifiableMap(_graphsByConfiguration);
  }

  /**
   * Gets the dependency graphs for every calculation configuration.
   * 
   * @return  an unmodifiable collection of the dependency graphs for every calculation configuration, not null
   */
  public Collection<DependencyGraph> getAllDependencyGraphs() {
    return Collections.unmodifiableCollection(_graphsByConfiguration.values());
  }

  /**
   * Gets the dependency graph for a specified calculation configuration.
   * 
   * @param calcConfigName  the calculation configuration name, not null
   * @return the dependency graph for the specified calculation configuration, or null if no dependency graph
   *         was found.
   */
  public DependencyGraph getDependencyGraph(String calcConfigName) {
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

  //-------------------------------------------------------------------------
  
  @Override
  public DependencyGraphExplorer getDependencyGraphExplorer(String calcConfigName) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    DependencyGraph dependencyGraph = getDependencyGraph(calcConfigName);
    if (dependencyGraph == null) {
      throw new OpenGammaRuntimeException("The calculation configuration name " + calcConfigName + " does not exist in the view definition");
    }
    return new DependencyGraphExplorerImpl(dependencyGraph);
  }
  
  //-------------------------------------------------------------------------- 
  private static Pair<Instant, Instant> processValidityRange(Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Instant earliest = null;
    Instant latest = null;
    for (DependencyGraph graph : graphsByConfiguration.values()) {
      for (DependencyNode node : graph.getDependencyNodes()) {
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

  private static Collection<CompiledViewCalculationConfiguration> processCompiledCalculationConfigurations(Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Collection<CompiledViewCalculationConfiguration> compiledViewCalculationConfigurations = new ArrayList<CompiledViewCalculationConfiguration>();
    for (Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      DependencyGraph depGraph = entry.getValue();
      CompiledViewCalculationConfigurationImpl compiledCalcConfig = new CompiledViewCalculationConfigurationImpl(depGraph);
      compiledViewCalculationConfigurations.add(compiledCalcConfig);
    }
    return compiledViewCalculationConfigurations;
  }
  
}
