/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
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
 */
public class InMemoryExchangeMaster implements ExchangeMaster {
  // TODO: This is not hardened for production, as the data in the master can
  // be altered from outside as it is the same object

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "MemExg";

  /**
   * A cache of exchanges by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, ExchangeDocument> _exchanges = new ConcurrentHashMap<UniqueIdentifier, ExchangeDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty exchange master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryExchangeMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryExchangeMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchResult search(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final ExchangeSearchResult result = new ExchangeSearchResult();
    Collection<ExchangeDocument> docs = _exchanges.values();
    if (request.getIdentifiers().size() > 0) {
      docs = Collections2.filter(docs, new Predicate<ExchangeDocument>() {
        @Override
        public boolean apply(final ExchangeDocument doc) {
          for (IdentifierBundle bundle : request.getIdentifiers()) {
            if (doc.getExchange().getIdentifiers().containsAll(bundle)) {
              return true;
            }
          }
          return false;
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
  public ExchangeDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final ExchangeDocument document = _exchanges.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument add(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    
    final UniqueIdentifier uid = _uidSupplier.get();
    final ManageableExchange exchange = document.getExchange().clone();
    exchange.setUniqueIdentifier(uid);
    document.setExchangeId(uid);
    final Instant now = Instant.nowSystemClock();
    final ExchangeDocument doc = new ExchangeDocument();
    doc.setExchange(exchange);
    doc.setExchangeId(uid);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _exchanges.put(uid, doc);  // unique identifier should be unique
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument update(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getExchangeId(), "document.exchangeId");
    
    final UniqueIdentifier uid = document.getExchangeId();
    final Instant now = Instant.nowSystemClock();
    final ExchangeDocument storedDocument = _exchanges.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_exchanges.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    if (_exchanges.remove(uid) == null) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeHistoryResult history(final ExchangeHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getExchangeId(), "request.exchangeId");
    
    final ExchangeHistoryResult result = new ExchangeHistoryResult();
    final ExchangeDocument doc = get(request.getExchangeId());
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
