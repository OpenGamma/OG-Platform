/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import java.util.Map;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;

/**
 * This service handles the relationship between running {@link ViewClient}s and {@link StructuredMarketDataSnapshot}s.
 * 
 */
public interface MarketDataSnapshotter {

  //TODO: create snapshot from latest cycle
  
  /**
   * Mode in which to take the snapshot
   */
  public enum Mode {
    /**
     * Include structures such as yield curves, vol surfaces and vol cubes in the snapshot and exclude those points from the unstructured section
     */
    STRUCTURED,
    /**
     * Do not store any structure in the snapshot - data for yield curves etc should all be included in the unstructured part of the snapshot
     */
    UNSTRUCTURED
  }

  /**
   * Produces a snapshot from a view cycle
   * @param client The client to use
   * @param cycle The cycle on which to base the snapshot
   * @return A snapshot representing the data used in the given view cycle
   */
  StructuredMarketDataSnapshot createSnapshot(ViewClient client, ViewCycle cycle);
  
  /**
   * Provides the specifications for interesting values for yield curves included in the given cycle.
   * Grouped by yield curve key and value name.
   * 
   * Currently includes
   * - The curve specification
   * - The nodal curve
   * - The interpolated curve
   *  
   * @param client The client to use
   * @param cycle The cycle on which to base the snapshot
   * @return The specifications grouped by yield curve key and value name
   */
  Map<YieldCurveKey, Map<String, ValueRequirement>> getYieldCurveSpecifications(ViewClient client, ViewCycle cycle);
}
