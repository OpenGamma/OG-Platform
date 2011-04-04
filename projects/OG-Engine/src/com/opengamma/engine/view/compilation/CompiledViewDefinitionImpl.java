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
import javax.time.InstantProvider;

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
 * Default implementation of {@link CompiledViewDefinition}.
 */
public class CompiledViewDefinitionImpl implements CompiledViewDefinition {

  private final ViewDefinition _viewDefinition;
  private final Portfolio _portfolio;
  private final Map<String, DependencyGraph> _graphsByConfiguration;
  private final Map<ValueRequirement, ValueSpecification> _liveDataRequirements;
  private final Set<String> _securityTypes;
  private final Instant _earliestValidity;
  private final Instant _latestValidity;
  private final long _functionInitId;

  /**
   * Constructs an instance.
   * 
   * @param viewDefinition  the view definition, not null
   * @param graphsByConfiguration  the dependency graphs by calculation configuration name, not null
   * @param portfolio  the portfolio, possibly null
   * @param functionInitId  the function init ID that was used when creating the dependency graphs
   */
  public CompiledViewDefinitionImpl(ViewDefinition viewDefinition, Map<String, DependencyGraph> graphsByConfiguration, Portfolio portfolio, long functionInitId) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    ArgumentChecker.notNull(graphsByConfiguration, "graphsByConfiguration");

    _viewDefinition = viewDefinition;
    _portfolio = portfolio;
    _functionInitId = functionInitId;
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
    _earliestValidity = earliest;
    _latestValidity = latest;
  }
  
  //--------------------------------------------------------------------------
  @Override
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }
  
  @Override
  public Portfolio getPortfolio() {
    return _portfolio;
  }

  @Override
  public Map<ValueRequirement, ValueSpecification> getLiveDataRequirements() {
    return _liveDataRequirements;
  }
  
  @Override
  public Set<String> getOutputValueNames() {
    Set<String> valueNames = new HashSet<String>();
    for (DependencyGraph graph : getAllDependencyGraphs()) {
      for (ValueSpecification spec : graph.getOutputValues()) {
        valueNames.add(spec.getValueName());
      }
    }
    return valueNames;
  }
 
  @Override
  public Set<ComputationTarget> getComputationTargets() {
    Set<ComputationTarget> targets = new HashSet<ComputationTarget>();
    for (DependencyGraph dependencyGraph : _graphsByConfiguration.values()) {
      Set<ComputationTarget> requiredLiveData = dependencyGraph.getAllComputationTargets();
      targets.addAll(requiredLiveData);
    }
    return targets;
  }

  @Override
  public Set<String> getSecurityTypes() {
    return _securityTypes;
  }
  
  @Override
  public Instant getValidFrom() {
    return _earliestValidity;
  }

  @Override
  public Instant getValidTo() {
    return _latestValidity;
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
  
  /**
   * Checks whether the compilation results encapsulated in this instance are valid for a specific valuation time.
   * Note that this does not ensure that the view definition used for compilation is still up-to-date.
   * 
   * @param valuationTimeProvider  the valuation time
   * @return  {@code true} if the compilation results are valid for the valuation time, {@code false} otherwise
   */
  public boolean isValidFor(final InstantProvider valuationTimeProvider) {
    ArgumentChecker.notNull(valuationTimeProvider, "valuationTimeProvider");
    Instant valuationTime = valuationTimeProvider.toInstant();
    return (_earliestValidity == null || !valuationTime.isBefore(_earliestValidity))
        && (_latestValidity == null || !valuationTime.isAfter(_latestValidity));
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ViewEvaluationModel[" + getViewDefinition().getName() + ", " + getValidityString() + "]";
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
  
  private String getValidityString() {
    if (_earliestValidity == null && _latestValidity == null) {
      return "unrestricted validity";
    } else if (_earliestValidity == null) {
      return "valid until " + _latestValidity.toString();
    } else if (_latestValidity == null) {
      return "valid from " + _earliestValidity.toString();
    } else {
      return "valid from " + _earliestValidity.toString() + " to " + _latestValidity.toString();
    }
  }

}
