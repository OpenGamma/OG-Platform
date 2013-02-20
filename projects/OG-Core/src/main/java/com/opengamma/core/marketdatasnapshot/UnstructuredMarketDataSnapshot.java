/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;
import java.util.Set;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * A snapshot of market data taken at a particular instant, potentially altered by hand, that should be applied to computations in some scope, such as a yield curve.
 */
public interface UnstructuredMarketDataSnapshot {

  /**
   * Tests if the snapshot is empty (contains no values for any targets).
   * 
   * @return true if it is empty, false otherwise
   */
  boolean isEmpty();

  /**
   * Retrieves the value associated with the target identifier.
   * 
   * @param identifier the target identifier, not null
   * @param valueName the value name, not null
   * @return the associated value or null if none
   */
  ValueSnapshot getValue(ExternalId identifier, String valueName);

  /**
   * Retrieves the value associated with the target identifiers. If there are multiple matching values then an arbitrary choice is made.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   * @return the associated value or null if none
   */
  ValueSnapshot getValue(ExternalIdBundle identifiers, String valueName);

  /**
   * Returns all of the targets in the snapshot. The unique target identifier bundles are returned.
   * 
   * @return the targets in the bundle, not null
   */
  Set<ExternalIdBundle> getTargets();

  /**
   * Returns all of the values from the snapshot for a target. Only the values exactly matching the target bundle will be returned (that is, it is one of the target bundles returned by
   * {@link #getTargets}).
   * 
   * @param identifiers the target identifiers, not null
   * @return the values for that target or null if none
   */
  Map<String, ValueSnapshot> getTargetValues(ExternalIdBundle identifiers);

}
