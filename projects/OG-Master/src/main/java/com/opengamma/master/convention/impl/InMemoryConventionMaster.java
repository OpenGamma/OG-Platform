/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code ConventionMaster}.
 * <p>
 * This master does not support versioning of conventions.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryConventionMaster
    extends SimpleAbstractInMemoryMaster<ConventionDocument>
    implements ConventionMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemCnv";

  /**
   * Creates an instance.
   */
  public InMemoryConventionMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemoryConventionMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryConventionMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryConventionMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(ConventionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConvention(), "document.convention");
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionMetaDataResult metaData(final ConventionMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    ConventionMetaDataResult result = new ConventionMetaDataResult();
    if (request.isConventionTypes()) {
      Set<ConventionType> types = new HashSet<>();
      for (ConventionDocument doc : _store.values()) {
        types.add(doc.getConvention().getConventionType());
      }
      result.getConventionTypes().addAll(types);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionSearchResult search(final ConventionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<ConventionDocument> list = new ArrayList<ConventionDocument>();
    for (ConventionDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    Collections.sort(list, request.getSortOrder());
    
    ConventionSearchResult result = new ConventionSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ConventionDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Convention not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument add(final ConventionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConvention(), "document.convention");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableConvention convention = document.getConvention().clone();
    convention.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final ConventionDocument doc = new ConventionDocument(convention);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, doc.getVersionFromInstant(), doc.getVersionToInstant(), now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument update(final ConventionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getConvention(), "document.convention");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final ConventionDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Convention not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    document.setUniqueId(uniqueId.withVersion(""));
    document.getValue().setUniqueId(uniqueId.withVersion(""));
    if (_store.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), storedDocument.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    if (_store.remove(objectIdentifiable.getObjectId()) == null) {
      throw new DataNotFoundException("Convention not found: " + objectIdentifiable);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionDocument correct(final ConventionDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionHistoryResult history(final ConventionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final ConventionHistoryResult result = new ConventionHistoryResult();
    final ConventionDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

}
