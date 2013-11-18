/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Default implementation of {@link CompiledViewCalculationConfiguration}.
 */
public class CompiledViewCalculationConfigurationImpl implements CompiledViewCalculationConfiguration, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _name;
  private final Set<ComputationTargetSpecification> _computationTargets;
  private final Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputSpecifications;
  private final Map<ValueSpecification, Collection<ValueSpecification>> _marketDataAliases;
  private final Map<DistinctMarketDataSelector, Set<ValueSpecification>> _marketDataSelections;
  private final Map<DistinctMarketDataSelector, FunctionParameters> _marketDataSelectionFunctionParameters;

  /**
   * Constructs an instance
   * 
   * @param name the name of the view calculation configuration, not null
   * @param computationTargets the computation targets, not null
   * @param terminalOutputSpecs the output specifications, not null
   * @param marketDataSpecs the market data specifications, not null
   */
  public CompiledViewCalculationConfigurationImpl(String name,
      Set<ComputationTargetSpecification> computationTargets,
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputSpecs,
      Map<ValueSpecification, Collection<ValueSpecification>> marketDataSpecs) {
    this(name, computationTargets, terminalOutputSpecs, marketDataSpecs,
        Collections.<DistinctMarketDataSelector, Set<ValueSpecification>>emptyMap(),
        Collections.<DistinctMarketDataSelector, FunctionParameters>emptyMap());
  }

  /**
   * Constructs an instance
   * 
   * @param name the name of the view calculation configuration, not null
   * @param computationTargets the computation targets, not null
   * @param terminalOutputSpecifications the output specifications, not null
   * @param marketDataSpecifications the market data specifications, not null
   * @param marketDataSelections the market data selections that have been made to, not null
   * @param marketDataSelectionFunctionParameters the function parameters to be used for the market data selections, not null
   */
  public CompiledViewCalculationConfigurationImpl(
      String name,
      Set<ComputationTargetSpecification> computationTargets,
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputSpecifications,
      Map<ValueSpecification, Collection<ValueSpecification>> marketDataSpecifications,
      Map<DistinctMarketDataSelector, Set<ValueSpecification>> marketDataSelections,
      Map<DistinctMarketDataSelector, FunctionParameters> marketDataSelectionFunctionParameters) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(computationTargets, "computationTargets");
    ArgumentChecker.notNull(terminalOutputSpecifications, "terminalOutputSpecifications");
    ArgumentChecker.notNull(marketDataSpecifications, "marketDataSpecifications");
    ArgumentChecker.notNull(marketDataSelections, "marketDataSelections");
    ArgumentChecker.notNull(marketDataSelectionFunctionParameters, "marketDataSelectionFunctionParameters");
    _name = name;
    _computationTargets = ImmutableSet.copyOf(computationTargets);
    _terminalOutputSpecifications = ImmutableMap.copyOf(terminalOutputSpecifications);
    _marketDataAliases = ImmutableMap.copyOf(marketDataSpecifications);
    _marketDataSelections = ImmutableMap.copyOf(marketDataSelections);
    _marketDataSelectionFunctionParameters = ImmutableMap.copyOf(marketDataSelectionFunctionParameters);
  }

  /**
   * Constructs an instance from a dependency graph
   * 
   * @param dependencyGraph the dependency graph, not null
   * @return the new instance, not null
   */
  public static CompiledViewCalculationConfigurationImpl of(final DependencyGraph dependencyGraph) {
    return of(dependencyGraph, Collections.<DistinctMarketDataSelector, Set<ValueSpecification>>emptyMap(), Collections.<DistinctMarketDataSelector, FunctionParameters>emptyMap());
  }

  /**
   * Constructs an instance from a dependency graph with market data manipulation selections and function parameters
   * 
   * @param graph the dependency graph, not null
   * @param marketDataSelections the market data selections that have been made to support manipulation of the structured market data, not null
   * @param marketDataSelectionFunctionParameters the function params to be used for the market data selections, not null
   * @return the new instance, not null
   */
  public static CompiledViewCalculationConfigurationImpl of(final DependencyGraph graph,
      final Map<DistinctMarketDataSelector, Set<ValueSpecification>> marketDataSelections,
      final Map<DistinctMarketDataSelector, FunctionParameters> marketDataSelectionFunctionParameters) {
    ArgumentChecker.notNull(graph, "graph");
    final Map<ValueSpecification, ?> terminals = graph.getTerminalOutputs();
    final Set<ComputationTargetSpecification> targets = new HashSet<ComputationTargetSpecification>();
    final Map<ValueSpecification, Collection<ValueSpecification>> marketData = new HashMap<ValueSpecification, Collection<ValueSpecification>>();
    final Set<DependencyNode> visited = Sets.newHashSetWithExpectedSize(graph.getSize());
    final int rootCount = graph.getRootCount();
    for (int i = 0; i < rootCount; i++) {
      final DependencyNode root = graph.getRootNode(i);
      processNode(root, terminals, targets, marketData, visited);
    }
    return new CompiledViewCalculationConfigurationImpl(graph.getCalculationConfigurationName(),
        targets,
        graph.getTerminalOutputs(),
        marketData,
        marketDataSelections,
        marketDataSelectionFunctionParameters);
  }

  private static void processNode(final DependencyNode node, final Map<ValueSpecification, ?> terminals, final Set<ComputationTargetSpecification> targets,
      final Map<ValueSpecification, Collection<ValueSpecification>> marketData, final Set<DependencyNode> visited) {
    if (!visited.add(node)) {
      return;
    }
    targets.add(node.getTarget());
    final int inputs = node.getInputCount();
    if (inputs == 1) {
      if (MarketDataAliasingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        final ValueSpecification marketDataSpec = node.getInputValue(0);
        Collection<ValueSpecification> aliases = marketData.get(marketDataSpec);
        final int outputs = node.getOutputCount();
        if (aliases == null) {
          aliases = new ArrayList<ValueSpecification>(outputs);
          marketData.put(marketDataSpec, aliases);
        }
        for (int i = 0; i < outputs; i++) {
          aliases.add(node.getOutputValue(i));
        }
        if (visited.add(node.getInputNode(0))) {
          if (terminals.containsKey(marketDataSpec)) {
            aliases.add(marketDataSpec);
          }
        }
        return;
      }
    } else if (inputs == 0) {
      if (MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        final ValueSpecification marketDataSpec = node.getOutputValue(0);
        Collection<ValueSpecification> aliases = marketData.get(marketDataSpec);
        if (aliases == null) {
          aliases = new ArrayList<ValueSpecification>(1);
          marketData.put(marketDataSpec, aliases);
        }
        aliases.add(node.getOutputValue(0));
      }
      return;
    }
    for (int i = 0; i < inputs; i++) {
      processNode(node.getInputNode(i), terminals, targets, marketData, visited);
    }
  }

  // CompiledViewCalculationConfiguration

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
    final Set<Pair<String, ValueProperties>> valueNames = new HashSet<>();
    for (final ValueSpecification spec : getTerminalOutputSpecifications().keySet()) {
      valueNames.add(Pairs.of(spec.getValueName(), spec.getProperties()));
    }
    return valueNames;
  }

  @Override
  public Map<DistinctMarketDataSelector, Set<ValueSpecification>> getMarketDataSelections() {
    return _marketDataSelections;
  }

  @Override
  public Map<DistinctMarketDataSelector, FunctionParameters> getMarketDataSelectionFunctionParameters() {
    return _marketDataSelectionFunctionParameters;
  }

  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    return _computationTargets;
  }

  @Override
  public Set<ValueSpecification> getMarketDataRequirements() {
    return getMarketDataAliases().keySet();
  }

  @Override
  public Map<ValueSpecification, Collection<ValueSpecification>> getMarketDataAliases() {
    return _marketDataAliases;
  }

  // Object

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CompiledViewCalculationConfiguration)) {
      return false;
    }
    final CompiledViewCalculationConfiguration other = (CompiledViewCalculationConfiguration) o;
    return ObjectUtils.equals(getName(), other.getName())
        && ObjectUtils.equals(getTerminalOutputSpecifications(), other.getTerminalOutputSpecifications())
        && ObjectUtils.equals(getComputationTargets(), other.getComputationTargets())
        && ObjectUtils.equals(getMarketDataRequirements(), other.getMarketDataRequirements());
  }

  @Override
  public int hashCode() {
    int hc = ObjectUtils.hashCode(getName());
    hc += (hc << 4) + ObjectUtils.hashCode(getTerminalOutputSpecifications());
    hc += (hc << 4) + ObjectUtils.hashCode(getComputationTargets());
    hc += (hc << 4) + ObjectUtils.hashCode(getMarketDataRequirements());
    return hc;
  }

}
