/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;

/**
 * Indicates a market data structure on which a shift is to be performed within the execution of a
 * view context. A specification indicates what type and item of market data it wishes to manipulate
 * such that when the engine compiles its dependency graph it can proxy particular market data nodes.
 * The specification does not indicate the actual manipulation to be performed.
 */
public interface MarketDataShiftSpecification {

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
   * Indicates if this shift specification will have any affect when applied to the
   * specified snapshot. Only if this method is true will the {@link #apply} method be called.
   * Note that even if an implementation is applicable to a particular snapshot, the apply
   * method may not have an effect on the snapshot, depending on the actual data it contains.
   *
   * @param calculationConfigurationName@return true if the shift is applicable to the specified snapshot
   */
  boolean appliesTo(StructureIdentifier structureId, String calculationConfigurationName);


  StructureType getApplicableStructureType();

  /**
   * Applies the shift to the underlying snapshot returning a new snapshot. It may be that
   * applying the shift has no effect on the data in the snapshot. If this is the case,
   * then in general the underlying snapshot should be returned directly.
   *
   * @param structuredSnapshot the snapshot to apply the shift to
   * @return a shifted version of the original snapshot
   */
  StructuredMarketDataSnapshot apply(StructuredMarketDataSnapshot structuredSnapshot);

}
