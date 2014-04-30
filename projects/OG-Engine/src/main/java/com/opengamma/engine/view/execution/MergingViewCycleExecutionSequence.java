/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.List;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Execution sequence that creates execution options by merging two sets of options. Values are used from a base set of options if available, otherwise they are taken from a default set.
 */
public abstract class MergingViewCycleExecutionSequence implements ViewCycleExecutionSequence {

  /**
   * Returns execution options created by merging two sets of options. Values are used from the base set of options if available, otherwise they are taken from the defaults.
   * 
   * @param base The base set of execution options, not null
   * @param defaults The default options whose values are used if there are values missing in the base options, can be null
   * @return A set of merged options, not null
   */
  protected ViewCycleExecutionOptions merge(ViewCycleExecutionOptions base, ViewCycleExecutionOptions defaults) {
    List<MarketDataSpecification> marketDataSpecifications = base.getMarketDataSpecifications();
    MarketDataSelector marketDataSelector = base.getMarketDataSelector();
    Map<DistinctMarketDataSelector, FunctionParameters> functionParameters = base.getFunctionParameters();
    Instant valuationTime = base.getValuationTime();
    String name = base.getName();
    if (defaults != null) {
      if (marketDataSpecifications.isEmpty()) {
        marketDataSpecifications = defaults.getMarketDataSpecifications();
      }
      marketDataSelector = defaults.getMarketDataSelector();

      if (functionParameters.isEmpty() && !defaults.getFunctionParameters().isEmpty()) {
        functionParameters = defaults.getFunctionParameters();
      }

      if (valuationTime == null) {
        valuationTime = defaults.getValuationTime();
      }
      
      if (name == null) {
        name = defaults.getName();
      }
    }
    return ViewCycleExecutionOptions.builder()
        .setName(name)
        .setValuationTime(valuationTime)
        .setMarketDataSpecifications(marketDataSpecifications)
        .setMarketDataSelector(marketDataSelector)
        .setFunctionParameters(functionParameters)
        .create();
  }

}
