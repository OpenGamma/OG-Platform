/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

/**
 * A cache decorating a {@code ConfigMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a {@code DbConfigMaster}. In particular, prefetching is employed in paged queries,
 * which tend to scale poorly.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingConfigMaster extends AbstractEHCachingMaster<ConfigDocument> implements ConfigMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingConfigMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;
  
  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name          the cache name, not empty
   * @param underlying    the underlying config master, not null
   * @param cacheManager  the cache manager, not null
   */
  @SuppressWarnings("rawtypes")
  public EHCachingConfigMaster(final String name, final ConfigMaster underlying, final CacheManager cacheManager) {
    super(name + "Config", underlying, cacheManager);

        // Create the doc search cache and register a config master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "Config", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {

        // Fetch search results from underlying master
        ConfigSearchResult<?> result =
            ((ConfigMaster) getUnderlying()).search((ConfigSearchRequest<?>)
                EHCachingSearchCache.withPagingRequest((AbstractSearchRequest) request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        List<UniqueId> uids = EHCachingSearchCache.extractUniqueIds(result.getDocuments());
        return IntObjectPair.of(result.getPaging().getTotalItems(), uids);
      }
    });

    // Create the history search cache and register a security master searcher
    _historySearchCache = new EHCachingSearchCache(name + "ConfigHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @SuppressWarnings("unchecked")
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        ConfigHistoryResult result = ((ConfigMaster) getUnderlying()).history((ConfigHistoryRequest)
            EHCachingSearchCache.withPagingRequest((ConfigHistoryRequest) request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        List<UniqueId> uids = EHCachingSearchCache.extractUniqueIds(result.getDocuments());
        return IntObjectPair.of(result.getPaging().getTotalItems(), uids);
      }
    });
    
    // Prime search cache
    ConfigSearchRequest defaultSearch = new ConfigSearchRequest();
    defaultSearch.setSortOrder(ConfigSearchSortOrder.NAME_ASC);
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);
  }

  @Override
  public <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    List<ConfigDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    ConfigSearchResult<T> result = new ConfigSearchResult<>(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      ConfigSearchResult<T> check = ((ConfigMaster) getUnderlying()).search(request);
      if (!result.getPaging().equals(check.getPaging())) {
        s_logger.error("_documentSearchCache.getCache().getName() + \" returned paging:\\n\"" + result.getPaging() +
                           "\nbut the underlying master returned paging:\n" + check.getPaging());
      }
      if (!result.getDocuments().equals(check.getDocuments())) {
        s_logger.error(_documentSearchCache.getCache().getName() + " returned documents:\n" + result.getDocuments() +
                           "\nbut the underlying master returned documents:\n" + check.getDocuments());
      }
    }
    return result;
  }

  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    return ((ConfigMaster) getUnderlying()).metaData(request);
  }

  @Override
  public <R> ConfigHistoryResult<R> history(ConfigHistoryRequest<R> request) {

    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    List<ConfigDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    ConfigHistoryResult<R> result = new ConfigHistoryResult<>(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;    
  }

}
