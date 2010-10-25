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

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Represents the compilation output of a view definition, for downstream engine components to work with while the view
 * definition is potentially being modified and re-compiled concurrently.
 */
public class ViewEvaluationModel {

  private final Portfolio _portfolio;
  private final Map<String, DependencyGraph> _graphsByConfiguration;
  private final Map<ValueRequirement, ValueSpecification> _liveDataRequirements;
  private final Set<String> _securityTypes;
  private final long _earliestValidity;
  private final long _latestValidity;

  /**
   * Constructs an instance.
   * 
   * @param graphsByConfiguration  the dependency graphs by calculation configuration name, not null
   * @param portfolio  the portfolio, possibly null
   */
  public ViewEvaluationModel(Map<String, DependencyGraph> graphsByConfiguration, Portfolio portfolio) {
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");

    _portfolio = portfolio;
    _graphsByConfiguration = Collections.unmodifiableMap(graphsByConfiguration);
    _liveDataRequirements = Collections.unmodifiableMap(processLiveDataRequirements(graphsByConfiguration));
    _securityTypes = Collections.unmodifiableSet(processSecurityTypes(graphsByConfiguration));
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
    _earliestValidity = (earliest != null) ? earliest.toEpochMillisLong() : Long.MIN_VALUE;
    _latestValidity = (latest != null) ? latest.toEpochMillisLong() : Long.MAX_VALUE;
  }

  // --------------------------------------------------------------------------
  public Map<String, DependencyGraph> getDependencyGraphsByConfiguration() {
    return _graphsByConfiguration;
  }

  public Collection<DependencyGraph> getAllDependencyGraphs() {
    return _graphsByConfiguration.values();
  }

  public DependencyGraph getDependencyGraph(String name) {
    return _graphsByConfiguration.get(name);
  }

  public Portfolio getPortfolio() {
    return _portfolio;
  }

  public Map<ValueRequirement, ValueSpecification> getAllLiveDataRequirements() {
    return _liveDataRequirements;
  }

  public Set<String> getAllSecurityTypes() {
    return _securityTypes;
  }

  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    Set<ComputationTargetSpecification> targets = new HashSet<ComputationTargetSpecification>();
    for (DependencyGraph dependencyGraph : _graphsByConfiguration.values()) {
      Set<ComputationTargetSpecification> requiredLiveData = dependencyGraph.getAllComputationTargets();
      targets.addAll(requiredLiveData);
    }
    return targets;
  }

  public Set<String> getAllOutputValueNames() {
    Set<String> valueNames = new HashSet<String>();
    for (DependencyGraph graph : getAllDependencyGraphs()) {
      for (ValueSpecification spec : graph.getOutputValues()) {
        valueNames.add(spec.getValueName());
      }
    }
    return valueNames;
  }

  // --------------------------------------------------------------------------
  private static Map<ValueRequirement, ValueSpecification> processLiveDataRequirements(Map<String, DependencyGraph> graphsByConfiguration) {
    Map<ValueRequirement, ValueSpecification> result = new HashMap<ValueRequirement, ValueSpecification>();
    for (DependencyGraph dependencyGraph : graphsByConfiguration.values()) {
      for (Pair<ValueRequirement, ValueSpecification> liveData : dependencyGraph.getAllRequiredLiveData()) {
        result.put(liveData.getFirst(), liveData.getSecond());
      }
    }
    return result;
  }

  private static Set<String> processSecurityTypes(Map<String, DependencyGraph> graphsByConfiguration) {
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

  public boolean isValidFor(final long timestamp) {
    return (timestamp >= _earliestValidity) && (timestamp <= _latestValidity);
  }

}
