/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.listener.MasterChangedType;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code MarketDataSnapshotMaster}.
 * <p>
 * This snapshot master does not support versioning of snapshots.
 */
public class InMemorySnapshotMaster implements MarketDataSnapshotMaster {
  //TODO: This is not hardened for production, as the data in the master can
  // be altered from outside as it is the same object

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemSnap";

  /**
   * A cache of snapshots by identifier.
   */
  private final ConcurrentMap<ObjectId, MarketDataSnapshotDocument> _store = new ConcurrentHashMap<ObjectId, MarketDataSnapshotDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final MasterChangeManager _changeManager;

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
  public InMemorySnapshotMaster(final MasterChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemorySnapshotMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicMasterChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemorySnapshotMaster(final Supplier<ObjectId> objectIdSupplier, final MasterChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
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
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableMarketDataSnapshot snapshot = document.getSnapshot();
    snapshot.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.masterChanged(MasterChangedType.ADDED, null, uniqueId, now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument update(MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");

    final UniqueId uniqueId = document.getUniqueId();
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
    _changeManager.masterChanged(MasterChangedType.UPDATED, uniqueId, document.getUniqueId(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_store.remove(uniqueId.getObjectId()) == null) {
      throw new DataNotFoundException("Security not found: " + uniqueId);
    }
    _changeManager.masterChanged(MasterChangedType.REMOVED, uniqueId, null, Instant.now());
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
    final MarketDataSnapshotDocument doc = _store.get(request.getObjectId());
    final List<MarketDataSnapshotDocument> list = (doc != null) ? Collections.singletonList(doc) : Collections.<MarketDataSnapshotDocument>emptyList();
    final MarketDataSnapshotHistoryResult result = new MarketDataSnapshotHistoryResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(list);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public MasterChangeManager changeManager() {
    return _changeManager;
  }

}
