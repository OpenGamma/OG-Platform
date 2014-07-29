/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.Instant;

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
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.master.legalentity.LegalEntityMetaDataResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code LegalEntityMaster}.
 * <p/>
 * This master does not support versioning of legalEntities.
 * <p/>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryLegalEntityMaster
    extends SimpleAbstractInMemoryMaster<LegalEntityDocument>
    implements LegalEntityMaster {

  /**
   * The default scheme used for each {@link com.opengamma.id.ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemLen";

  /**
   * Creates an instance.
   */
  public InMemoryLegalEntityMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager the change manager, not null
   */
  public InMemoryLegalEntityMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   */
  public InMemoryLegalEntityMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   * @param changeManager    the change manager, not null
   */
  public InMemoryLegalEntityMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(LegalEntityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getLegalEntity(), "document.legalentity");
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityMetaDataResult metaData(final LegalEntityMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    LegalEntityMetaDataResult result = new LegalEntityMetaDataResult();
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntitySearchResult search(final LegalEntitySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<LegalEntityDocument> list = new ArrayList<LegalEntityDocument>();
    for (LegalEntityDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    Collections.sort(list, request.getSortOrder());

    LegalEntitySearchResult result = new LegalEntitySearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final LegalEntityDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("LegalEntity not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument add(final LegalEntityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getLegalEntity(), "document.legalentity");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableLegalEntity legalentity = document.getLegalEntity().clone();
    legalentity.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final LegalEntityDocument doc = new LegalEntityDocument(legalentity);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, doc.getVersionFromInstant(), doc.getVersionToInstant(), now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument update(final LegalEntityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getLegalEntity(), "document.legalentity");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final LegalEntityDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("LegalEntity not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    document.setUniqueId(uniqueId.withVersion(""));
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
      throw new DataNotFoundException("LegalEntity not found: " + objectIdentifiable);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityDocument correct(final LegalEntityDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityHistoryResult history(final LegalEntityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final LegalEntityHistoryResult result = new LegalEntityHistoryResult();
    final LegalEntityDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

}
