/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import org.threeten.bp.Instant;

/**
 * Represent a snapshot of a curve in a {@code StructuredMarketDataSnapshot}.
 *
 * @see com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot
 */
public interface CurveSnapshot {

  /**
   * Gets the valuation instant.
   * <p>
   * This is the instant at which the yield curve was evaluated, and is
   * used to generate the snapshot keys.
   *
   * @return the valuation instant
   */
  Instant getValuationTime();

  /**
   * Gets the unstructured market data snapshot.
   * <p>
   * This contains the values that should be applied when building the curve.
   *
   * @return the values which should be applied when building this curve
   */
  UnstructuredMarketDataSnapshot getValues();

}
