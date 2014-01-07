/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.HolidaySearchSortOrder;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

/**
 * A cache decorating a {@code HolidayMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a {@code DbHolidayMaster}. In particular, prefetching is employed in paged queries,
 * which tend to scale poorly.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingHolidayMaster extends AbstractEHCachingMaster<HolidayDocument> implements HolidayMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHolidayMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;
  
  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;
  
  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name          the cache name, not empty
   * @param underlying    the underlying holiday master, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingHolidayMaster(final String name, final HolidayMaster underlying, final CacheManager cacheManager) {
    super(name + "Holiday", underlying, cacheManager);

    // Create the doc search cache and register a holiday master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "Holiday", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        HolidaySearchResult result = ((HolidayMaster) getUnderlying()).search((HolidaySearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a security master searcher
    _historySearchCache = new EHCachingSearchCache(name + "HolidayHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        HolidayHistoryResult result = ((HolidayMaster) getUnderlying()).history((HolidayHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });
    
    // Prime search cache
    HolidaySearchRequest defaultSearch = new HolidaySearchRequest();
    defaultSearch.setSortOrder(HolidaySearchSortOrder.NAME_ASC);
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);
  }

  @Override
  public HolidayMetaDataResult metaData(HolidayMetaDataRequest request) {
    return ((HolidayMaster) getUnderlying()).metaData(request);
  }

  @Override
  public HolidaySearchResult search(HolidaySearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    List<HolidayDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    HolidaySearchResult result = new HolidaySearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      HolidaySearchResult check = ((HolidayMaster) getUnderlying()).search(request);
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
  public HolidayHistoryResult history(HolidayHistoryRequest request) {

    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    List<HolidayDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    HolidayHistoryResult result = new HolidayHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;    
  }

}
