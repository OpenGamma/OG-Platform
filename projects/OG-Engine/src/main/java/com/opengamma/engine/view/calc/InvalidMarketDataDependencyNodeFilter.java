/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Filters a dependency graph to exclude any market data sourcing nodes from a previous data provider that are not valid for the new provider.
 */
/* package */final class InvalidMarketDataDependencyNodeFilter implements DependencyNodeFilter {

  private static final Logger s_logger = LoggerFactory.getLogger(InvalidMarketDataDependencyNodeFilter.class);

  private final ComputationTargetResolver.AtVersionCorrection _targetResolver;
  private final MarketDataAvailabilityProvider _marketData;

  public InvalidMarketDataDependencyNodeFilter(final ComputationTargetResolver.AtVersionCorrection targetResolver, final MarketDataAvailabilityProvider marketData) {
    _targetResolver = targetResolver;
    _marketData = marketData;
  }

  // DependencyNodeFilter

  @Override
  public boolean accept(final DependencyNode node) {
    if (!(node.getFunction().getFunction() instanceof MarketDataSourcingFunction)) {
      return true;
    }
    boolean usedRaw = false;
    for (DependencyNode dependent : node.getDependentNodes()) {
      if (dependent.getFunction().getFunction() instanceof MarketDataAliasingFunction) {
        final ComputationTarget target = _targetResolver.resolve(dependent.getComputationTarget());
        if (target == null) {
          // This shouldn't normally happen (a default target specification will always be created that gives a stub Primitive instance) unless
          // the target specification cannot be resolved by the target resolver any more.
          s_logger.warn("Couldn't resolve {}", dependent.getComputationTarget());
          return false;
        }
        final Object targetValue = target.getValue();
        for (ValueSpecification desiredOutput : dependent.getOutputValues()) {
          final ValueRequirement desiredValue = desiredOutput.toRequirementSpecification();
          final ValueSpecification marketData = _marketData.getAvailability(dependent.getComputationTarget(), targetValue, desiredValue);
          if (marketData != null) {
            if (node.getOutputValues().contains(marketData)) {
              s_logger.debug("Raw market data entry {} still available for {}", marketData, desiredOutput);
            } else {
              // TODO: This might not actually mean invalidating all downstream nodes - it may be possible to just swap out the MarketDataSourcingFunction
              s_logger.debug("New raw market data of {} required for {}", marketData, desiredOutput);
              return false;
            }
          } else {
            s_logger.debug("Indirect use of {} via {} no longer available from market data provider", node, desiredOutput);
            return false;
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
        return false;
      }
      final Object targetValue = target.getValue();
      for (ValueSpecification desiredOutput : node.getOutputValues()) {
        final ValueRequirement desiredValue = desiredOutput.toRequirementSpecification();
        final ValueSpecification marketData = _marketData.getAvailability(node.getComputationTarget(), targetValue, desiredValue);
        if (marketData != null) {
          if (desiredOutput.equals(marketData)) {
            s_logger.debug("Market data entry {} still available", desiredOutput);
          } else {
            s_logger.debug("New market data of {} required for {}", marketData, desiredOutput);
            return false;
          }
        } else {
          s_logger.debug("{} no longer available from market data provider", desiredOutput);
          return false;
        }
      }
    }
    return true;
  }

}
