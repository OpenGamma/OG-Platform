/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * Wraps a snapshot master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class UserSnapshotMaster implements MarketDataSnapshotMaster {

  private final String _userName;
  private final String _clientName;
  private final UserDataTracker _tracker;
  private final MarketDataSnapshotMaster _underlying;

  /**
   * Creates an instance.
   * 
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker of user data, not null
   * @param underlying  the underlying master, not null
   */
  public UserSnapshotMaster(final String userName, final String clientName, final UserDataTracker tracker,
      final MarketDataSnapshotMaster underlying) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _underlying = underlying;
  }

  @Override
  public MarketDataSnapshotDocument get(UniqueIdentifier uniqueId) {
    return _underlying.get(uniqueId);
  }

  @Override
  public MarketDataSnapshotDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _underlying.get(objectId, versionCorrection);
  }

  @Override
  public MarketDataSnapshotDocument add(MarketDataSnapshotDocument document) {
    document = _underlying.add(document);
    if (document.getUniqueId() != null) {
      _tracker.created(_userName, _clientName, UserDataType.MARKET_DATA_SNAPSHOT, document.getUniqueId());
    }
    return document;
  }

  @Override
  public MarketDataSnapshotDocument update(MarketDataSnapshotDocument document) {
    return _underlying.update(document);
  }

  @Override
  public void remove(UniqueIdentifier uniqueId) {
    _underlying.remove(uniqueId);
    _tracker.deleted(_userName, _clientName, UserDataType.MARKET_DATA_SNAPSHOT, uniqueId);
  }

  @Override
  public MarketDataSnapshotDocument correct(MarketDataSnapshotDocument document) {
    return _underlying.correct(document);
  }

  @Override
  public MarketDataSnapshotSearchResult search(MarketDataSnapshotSearchRequest request) {
    return _underlying.search(request);
  }

}
