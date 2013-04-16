/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * An implementation of {@link MarketDataSnapshot} backed by an {@link InMemoryLKVMarketDataProvider}.
 */
public class InMemoryLKVMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataSnapshot.class);

  private final InMemoryLKVMarketDataProvider _provider;
  private Instant _snapshotTime;
  private Map<ValueSpecification, Object> _snapshot;

  public InMemoryLKVMarketDataSnapshot(final InMemoryLKVMarketDataProvider provider) {
    _provider = provider;
  }

  @Override
  public UniqueId getUniqueId() {
    // REVIEW 2013-02-04 Andrew -- This is not a suitable unique id. It should be allocated by whatever stores/creates these snapshots
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "InMemoryLKVMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotTime != null ? _snapshotTime : Instant.now();
  }

  @Override
  public void init() {
    init(null, 0, null);
  }

  @Override
  public void init(final Set<ValueSpecification> valuesRequired, final long timeout, final TimeUnit unit) {
    _snapshot = getProvider().doSnapshot();
    _snapshotTime = Instant.now();
    s_logger.debug("Snapshotted at {}", _snapshotTime);
  }
  
  @Override
  public boolean isInitialized() {
    return _snapshot != null;
  }
  
  @Override
  public boolean isEmpty() {
    assertInitialized();
    return _snapshot.isEmpty();
  }

  @Override
  public Instant getSnapshotTime() {
    return _snapshotTime;
  }

  @Override
  public Object query(final ValueSpecification value) {
    return getSnapshot().get(value);
  }

  //-------------------------------------------------------------------------
  public Set<ValueSpecification> getAllValueKeys() {
    return Collections.unmodifiableSet(getSnapshot().keySet());
  }

  //-------------------------------------------------------------------------
  private Map<ValueSpecification, Object> getSnapshot() {
    if (_snapshot == null) {
      throw new IllegalStateException("Snapshot has not been initialised");
    }
    return _snapshot;
  }

  private InMemoryLKVMarketDataProvider getProvider() {
    return _provider;
  }

}
