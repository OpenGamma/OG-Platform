/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.net.URI;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link MarketDataSnapshotMaster}.
 */
public class RemoteMarketDataSnapshotMaster extends AbstractRemoteMaster implements MarketDataSnapshotMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteMarketDataSnapshotMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteMarketDataSnapshotMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataMarketDataSnapshotsResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(MarketDataSnapshotSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataMarketDataSnapshotResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(MarketDataSnapshotDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataMarketDataSnapshotResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(MarketDataSnapshotDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument add(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");
    
    URI uri = DataMarketDataSnapshotsResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(MarketDataSnapshotDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument update(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataMarketDataSnapshotResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(MarketDataSnapshotDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataMarketDataSnapshotResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotHistoryResult history(final MarketDataSnapshotHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataMarketDataSnapshotResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(MarketDataSnapshotHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument correct(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataMarketDataSnapshotResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).get(MarketDataSnapshotDocument.class);
  }

}
