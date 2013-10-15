/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ExecutorServiceFactoryBean;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

/**
 * A cache for search results, providing common search caching logic across caching masters and across search types.
 *
 * <p>
 * The cache is implemented using {@code EHCache}.
 *
 * TODO investigate possibility of using SelfPopulatingCache for the search cache too
 * TODO eliminate/minimise duplicate caching of similar searches (e.g. history searches with different date ranges)
 * TODO OPTIMIZE finer grain range locking
 * TODO OPTIMIZE cache replacement policy/handling huge requests that would flush out entire content
 * TODO OPTIMIZE underlying search request coalescing
 */
public class EHCachingSearchCache {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingSearchCache.class);
  /** The number of units to prefetch on either side of the current paging request */
  protected static final int PREFETCH_RADIUS = 2;
  /** The size of a prefetch unit */
  protected static final int PREFETCH_GRANULARITY = 200;
  /** The maximum number of concurrent prefetch operations */
  protected static final int MAX_PREFETCH_CONCURRENCY = 8;
  /** Cache name. */
  private static final String CACHE_NAME_SUFFIX = "PagedSearchCache";
  /** Check cached results against results from underlying */
  public static final boolean TEST_AGAINST_UNDERLYING = false; //s_logger.isDebugEnabled();

  /** The cache manager */
  private final CacheManager _cacheManager;
  /**
   * The UniqueId cache indexed by search requests.
   * A cache entry contains unpaged (total) result size and a map from start indices to ranges of cached result UniqueIds
   */
  private final Cache _searchRequestCache;
  /** The prefetch thread executor service */
  private final ExecutorService _executorService;
  /** The searcher provides access to a master-specific search operation */
  private final Searcher _searcher;

  /**
   * A cache searcher, used by the  search cache to pass search requests to an underlying master without
   * knowing its type and retrieve the result unique Ids without knowing the document type.
   */
  public interface Searcher {
    /** 
     * Searches an underlying master, casting search requests/results as required for a specific master.
     * 
     * @param request  the search request, without paging (will be cast to a search request for a specific master)
     * @param pagingRequest  the paging request
     * @return the search result
     */
    IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest);
  }

  /**
   * Create a new search cache.
   *
   * @param name  a unique name for this cache, not empty
   * @param cacheManager  the cache manager to use, not null
   * @param searcher  the CacheSearcher to use for passing search requests to an underlying master, not null
   */
  public EHCachingSearchCache(String name, CacheManager cacheManager, Searcher searcher) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(searcher, "searcher");

    _cacheManager = cacheManager;
    _searcher = searcher;

    // Load cache configuration
    if (cacheManager.getCache(name + CACHE_NAME_SUFFIX) == null) {
      // If cache config not found, set up programmatically
      s_logger.warn("Could not load a cache configuration for " + name + CACHE_NAME_SUFFIX
                  + ", building a default configuration programmatically instead");
      getCacheManager().addCache(new Cache(tweakCacheConfiguration(new CacheConfiguration(name + CACHE_NAME_SUFFIX,
                                                                                          10000))));
    }
    _searchRequestCache = cacheManager.getCache(name + CACHE_NAME_SUFFIX);

    // Async prefetch executor service
    ExecutorServiceFactoryBean execBean = new ExecutorServiceFactoryBean();
    execBean.setNumberOfThreads(MAX_PREFETCH_CONCURRENCY);
    execBean.setStyle(ExecutorServiceFactoryBean.Style.CACHED);
    _executorService = execBean.getObjectCreating();
  }

  private static CacheConfiguration tweakCacheConfiguration(CacheConfiguration cacheConfiguration) {
    // Make copies of cached objects (use default Serializable copy)
    cacheConfiguration.setCopyOnRead(true);
    cacheConfiguration.setCopyOnWrite(true);
    cacheConfiguration.setStatistics(true);
    return cacheConfiguration;
  }

  //-------------------------------------------------------------------------
  /**
   * If result is entirely cached return it immediately; otherwise, fetch any missing ranges from the underlying
   * master in the foreground, cache and return it.
   *
   * @param requestBean       the search request, without paging
   * @param pagingRequest     the paging request
   * @param blockUntilCached  if true, block the request until all caching, including related prefetching, is done
   * @return                  the total number of results (ignoring paging), and the unique IDs of the requested result page
   */
  public IntObjectPair<List<UniqueId>> search(final Bean requestBean, PagingRequest pagingRequest,
                                                     boolean blockUntilCached) {

    ArgumentChecker.notNull(requestBean, "requestBean");
    ArgumentChecker.notNull(pagingRequest, "pagingRequest");

    // Fetch the total #results and cached ranges for the search request (without paging)
    final IntObjectPair<ConcurrentNavigableMap<Integer, List<UniqueId>>> info =
        getCachedRequestInfo(requestBean, pagingRequest);
    final int totalResults = info.getFirstInt();
    final ConcurrentNavigableMap<Integer, List<UniqueId>> rangeMap = info.getSecond();

    // Fix indexes larger than the total doc count
    if (pagingRequest.getFirstItem() >= totalResults) {
      pagingRequest = PagingRequest.ofIndex(totalResults, 0);
    } else if (pagingRequest.getLastItem() >= totalResults) {
      pagingRequest = PagingRequest.ofIndex(pagingRequest.getFirstItem(), totalResults - pagingRequest.getFirstItem());
    }

    // Ensure that the required range is cached in its entirety
    IntObjectPair<List<UniqueId>> pair =
        cacheSuperRange(requestBean, pagingRequest, rangeMap, totalResults, blockUntilCached);

    final int superIndex = pair.getFirstInt();
    final List<UniqueId> superRange = pair.getSecond();

    // Create and return the search result
    final List<UniqueId> resultUniqueIds = superRange.subList(
        pagingRequest.getFirstItem() - superIndex,
        Math.min(pagingRequest.getLastItem()  - superIndex, superRange.size()));

    return IntObjectPair.of(totalResults, resultUniqueIds);
  }

  /**
   * Calculate the range that should be prefetched for the supplied request and initiate the fetching of any uncached
   * ranges from the underlying master in the background, without blocking.
   *
   * @param requestBean   the search request, without paging
   * @param pagingRequest the paging request
   */
  public void prefetch(final Bean requestBean, PagingRequest pagingRequest) {

    // Build larger range to prefetch
    final int start =
        (pagingRequest.getFirstItem() / PREFETCH_GRANULARITY >= PREFETCH_RADIUS)
        ? ((pagingRequest.getFirstItem() / PREFETCH_GRANULARITY) * PREFETCH_GRANULARITY)
              - (PREFETCH_RADIUS * PREFETCH_GRANULARITY)
        : 0;
    final int end =
        (pagingRequest.getLastItem() < Integer.MAX_VALUE - (PREFETCH_RADIUS * PREFETCH_GRANULARITY))
        ? ((pagingRequest.getLastItem() / PREFETCH_GRANULARITY) * PREFETCH_GRANULARITY)
              + (PREFETCH_RADIUS * PREFETCH_GRANULARITY)
        : Integer.MAX_VALUE;

    final PagingRequest superPagingRequest = PagingRequest.ofIndex(start, end - start);

    // Submit search task to background executor
    getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        search(requestBean, superPagingRequest, true); // block until cached
      }
    });
  }

  //-------------------------------------------------------------------------

  /**
   * Retrieve from cache the total #results and the cached  ranges for the supplied search request (without
   * taking into account its paging request). If an cached entry is not found for the unpaged search request, then
   * create one, populate it with the results of the supplied paged search request, and return it.
   *
   * @param requestBean the search request, without paging
   * @param pagingRequest the paging request
   * @return        the total result count and a range map of cached UniqueIds for the supplied search request without
   *                paging
   */
  @SuppressWarnings("unchecked")
  private IntObjectPair<ConcurrentNavigableMap<Integer, List<UniqueId>>> getCachedRequestInfo(
      final Bean requestBean, PagingRequest pagingRequest) {

    // Get cache entry for current request (or create and get a primed cache entry if not found)
    // TODO investigate atomicity
    final Element element = getCache().get(withPagingRequest(requestBean, null));
    if (element != null) {
      return (IntObjectPair<ConcurrentNavigableMap<Integer, List<UniqueId>>>) element.getObjectValue();
    } else {
      // Build a new cached map entry and pre-fill it with the results of the supplied search request
      final IntObjectPair<List<UniqueId>> resultToCache = getSearcher().search(requestBean, pagingRequest);
      final ConcurrentNavigableMap<Integer, List<UniqueId>> rangeMapToCache = new ConcurrentSkipListMap<>();

      // Cache UniqueIds in search cache
      rangeMapToCache.put(pagingRequest.getFirstItem(), resultToCache.getSecond());

      final IntObjectPair<ConcurrentNavigableMap<Integer, List<UniqueId>>> newResult =
          IntObjectPair.of(resultToCache.getFirstInt(), rangeMapToCache);
      getCache().put(new Element(withPagingRequest(requestBean, null), newResult));
      return newResult;
    }
  }

  /**
   * Fill in any uncached gaps for the requested range from the underlying, creating a single cached 'super range' from
   * which the entire current search request can be satisfied. This method may be called concurrently in multiple
   * threads.
   *
   * @param requestBean     the search request, without paging
   * @param pagingRequest   the paging request
   * @param rangeMap        the range map of cached UniqueIds for the supplied search request without paging
   * @param totalResults    the total number of results without paging
   * @return                a super-range of cached UniqueIds that contains at least the requested UniqueIds
   */
  private IntObjectPair<List<UniqueId>> cacheSuperRange(final Bean requestBean, PagingRequest pagingRequest,
                            final ConcurrentNavigableMap<Integer, List<UniqueId>> rangeMap, int totalResults,
                            boolean blockUntilCached) {

    final int startIndex = pagingRequest.getFirstItem();
    final int endIndex = pagingRequest.getLastItem();

    final List<UniqueId> superRange;
    final int superIndex;

    if (blockUntilCached) {
      synchronized (rangeMap) {

        // Determine the super range's start index
        if (rangeMap.floorKey(startIndex) != null &&
            rangeMap.floorKey(startIndex) +
                rangeMap.floorEntry(startIndex).getValue().size() >= startIndex) {
          superRange = rangeMap.floorEntry(startIndex).getValue();
          superIndex = rangeMap.floorKey(startIndex);
        } else {
          superRange = Collections.synchronizedList(new LinkedList<UniqueId>());
          superIndex = startIndex;
          rangeMap.put(superIndex, superRange);
        }

        // Get map subset from closest start index above requested start index, to closest start index below requested
        // end index
        final Integer start = rangeMap.ceilingKey(startIndex + 1);
        final Integer end = rangeMap.floorKey(endIndex);
        @SuppressWarnings("unchecked")
        final ConcurrentNavigableMap<Integer, List<UniqueId>> subMap =
            (ConcurrentNavigableMap<Integer, List<UniqueId>>) (
              ((start != null) && (end != null) && (start <= end))
                  ? rangeMap.subMap(start, true, end, true)
                  : new ConcurrentSkipListMap<>()
            );

        // Iterate through ranges within the paging request range, concatenating them into the super range while
        // filling in any gaps from the underlying master
        int currentIndex = superIndex + superRange.size();
        final List<Integer> toRemove = new LinkedList<>();
        for (Map.Entry<Integer, List<UniqueId>> entry : subMap.entrySet()) {
          // If required, fill gap from underlying
          if (entry.getKey() > currentIndex) {
            List<UniqueId> missingResult =
                getSearcher().search(requestBean,
                                     PagingRequest.ofIndex(currentIndex, entry.getKey() - currentIndex)).getSecond();

            // Cache UniqueIds in search cache
            superRange.addAll(missingResult);

            currentIndex += missingResult.size();
          }

          // Add next cached range
          superRange.addAll(entry.getValue());
          currentIndex += entry.getValue().size();

          toRemove.add(entry.getKey());
        }


        // Remove original cached ranges (now part of the super range)
        for (Integer i : toRemove) {
          rangeMap.remove(i);
        }

        // If required, fill in the final range from underlying
        if (currentIndex < endIndex) {
          final List<UniqueId> missingResult =
              getSearcher().search(requestBean,
                                   PagingRequest.ofIndex(currentIndex, endIndex - currentIndex)).getSecond();

          // Cache UniqueIds in search cache
          superRange.addAll(missingResult);

          currentIndex += missingResult.size();
        }

        // put expanded super range into range map
        rangeMap.put(superIndex, superRange);

        final IntObjectPair<ConcurrentNavigableMap<Integer, List<UniqueId>>> newValue =
            IntObjectPair.of(totalResults, rangeMap);
        getCache().put(new Element(withPagingRequest(requestBean, null), newValue));
      }

    } else {
      // If entirely cached then return cached values
      if (rangeMap.floorKey(startIndex) != null &&
          rangeMap.floorKey(startIndex) + rangeMap.floorEntry(startIndex).getValue().size() >= endIndex) {
        superRange = rangeMap.floorEntry(startIndex).getValue();
        superIndex = rangeMap.floorKey(startIndex);

      // If not entirely cached then just fetch from underlying without caching
      } else {
        final List<UniqueId> missingResult = getSearcher().search(requestBean, pagingRequest).getSecond();
        superRange = Collections.synchronizedList(new LinkedList<UniqueId>());

        // Cache UniqueIds in search cache
        superRange.addAll(missingResult);

        superIndex = startIndex;
      }
    }

    return IntObjectPair.of(superIndex, superRange);
  }

  //-------------------------------------------------------------------------

  /**
   * Extract a list of unique Ids from a list of uniqueIdentifiable objects
   *
   * @param uniqueIdentifiables The uniquely identifiable objects
   * @return                    a list of unique Ids
   */
  public static List<UniqueId> extractUniqueIds(List<? extends UniqueIdentifiable> uniqueIdentifiables) {
    List<UniqueId> result = new LinkedList<>();
    for (UniqueIdentifiable uniqueIdentifiable : uniqueIdentifiables) {
      result.add(uniqueIdentifiable.getUniqueId());
    }
    return result;
  }

  /**
   * Insert documents in the specified document cache
   *
   * @param documents       The list of documents to be inserted
   * @param documentCache   The document cache in which to insert the documents
   */
  public static void cacheDocuments(List<? extends AbstractDocument> documents, Ehcache documentCache) {
    for (AbstractDocument document : documents) {
      documentCache.put(new Element(document.getUniqueId(), document));
    }
  }

  /**
   * Return a clone of the supplied search request, but with its paging request replaced
   *
   * @param requestBean the search request whose paging request to replace (currently a doc or history search request)
   * @param pagingRequest the paging request, null allowed
   * @return        a clone of the supplied search request, with its paging request replaced
   */

  public static Bean withPagingRequest(final Bean requestBean, final PagingRequest pagingRequest) {
    if (requestBean.propertyNames().contains("pagingRequest")) {
      final Bean newRequest = JodaBeanUtils.clone(requestBean);
      if (pagingRequest != null) {
        newRequest.property("pagingRequest").set(
            PagingRequest.ofIndex(pagingRequest.getFirstItem(), pagingRequest.getPagingSize()));
      } else {
        newRequest.property("pagingRequest").set(null);
      }
      return newRequest;
    } else {
      throw new OpenGammaRuntimeException(
          "Could not invoke setPagingRequest() on request object of type " + requestBean.getClass());
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    // No change management for searches... yet
    //getUnderlying().changeManager().removeChangeListener(_changeListener);

    getCacheManager().removeCache(getCache().getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cache searcher.
   *
   * @return the cache searcher instance
   */
  public Searcher getSearcher() {
    return _searcher;
  }

  /**
   * Gets the cache.
   *
   * @return the cache instance
   */
  public Ehcache getCache() {
    return _searchRequestCache;
  }

  /**
   * Gets the cache manager.
   *
   * @return the cache manager instance
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Gets the executor service used for prefetching.
   *
   * @return the executor service instance
   */
  public ExecutorService getExecutorService() {
    return _executorService;
  }

}
