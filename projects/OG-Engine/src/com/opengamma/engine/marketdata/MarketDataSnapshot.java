/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;

/**
 * Represents a market data snapshot.
 */
@PublicSPI
public interface MarketDataSnapshot {

  /**
   * Gets an indication of the time which will be associated with the snapshot once it has been initialised. This might
   * be used to build a dependency graph which will probably be valid for the snapshotted data.
   * <p>
   * For efficiency, implementations which deal with static snapshots may initialise the snapshot and return the actual
   * snapshot time.
   * 
   * @return an indication of the snapshot time, not {@code null}
   */
  Instant getSnapshotTimeIndication();
  
  /**
   * Performs the snapshot based on the data currently available. Some expected values may be missing.
   */
  void init();
  
  /**
   * Performs the snapshot, and attempts to ensure that the required values are present. Waits no longer than the
   * timeout which, if exceeded, will not cause the operation to fail but implies that one or more required values will
   * be missing.
   * 
   * @param valuesRequired  the values required in the snapshot, not {@code null}
   * @param timeout  the maximum time to wait for the required values
   * @param unit  the timeout unit, not {@code null}
   */
  void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit);
  
  /**
   * Gets the time associated with the snapshot.
   * 
   * @return the snapshot time, not {@code null}
   * @throws IllegalStateException  if the snapshot has not been initialised by calling {@link #init(Set, long, TimeUnit)}
   */
  Instant getSnapshotTime();
  
  /**
   * Queries whether this snapshot contains structured data. If this method returns {@code false} then any structured
   * data query should return {@code null}.
   * 
   * @return {@code true} if this snapshot contains structured data, {@code false} otherwise
   * @throws IllegalStateException  if the snapshot has not been initialised by calling {@link #init(Set, long, TimeUnit)}
   */
  boolean hasStructuredData();
  
  /**
   * Queries the snapshot for a piece of data.
   *  
   * @param requirement  the value required from the snapshot, not {@code null}
   * @return  the value found in the snapshot, or {@code null} if the snapshot does not exist or no such value was
   *          found in the snapshot.
   * @throws IllegalStateException  if the snapshot has not been initialised by calling {@link #init(Set, long, TimeUnit)}
   */
  Object query(ValueRequirement requirement); 
  
  /**
   * Queries a snapshot for a bundle of structured market data.
   *  
   * @param marketDataKey the structured market data key, not {@code null}
   * @return  the value found in the snapshot, or {@code null} if the snapshot does not exist or no such value was
   *          found in the snapshot.
   * @throws IllegalStateException  if the snapshot has not been initialised by calling {@link #init(Set, long, TimeUnit)}
   */
  Object query(StructuredMarketDataKey marketDataKey);

}
