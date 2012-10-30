/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Aggregation functions for portfolios.
 */
public class PortfolioAggregationFunctions {

  /**
   * The aggregators.
   */
  private final List<AggregationFunction<?>> _functions;

  /**
   * Creates an instance.
   * 
   * @param functions  the functions, not null
   */
  public PortfolioAggregationFunctions(Iterable<AggregationFunction<?>> functions) {
    _functions = ImmutableList.copyOf(functions);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the aggregators.
   * 
   * @return the immutable list of aggregators, not null
   */
  public List<AggregationFunction<?>> getFunctions() {
    return _functions;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the aggregators mapped by name.
   * 
   * @return the immutable map of aggregation functions, not null
   */
  public Map<String, AggregationFunction<?>> getMappedFunctions() {
    Map<String, AggregationFunction<?>> result = new HashMap<String, AggregationFunction<?>>();
    for (AggregationFunction<?> portfolioAggregator : _functions) {
      result.put(portfolioAggregator.getName(), portfolioAggregator);
    }
    return ImmutableMap.copyOf(result);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "PortfolioAggregationFunctions" + _functions;
  }

}
