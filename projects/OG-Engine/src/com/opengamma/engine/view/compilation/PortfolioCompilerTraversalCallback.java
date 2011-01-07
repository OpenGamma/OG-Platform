/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.util.tuple.Pair;

/**
 * Compiles dependency graphs for each stage in a portfolio tree.
 */
/* package */class PortfolioCompilerTraversalCallback extends AbstractPortfolioNodeTraversalCallback {
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioCompilerTraversalCallback.class);
  private final DependencyGraphBuilder _dependencyGraphBuilder;
  private final ViewCalculationConfiguration _calculationConfiguration;
  private final ResultModelDefinition _resultModelDefinition;

  public PortfolioCompilerTraversalCallback(DependencyGraphBuilder dependencyGraphBuilder, ViewCalculationConfiguration calculationConfiguration) {
    _dependencyGraphBuilder = dependencyGraphBuilder;
    _calculationConfiguration = calculationConfiguration;
    _resultModelDefinition = calculationConfiguration.getViewDefinition().getResultModelDefinition();
  }

  /**
   * Gathers all security types. 
   */
  protected static class SubNodeSecurityTypeAccumulator extends AbstractPortfolioNodeTraversalCallback {

    private final Set<String> _subNodeSecurityTypes = new TreeSet<String>();

    /**
     * @return the subNodeSecurityTypes
     */
    public Set<String> getSubNodeSecurityTypes() {
      return _subNodeSecurityTypes;
    }

    @Override
    public void preOrderOperation(Position position) {
      _subNodeSecurityTypes.add(position.getSecurity().getSecurityType());
    }

  }

  /**
   * @param portfolioNode
   * @return
   */
  private static Set<String> getSubNodeSecurityTypes(PortfolioNode portfolioNode) {
    SubNodeSecurityTypeAccumulator accumulator = new SubNodeSecurityTypeAccumulator();
    PortfolioNodeTraverser.depthFirst(accumulator).traverse(portfolioNode);
    return accumulator.getSubNodeSecurityTypes();
  }

  @Override
  public void preOrderOperation(PortfolioNode portfolioNode) {
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(portfolioNode);
    for (Position position : allPositions) {
      for (Trade trade : position.getTrades()) {
        if (trade.getSecurity() == null) {
          s_logger.debug("found a trade with security not resolved {}", trade);
        }
      }
    }
    addPortfolioRequirements(portfolioNode);
    addTradeRequirements(portfolioNode);
  }

  private void addTradeRequirements(PortfolioNode portfolioNode) {
    Set<String> subNodeSecurityTypes = getSubNodeSecurityTypes(portfolioNode);
    Map<String, Set<Pair<String, ValueProperties>>> outputsBySecurityType = _calculationConfiguration.getTradeRequirementsBySecurityType();
    for (String secType : subNodeSecurityTypes) {
      Set<Pair<String, ValueProperties>> requiredOutputs = outputsBySecurityType.get(secType);
      if ((requiredOutputs == null) || requiredOutputs.isEmpty()) {
        continue;
      }
      Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      // add requirements for trades as well
      if (_resultModelDefinition.getTradeOutputMode() != ResultOutputMode.NONE) {
        for (Position position : portfolioNode.getPositions()) {
          requirements.clear();
          for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
            requirements.add(new ValueRequirement(requiredOutput.getFirst(), position, requiredOutput.getSecond()));
          }
          // add requirements for trades as well
          for (Trade trade : position.getTrades()) {
            for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
              requirements.add(new ValueRequirement(requiredOutput.getFirst(), trade, requiredOutput.getSecond()));
            }
          }
          _dependencyGraphBuilder.addTarget(requirements);
        }
      }
    }
  }

  private void addPortfolioRequirements(PortfolioNode portfolioNode) {
    Set<String> subNodeSecurityTypes = getSubNodeSecurityTypes(portfolioNode);
    Map<String, Set<Pair<String, ValueProperties>>> outputsBySecurityType = _calculationConfiguration.getPortfolioRequirementsBySecurityType();
    for (String secType : subNodeSecurityTypes) {
      Set<Pair<String, ValueProperties>> requiredOutputs = outputsBySecurityType.get(secType);
      if ((requiredOutputs == null) || requiredOutputs.isEmpty()) {
        continue;
      }
      Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      // If the outputs are not even required in the results then there's no point adding them as terminal outputs
      if (_resultModelDefinition.getAggregatePositionOutputMode() != ResultOutputMode.NONE) {
        for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
          requirements.add(new ValueRequirement(requiredOutput.getFirst(), portfolioNode, requiredOutput.getSecond()));
        }
        _dependencyGraphBuilder.addTarget(requirements);
      }
      if (_resultModelDefinition.getPositionOutputMode() != ResultOutputMode.NONE) {
        for (Position position : portfolioNode.getPositions()) {
          requirements.clear();
          for (Pair<String, ValueProperties> requiredOutput : requiredOutputs) {
            requirements.add(new ValueRequirement(requiredOutput.getFirst(), position, requiredOutput.getSecond()));
          }
          _dependencyGraphBuilder.addTarget(requirements);
        }
      }
    }
  }
  
}
