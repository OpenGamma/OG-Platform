/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CopyStrategyConfiguration;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;

/**
 * A cache decorating a master, mainly intended to reduce the frequency and repetition of queries to the underlying
 * master.
 * <p>
 * The cache is implemented using {@code EHCache}.
 *
 * TODO Check whether misses are cached by SelfPopulatingCache
 * TODO remove redundant cleanCache calls
 * TODO externalise configuration in xml file
 *
 *
 * @param <D> the document type returned by the master
 */
public abstract class AbstractEHCachingMaster<D extends AbstractDocument> implements AbstractChangeProvidingMaster<D> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEHCachingMaster.class);
  /** Cache name. */
  private static final String CACHE_NAME_SUFFIX = "UidToDocumentCache";
  /** Check cached results against results from underlying */
  public static final boolean TEST_AGAINST_UNDERLYING = false; //s_logger.isDebugEnabled();

  /** The underlying master. */
  private final AbstractChangeProvidingMaster<D> _underlying;
  /** The cache manager. */
  private final CacheManager _cacheManager;
  /** Listens for changes in the underlying security source. */
  private final ChangeListener _changeListener;
  /** The local change manager. */
  private final ChangeManager _changeManager;
  /** The document cache indexed by UniqueId. */
  private final Ehcache _uidToDocumentCache;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   *
   * @param name          the cache name, not empty
   * @param underlying    the underlying source, not null
   * @param cacheManager  the cache manager, not null
   */
  public AbstractEHCachingMaster(final String name, final AbstractChangeProvidingMaster<D> underlying, final CacheManager cacheManager) {
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");

    _underlying = underlying;
    _cacheManager = cacheManager;

    // Load cache configuration
    if (cacheManager.getCache(name + CACHE_NAME_SUFFIX) == null) {
      // If cache config not found, set up programmatically
      s_logger.warn("Could not load a cache configuration for " + name + CACHE_NAME_SUFFIX
                  + ", building a default configuration programmatically instead");
      getCacheManager().addCache(new Cache(tweakCacheConfiguration(new CacheConfiguration(name + CACHE_NAME_SUFFIX,
                                                                                          10000))));
    }
    _uidToDocumentCache = new SelfPopulatingCache(_cacheManager.getCache(name + CACHE_NAME_SUFFIX),
                                                  new UidToDocumentCacheEntryFactory<>(_underlying));
    getCacheManager().replaceCacheWithDecoratedCache(_cacheManager.getCache(name + CACHE_NAME_SUFFIX),
                                                     getUidToDocumentCache());

    // Listen to change events from underlying, clean this cache accordingly and relay events to our change listeners
    _changeManager = new BasicChangeManager();
    _changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        final ObjectId oid = event.getObjectId();
        final Instant versionFrom = event.getVersionFrom();
        final Instant versionTo = event.getVersionTo();
        cleanCaches(oid, versionFrom, versionTo);
        _changeManager.entityChanged(event.getType(), event.getObjectId(),
            event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
      }
    };
    underlying.changeManager().addChangeListener(_changeListener);
  }

  private CacheConfiguration tweakCacheConfiguration(CacheConfiguration cacheConfiguration) {

    // Set searchable index
    Searchable uidToDocumentCacheSearchable = new Searchable();
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("ObjectId")
                                                        .expression("value.getObjectId().toString()"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("VersionFromInstant")
                                                        .className("com.opengamma.master.cache.InstantExtractor"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("VersionToInstant")
                                                        .className("com.opengamma.master.cache.InstantExtractor"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("CorrectionFromInstant")
                                                        .className("com.opengamma.master.cache.InstantExtractor"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("CorrectionToInstant")
                                                        .className("com.opengamma.master.cache.InstantExtractor"));
    cacheConfiguration.addSearchable(uidToDocumentCacheSearchable);

    // Make copies of cached objects
    CopyStrategyConfiguration copyStrategyConfiguration = new CopyStrategyConfiguration();
    copyStrategyConfiguration.setClass("com.opengamma.master.cache.JodaBeanCopyStrategy");
    cacheConfiguration.addCopyStrategy(copyStrategyConfiguration);
    cacheConfiguration.setCopyOnRead(true);
    cacheConfiguration.setCopyOnWrite(true);

    cacheConfiguration.setStatistics(true);

    return cacheConfiguration;
  }

  //-------------------------------------------------------------------------

  @Override
  public D get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    // Search through attributes for specified oid, versions/corrections
    Results results = getUidToDocumentCache().createQuery()
        .includeKeys().includeValues()
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("ObjectId"))
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("VersionFromInstant"))
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("VersionToInstant"))
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("CorrectionFromInstant"))
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("CorrectionToInstant"))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("ObjectId").eq(objectId.toString()))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("VersionFromInstant")
            .le(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT).getVersionAsOf().toString()))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("VersionToInstant")
            .gt(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT.minusNanos(1)).getVersionAsOf().toString()))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("CorrectionFromInstant")
            .le(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT).getCorrectedTo().toString()))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("CorrectionToInstant")
            .gt(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT.minusNanos(1)).getCorrectedTo().toString()))
        .execute();

    // Found a matching cached document
    if (results.size() == 1 && results.all().get(0).getValue() != null) {
      @SuppressWarnings("unchecked")
      D result = (D) results.all().get(0).getValue();

      // Debug: check result against underlying
      if (TEST_AGAINST_UNDERLYING) {
        D check = getUnderlying().get(objectId, versionCorrection);
        if (!result.equals(check)) {
          s_logger.error(getUidToDocumentCache().getName() + " returned:\n" + result + "\nbut the underlying master returned:\n" + check);
        }
      }

      // Return cached value
      return result;

    // No cached document found, fetch from underlying by oid/vc instead
    // Note: no self-populating by oid/vc, and no caching of misses by oid/vc
    } else if (results.size() == 0) {
      // Get from underlying by oid/vc, throwing exception if not there
      D result = _underlying.get(objectId, versionCorrection);

      // Explicitly insert in cache
      getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

      return result;

    // Invalid result
    } else {
      throw new DataNotFoundException("Unable to uniquely identify a document with ObjectId " + objectId
                                      + " and VersionCorrection " + versionCorrection
                                      + " because more than one cached search result matches: " + results);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public D get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    // Get from cache, which in turn self-populates from the underlying master
    Element element;
    try {
      element = getUidToDocumentCache().get(uniqueId);
    } catch (CacheException e) {
      throw new DataNotFoundException(e.getMessage());
    }

    if (element != null && element.getObjectValue() != null) {

      // Debug: check result against underlying
      if (TEST_AGAINST_UNDERLYING) {
        D check = getUnderlying().get(uniqueId);
        if (!((D) element.getObjectValue()).equals(check)) {
          s_logger.error(getUidToDocumentCache().getName() + " returned:\n" + ((D) element.getObjectValue()) + "\nbut the underlying master returned:\n"  + check);
        }
      }
      return (D) element.getObjectValue();
    } else {
      throw new DataNotFoundException("No document found with the specified UniqueId");
    }
  }

  @Override
  public Map<UniqueId, D> get(Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    Map<UniqueId, D> result = new HashMap<>();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        D object = get(uniqueId);
        result.put(uniqueId, object);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }

    return result;
  }

  //-------------------------------------------------------------------------

  @Override
  public D add(D document) {
    ArgumentChecker.notNull(document, "document");

    // Add document to underlying master
    D result = getUnderlying().add(document);

    // Store document in UniqueId cache
    getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

    return result;
  }

  @Override
  public D update(D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObjectId(), "document.objectId");

    // Flush previous latest version (and all its corrections) from cache
    cleanCaches(document.getObjectId(), Instant.now(), InstantExtractor.MAX_INSTANT);

    // Update document in underlying master
    D result = getUnderlying().update(document);

    // Store document in UniqueId cache
    getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

    return result;
  }

  @Override
  public void remove(ObjectIdentifiable objectId) {
    ArgumentChecker.notNull(objectId, "objectId");

    // Remove document from underlying master
    getUnderlying().remove(objectId);

    // Adjust version/correction validity of latest version in Oid cache
    // Note: cleanCaches is already triggered by underlying master, so this is probably redundant
    cleanCaches(objectId.getObjectId(), Instant.now(), null);
  }

  @Override
  public D correct(D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    // Flush the previous latest correction from cache
    getUidToDocumentCache().remove(document.getUniqueId());

    // Correct document in underlying master
    D result = getUnderlying().correct(document);

    // Store latest correction in UniqueId cache
    getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

    return result;
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    // Flush the original version from cache
    getUidToDocumentCache().remove(uniqueId);

    // Replace version in underlying master
    List<UniqueId> results = getUnderlying().replaceVersion(uniqueId, replacementDocuments);

    // Don't cache replacementDocuments, whose version, correction instants may have been altered by underlying master

    return results;
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    // Flush all existing versions from cache
    cleanCaches(objectId.getObjectId(), null, null);

    // Replace all versions in underlying master
    List<UniqueId> results = getUnderlying().replaceAllVersions(objectId, replacementDocuments);

    // Don't cache replacementDocuments, whose version, correction instants may have been altered by underlying master

    return results;
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    // Flush all existing versions from cache
    cleanCaches(objectId.getObjectId(), null, null);

    // Replace versions in underlying master
    List<UniqueId> results = getUnderlying().replaceVersions(objectId, replacementDocuments);

    // Don't cache replacementDocuments, whose version, correction instants may have been altered by underlying master

    return results;
  }

  @Override
  public UniqueId replaceVersion(D replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");

    final List<UniqueId> result =
        replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<D>emptyList());
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd) {
    final List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  //-------------------------------------------------------------------------

  private void cleanCaches(ObjectId objectId, Instant fromVersion, Instant toVersion) {

    Results results = getUidToDocumentCache().createQuery().includeKeys()
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("ObjectId"))
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("VersionFromInstant"))
        .includeAttribute(getUidToDocumentCache().getSearchAttribute("VersionToInstant"))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("ObjectId")
                         .eq(objectId.toString()))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("VersionFromInstant")
                         .le((fromVersion != null ? fromVersion : InstantExtractor.MIN_INSTANT).toString()))
        .addCriteria(getUidToDocumentCache().getSearchAttribute("VersionToInstant")
                         .ge((toVersion != null ? toVersion : InstantExtractor.MAX_INSTANT).toString()))
        .execute();

    for (Result result : results.all()) {
      getUidToDocumentCache().remove(result.getKey());
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    getUnderlying().changeManager().removeChangeListener(_changeListener);
    getCacheManager().removeCache(getUidToDocumentCache().getName());
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the underlying source of items.
   *
   * @return the underlying source of items, not null
   */
  protected AbstractChangeProvidingMaster<D> getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   *
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Gets the document by UniqueId cache.
   *
   * @return the cache, not null
   */
  protected Ehcache getUidToDocumentCache() {
    return _uidToDocumentCache;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
