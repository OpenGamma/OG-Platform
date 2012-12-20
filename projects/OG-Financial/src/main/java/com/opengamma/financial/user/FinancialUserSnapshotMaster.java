/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.ChangeProvidingDecorator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * Wraps a snapshot master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserSnapshotMaster extends AbstractFinancialUserMaster<MarketDataSnapshotDocument> implements MarketDataSnapshotMaster {

  /**
   * The underlying master.
   */
  private final MarketDataSnapshotMaster _underlying;
  private final AbstractChangeProvidingMaster<MarketDataSnapshotDocument> _changeProvidingMaster;


  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserSnapshotMaster(FinancialClient client, MarketDataSnapshotMaster underlying) {
    super(client, FinancialUserDataType.MARKET_DATA_SNAPSHOT);
    _underlying = underlying;
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
  }


  @Override
  public MarketDataSnapshotDocument add(MarketDataSnapshotDocument document) {
    return _changeProvidingMaster.add(document);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, MarketDataSnapshotDocument documentToAdd) {
    return _changeProvidingMaster.addVersion(objectId, documentToAdd);
  }

  @Override
  public MarketDataSnapshotDocument correct(MarketDataSnapshotDocument document) {
    return _changeProvidingMaster.correct(document);
  }

  @Override
  public MarketDataSnapshotDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _changeProvidingMaster.get(objectId, versionCorrection);
  }

  @Override
  public MarketDataSnapshotDocument get(UniqueId uniqueId) {
    return _changeProvidingMaster.get(uniqueId);
  }

  @Override
  public Map<UniqueId, MarketDataSnapshotDocument> get(Collection<UniqueId> uniqueIds) {
    return _changeProvidingMaster.get(uniqueIds);
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
    _changeProvidingMaster.remove(oid);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    _changeProvidingMaster.removeVersion(uniqueId);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<MarketDataSnapshotDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(MarketDataSnapshotDocument replacementDocument) {
    return _changeProvidingMaster.replaceVersion(replacementDocument);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<MarketDataSnapshotDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<MarketDataSnapshotDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public MarketDataSnapshotDocument update(MarketDataSnapshotDocument document) {
    return _changeProvidingMaster.update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeProvidingMaster.changeManager();
  }

  @Override
  public MarketDataSnapshotHistoryResult history(MarketDataSnapshotHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public MarketDataSnapshotSearchResult search(MarketDataSnapshotSearchRequest request) {
    return _underlying.search(request);
  }
}
