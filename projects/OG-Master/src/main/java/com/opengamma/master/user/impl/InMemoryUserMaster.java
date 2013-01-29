/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

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
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserHistoryRequest;
import com.opengamma.master.user.UserHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code UserMaster}.
 * <p>
 * This master does not support versioning of users.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryUserMaster
    extends SimpleAbstractInMemoryMaster<UserDocument>
    implements UserMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemUsr";

  /**
   * Creates an instance.
   */
  public InMemoryUserMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemoryUserMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryUserMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryUserMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(UserDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUser(), "document.user");
  }

  //-------------------------------------------------------------------------
  @Override
  public UserSearchResult search(final UserSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<UserDocument> list = new ArrayList<UserDocument>();
    for (UserDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    Collections.sort(list, request.getSortOrder());
    
    UserSearchResult result = new UserSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final UserDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("User not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDocument add(final UserDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUser(), "document.user");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableOGUser user = document.getUser().clone();
    user.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final UserDocument doc = new UserDocument(user);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, doc.getVersionFromInstant(), doc.getVersionToInstant(), now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDocument update(final UserDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getUser(), "document.user");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final UserDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("User not found: " + uniqueId);
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
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    if (_store.remove(objectIdentifiable.getObjectId()) == null) {
      throw new DataNotFoundException("User not found: " + objectIdentifiable);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDocument correct(final UserDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserHistoryResult history(final UserHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final UserHistoryResult result = new UserHistoryResult();
    final UserDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

}
