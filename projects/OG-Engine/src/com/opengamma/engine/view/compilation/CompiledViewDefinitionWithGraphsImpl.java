/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.time.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
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
    this(viewDefinition, portfolio, processLiveDataRequirements(graphsByConfiguration),
        processOutputValueNames(graphsByConfiguration), processComputationTargets(graphsByConfiguration),
        processSecurityTypes(graphsByConfiguration), processValidityRange(graphsByConfiguration),
        graphsByConfiguration, functionInitId);
  }
  
  private CompiledViewDefinitionWithGraphsImpl(ViewDefinition viewDefinition, Portfolio portfolio,
      Map<ValueRequirement, ValueSpecification> liveDataRequirements, Set<String> outputValueNames,
      Set<ComputationTarget> computationTargets, Set<String> securityTypes, Pair<Instant, Instant> validityRange,
      Map<String, DependencyGraph> graphsByConfiguration, long functionInitId) {
    super(viewDefinition, portfolio, liveDataRequirements, outputValueNames, computationTargets, securityTypes, validityRange.getFirst(), validityRange.getSecond());
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
   * @return the dependency graph for the specified calculation configuration, or {@code null} if no dependency graph
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

  //--------------------------------------------------------------------------
  private static Map<ValueRequirement, ValueSpecification> processLiveDataRequirements(Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Map<ValueRequirement, ValueSpecification> result = new HashMap<ValueRequirement, ValueSpecification>();
    for (DependencyGraph dependencyGraph : graphsByConfiguration.values()) {
      for (Pair<ValueRequirement, ValueSpecification> liveData : dependencyGraph.getAllRequiredLiveData()) {
        result.put(liveData.getFirst(), liveData.getSecond());
      }
    }
    return result;
  }

  private static Set<String> processSecurityTypes(Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Set<String> securityTypes = new TreeSet<String>();
    for (DependencyGraph dependencyGraph : graphsByConfiguration.values()) {
      for (DependencyNode dependencyNode : dependencyGraph.getDependencyNodes()) {
        if (dependencyNode.getComputationTarget().getType() != ComputationTargetType.SECURITY) {
          continue;
        }
        securityTypes.add(dependencyNode.getComputationTarget().getSecurity().getSecurityType());
      }
    }
    return securityTypes;
  }
  
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
  
  private static Set<String> processOutputValueNames(Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Set<String> valueNames = new HashSet<String>();
    for (DependencyGraph graph : graphsByConfiguration.values()) {
      for (ValueSpecification spec : graph.getOutputValues()) {
        valueNames.add(spec.getValueName());
      }
    }
    return valueNames;
  }
  
  private static Set<ComputationTarget> processComputationTargets(Map<String, DependencyGraph> graphsByConfiguration) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");
    Set<ComputationTarget> targets = new HashSet<ComputationTarget>();
    for (DependencyGraph dependencyGraph : graphsByConfiguration.values()) {
      Set<ComputationTarget> requiredLiveData = dependencyGraph.getAllComputationTargets();
      targets.addAll(requiredLiveData);
    }
    return targets;
  }

}
