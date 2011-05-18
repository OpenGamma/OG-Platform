/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.util.Map;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSchemeDelegator;

/**
 * A source of snapshots that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSnapshotSource extends UniqueIdentifierSchemeDelegator<MarketDataSnapshotSource> implements MarketDataSnapshotSource {

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingSnapshotSource(MarketDataSnapshotSource defaultSource) {
    super(defaultSource);
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingSnapshotSource(MarketDataSnapshotSource defaultSource, Map<String, MarketDataSnapshotSource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
  }
  
  @Override
  public StructuredMarketDataSnapshot getSnapshot(UniqueIdentifier uid) {
    return chooseDelegate(uid).getSnapshot(uid);
  }

}
