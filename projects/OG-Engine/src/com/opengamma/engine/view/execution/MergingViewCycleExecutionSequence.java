/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.List;

import javax.time.Instant;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * 
 */
public abstract class MergingViewCycleExecutionSequence implements ViewCycleExecutionSequence {

  protected ViewCycleExecutionOptions merge(ViewCycleExecutionOptions nextCycle, ViewCycleExecutionOptions defaults) {
    List<MarketDataSpecification> marketDataSpecifications = nextCycle.getMarketDataSpecifications();
    Instant valuationTime = nextCycle.getValuationTime();
    if (defaults != null) {
      if (marketDataSpecifications.isEmpty()) {
        marketDataSpecifications = defaults.getMarketDataSpecifications();
      }
      if (valuationTime == null) {
        valuationTime = defaults.getValuationTime();
      }
    }
    return new ViewCycleExecutionOptions(valuationTime, marketDataSpecifications);
  }

}
