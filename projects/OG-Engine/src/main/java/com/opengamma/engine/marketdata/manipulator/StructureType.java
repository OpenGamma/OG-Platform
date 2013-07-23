/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.engine.value.ValueRequirementNames;

/**
 * The type of market data which is to take part in a manipulation with an
 * associated extractor for extracting the daat from a dependency graph node.
 */
public enum StructureType {

  /**
   * Represents a yield curve structure within a dependency graph.
   */
  YIELD_CURVE(new YieldCurveNodeExtractor()),

  /**
   * Represents a volatility surface structure within a dependency graph.
   */
  VOLATILITY_SURFACE(new VolatilitySurfaceNodeExtractor(ValueRequirementNames.VOLATILITY_SURFACE)),

  /**
   * Represents a volatility surface structure within a dependency graph.
   */
  INTERPOLATED_VOLATILITY_SURFACE(new VolatilitySurfaceNodeExtractor(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE)),

  /**
   * Represents a volatility cube structure within a dependency graph.
   */
  VOLATILITY_CUBE(new VolatilityCubeNodeExtractor()),

  /**
   * Represents a market data point within a dependency graph.
   */
  MARKET_DATA_POINT(new MarketDataPointNodeExtractor());

  /**
   * The extractor for a particular structured data type.
   */
  private final NodeExtractor _nodeExtractor;

  private StructureType(NodeExtractor nodeExtractor) {
    _nodeExtractor = nodeExtractor;
  }

  /**
   * Gets the extractor for a structured data type.
   *
   * @return the associated extractor for this instance
   */
  public NodeExtractor getNodeExtractor() {
    return _nodeExtractor;
  }
}
