/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * A common interface to support handling both the old-style
 * {@code StructuredMarketDataSnapshot} and the new, more specific
 * snapshots.
 */
public interface NamedSnapshot extends UniqueIdentifiable {

  /**
   * Gets the name of the snapshot.
   *
   * @return the name, not null
   */
  String getName();

  /**
   * Create a version of this snapshot with the unique id set to the
   * supplied value. Ideally, mutable implementations would return a
   * copy but this interface does not make this restriction.
   * <p>
   * This is intended for use by masters when inserting new snapshots.
   *
   * @param uniqueId  the new value for the unique id, not null
   * @return a copy of this snapshot, not null
   */
  NamedSnapshot withUniqueId(UniqueId uniqueId);
}
