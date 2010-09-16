/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.financial.world.exchange.Exchange;
import com.opengamma.financial.world.exchange.master.ExchangeDocument;
import com.opengamma.financial.world.exchange.master.ExchangeMaster;
import com.opengamma.financial.world.exchange.master.ExchangeSearchHistoricRequest;
import com.opengamma.financial.world.exchange.master.ExchangeSearchHistoricResult;
import com.opengamma.financial.world.exchange.master.ExchangeSearchRequest;
import com.opengamma.financial.world.exchange.master.ExchangeSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
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
  public static final String DEFAULT_UID_SCHEME = "Memory";

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
  public ExchangeSearchResult searchExchanges(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final ExchangeSearchResult result = new ExchangeSearchResult();
    Collection<ExchangeDocument> docs = _exchanges.values();
    if (request.getIdentityKey() != null) {
      docs = Collections2.filter(docs, new Predicate<ExchangeDocument>() {
        @Override
        public boolean apply(final ExchangeDocument doc) {
          return doc.getExchange().getIdentifiers().containsAny(request.getIdentityKey());
        }
      });
    }
    if (request.getName() != null) {
      docs = Collections2.filter(docs, new Predicate<ExchangeDocument>() {
        @Override
        public boolean apply(final ExchangeDocument doc) {
          return request.getName().equals(doc.getExchange().getName());
        }
      });
    }
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument getExchange(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final ExchangeDocument document = _exchanges.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument addExchange(final ExchangeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    
    final UniqueIdentifier uid = _uidSupplier.get();
    final Exchange exchange = document.getExchange().clone();
    exchange.setUniqueIdentifier(uid);
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
  public ExchangeDocument updateExchange(final ExchangeDocument document) {
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
  public void removeExchange(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    if (_exchanges.remove(uid) == null) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchHistoricResult searchHistoricExchange(final ExchangeSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getExchangeId(), "request.exchangeId");
    
    final ExchangeSearchHistoricResult result = new ExchangeSearchHistoricResult();
    final ExchangeDocument doc = getExchange(request.getExchangeId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public ExchangeDocument correctExchange(final ExchangeDocument document) {
    return updateExchange(document);
  }

}
