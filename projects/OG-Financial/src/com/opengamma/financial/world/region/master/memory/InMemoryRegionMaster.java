/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region.master.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.financial.world.region.master.ManageableRegion;
import com.opengamma.financial.world.region.master.RegionDocument;
import com.opengamma.financial.world.region.master.RegionDocumentComparator;
import com.opengamma.financial.world.region.master.RegionHistoryRequest;
import com.opengamma.financial.world.region.master.RegionHistoryResult;
import com.opengamma.financial.world.region.master.RegionMaster;
import com.opengamma.financial.world.region.master.RegionSearchRequest;
import com.opengamma.financial.world.region.master.RegionSearchResult;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code RegionMaster}.
 * <p>
 * This master does not support versioning of regions.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryRegionMaster implements RegionMaster {

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "MemReg";

  /**
   * A cache of regions by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, RegionDocument> _regions = new ConcurrentHashMap<UniqueIdentifier, RegionDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty region master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryRegionMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryRegionMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionSearchResult search(final RegionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final RegionSearchResult result = new RegionSearchResult();
    Collection<RegionDocument> docs = _regions.values();
    if (request.getProviderId() != null) {
      docs = Collections2.filter(docs, new Predicate<RegionDocument>() {
        @Override
        public boolean apply(final RegionDocument doc) {
          return request.getProviderId().equals(doc.getProviderId());
        }
      });
    }
    if (request.getClassification() != null) {
      docs = Collections2.filter(docs, new Predicate<RegionDocument>() {
        @Override
        public boolean apply(final RegionDocument doc) {
          return doc.getRegion().getClassification() == request.getClassification();
        }
      });
    }
    if (request.getChildrenOfId() != null) {
      docs = Collections2.filter(docs, new Predicate<RegionDocument>() {
        @Override
        public boolean apply(final RegionDocument doc) {
          return doc.getRegion().getParentRegionIds().contains(request.getChildrenOfId());
        }
      });
    }
    if (request.getIdentifiers().size() > 0) {
      docs = Collections2.filter(docs, new Predicate<RegionDocument>() {
        @Override
        public boolean apply(final RegionDocument doc) {
          for (IdentifierBundle bundle : request.getIdentifiers()) {
            if (doc.getRegion().getIdentifiers().containsAll(bundle)) {
              return true;
            }
          }
          return false;
        }
      });
    }
    final String name = request.getName();
    if (name != null) {
      docs = Collections2.filter(docs, new Predicate<RegionDocument>() {
        @Override
        public boolean apply(final RegionDocument doc) {
          return RegexUtils.wildcardsToPattern(name).matcher(doc.getRegion().getName()).matches();
        }
      });
    }
    result.setPaging(Paging.of(docs, request.getPagingRequest()));
    List<RegionDocument> list = new ArrayList<RegionDocument>(docs);
    Collections.sort(list, RegionDocumentComparator.ASC);
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final RegionDocument document = _regions.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Region not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument add(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getRegion(), "document.region");
    
    final UniqueIdentifier uid = _uidSupplier.get();
    final ManageableRegion region = document.getRegion();
    region.setUniqueIdentifier(uid);
    document.setRegionId(uid);
    final Instant now = Instant.nowSystemClock();
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    _regions.put(uid, document);  // unique identifier should be unique
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionDocument update(final RegionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getRegion(), "document.region");
    ArgumentChecker.notNull(document.getRegionId(), "document.regionId");
    
    final UniqueIdentifier uid = document.getRegionId();
    final Instant now = Instant.nowSystemClock();
    final RegionDocument storedDocument = _regions.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Region not found: " + uid);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_regions.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    if (_regions.remove(uid) == null) {
      throw new DataNotFoundException("Region not found: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public RegionHistoryResult history(final RegionHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getRegionId(), "request.regionId");
    
    final RegionHistoryResult result = new RegionHistoryResult();
    final RegionDocument doc = get(request.getRegionId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public RegionDocument correct(final RegionDocument document) {
    return update(document);
  }

}
