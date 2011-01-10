/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
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
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "MemHol";

  /**
   * A cache of holidays by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, HolidayDocument> _holidays = new ConcurrentHashMap<UniqueIdentifier, HolidayDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty holiday master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryHolidayMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryHolidayMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final HolidaySearchResult result = new HolidaySearchResult();
    Collection<HolidayDocument> docs = _holidays.values();
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
  public HolidayDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final HolidayDocument document = _holidays.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Holiday not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument add(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    final UniqueIdentifier uid = _uidSupplier.get();
    final ManageableHoliday holiday = document.getHoliday();
    holiday.setUniqueId(uid);
    document.setUniqueId(uid);
    final Instant now = Instant.now();
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    _holidays.put(uid, document);  // unique identifier should be unique
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument update(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    final UniqueIdentifier uid = document.getUniqueId();
    final Instant now = Instant.now();
    final HolidayDocument storedDocument = _holidays.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Holiday not found: " + uid);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_holidays.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    if (_holidays.remove(uid) == null) {
      throw new DataNotFoundException("Holiday not found: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final HolidayHistoryResult result = new HolidayHistoryResult();
    final HolidayDocument doc = get(request.getObjectId());
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
