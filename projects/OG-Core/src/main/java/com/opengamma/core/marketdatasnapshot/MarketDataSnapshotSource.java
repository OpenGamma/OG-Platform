/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of snapshot information as accessed by the main application.
 * <p>
 * This interface provides a simple view of snapshots as needed by the engine.
 * This may be backed by a full-featured snapshot master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface MarketDataSnapshotSource extends Source<StructuredMarketDataSnapshot> {

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

  /**
   * Gets a single snapshot matching by both name and type.
   * <p>
   * A type and name lookup does not guarantee to match a single snapshot element
   * but it normally will. In the case where it does not an implementation will
   * need some mechanism to decide what the best-fit match is.
   *
   * @param  <S> the type of snapshot element
   * @param  type the snapshot type, not null
   * @param  snapshotName the snapshot name, not null
   * @param  versionCorrection the version-correction, not null
   * @return the snapshot matching the name and type, not null
   * @throws IllegalArgumentException if the name or version-correction is invalid
   * @throws DataNotFoundException if the snapshot cannot be found
   * @throws RuntimeException if an error occurs
   */
  <S extends NamedSnapshot> S getSingle(Class<S> type, String snapshotName, VersionCorrection versionCorrection);

}
