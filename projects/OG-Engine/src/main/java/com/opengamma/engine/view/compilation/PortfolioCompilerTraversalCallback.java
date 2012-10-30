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
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
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
  private final ConcurrentMap<ComputationTargetReference, UniqueId> _resolutions;

  public PortfolioCompilerTraversalCallback(final ViewCalculationConfiguration calculationConfiguration, final DependencyGraphBuilder builder,
      final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
    _calculationConfiguration = calculationConfiguration;
    _resultModelDefinition = calculationConfiguration.getViewDefinition().getResultModelDefinition();
    _builder = builder;
    _resolutions = resolutions;
  }

  protected void addValueRequirement(final ValueRequirement valueRequirement) {
    _builder.addTarget(valueRequirement);
  }

  /**
   * Store details of the security link in the resolution cache. The link is assumed to be a record of the link to the object, for example is it held by strong (object id) or weak (external id)
   * reference.
   * 
   * @param link the link to store - the identifier is taken from this along with the resolved unique identifier
   */
  private void store(final SecurityLink link) {
    final ComputationTargetReference key;
    final UniqueId uid;
    if (link.getTarget() != null) {
      uid = link.getTarget().getUniqueId();
      if (link.getObjectId() != null) {
        key = new ComputationTargetSpecification(ComputationTargetType.SECURITY, uid.toLatest());
      } else if (!link.getExternalId().isEmpty()) {
        key = new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId());
      } else {
        return;
      }
      final UniqueId existing = _resolutions.putIfAbsent(MemoryUtils.instance(key), uid);
      assert (existing == null) || existing.equals(uid);
    }
  }

  @Override
  public void preOrderOperation(final PortfolioNode node) {
    _nodeRequirements.put(node.getUniqueId(), new HashSet<Pair<String, ValueProperties>>());
    final Set<Pair<String, ValueProperties>> requiredOutputs = _calculationConfiguration.getPortfolioRequirementsBySecurityType().get(ViewCalculationConfiguration.SECURITY_TYPE_AGGREGATE_ONLY);
    if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
      final ComputationTargetSpecification nodeSpec = ComputationTargetSpecification.of(node);
      for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
        addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), nodeSpec, requiredOutput.getSecond()));
      }
    }
  }

  @Override
  public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
    final Security security = position.getSecurity();
    if (security == null) {
      return;
    }
    store(position.getSecurityLink());
    final String securityType = security.getSecurityType();
    Set<Pair<String, ValueProperties>> requiredOutputs;
    if ((_resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE)
        || (_resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE)) {
      requiredOutputs = _calculationConfiguration.getPortfolioRequirementsBySecurityType().get(securityType);
      if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
        if (_resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE) {
          final Set<Pair<String, ValueProperties>> nodeRequirements = _nodeRequirements.get(parentNode.getUniqueId());
          synchronized (nodeRequirements) {
            nodeRequirements.addAll(requiredOutputs);
          }
        }
        if (_resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE) {
          // TODO: [PLAT-2286] Don't need to create the parent node specification each time
          final ComputationTargetSpecification positionSpec = ComputationTargetSpecification.of(parentNode).containing(ComputationTargetType.POSITION, position.getUniqueId().toLatest());
          for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
            addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), positionSpec, requiredOutput.getSecond()));
          }
        }
      }
    }
    final Collection<Trade> trades = position.getTrades();
    if (!trades.isEmpty()) {
      if (_resultModelDefinition.getTradeOutputMode() != ResultOutputMode.NONE) {
        requiredOutputs = _calculationConfiguration.getTradeRequirementsBySecurityType().get(securityType);
        if ((requiredOutputs != null) && !requiredOutputs.isEmpty()) {
          for (Trade trade : trades) {
            // TODO: [PLAT-2286] Scope the trade underneath it's parent portfolio node and position
            final ComputationTargetSpecification tradeSpec = ComputationTargetSpecification.of(trade);
            for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
              addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), tradeSpec, requiredOutput.getSecond()));
            }
          }
        }
      }
      for (Trade trade : position.getTrades()) {
        store(trade.getSecurityLink());
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
    final ComputationTargetSpecification nodeSpec = ComputationTargetSpecification.of(node);
    for (Pair<String, ValueProperties> requiredOutput : nodeRequirements) {
      addValueRequirement(new ValueRequirement(requiredOutput.getFirst(), nodeSpec, requiredOutput.getSecond()));
    }
  }

}
