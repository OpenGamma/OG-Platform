/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Filters a dependency graph to exclude any market data sourcing nodes from a previous data provider that are not valid for the new provider.
 */
/* package */final class InvalidMarketDataDependencyNodeFilter implements DependencyNodeFilter {

  private final MarketDataAvailabilityProvider _marketData;

  public InvalidMarketDataDependencyNodeFilter(final MarketDataAvailabilityProvider marketData) {
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
        for (ValueSpecification desiredOutput : dependent.getOutputValues()) {
          // TODO: Infer a "desired value" from the desiredOutput
          // TODO: Test this against the market data availability provider - does the same node pair arise
          System.err.println("TODO: Determine whether " + node + " aliased to " + desiredOutput + " is still valid");
        }
      } else {
        usedRaw = true;
      }
    }
    if (usedRaw) {
      for (ValueSpecification desiredOutput : node.getOutputValues()) {
        // TODO: Infer a "desired value" from the desiredOutput
        // TODO: Test this against the market data availability provider - does the same MDS node arise
        System.err.println("TODO: Determine whether " + node + " producing " + desiredOutput + " is still valid");
      }
    }
    return true;
  }

}
