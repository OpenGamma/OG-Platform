/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionDocumentComparator;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code RegionMaster}.
 * <p>
 * This master does not support versioning of regions.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryRegionMaster implements RegionMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemReg";

  /**
   * A cache of regions by identifier.
   */
  private final ConcurrentMap<ObjectId, RegionDocument> _store = new ConcurrentHashMap<ObjectId, RegionDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   */
  public InMemoryRegionMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public InMemoryRegionMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryRegionMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }


  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryRegionMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionSearchResult search(final RegionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<RegionDocument> list = new ArrayList<RegionDocument>();
    for (RegionDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    Collections.sort(list, RegionDocumentComparator.ASC);
    RegionSearchResult result = new RegionSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument get(final ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final RegionDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Region not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument add(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getRegion(), "document.region");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableRegion region = document.getRegion();
    region.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    _store.put(objectId, document);
    _changeManager.entityChanged(ChangeType.ADDED, null, uniqueId, now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument update(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getRegion(), "document.region");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final RegionDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Region not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_store.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.UPDATED, uniqueId, document.getUniqueId(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_store.remove(uniqueId.getObjectId()) == null) {
      throw new DataNotFoundException("Region not found: " + uniqueId);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, uniqueId, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument correct(final RegionDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionHistoryResult history(final RegionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final RegionHistoryResult result = new RegionHistoryResult();
    final RegionDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
