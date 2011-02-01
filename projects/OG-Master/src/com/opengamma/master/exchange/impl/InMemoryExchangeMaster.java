/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.ObjectIdentifierSupplier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code ExchangeMaster}.
 * <p>
 * This exchange master does not support versioning of exchanges.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryExchangeMaster implements ExchangeMaster {

  /**
   * The default scheme used for each {@link ObjectIdentifier}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemExg";

  /**
   * A cache of exchanges by identifier.
   */
  private final ConcurrentMap<ObjectIdentifier, ExchangeDocument> _store = new ConcurrentHashMap<ObjectIdentifier, ExchangeDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectIdentifier> _objectIdSupplier;

  /**
   * Creates an empty exchange master using the default scheme for any {@link ObjectIdentifier}s created.
   */
  public InMemoryExchangeMaster() {
    this(new ObjectIdentifierSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryExchangeMaster(final Supplier<ObjectIdentifier> objectIdSupplier) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    _objectIdSupplier = objectIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchResult search(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final ExchangeSearchResult result = new ExchangeSearchResult();
    Collection<ExchangeDocument> docs = _store.values();
    if (request.getExchangeIds() != null) {
      docs = Collections2.filter(docs, new Predicate<ExchangeDocument>() {
        @Override
        public boolean apply(final ExchangeDocument doc) {
          return request.getExchangeIds().contains(doc.getUniqueId());
        }
      });
    }
    if (request.getExchangeKeys() != null) {
      docs = Collections2.filter(docs, new Predicate<ExchangeDocument>() {
        @Override
        public boolean apply(final ExchangeDocument doc) {
          return request.getExchangeKeys().matches(doc.getExchange().getIdentifiers());
        }
      });
    }
    final String name = request.getName();
    if (name != null) {
      docs = Collections2.filter(docs, new Predicate<ExchangeDocument>() {
        @Override
        public boolean apply(final ExchangeDocument doc) {
          return RegexUtils.wildcardsToPattern(name).matcher(doc.getName()).matches();
        }
      });
    }
    result.setPaging(Paging.of(docs, request.getPagingRequest()));
    result.getDocuments().addAll(request.getPagingRequest().select(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final UniqueIdentifier uniqueId) {
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
    
    final ObjectIdentifier objectId = _objectIdSupplier.get();
    final UniqueIdentifier uniqueId = objectId.atVersion("");
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
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument update(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
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
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (_store.remove(uniqueId.getObjectId()) == null) {
      throw new DataNotFoundException("Exchange not found: " + uniqueId);
    }
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
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public ExchangeDocument correct(final ExchangeDocument document) {
    return update(document);
  }

}
