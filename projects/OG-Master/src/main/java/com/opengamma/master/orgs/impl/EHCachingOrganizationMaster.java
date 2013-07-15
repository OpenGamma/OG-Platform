/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.orgs.impl;

import java.util.ArrayList;
import java.util.List;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.ObjectsPair;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code OrganizationMaster}, mainly intended to reduce the frequency and repetition of queries to
 * the underlying master.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingOrganizationMaster extends AbstractEHCachingMaster<OrganizationDocument> implements OrganizationMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingOrganizationMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;

  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name          the cache name, not null
   * @param underlying    the underlying organisation master, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingOrganizationMaster(final String name, final OrganizationMaster underlying, final CacheManager cacheManager) {
    super(name + "Organization", underlying, cacheManager);

    // Create the document search cache and register a organisation master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "Organization", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public ObjectsPair<Integer, List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        OrganizationSearchResult result = ((OrganizationMaster) getUnderlying()).search((OrganizationSearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return new ObjectsPair<>(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a organisation master searcher
    _historySearchCache = new EHCachingSearchCache(name + "OrganizationHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public ObjectsPair<Integer, List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        OrganizationHistoryResult result = ((OrganizationMaster) getUnderlying()).history((OrganizationHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return new ObjectsPair<>(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Prime document search cache
    OrganizationSearchRequest defaultSearch = new OrganizationSearchRequest();
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);
  }

  @Override
  public OrganizationSearchResult search(OrganizationSearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    ObjectsPair<Integer, List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false);

    List<OrganizationDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    OrganizationSearchResult result = new OrganizationSearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirst()));

    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(Instant.now());
    result.setVersionCorrection(vc);

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      OrganizationSearchResult check = ((OrganizationMaster) getUnderlying()).search(request);
      if (!result.getPaging().equals(check.getPaging())) {
        s_logger.error(_documentSearchCache.getCache().getName()
                           + "\n\tCache:\t" + result.getPaging()
                           + "\n\tUnderlying:\t" + check.getPaging());
      }
      if (!result.getDocuments().equals(check.getDocuments())) {
        System.out.println(_documentSearchCache.getCache().getName() + ": ");
        if (check.getDocuments().size() != result.getDocuments().size()) {
          System.out.println("\tSizes differ (Underlying " + check.getDocuments().size()
                             + "; Cache " + result.getDocuments().size() + ")");
        } else {
          for (int i = 0; i < check.getDocuments().size(); i++) {
            if (!check.getDocuments().get(i).equals(result.getDocuments().get(i))) {
              System.out.println("\tUnderlying\t" + i + ":\t" + check.getDocuments().get(i).getUniqueId());
              System.out.println("\tCache     \t" + i + ":\t" + result.getDocuments().get(i).getUniqueId());
            }
          }
        }
      }
    }

    return result;
  }

  @Override
  public OrganizationHistoryResult history(OrganizationHistoryRequest request) {

    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    ObjectsPair<Integer, List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    List<OrganizationDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    OrganizationHistoryResult result = new OrganizationHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirst()));
    return result;
  }

  @Override
  public ManageableOrganization getOrganization(UniqueId uid) {
    return get(uid).getOrganization();
  }

}
