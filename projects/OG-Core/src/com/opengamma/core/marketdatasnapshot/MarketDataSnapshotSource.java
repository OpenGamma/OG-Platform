/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A source of snapshot information as accessed by the main application.
 * <p>
 * This interface provides a simple view of snapshots as needed by the engine.
 * This may be backed by a full-featured snapshot master, or by a much simpler data structure.
 */
@PublicSPI
public interface MarketDataSnapshotSource {

  /**
   * Finds a specific snapshot by unique identifier.
   * <p>
   * Since a unique identifier is unique, there are no complex matching issues.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the snapshot, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  StructuredMarketDataSnapshot getSnapshot(UniqueIdentifier uniqueId);

  /**
  * Adds a listener to the source.
  * <p>
  * The listener will receive events for the source which change the result of:
  * 
  * <code>
  * getSnapshot(uniqueId);
  * </code>
  * 
  * @param listener  the listener to add, not null
  * @param uniqueId the identifier to register interest in
  * */
  void addChangeListener(UniqueIdentifier uniqueId, MarketDataSnapshotChangeListener listener);

  /**
   * Removes a listener from the source.
   * <p>
   * The listener will cease receiving events for this {@link UniqueIdentifier} on the source
   * 
   * @param listener  the listener to remove, not null
   * @param uniqueId the identifier to unregister interest in
   * */
  void removeChangeListener(UniqueIdentifier uniqueId, MarketDataSnapshotChangeListener listener);

}
