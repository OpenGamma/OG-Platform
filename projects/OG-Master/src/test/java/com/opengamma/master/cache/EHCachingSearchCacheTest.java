/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.IntObjectPair;

@Test(groups = TestGroup.UNIT)
public class EHCachingSearchCacheTest {

  private static final int TOTAL_SIZE = 100;
  private static final String TEST_SCHEME = "TEST";

  @Test
  public void testSearchCache() {
    for (int requestSize = 1; requestSize < TOTAL_SIZE; requestSize = requestSize + 17) {
      for (int requestStartStepSize = 1; requestStartStepSize < TOTAL_SIZE / 2; requestStartStepSize = requestStartStepSize + 71) {
        EHCachingSearchCache searchCache = getCleanSearchCache();
        for (int requestStartPos = 0; requestStartPos * requestStartStepSize < TOTAL_SIZE * 4; requestStartPos++) {
          PagingRequest pagingRequest = PagingRequest.ofIndex((requestStartPos * requestStartStepSize) % TOTAL_SIZE, requestSize);
          assertEquals(searchCache.search(new SecuritySearchRequest(), pagingRequest, false).getSecond(),
                       buildResultIDs(
                         PagingRequest.ofIndex(
                           pagingRequest.getFirstItem(),
                           Math.min(pagingRequest.getLastItem() - pagingRequest.getFirstItem(),
                                    TOTAL_SIZE - pagingRequest.getFirstItem()
                           )
                         )
                       )
          );
        }
      }
    }
  }

  @Test
  public void testSearchCachePrefetching() {
    for (int requestSize = 1; requestSize < TOTAL_SIZE; requestSize = requestSize + 17) {
      for (int requestStartStepSize = 1; requestStartStepSize < TOTAL_SIZE / 2; requestStartStepSize = requestStartStepSize + 71) {
        EHCachingSearchCache searchCache = getCleanSearchCache();
        for (int requestStartPos = 0; requestStartPos * requestStartStepSize < TOTAL_SIZE * 4; requestStartPos++) {
          PagingRequest pagingRequest = PagingRequest.ofIndex((requestStartPos * requestStartStepSize) % TOTAL_SIZE, requestSize);
          searchCache.prefetch(new SecuritySearchRequest(), pagingRequest);
          assertEquals(searchCache.search(new SecuritySearchRequest(), pagingRequest, false).getSecond(),
                       buildResultIDs(
                         PagingRequest.ofIndex(
                           pagingRequest.getFirstItem(),
                           Math.min(pagingRequest.getLastItem() - pagingRequest.getFirstItem(),
                                    TOTAL_SIZE - pagingRequest.getFirstItem()
                           )
                         )
                       )
          );
        }
      }
    }
  }

  /**
   * Returns an empty cache manager
   * @return the cache manager
   */
  private CacheManager getCleanCacheManager() {
    CacheManager cacheManager = EHCacheUtils.createTestCacheManager(getClass().getName() + System.currentTimeMillis());
    cacheManager.clearAll();
    cacheManager.removalAll();
    return cacheManager;
  }

  private EHCachingSearchCache getCleanSearchCache() {
    return new EHCachingSearchCache("Test", getCleanCacheManager(), new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(Bean request, PagingRequest pagingRequest) {

        List<UniqueId> result = buildResultIDs(pagingRequest);

        return IntObjectPair.of(TOTAL_SIZE, result);
      }
    });
  }

  private List<UniqueId> buildResultIDs(PagingRequest pagingRequest) {
    List<UniqueId> result = new ArrayList<>();

    for (int i = pagingRequest.getFirstItem(); i < pagingRequest.getLastItem(); i++) {
      UniqueId uniqueId = UniqueId.of(TEST_SCHEME, Integer.toString(i), "1");
      result.add(uniqueId);
    }
    return result;
  }

}
