/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.*;
import com.opengamma.master.user.*;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, in-memory implementation of {@code UserMaster}.
 * <p>
 * This master does not support versioning of users.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryUserMaster implements UserMaster {
  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemUsr";

  /**
   * A cache of exchanges by identifier.
   */
  private final ConcurrentMap<ObjectId, UserDocument> _store = new ConcurrentHashMap<ObjectId, UserDocument>();
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
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  @Override
  public UserDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public UserDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final UserDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("User not found: " + objectId);
    }
    return document;
  }

  @Override
  public UserDocument add(UserDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.user");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableOGUser user = document.getObject().clone();
    user.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final UserDocument doc = new UserDocument();
    doc.setObject(user);
    doc.setUniqueId(uniqueId);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, doc.getVersionFromInstant(), doc.getVersionToInstant(), now);
    return doc;
  }

  @Override
  public UserDocument update(UserDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getObject(), "document.user");
    
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
    if (_store.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    Instant versionFrom = storedDocument.getVersionFromInstant().isAfter(document.getVersionFromInstant()) ? document.getVersionFromInstant() : storedDocument.getVersionFromInstant();
    Instant versionTo = storedDocument.getVersionToInstant().isBefore(document.getVersionToInstant()) ? document.getVersionToInstant() : storedDocument.getVersionToInstant();
    _changeManager.entityChanged(ChangeType.CHANGED, uniqueId.getObjectId(), versionFrom, versionTo, now);
    return document;
  }

  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    UserDocument removed = _store.remove(objectIdentifiable.getObjectId());
    if (removed == null) {
      throw new DataNotFoundException("User not found: " + objectIdentifiable);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, removed.getObjectId(), removed.getVersionFromInstant(), removed.getVersionToInstant(), Instant.now());
  }

  @Override
  public UserDocument correct(UserDocument document) {
    return update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public UserSearchResult search(UserSearchRequest request) {
    List<UserDocument> docs = new LinkedList<UserDocument>();
    
    for (UserDocument doc : _store.values()) {
      if (request.matches(doc)) {
        docs.add(doc);
      }
    }
    
    return new UserSearchResult(docs);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<UserDocument> replacementDocuments) {

    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    if (replacementDocuments.isEmpty()) {
      //removing a version
      UserDocument removed = _store.remove(uniqueId.getObjectId());
      if (removed == null) {
        throw new DataNotFoundException("User not found: " + uniqueId);
      }
      _changeManager.entityChanged(ChangeType.REMOVED, removed.getObjectId(), removed.getVersionFromInstant(), removed.getVersionToInstant(), Instant.now());
      return Collections.emptyList();
    } else {
      UserDocument document = replacementDocuments.get(replacementDocuments.size() - 1);
      ArgumentChecker.notNull(document, "document");
      ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
      ArgumentChecker.notNull(document.getObject(), "document.user");

      final Instant now = Instant.now();
      final UserDocument storedDocument = _store.get(uniqueId.getObjectId());
      if (storedDocument == null) {
        throw new DataNotFoundException("User not found: " + uniqueId);
      }
      document.setUniqueId(uniqueId.toLatest());
      document.setVersionFromInstant(now);
      document.setVersionToInstant(null);
      document.setCorrectionFromInstant(now);
      document.setCorrectionToInstant(null);
      if (_store.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
        throw new IllegalArgumentException("Concurrent modification");
      }
      Instant versionFrom = storedDocument.getVersionFromInstant().isAfter(document.getVersionFromInstant()) ? document.getVersionFromInstant() : storedDocument.getVersionFromInstant();
      Instant versionTo = storedDocument.getVersionToInstant().isBefore(document.getVersionToInstant()) ? document.getVersionToInstant() : storedDocument.getVersionToInstant();
      _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), versionFrom, versionTo, now);
      return Collections.singletonList(document.getUniqueId());
    }
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<UserDocument> replacementDocuments) {
    return replaceVersion(objectId.getObjectId().atLatestVersion(), replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<UserDocument> replacementDocuments) {
    return replaceVersion(objectId.getObjectId().atLatestVersion(), replacementDocuments);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, UserDocument documentToAdd) {
    List<UniqueId> result = replaceVersion(objectId.getObjectId().atLatestVersion(), Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public UniqueId replaceVersion(UserDocument replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<UserDocument>emptyList());
  }
  
  @Override
  public Map<UniqueId, UserDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, UserDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      UserDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }
}
