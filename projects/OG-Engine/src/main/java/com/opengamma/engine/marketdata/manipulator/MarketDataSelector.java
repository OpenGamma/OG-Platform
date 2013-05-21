/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

/**
 * Indicates a market data structure on which a shift is to be performed within the execution of a
 * view context. A specification indicates what type and item of market data it wishes to manipulate
 * such that when the engine compiles its dependency graph it can proxy particular market data nodes.
 * The specification does not indicate the actual manipulation to be performed.
 */
public interface MarketDataSelector {

  /**
   * The type of market data which is to take part in a manipulation.
   */
  public enum StructureType {YIELD_CURVE, VOLATILITY_SURFACE, VOLATILITY_CUBE, MARKET_DATA_POINT, NONE}

  /**
   * Indicates if the specification contains an active shift to be applied. This allows
   * us to avoid unecessary work applying specifications that do nothing whilst also
   * avoiding null checks.
   *
   * @return true if the specification contains active shifts
   */
  boolean containsShifts();

  /**
   * Indicates if this selector is applicable to the specified market data structure. If it is, then
   * the underlying selector that matches is returned.
   *
   * @param structureId the id of the structure to test against
   * @param calculationConfigurationName the calculation configuration
   * @return the underlying selector that matches the specified market data structure, null if there is no match
   */
  MarketDataSelector findMatchingSelector(StructureIdentifier structureId, String calculationConfigurationName);

  StructureType getApplicableStructureType();

}
