/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * Represents a market data snapshot.
 */
@PublicSPI
public interface MarketDataSnapshot extends UniqueIdentifiable {

  // REVIEW 2013-02-01 Andrew -- Shouldn't the scheme be positioned within the Master that allocates identifiers for it?

  /**
   * The ID scheme used for market data snapshots which do not have an alternative identifier.
   */
  String MARKET_DATA_SNAPSHOT_ID_SCHEME = "Mds";

  /**
   * Gets unique id of the market data snapshot.
   * 
   * @return an unique id of the market data snapshot.
   */
  @Override
  UniqueId getUniqueId();

  /**
   * Gets an indication of the time which will be associated with the snapshot once it has been initialised. This might be used to build a dependency graph which will probably be valid for the
   * snapshotted data.
   * <p>
   * For efficiency, implementations which deal with static snapshots may initialise the snapshot and return the actual snapshot time.
   * 
   * @return an indication of the snapshot time, not null
   */
  Instant getSnapshotTimeIndication();

  /**
   * Performs the snapshot based on the data currently available. Some expected values may be missing.
   */
  void init();

  /**
   * Performs the snapshot, and attempts to ensure that the required values are present. Waits no longer than the timeout which, if exceeded, will not cause the operation to fail but implies that one
   * or more required values will be missing.
   * 
   * @param values the values required in the snapshot, not null
   * @param timeout the maximum time to wait for the required values. If less than or equal to zero, the effect is not to wait at all.
   * @param unit the timeout unit, not null
   */
  void init(Set<ValueSpecification> values, long timeout, TimeUnit unit);

  /**
   * Gets whether this market data snapshot has been initialized.
   * 
   * @return true if this market data snapshot has been initialized, false otherwise
   */
  boolean isInitialized();

  /**
   * Gets whether this market data snapshot is empty.
   * <p>
   * This may be used to ignore the snapshot, so implementations may return false for efficiency even if the snapshot may in fact be empty.
   * 
   * @return true if this market data snapshot is empty, false otherwise
   * @throws IllegalStateException if the snapshot has not been initialized by calling {@link #init(Set, long, TimeUnit)}
   */
  boolean isEmpty();

  /**
   * Gets the time associated with the snapshot.
   * 
   * @return the snapshot time, not null
   * @throws IllegalStateException if the snapshot has not been initialized by calling {@link #init(Set, long, TimeUnit)}
   */
  Instant getSnapshotTime();

  /**
   * Queries the snapshot for a piece of data.
   * 
   * @param specification the value required from the snapshot, not null
   * @return the value from the snapshot, null if the snapshot does not exist or no such value was found in the snapshot.
   * @throws IllegalStateException if the snapshot has not been initialized by calling {@link #init(Set, long, TimeUnit)}
   */
  Object query(ValueSpecification specification);

  /**
   * Queries the snapshot for multiple pieces of data.
   * 
   * @param specifications the values required from the snapshot, not null
   * @return the values found in the snapshot, not null but missing entries if values were not found in the snapshot
   * @throws IllegalStateException if the snapshot has not been initialized by calling {@link #init(Set, long, TimeUnit)}
   */
  Map<ValueSpecification, Object> query(Set<ValueSpecification> specifications);

}
