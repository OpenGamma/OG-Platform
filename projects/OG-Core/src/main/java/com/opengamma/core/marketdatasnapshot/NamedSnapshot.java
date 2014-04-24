/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.id.UniqueIdentifiable;

/**
 * A common interface to support handling both the old-style StructuredMarketDataSnapshot
 * and the new, more specific snapshots.
 */
public interface NamedSnapshot extends UniqueIdentifiable {

  /**
   * Gets the name of the snapshot.
   *
   * @return the name
   */
  String getName();
}
