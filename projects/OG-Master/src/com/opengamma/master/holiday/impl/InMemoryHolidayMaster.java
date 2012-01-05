/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code HolidayMaster}.
 * <p>
 * This master does not support versioning of holidays.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryHolidayMaster implements HolidayMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemHol";

  /**
   * A cache of holidays by identifier.
   */
  private final ConcurrentMap<ObjectId, HolidayDocument> _store = new ConcurrentHashMap<ObjectId, HolidayDocument>();
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
  public InMemoryHolidayMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public InMemoryHolidayMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryHolidayMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }


  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryHolidayMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayMetaDataResult metaData(HolidayMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    HolidayMetaDataResult result = new HolidayMetaDataResult();
    if (request.isHolidayTypes()) {
      result.getHolidayTypes().addAll(Arrays.asList(HolidayType.values()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<HolidayDocument> list = new ArrayList<HolidayDocument>();
    for (HolidayDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    final HolidaySearchResult result = new HolidaySearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final HolidayDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Holiday not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument add(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableHoliday holiday = document.getHoliday();
    holiday.setUniqueId(uniqueId);
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
  public HolidayDocument update(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final HolidayDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Holiday not found: " + uniqueId);
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
      throw new DataNotFoundException("Holiday not found: " + uniqueId);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, uniqueId, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument correct(final HolidayDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final HolidayHistoryResult result = new HolidayHistoryResult();
    final HolidayDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
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
