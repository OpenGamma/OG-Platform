/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

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
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code HolidayMaster}.
 * <p>
 * This master does not support versioning of holidays.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryHolidayMaster implements HolidayMaster {

  /**
   * The default scheme used for each {@link ObjectIdentifier}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemHol";

  /**
   * A cache of holidays by identifier.
   */
  private final ConcurrentMap<ObjectIdentifier, HolidayDocument> _store = new ConcurrentHashMap<ObjectIdentifier, HolidayDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectIdentifier> _objectIdSupplier;

  /**
   * Creates an empty master using the default scheme for any {@link ObjectIdentifier}s created.
   */
  public InMemoryHolidayMaster() {
    this(new ObjectIdentifierSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryHolidayMaster(final Supplier<ObjectIdentifier> objectIdSupplier) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    _objectIdSupplier = objectIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final HolidaySearchResult result = new HolidaySearchResult();
    Collection<HolidayDocument> docs = _store.values();
    if (request.getProviderKey() != null) {
      docs = Collections2.filter(docs, new Predicate<HolidayDocument>() {
        @Override
        public boolean apply(final HolidayDocument doc) {
          return request.getProviderKey().equals(doc.getProviderKey());
        }
      });
    }
    if (request.getCurrency() != null) {
      docs = Collections2.filter(docs, new Predicate<HolidayDocument>() {
        @Override
        public boolean apply(final HolidayDocument doc) {
          return request.getCurrency().equals(doc.getHoliday().getCurrency());
        }
      });
    }
    if (request.getRegionKeys() != null) {
      docs = Collections2.filter(docs, new Predicate<HolidayDocument>() {
        @Override
        public boolean apply(final HolidayDocument doc) {
          return doc.getHoliday().getRegionKey() != null &&
            request.getRegionKeys().contains(doc.getHoliday().getRegionKey());
        }
      });
    }
    if (request.getExchangeKeys() != null) {
      docs = Collections2.filter(docs, new Predicate<HolidayDocument>() {
        @Override
        public boolean apply(final HolidayDocument doc) {
          return doc.getHoliday().getExchangeKey() != null &&
            request.getExchangeKeys().contains(doc.getHoliday().getExchangeKey());
        }
      });
    }
    final String name = request.getName();
    if (name != null) {
      docs = Collections2.filter(docs, new Predicate<HolidayDocument>() {
        @Override
        public boolean apply(final HolidayDocument doc) {
          return RegexUtils.wildcardsToPattern(name).matcher(doc.getName()).matches();
        }
      });
    }
    if (request.getType() != null) {
      docs = Collections2.filter(docs, new Predicate<HolidayDocument>() {
        @Override
        public boolean apply(final HolidayDocument doc) {
          return doc.getHoliday().getType() == request.getType();
        }
      });
    }
    result.setPaging(Paging.of(docs, request.getPagingRequest()));
    result.getDocuments().addAll(request.getPagingRequest().select(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final UniqueIdentifier uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final HolidayDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Holiday not found: " + objectId);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument add(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    final ObjectIdentifier objectId = _objectIdSupplier.get();
    final UniqueIdentifier uniqueId = objectId.atVersion("");
    final ManageableHoliday holiday = document.getHoliday();
    holiday.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    _store.put(objectId, document);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument update(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final HolidayDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Holiday not found: " + uniqueId);
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
      throw new DataNotFoundException("Holiday not found: " + uniqueId);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final HolidayHistoryResult result = new HolidayHistoryResult();
    final HolidayDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public HolidayDocument correct(final HolidayDocument document) {
    return update(document);
  }

}
