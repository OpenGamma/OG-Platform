/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.*;
import com.opengamma.master.MasterUtils;
import com.opengamma.master.config.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code ConfigMaster}.
 * <p>
 * This master does not support versioning of configuration documents.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryConfigMaster implements ConfigMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemCfg";

  /**
   * A cache of securities by identifier.
   */
  private final ConcurrentMap<ObjectId, ConfigDocument<?>> _store = new ConcurrentHashMap<ObjectId, ConfigDocument<?>>();
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
  public InMemoryConfigMaster() {
    this(new ObjectIdSupplier(InMemoryConfigMaster.DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemoryConfigMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(InMemoryConfigMaster.DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryConfigMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }


  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryConfigMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ConfigDocument<?> document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Config not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> add(ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");

    final Object value = document.getValue();
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();
    IdUtils.setInto(value, uniqueId);
    final ConfigDocument<Object> doc = new ConfigDocument<Object>(document.getType());
    doc.setName(document.getName());
    doc.setValue(value);
    doc.setUniqueId(uniqueId);
    doc.setVersionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, null, uniqueId, now);
    return (ConfigDocument<T>) doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigDocument<T> update(ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getValue(), "document.value");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final ConfigDocument<?> storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Config not found: " + uniqueId);
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
  public void remove(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_store.remove(uniqueId.getObjectId()) == null) {
      throw new DataNotFoundException("Config not found: " + uniqueId);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, uniqueId, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigDocument<T> correct(ConfigDocument<T> document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //------------------------------------------------------------------------- 
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(UniqueId uniqueId, Class<T> clazz) {
    ArgumentChecker.notNull(clazz, "clazz");
    ConfigDocument<?> document = get(uniqueId);
    if (!clazz.isInstance(document.getValue())) {
      throw new DataNotFoundException("Config not found: " + uniqueId.getObjectId());
    }
    return (ConfigDocument<T>) document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    ConfigMetaDataResult result = new ConfigMetaDataResult();
    if (request.isConfigTypes()) {
      Set<Class<?>> types = Sets.newHashSet();
      for (ConfigDocument<?> doc : _store.values()) {
        types.add(doc.getValue().getClass());
      }
      result.getConfigTypes().addAll(types);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    final List<ConfigDocument<T>> list = new ArrayList<ConfigDocument<T>>();
    for (ConfigDocument<?> doc : _store.values()) {
      if (request.matches(doc)) {
        list.add((ConfigDocument<T>) doc);
      }
    }
    final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    result.setPaging(Paging.of(request.getPagingRequest(), list));

    List<ConfigDocument<T>> select = request.getPagingRequest().select(list);

    result.getDocuments().addAll(select);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigHistoryResult<T> history(ConfigHistoryRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    ArgumentChecker.notNull(request.getType(), "request.configClazz");

    final ConfigHistoryResult<T> result = new ConfigHistoryResult<T>();
    final ConfigDocument<T> doc = get(request.getObjectId(), VersionCorrection.LATEST, request.getType());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<T> clazz) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(clazz, "clazz");

    ConfigDocument<?> document = get(objectId, versionCorrection);
    if (!clazz.isInstance(document.getValue())) {
      throw new DataNotFoundException("Config not found: " + objectId);
    }
    return (ConfigDocument<T>) document;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public <T> List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<ConfigDocument<T>> replacementDocuments) {
    return replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public <T> List<UniqueId> replaceVersion(UniqueId uniqueId, List<ConfigDocument<T>> replacementDocuments) {
    return replaceAllVersions(uniqueId.getObjectId(), replacementDocuments);
  }

  @Override
  public <T> List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<ConfigDocument<T>> replacementDocuments) {
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    ArgumentChecker.notNull(objectId, "objectId");
    for (ConfigDocument<T> replacementDocument : replacementDocuments) {
      validateDocument(replacementDocument);
    }
    final Instant now = Instant.now();
    ArgumentChecker.isTrue(MasterUtils.checkUniqueVersionsFrom(replacementDocuments), "No two versioned documents may have the same \"version from\" instant");

    final ConfigDocument<?> storedDocument = _store.get(objectId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Document not found: " + objectId.getObjectId());
    }

    if (replacementDocuments.isEmpty()) {
      _store.remove(objectId.getObjectId());
      //_changeManager.entityChanged(ChangeType.REMOVED, uniqueId, document.getUniqueId(), now);
      return Collections.emptyList();
    } else {
      Instant storedVersionFrom = storedDocument.getVersionFromInstant();
      Instant storedVersionTo = storedDocument.getVersionToInstant();

      List<ConfigDocument<T>> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, storedVersionFrom, storedVersionTo, replacementDocuments);

      ConfigDocument<T> lastReplacementDocument = orderedReplacementDocuments.get(orderedReplacementDocuments.size() - 1);

      if (_store.replace(objectId.getObjectId(), storedDocument, lastReplacementDocument) == false) {
        throw new IllegalArgumentException("Concurrent modification");
      }
      return ImmutableList.of(lastReplacementDocument.getUniqueId());
    }
  }


  @Override
  public <T> UniqueId addVersion(ObjectIdentifiable objectId, ConfigDocument<T> documentToAdd) {
    List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }


  @Override
  public <T> void removeVersion(UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<ConfigDocument<T>>emptyList());
  }

  @Override
  public <T> UniqueId replaceVersion(ConfigDocument<T> replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  private <T> void validateDocument(ConfigDocument<T> document) {
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
  }

}
