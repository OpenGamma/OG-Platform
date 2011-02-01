/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

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
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
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
   * The default scheme used for each {@link ObjectIdentifier}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemSec";

  /**
   * A cache of securities by identifier.
   */
  private final ConcurrentMap<ObjectIdentifier, SecurityDocument> _store = new ConcurrentHashMap<ObjectIdentifier, SecurityDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectIdentifier> _objectIdSupplier;

  /**
   * Creates an empty security master using the default scheme for any {@link ObjectIdentifier}s created.
   */
  public InMemorySecurityMaster() {
    this(new ObjectIdentifierSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemorySecurityMaster(final Supplier<ObjectIdentifier> objectIdSupplier) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    _objectIdSupplier = objectIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final SecuritySearchResult result = new SecuritySearchResult();
    Collection<SecurityDocument> docs = _store.values();
    if (request.getSecurityType() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return request.getSecurityType().equals(doc.getSecurity().getSecurityType());
        }
      });
    }
    if (request.getSecurityIds() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return request.getSecurityIds().contains(doc.getUniqueId());
        }
      });
    }
    if (request.getSecurityKeys() != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return request.getSecurityKeys().matches(doc.getSecurity().getIdentifiers());
        }
      });
    }
    final String name = request.getName();
    if (name != null) {
      docs = Collections2.filter(docs, new Predicate<SecurityDocument>() {
        @Override
        public boolean apply(final SecurityDocument doc) {
          return RegexUtils.wildcardsToPattern(name).matcher(doc.getName()).matches();
        }
      });
    }
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueIdentifier uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final SecurityDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Security not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    
    final ObjectIdentifier objectId = _objectIdSupplier.get();
    final UniqueIdentifier uniqueId = objectId.atVersion("");
    final ManageableSecurity security = document.getSecurity();
    security.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    final SecurityDocument doc = new SecurityDocument(security);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    _store.put(objectId, doc);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final SecurityDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Security not found: " + uniqueId);
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
      throw new DataNotFoundException("Security not found: " + uniqueId);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final SecurityHistoryResult result = new SecurityHistoryResult();
    final SecurityDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
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
