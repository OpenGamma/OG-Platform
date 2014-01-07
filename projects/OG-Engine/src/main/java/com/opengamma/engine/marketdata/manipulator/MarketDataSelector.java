/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.io.Serializable;
import java.util.Set;

/**
 * Indicates a market data structure on which a shift is to be performed within the execution of a
 * view context. A specification indicates what type and item of market data it wishes to manipulate
 * such that when the engine compiles its dependency graph it can proxy particular market data nodes.
 * The specification does not indicate the actual manipulation to be performed.
 */
public interface MarketDataSelector extends Serializable {

  /**
   * Indicates if the specification contains an active selection to be applied. This allows
   * us to avoid unecessary work applying selections that do nothing whilst also
   * avoiding null checks.
   *
   * @return true if the specification contains active selections
   */
  boolean hasSelectionsDefined();

  /**
   * Indicates the distinct underlying selector that is applicable to the specified market data
   * structure. If one is found, then it is returned.
   *
   *
   *
   * @param structureId the id of the structure to test against
   * @param calculationConfigurationName the calculation configuration
   * @param resolver For looking up data used in the selection criteria, e.g. securities
   * @return the underlying selector that matches the specified market data structure, null if there is no match
   */
  DistinctMarketDataSelector findMatchingSelector(StructureIdentifier<?> structureId,
                                                  String calculationConfigurationName,
                                                  SelectorResolver resolver);

  /**
   * Gets the set of structure types that a selector matches. Most selectors will match just
   * one, but some may be capable of selecting multiple types.
   *
   * @return the set of structure types that a match can be made against, not null
   */
  Set<StructureType> getApplicableStructureTypes();
}
