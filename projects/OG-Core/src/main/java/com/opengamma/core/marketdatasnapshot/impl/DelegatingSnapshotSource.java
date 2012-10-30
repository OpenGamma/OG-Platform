/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static com.google.common.collect.Maps.newHashMap;
import static com.opengamma.util.functional.Functional.groupBy;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.functional.Function1;

/**
 * A source of snapshots that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSnapshotSource extends UniqueIdSchemeDelegator<MarketDataSnapshotSource> implements MarketDataSnapshotSource {

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
  public StructuredMarketDataSnapshot get(ObjectId objectId, VersionCorrection versionCorrection) {
    return chooseDelegate(objectId.getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public StructuredMarketDataSnapshot get(UniqueId uniqueId) {
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public Map<UniqueId, StructuredMarketDataSnapshot> get(Collection<UniqueId> uniqueIds) {
    Map<String, Collection<UniqueId>> groups = groupBy(uniqueIds, new Function1<UniqueId, String>() {
      @Override
      public String execute(UniqueId uniqueId) {
        return uniqueId.getScheme();
      }
    });    
    
    Map<UniqueId, StructuredMarketDataSnapshot> snapshots = newHashMap();
    
    for (Map.Entry<String, Collection<UniqueId>> entries : groups.entrySet()) {
      snapshots.putAll(chooseDelegate(entries.getKey()).get(entries.getValue()));
    }
    
    return snapshots;
  }
  
  @Override
  public void addChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    chooseDelegate(uniqueId.getScheme()).addChangeListener(uniqueId, listener);
  }

  @Override
  public void removeChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    chooseDelegate(uniqueId.getScheme()).removeChangeListener(uniqueId, listener);
  }
}
