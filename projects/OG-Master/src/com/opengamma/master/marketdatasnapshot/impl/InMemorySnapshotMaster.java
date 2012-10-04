/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.time.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.*;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.marketdatasnapshot.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code MarketDataSnapshotMaster}.
 * <p>
 * This snapshot master does not support versioning of snapshots.
 */
public class InMemorySnapshotMaster extends SimpleAbstractInMemoryMaster<ManageableMarketDataSnapshot, MarketDataSnapshotDocument> implements MarketDataSnapshotMaster {
  //TODO: This is not hardened for production, as the data in the master can
  // be altered from outside as it is the same object

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemSnap";


  /**
   * Creates an instance.
   */
  public InMemorySnapshotMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemorySnapshotMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemorySnapshotMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemorySnapshotMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  protected void validateScheme(final String scheme) {
  }

  protected void validateUniqueId(final UniqueId uniqueId) {
    validateScheme(uniqueId.getScheme());
  }

  protected void validateObjectId(final ObjectId objectId) {
    validateScheme(objectId.getScheme());
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotSearchResult search(MarketDataSnapshotSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<MarketDataSnapshotDocument> list = new ArrayList<MarketDataSnapshotDocument>();
    for (MarketDataSnapshotDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    MarketDataSnapshotSearchResult result = new MarketDataSnapshotSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    validateUniqueId(uniqueId);
    final MarketDataSnapshotDocument document = _store.get(uniqueId.getObjectId());
    if (document == null || !document.getUniqueId().equals(uniqueId)) {
      throw new DataNotFoundException("Snapshot not found: " + uniqueId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    validateObjectId(objectId.getObjectId());
    final MarketDataSnapshotDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Snapshot not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument add(MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.snapshot");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableMarketDataSnapshot snapshot = document.getObject();
    snapshot.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, doc.getVersionFromInstant(), doc.getVersionToInstant(), now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument update(MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getObject(), "document.snapshot");
    final UniqueId uniqueId = document.getUniqueId();
    validateUniqueId(uniqueId);
    final Instant now = Instant.now();
    final MarketDataSnapshotDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Snapshot not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_store.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    validateObjectId(objectIdentifiable.getObjectId());
    if (_store.remove(objectIdentifiable.getObjectId()) == null) {
      throw new DataNotFoundException("Security not found: " + objectIdentifiable);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument correct(MarketDataSnapshotDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotHistoryResult history(MarketDataSnapshotHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    validateObjectId(request.getObjectId());
    final MarketDataSnapshotDocument doc = _store.get(request.getObjectId());
    final List<MarketDataSnapshotDocument> list = (doc != null) ? Collections.singletonList(doc) : Collections.<MarketDataSnapshotDocument>emptyList();
    final MarketDataSnapshotHistoryResult result = new MarketDataSnapshotHistoryResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(list);
    return result;
  }

  @Override
  protected void validateDocument(MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.snapshot");
  }
}
