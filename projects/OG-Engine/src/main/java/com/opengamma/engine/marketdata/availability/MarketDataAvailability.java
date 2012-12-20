/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.util.PublicSPI;

/**
 * The availability status of market data, as advertised by a {@link MarketDataAvailabilityProvider}
 * for a given requirement.
 * <p>
 * Availability status is used, for example, in dependency graph building to decide whether
 * to satisfy a requirement directly from market data or use a function application. 
 */
@PublicSPI
public enum MarketDataAvailability {

  /**
   * Market data <em>should</em> be available for the requirement.
   * <p>
   * Note that it is possible that the market data source may be unable to satisfy the
   * requirement at graph execution time despite the {@code AVAILABLE} status.
   */
  AVAILABLE,

  /**
   * Market data is not available. To build a dependency graph, the requirement must be
   * satisfied by some other means such as a function application. 
   */
  NOT_AVAILABLE,

  /**
   * Market data is not available, but should have been.
   * <p>
   * As market data could have been available, a graph building algorithm should not
   * attempt a function application to satisfy the requirement but treat it as
   * unsatisfiable.
   */
  MISSING;

  /**
   * Tests if this enum value is the {@link #AVAILABLE} value.
   * 
   * @return true if this is the {@link #AVAILABLE} value, false otherwise
   */
  public boolean isAvailable() {
    return this == AVAILABLE;
  }

}
