/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of {@link CompiledViewCalculationConfiguration}.
 */
public class CompiledViewCalculationConfigurationImpl implements CompiledViewCalculationConfiguration {
  
  private final String _name;
  private final Set<ComputationTargetSpecification> _computationTargets;
  private final Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputSpecifications;
  private final Map<ValueRequirement, ValueSpecification> _marketDataRequirements;
  
  /**
   * Constructs an instance
   * 
   * @param name  the name of the view calculation configuration, not null
   * @param computationTargets  the computation targets, not null
   * @param terminalOutputSpecifications  the output specifications, not null
   * @param marketDataRequirements  the market data requirements, not null
   */
  public CompiledViewCalculationConfigurationImpl(String name, Set<ComputationTargetSpecification> computationTargets,
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputSpecifications,
      Map<ValueRequirement, ValueSpecification> marketDataRequirements) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(computationTargets, "computationTargets");
    ArgumentChecker.notNull(terminalOutputSpecifications, "terminalOutputSpecifications");
    ArgumentChecker.notNull(marketDataRequirements, "marketDataRequirements");
    _name = name;
    _computationTargets = computationTargets;
    _terminalOutputSpecifications = terminalOutputSpecifications;
    _marketDataRequirements = marketDataRequirements;
  }
  
  /**
   * Constructs an instance from a dependency graph
   * 
   * @param dependencyGraph  the dependency graph, not null
   */
  public CompiledViewCalculationConfigurationImpl(DependencyGraph dependencyGraph) {
    this(dependencyGraph.getCalculationConfigurationName(), processComputationTargets(dependencyGraph),
        processTerminalOutputSpecifications(dependencyGraph), processMarketDataRequirements(dependencyGraph));
  }
  
  @Override
  public String getName() {
    return _name;
  }
  
  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputSpecifications() {
    return Collections.unmodifiableMap(_terminalOutputSpecifications);
  }
  
  @Override
  public Set<Pair<String, ValueProperties>> getTerminalOutputValues() {
    Set<Pair<String, ValueProperties>> valueNames = new HashSet<Pair<String, ValueProperties>>();
    for (ValueSpecification spec : getTerminalOutputSpecifications().keySet()) {
      valueNames.add(Pair.of(spec.getValueName(), spec.getProperties()));
    }
    return valueNames;

  }
  
  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    return _computationTargets;
  }
  
  @Override
  public Map<ValueRequirement, ValueSpecification> getMarketDataRequirements() {
    return _marketDataRequirements;
  }

  //-------------------------------------------------------------------------
  private static Map<ValueRequirement, ValueSpecification> processMarketDataRequirements(DependencyGraph dependencyGraph) {
    ArgumentChecker.notNull(dependencyGraph, "dependencyGraph");
    Map<ValueRequirement, ValueSpecification> result = new HashMap<ValueRequirement, ValueSpecification>();
    for (Pair<ValueRequirement, ValueSpecification> marketData : dependencyGraph.getAllRequiredMarketData()) {
      result.put(marketData.getFirst(), marketData.getSecond());
    }
    return result;
  }
  
  private static Map<ValueSpecification, Set<ValueRequirement>> processTerminalOutputSpecifications(DependencyGraph dependencyGraph) {
    ArgumentChecker.notNull(dependencyGraph, "dependencyGraph");
    return dependencyGraph.getTerminalOutputs();
  }
  
  private static Set<ComputationTargetSpecification> processComputationTargets(DependencyGraph dependencyGraph) {
    ArgumentChecker.notNull(dependencyGraph, "dependencyGraph");
    return dependencyGraph.getAllComputationTargets();
  }
  
}
