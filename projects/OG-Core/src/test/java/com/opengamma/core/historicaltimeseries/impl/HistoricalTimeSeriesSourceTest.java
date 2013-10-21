/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = {TestGroup.INTEGRATION, "ehcache"})  // this fails randomly
public class HistoricalTimeSeriesSourceTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesSourceTest.class);
  private static final String ALPHAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private Set<String> _usedIds = new HashSet<String>();

  private boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }

  private LocalDateDoubleTimeSeries randomTimeSeries() {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate start = LocalDate.of(2000, 1, 2);
    LocalDate end = start.plusYears(10);
    LocalDate current = start;
    while (current.isBefore(end)) {
      current = current.plusDays(1);
      if (isWeekday(current)) {
        bld.put(current, Math.random());
      }
    }
    return bld.build();
  }

  private int random(int maxBoundExclusive) {
    return (int) (Math.floor(Math.random() * maxBoundExclusive));
  }

  private String makeRandomId() {
    StringBuilder sb = new StringBuilder();
    sb.append(ALPHAS.charAt(random(26)));
    sb.append(ALPHAS.charAt(random(26)));
    sb.append(Integer.toString(random(10)));
    sb.append(Integer.toString(random(10)));
    return sb.toString();
  }

  // be careful not to call this more than 26^2 * 100 times, or it will loop forever, and it will get progressively slower.
  // now put in a test as it gets near the limit.
  private String makeUniqueRandomId() {
    if (_usedIds.size() > 26 * 26 * 90) {
      Assert.fail("tried to create too many ids");
    }
    String id;
    do {
      id = makeRandomId();
      s_logger.info(id);
    } while (_usedIds.contains(id));
    _usedIds.add(id);
    return id;
  }

  private ExternalIdBundle makeBundle() {
    return ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(makeUniqueRandomId()), 
                                ExternalSchemes.bloombergBuidSecurityId(makeUniqueRandomId()));
  }

  /**
   * Generates 100 id bundles, and for each one generates a random time series for every combination 
   * of data source, data provider and field. Inserts each time series in a structured map and also
   * in a mock hts source, and checks that the contents of the map and the mock tally.
   * @return  the populated mock hts source and the set of generated id bundles. 
   */
  private Pair<HistoricalTimeSeriesSource, Set<ExternalIdBundle>> buildAndTestInMemoryProvider() {
    MockHistoricalTimeSeriesSource inMemoryHistoricalSource = new MockHistoricalTimeSeriesSource();
    
    // create map: Id bundle -> data source -> data provider -> field -> time series
    Map<ExternalIdBundle, Map<String, Map<String, Map<String, LocalDateDoubleTimeSeries>>>> map = 
        new HashMap<ExternalIdBundle, Map<String, Map<String, Map<String, LocalDateDoubleTimeSeries>>>>();
    for (int i = 0; i < 100; i++) {
      ExternalIdBundle ids = makeBundle();
      Map<String, Map<String, Map<String, LocalDateDoubleTimeSeries>>> dsidsSubMap = map.get(ids);
      if (dsidsSubMap == null) {
        dsidsSubMap = new HashMap<String, Map<String, Map<String, LocalDateDoubleTimeSeries>>>();
        map.put(ids, dsidsSubMap);
      }
      for (String dataSource : new String[] {"BLOOMBERG", "REUTERS", "JPM"}) {
        Map<String, Map<String, LocalDateDoubleTimeSeries>> dataSourceSubMap = dsidsSubMap.get(dataSource);
        if (dataSourceSubMap == null) {
          dataSourceSubMap = new HashMap<String, Map<String, LocalDateDoubleTimeSeries>>();
          dsidsSubMap.put(dataSource, dataSourceSubMap);
        }
        for (String dataProvider : new String[] {"UNKNOWN", "CMPL", "CMPT"}) {
          Map<String, LocalDateDoubleTimeSeries> dataProviderSubMap = dataSourceSubMap.get(dataProvider);
          if (dataProviderSubMap == null) {
            dataProviderSubMap = new HashMap<String, LocalDateDoubleTimeSeries>();
            dataSourceSubMap.put(dataProvider, dataProviderSubMap);
          }
          for (String field : new String[] {"PX_LAST", "VOLUME"}) {
            LocalDateDoubleTimeSeries randomTimeSeries = randomTimeSeries();
            
            // Insert generated time series in map
            dataProviderSubMap.put(field, randomTimeSeries);
            
            // Also insert generated time series in mock source
            inMemoryHistoricalSource.storeHistoricalTimeSeries(ids, dataSource, dataProvider, field, randomTimeSeries);
          }
        }
      }
    }
    
    // assert consistency between map and mock source (both by unique id and by source/provider/field) for each generated entry
    for (ExternalIdBundle dsids : map.keySet()) {
      for (String dataSource : new String[] {"BLOOMBERG", "REUTERS", "JPM"}) {
        for (String dataProvider : new String[] {"UNKNOWN", "CMPL", "CMPT"}) {
          for (String field : new String[] {"PX_LAST", "VOLUME"}) {
            LocalDateDoubleTimeSeries expectedTS = map.get(dsids).get(dataSource).get(dataProvider).get(field);
            HistoricalTimeSeries hts = inMemoryHistoricalSource.getHistoricalTimeSeries(dsids, dataSource, dataProvider, field);
            assertEquals(expectedTS, hts.getTimeSeries());
            assertEquals(hts, inMemoryHistoricalSource.getHistoricalTimeSeries(hts.getUniqueId()));
          }
        }
      }
    }
    return Pairs.of((HistoricalTimeSeriesSource) inMemoryHistoricalSource, map.keySet());
  }

  public void testInMemoryProvider() {
    buildAndTestInMemoryProvider();
  }

  //-------------------------------------------------------------------------
  public void testEHCachingHistoricalTimeSeriesSource() {
    CacheManager cacheManager = EHCacheUtils.createTestCacheManager(HistoricalTimeSeriesSourceTest.class);
    doTestCaching(cacheManager);
    EHCacheUtils.shutdownQuiet(cacheManager);
  }

  private void doTestCaching(CacheManager cacheManager) {
    // Populate an in-memory mock source (inMemoryHistoricalSource)
    Pair<HistoricalTimeSeriesSource, Set<ExternalIdBundle>> providerAndDsids = buildAndTestInMemoryProvider();
    HistoricalTimeSeriesSource inMemoryHistoricalSource = providerAndDsids.getFirst();
    
    // Set up a caching hts source with the mock underlying it (cachedProvider)
    EHCachingHistoricalTimeSeriesSource cachedProvider = new EHCachingHistoricalTimeSeriesSource(inMemoryHistoricalSource, cacheManager);
    
    // Obtain the id bundles it contains (dsids)
    Set<ExternalIdBundle> identifiers = providerAndDsids.getSecond();
    ExternalIdBundle[] dsids = identifiers.toArray(new ExternalIdBundle[] {});
    
    String[] dataSources = new String[] {"BLOOMBERG", "REUTERS", "JPM"};
    String[] dataProviders = new String[] {"UNKNOWN", "CMPL", "CMPT"};
    String[] fields = new String[] {"PX_LAST", "VOLUME"};
    
    for (int i = 0; i < 10000; i++) {
      // Randomly generate query parameters
      ExternalIdBundle ids = dsids[random(dsids.length)];
      String dataSource = dataSources[random(dataSources.length)];
      String dataProvider = dataProviders[random(dataProviders.length)];
      String field = fields[random(fields.length)];
      LocalDate startDate = Math.random() > 0.5 ? LocalDate.of(1998, 1, 2).plusDays(random(356 * 10)) : null;
      boolean includeStart = Math.random() > 0.5 ? true : false;
      LocalDate endDate = Math.random() > 0.5 
          ? startDate == null ? LocalDate.of(2000, 1, 2).plusDays(random(356 * 10)) : startDate.plusDays(random(356 * 5)) 
          : null;
      boolean includeEnd = Math.random() > 0.5 ? true : false;
      Integer maxPoints = Math.random() > 0.5 ? random(356 * 5) : null;

      // Fetch series/sub-series directly from in-memory mock source
      HistoricalTimeSeries inMemSeries = 
          (startDate == null && endDate == null && maxPoints == null)
            ? inMemoryHistoricalSource.getHistoricalTimeSeries(ids, dataSource, dataProvider, field)
            : (maxPoints == null)
              ? inMemoryHistoricalSource.getHistoricalTimeSeries(ids, dataSource, dataProvider, field, 
                                                                 startDate, includeStart, endDate, includeEnd)
              : inMemoryHistoricalSource.getHistoricalTimeSeries(ids, dataSource, dataProvider, field,
                                                                 startDate, includeStart, endDate, includeEnd, maxPoints);      

      // Fetch latest data point directly from in-memory mock source
      Pair<LocalDate, Double> inMemLatest = 
          (startDate == null && endDate == null)
            ? inMemoryHistoricalSource.getLatestDataPoint(ids, dataSource, dataProvider, field)
            : inMemoryHistoricalSource.getLatestDataPoint(ids, dataSource, dataProvider, field,
                                                          startDate, includeStart, endDate, includeEnd);
      
      // Compare latest data point with cached by externalid/source/provider/field
      assertEquals(inMemLatest, (startDate == null && endDate == null)
                                  ? cachedProvider.getLatestDataPoint(ids, dataSource, dataProvider, field)
                                  : cachedProvider.getLatestDataPoint(ids, dataSource, dataProvider, field,
                                                                      startDate, includeStart, endDate, includeEnd));
      
      // Compare latest data point with cached by uniqueId
      assertEquals(inMemLatest, (startDate == null && endDate == null)
          ? cachedProvider.getLatestDataPoint(inMemSeries.getUniqueId())
          : cachedProvider.getLatestDataPoint(inMemSeries.getUniqueId(),
                                              startDate, includeStart, endDate, includeEnd));
            
      // Select a testing order randomly (order might affect cache patterns)
      if (Math.random() > 0.5) {
        
        // First compare series/sub-series with cached by externalid/source/provider/field 
        HistoricalTimeSeries cachedSeries = 
            (startDate == null && endDate == null && maxPoints == null)
            ? cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field)
            : (maxPoints == null)
              ? cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field, 
                                                       startDate, includeStart, endDate, includeEnd)
              : cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field,
                                                       startDate, includeStart, endDate, includeEnd, maxPoints);      
        assertEquals(inMemSeries, cachedSeries); 
            
        // Then compare series/sub-series with cached by UniqueId
        cachedSeries =
            (startDate == null && endDate == null && maxPoints == null)
            ? cachedProvider.getHistoricalTimeSeries(cachedSeries.getUniqueId())
            : (maxPoints == null)
              ? cachedProvider.getHistoricalTimeSeries(inMemSeries.getUniqueId(), 
                                                       startDate, includeStart, endDate, includeEnd)
              : cachedProvider.getHistoricalTimeSeries(inMemSeries.getUniqueId(),
                                                       startDate, includeStart, endDate, includeEnd, maxPoints);      
        assertEquals(inMemSeries, cachedSeries);
        
      } else {
        // First compare series/sub-series with cached by UniqueId
        HistoricalTimeSeries cachedSeries =
            (startDate == null && endDate == null && maxPoints == null)
            ? cachedProvider.getHistoricalTimeSeries(inMemSeries.getUniqueId())
            : (maxPoints == null)
              ? cachedProvider.getHistoricalTimeSeries(inMemSeries.getUniqueId(), 
                                                       startDate, includeStart, endDate, includeEnd)
              : cachedProvider.getHistoricalTimeSeries(inMemSeries.getUniqueId(),
                                                       startDate, includeStart, endDate, includeEnd, maxPoints);      
        assertEquals(inMemSeries, cachedSeries);        

        // Then compare series/sub-series with cached by externalid/source/provider/field 
        cachedSeries = 
            (startDate == null && endDate == null && maxPoints == null)
            ? cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field)
            : (maxPoints == null)
              ? cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field, 
                                                       startDate, includeStart, endDate, includeEnd)
              : cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field,
                                                       startDate, includeStart, endDate, includeEnd, maxPoints);      
        assertEquals(inMemSeries, cachedSeries);        
      }
      
      // Test getting id bundle
      HistoricalTimeSeries historicalTimeSeries = cachedProvider.getHistoricalTimeSeries(ids, dataSource, dataProvider, field);
      assertEquals(ids, cachedProvider.getExternalIdBundle(historicalTimeSeries.getUniqueId()));
    }

    // Shut down cache
    cachedProvider.shutdown();
  }

}
