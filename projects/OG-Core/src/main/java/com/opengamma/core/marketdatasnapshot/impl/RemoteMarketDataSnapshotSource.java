/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.net.URI;

import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides remote access to an {@link MarketDataSnapshotSource}.
 */
public class RemoteMarketDataSnapshotSource extends AbstractRemoteSource<NamedSnapshot> implements MarketDataSnapshotSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteMarketDataSnapshotSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public NamedSnapshot get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataMarketDataSnapshotSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(NamedSnapshot.class);
  }

  @Override
  public NamedSnapshot get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
        
    URI uri = DataMarketDataSnapshotSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(NamedSnapshot.class);
  }
  
  @Override
  public void addChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends NamedSnapshot> S getSingle(Class<S> type,
                                               String snapshotName,
                                               VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(snapshotName, "snapshotName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    URI uri = DataMarketDataSnapshotSourceResource.uriSearchSingle(getBaseUri(), type, snapshotName, versionCorrection);
    NamedSnapshot snapshot = accessRemote(uri).get(NamedSnapshot.class);

    if (type.isAssignableFrom(snapshot.getClass())) {
      return type.cast(snapshot);
    } else {
      throw new IllegalArgumentException("The requested object is of type: " +
                                             snapshot.getClass().getName() + ", not " + type.getName());
    }
  }
}
