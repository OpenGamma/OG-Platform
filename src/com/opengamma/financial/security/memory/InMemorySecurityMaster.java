/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import org.apache.commons.lang.Validate;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.SecurityDocument;
import com.opengamma.financial.security.SecurityMaster;
import com.opengamma.financial.security.SecuritySearchHistoricRequest;
import com.opengamma.financial.security.SecuritySearchHistoricResult;
import com.opengamma.financial.security.SecuritySearchRequest;
import com.opengamma.financial.security.SecuritySearchResult;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code SecurityMaster}.
 * <p>
 * This security master does not support versioning of securities.
 */
public class InMemorySecurityMaster implements SecurityMaster {
  // TODO: This is not hardened for production, as the data in the master can
  // be altered from outside as it is the same object

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "Memory";

  /**
   * A cache of securities by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, SecurityDocument> _securities = new ConcurrentHashMap<UniqueIdentifier, SecurityDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty security master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemorySecurityMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemorySecurityMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    Validate.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    Validate.notNull(request, "request");
    final SecuritySearchResult result = new SecuritySearchResult();
    Collection<SecurityDocument> docs = _securities.values();
    if (request.getIdentifiers() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return doc.getSecurity().getIdentifiers().containsAny(request.getIdentifiers());
        }
      });
    }
    if (request.getSecurityType() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return request.getSecurityType().equals(doc.getSecurity().getSecurityType());
        }
      });
    }
    if (request.getName() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return request.getName().equals(doc.getSecurity().getName());
        }
      });
    }
    result.setDocument(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    final SecurityDocument document = _securities.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    Validate.notNull(document, "document");
    Validate.notNull(document.getSecurity(), "document.security");
    
    final Security security = document.getSecurity();
    final UniqueIdentifier uid = _uidSupplier.get();
    final Instant now = Instant.nowSystemClock();
    UniqueIdentifiables.setInto(security, uid);
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    doc.setUniqueIdentifier(uid);
    doc.setValidFrom(now);
    doc.setLastModified(now);
    _securities.put(uid, doc);  // unique identifier should be unique
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    Validate.notNull(document, "document");
    Validate.notNull(document.getSecurity(), "document.security");
    Validate.notNull(document.getUniqueIdentifier(), "document.uniqueIdentifier");
    
    final UniqueIdentifier uid = document.getUniqueIdentifier();
    final Instant now = Instant.nowSystemClock();
    final SecurityDocument storedDocument = _securities.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    document.setValidFrom(storedDocument.getValidFrom());
    document.setValidTo(storedDocument.getValidTo());
    document.setLastModified(now);
    if (_securities.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    
    if (_securities.remove(uid) == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchHistoricResult searchHistoric(final SecuritySearchHistoricRequest request) {
    Validate.notNull(request, "request");
    Validate.notNull(request.getObjectIdentifier(), "request.objectIdentifier");
    
    final SecuritySearchHistoricResult result = new SecuritySearchHistoricResult();
    final SecurityDocument doc = get(request.getObjectIdentifier());
    if (doc != null) {
      result.setDocument(doc);
    }
    result.setPaging(Paging.of(result.getDocument()));
    return result;
  }

  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    return update(document);
  }

}
