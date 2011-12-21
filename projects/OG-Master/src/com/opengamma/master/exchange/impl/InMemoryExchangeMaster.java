/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.util.ArrayList;
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
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code ExchangeMaster}.
 * <p>
 * This master does not support versioning of exchanges.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryExchangeMaster implements ExchangeMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemExg";

  /**
   * A cache of exchanges by identifier.
   */
  private final ConcurrentMap<ObjectId, ExchangeDocument> _store = new ConcurrentHashMap<ObjectId, ExchangeDocument>();
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
  public InMemoryExchangeMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public InMemoryExchangeMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryExchangeMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }


  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryExchangeMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchResult search(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<ExchangeDocument> list = new ArrayList<ExchangeDocument>();
    for (ExchangeDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(doc);
      }
    }
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ExchangeDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Exchange not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument add(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final ManageableExchange exchange = document.getExchange().clone();
    exchange.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final ExchangeDocument doc = new ExchangeDocument();
    doc.setExchange(exchange);
    doc.setUniqueId(uniqueId);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, null, uniqueId, now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument update(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final ExchangeDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Exchange not found: " + uniqueId);
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
      throw new DataNotFoundException("Exchange not found: " + uniqueId);
    }
    _changeManager.entityChanged(ChangeType.REMOVED, uniqueId, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument correct(final ExchangeDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeHistoryResult history(final ExchangeHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final ExchangeHistoryResult result = new ExchangeHistoryResult();
    final ExchangeDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
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
