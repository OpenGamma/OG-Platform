/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityMaster;
import com.opengamma.financial.security.master.SecurityHistoryRequest;
import com.opengamma.financial.security.master.SecurityHistoryResult;
import com.opengamma.financial.security.master.SecuritySearchRequest;
import com.opengamma.financial.security.master.SecuritySearchResult;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
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
  public static final String DEFAULT_UID_SCHEME = "MemSec";

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
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final SecuritySearchResult result = new SecuritySearchResult();
    Collection<SecurityDocument> docs = _securities.values();
    if (request.getIdentityKey() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return doc.getSecurity().getIdentifiers().containsAny(request.getIdentityKey());
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
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final SecurityDocument document = _securities.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    
    final DefaultSecurity security = document.getSecurity();
    final UniqueIdentifier uid = _uidSupplier.get();
    final Instant now = Instant.nowSystemClock();
    UniqueIdentifiables.setInto(security, uid);
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    doc.setSecurityId(uid);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _securities.put(uid, doc);  // unique identifier should be unique
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getSecurityId(), "document.securityId");
    
    final UniqueIdentifier uid = document.getSecurityId();
    final Instant now = Instant.nowSystemClock();
    final SecurityDocument storedDocument = _securities.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_securities.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    if (_securities.remove(uid) == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getSecurityId(), "request.securityId");
    
    final SecurityHistoryResult result = new SecurityHistoryResult();
    final SecurityDocument doc = get(request.getSecurityId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    return update(document);
  }

}
