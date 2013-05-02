/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Filters a dependency graph to exclude any market data sourcing nodes from a previous data provider that are not valid for the new provider.
 */
/* package */final class InvalidMarketDataDependencyNodeFilter implements DependencyNodeFilter {

  private static final Logger s_logger = LoggerFactory.getLogger(InvalidMarketDataDependencyNodeFilter.class);

  private final ComputationTargetResolver.AtVersionCorrection _targetResolver;
  private final MarketDataAvailabilityProvider _marketData;
  private final Map<ValueSpecification, Boolean> _valid = new ConcurrentHashMap<ValueSpecification, Boolean>();
  private int _invalidNodes;

  public InvalidMarketDataDependencyNodeFilter(final ComputationTargetResolver.AtVersionCorrection targetResolver, final MarketDataAvailabilityProvider marketData) {
    _targetResolver = targetResolver;
    _marketData = marketData;
  }

  private ValueRequirement inferValueRequirement(final ValueSpecification outputValue) {
    return new ValueRequirement(outputValue.getValueName(), outputValue.getTargetSpecification(), outputValue.getProperties().withoutAny(ValuePropertyNames.DATA_PROVIDER));
  }

  private void visit(final ValueSpecification marketData, final DependencyNode node) {
    if (_valid.containsKey(marketData)) {
      return;
    }
    boolean usedRaw = node.hasTerminalOutputValues();
    for (DependencyNode dependent : node.getDependentNodes()) {
      if (dependent.getFunction().getFunction() instanceof MarketDataAliasingFunction) {
        final ComputationTarget target = _targetResolver.resolve(dependent.getComputationTarget());
        if (target == null) {
          // This shouldn't normally happen (a default target specification will always be created that gives a stub Primitive instance) unless
          // the target specification cannot be resolved by the target resolver any more.
          s_logger.warn("Couldn't resolve {}", dependent.getComputationTarget());
          _valid.put(marketData, Boolean.FALSE);
          _invalidNodes++;
          return;
        }
        final Object targetValue = target.getValue();
        for (ValueSpecification desiredOutput : dependent.getOutputValues()) {
          final ValueRequirement desiredValue = inferValueRequirement(desiredOutput);
          final ValueSpecification requiredMarketData = _marketData.getAvailability(dependent.getComputationTarget(), targetValue, desiredValue);
          if (marketData.equals(requiredMarketData)) {
            s_logger.debug("Market data entry {} still available for {}", marketData, desiredValue);
          } else {
            s_logger.debug("New market data {} required for {}", requiredMarketData, desiredValue);
            _valid.put(marketData, Boolean.FALSE);
            _invalidNodes++;
            return;
          }
        }
      } else {
        usedRaw = true;
      }
    }
    if (usedRaw) {
      final ComputationTarget target = _targetResolver.resolve(node.getComputationTarget());
      if (target == null) {
        // This shouldn't normally happen (a default target specification will always be created that gives a stub Primitive instance) unless
        // the target specification cannot be resolved by the target resolver any more.
        s_logger.warn("Couldn't resolve {}", node.getComputationTarget());
        _valid.put(marketData, Boolean.FALSE);
        _invalidNodes++;
        return;
      }
      final Object targetValue = target.getValue();
      final ValueRequirement desiredValue = inferValueRequirement(marketData);
      final ValueSpecification requiredMarketData = _marketData.getAvailability(node.getComputationTarget(), targetValue, desiredValue);
      if (marketData.equals(requiredMarketData)) {
        s_logger.debug("Market data entry {} still available", marketData);
      } else {
        s_logger.debug("New market data of {} required for {}", requiredMarketData, desiredValue);
        _valid.put(marketData, Boolean.FALSE);
        _invalidNodes++;
        return;
      }
    }
    _valid.put(marketData, Boolean.TRUE);
  }

  public final class VisitBatch implements Runnable {

    private final DependencyNode[] _nodes;
    private final ValueSpecification[] _specifications;
    private int _index;

    private VisitBatch(final int size) {
      _nodes = new DependencyNode[size];
      _specifications = new ValueSpecification[size];
    }

    public boolean isFull() {
      return _index == _nodes.length;
    }

    public void add(final ValueSpecification specification, final DependencyNode node) {
      final int i = _index++;
      _nodes[i] = node;
      _specifications[i] = specification;
    }

    @Override
    public void run() {
      for (int i = 0; i < _index; i++) {
        visit(_specifications[i], _nodes[i]);
      }
    }

  }

  public VisitBatch visit(final int size) {
    return new VisitBatch(size);
  }

  public boolean hasInvalidNodes() {
    s_logger.info("{} invalid market data values (of {})", _invalidNodes, _valid.size());
    return _invalidNodes > 0;
  }

  // DependencyNodeFilter

  @Override
  public boolean accept(final DependencyNode node) {
    if (!(node.getFunction().getFunction() instanceof MarketDataSourcingFunction)) {
      return true;
    }
    for (ValueSpecification output : node.getOutputValues()) {
      if (Boolean.TRUE != _valid.get(output)) {
        return false;
      }
    }
    return true;
  }

}
