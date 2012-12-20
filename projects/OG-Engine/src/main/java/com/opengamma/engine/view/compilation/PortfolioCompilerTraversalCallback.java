/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * Compiles dependency graphs for each stage in a portfolio tree.
 */
/* package */class PortfolioCompilerTraversalCallback extends AbstractPortfolioNodeTraversalCallback {

  private final ViewCalculationConfiguration _calculationConfiguration;
  private final ResultModelDefinition _resultModelDefinition;
  private final ConcurrentMap<UniqueId, Set<Pair<String, ValueProperties>>> _nodeRequirements = new ConcurrentHashMap<UniqueId, Set<Pair<String, ValueProperties>>>();
  private final DependencyGraphBuilder _builder;

  public PortfolioCompilerTraversalCallback(final ViewCalculationConfiguration calculationConfiguration, final DependencyGraphBuilder builder) {
    _calculationConfiguration = calculationConfiguration;
    _resultModelDefinition = calculationConfiguration.getViewDefinition().getResultModelDefinition();
    _builder = builder;
  }

  protected void addValueRequirement(final ValueRequirement valueRequirement) {
    _builder.addTarget(valueRequirement);
  }

  @Override
  public void preOrderOperation(final PortfolioNode node) {
    _nodeRequirements.put(node.getUniqueId(), new HashSet<Pair<String, ValueProperties>>());
    final Set<Pair<String, ValueProperties>> requiredOutputs = _calculationConfiguration.getPortfolioRequirementsBySecurityType().get(ViewCalculationConfiguration.SECURITY_TYPE_AGGREGATE_ONLY);
    if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
      final ComputationTargetSpecification nodeSpec = new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
      for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
        addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), nodeSpec, requiredOutput.getSecond()));
      }
    }
  }

  @Override
  public void preOrderOperation(final Position position) {
    final Security security = position.getSecurity();
    if (security == null) {
      return;
    }
    final String securityType = security.getSecurityType();
    Set<Pair<String, ValueProperties>> requiredOutputs;
    if ((_resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE)
        || (_resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE)) {
      requiredOutputs = _calculationConfiguration.getPortfolioRequirementsBySecurityType().get(securityType);
      if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
        if (_resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE) {
          final Set<Pair<String, ValueProperties>> nodeRequirements = _nodeRequirements.get(position.getParentNodeId());
          synchronized (nodeRequirements) {
            nodeRequirements.addAll(requiredOutputs);
          }
        }
        if (_resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE) {
          final ComputationTargetSpecification positionSpec = new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId());
          for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
            addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), positionSpec, requiredOutput.getSecond()));
          }
        }
      }
    }
    if (_resultModelDefinition.getTradeOutputMode() != ResultOutputMode.NONE) {
      final Collection<Trade> trades = position.getTrades();
      if (!trades.isEmpty()) {
        requiredOutputs = _calculationConfiguration.getTradeRequirementsBySecurityType().get(securityType);
        if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
          for (Trade trade : trades) {
            final ComputationTargetSpecification tradeSpec = new ComputationTargetSpecification(ComputationTargetType.TRADE, trade.getUniqueId());
            for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
              addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), tradeSpec, requiredOutput.getSecond()));
            }
          }
        }
      }
    }
  }

  @Override
  public void postOrderOperation(final PortfolioNode node) {
    final Set<Pair<String, ValueProperties>> nodeRequirements = _nodeRequirements.get(node.getUniqueId());
    if (node.getParentNodeId() != null) {
      final Set<Pair<String, ValueProperties>> parentNodeRequirements = _nodeRequirements.get(node.getParentNodeId());
      synchronized (parentNodeRequirements) {
        parentNodeRequirements.addAll(nodeRequirements);
      }
    }
    final ComputationTargetSpecification nodeSpec = new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
    for (Pair<String, ValueProperties> requiredOutput : nodeRequirements) {
      addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), nodeSpec, requiredOutput.getSecond()));
    }
  }

}
