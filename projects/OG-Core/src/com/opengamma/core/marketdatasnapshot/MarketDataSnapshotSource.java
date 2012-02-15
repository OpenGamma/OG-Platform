/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
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
   * Gets a snapshot by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single snapshot at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the snapshot, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the snapshot could not be found
   * @throws RuntimeException if an error occurs
   */
  StructuredMarketDataSnapshot getSnapshot(UniqueId uniqueId);

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
  void addChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener);

  /**
   * Removes a listener from the source.
   * <p>
   * The listener will cease receiving events for this {@link UniqueId} on the source
   * 
   * @param listener  the listener to remove, not null
   * @param uniqueId the identifier to unregister interest in
   * */
  void removeChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener);

}
