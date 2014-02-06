/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

/**
 * A cache decorating a {@code HistoricalTimeSeriesMaster}, mainly intended to reduce the frequency and repetition of queries to
 * the underlying master.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingHistoricalTimeSeriesMaster extends AbstractEHCachingMaster<HistoricalTimeSeriesInfoDocument> implements HistoricalTimeSeriesMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalTimeSeriesMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;

  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name          the cache name, not null
   * @param underlying    the underlying historicalTimeSeries master, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingHistoricalTimeSeriesMaster(final String name, final HistoricalTimeSeriesMaster underlying, final CacheManager cacheManager) {
    super(name + "HistoricalTimeSeries", underlying, cacheManager);

    // Create the document search cache and register a historicalTimeSeries master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "HistoricalTimeSeries", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        HistoricalTimeSeriesInfoSearchResult result = ((HistoricalTimeSeriesMaster) getUnderlying()).search((HistoricalTimeSeriesInfoSearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a historicalTimeSeries master searcher
    _historySearchCache = new EHCachingSearchCache(name + "HistoricalTimeSeriesHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        HistoricalTimeSeriesInfoHistoryResult result = ((HistoricalTimeSeriesMaster) getUnderlying()).history((HistoricalTimeSeriesInfoHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Prime document search cache
    HistoricalTimeSeriesInfoSearchRequest defaultSearch = new HistoricalTimeSeriesInfoSearchRequest();
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);

    underlying.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        _historySearchCache.getCache().removeAll();
        _documentSearchCache.getCache().removeAll();
      }
    });
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false);

    List<HistoricalTimeSeriesInfoDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(Instant.now());
    result.setVersionCorrection(vc);

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      HistoricalTimeSeriesInfoSearchResult check = ((HistoricalTimeSeriesMaster) getUnderlying()).search(request);
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
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).getTimeSeries(uniqueId);  // TODO
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, HistoricalTimeSeriesGetFilter filter) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).getTimeSeries(uniqueId, filter);  // TODO
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId,
                                                      VersionCorrection versionCorrection) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).getTimeSeries(objectId, versionCorrection);  // TODO
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId,
                                                      VersionCorrection versionCorrection,
                                                      HistoricalTimeSeriesGetFilter filter) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).getTimeSeries(objectId, versionCorrection, filter);  // TODO
  }

  @Override
  public UniqueId updateTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).updateTimeSeriesDataPoints(objectId, series);  // TODO
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).correctTimeSeriesDataPoints(objectId, series);  // TODO
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(ObjectIdentifiable objectId,
                                             LocalDate fromDateInclusive,
                                             LocalDate toDateInclusive) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).removeTimeSeriesDataPoints(objectId, fromDateInclusive, toDateInclusive);  // TODO
  }

  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request) {

    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    List<HistoricalTimeSeriesInfoDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    HistoricalTimeSeriesInfoHistoryResult result = new HistoricalTimeSeriesInfoHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;
  }

  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    return ((HistoricalTimeSeriesMaster) getUnderlying()).metaData(request);
  }

}
