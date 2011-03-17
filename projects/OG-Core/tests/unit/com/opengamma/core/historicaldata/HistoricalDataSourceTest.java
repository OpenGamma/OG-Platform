/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaldata;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaldata.impl.EHCachingHistoricalDataSource;
import com.opengamma.core.historicaldata.impl.MockHistoricalDataSource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Test HistoricalDataSource.
 */
@Test
public class HistoricalDataSourceTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalDataSourceTest.class);
  private static final String ALPHAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private Set<String> _usedIds = new HashSet<String>();

  private boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }

  private LocalDateDoubleTimeSeries randomTimeSeries() {
    MutableLocalDateDoubleTimeSeries dts = new ListLocalDateDoubleTimeSeries();
    LocalDate start = LocalDate.of(2000, 1, 2);
    LocalDate end = start.plusYears(10);
    LocalDate current = start;
    while (current.isBefore(end)) {
      current = current.plusDays(1);
      if (isWeekday(current)) {
        dts.putDataPoint(current, Math.random());
      }
    }
    return dts;
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

  private IdentifierBundle makeDomainSpecificIdentifiers() {
    return IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId(makeUniqueRandomId()), SecurityUtils.bloombergBuidSecurityId(makeUniqueRandomId()));
  }

  private Pair<HistoricalDataSource, Set<IdentifierBundle>> buildAndTestInMemoryProvider() {
    MockHistoricalDataSource inMemoryHistoricalDataProvider = new MockHistoricalDataSource();
    Map<IdentifierBundle, Map<String, Map<String, Map<String, LocalDateDoubleTimeSeries>>>> map = new HashMap<IdentifierBundle, Map<String, Map<String, Map<String, LocalDateDoubleTimeSeries>>>>();
    for (int i = 0; i < 100; i++) {
      IdentifierBundle ids = makeDomainSpecificIdentifiers();
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
            dataProviderSubMap.put(field, randomTimeSeries);
            inMemoryHistoricalDataProvider.storeHistoricalTimeSeries(ids, dataSource, dataProvider, field, randomTimeSeries);
          }
        }
      }
    }
    for (IdentifierBundle dsids : map.keySet()) {
      for (String dataSource : new String[] {"BLOOMBERG", "REUTERS", "JPM"}) {
        for (String dataProvider : new String[] {"UNKNOWN", "CMPL", "CMPT"}) {
          for (String field : new String[] {"PX_LAST", "VOLUME"}) {
            LocalDateDoubleTimeSeries expectedTS = map.get(dsids).get(dataSource).get(dataProvider).get(field);
            Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = inMemoryHistoricalDataProvider.getHistoricalData(dsids, dataSource, dataProvider, field);
            assertEquals(expectedTS, tsPair.getSecond());
            assertEquals(expectedTS, inMemoryHistoricalDataProvider.getHistoricalData(tsPair.getFirst()));
          }
        }
      }
    }
    return Pair.of((HistoricalDataSource) inMemoryHistoricalDataProvider, map.keySet());
  }

  public void testInMemoryProvider() {
    buildAndTestInMemoryProvider();
  }

  public void testEHCachingHistoricalDataProvider() {
    Pair<HistoricalDataSource, Set<IdentifierBundle>> providerAndDsids = buildAndTestInMemoryProvider();
    HistoricalDataSource inMemoryHistoricalDataProvider = providerAndDsids.getFirst();
    EHCachingHistoricalDataSource cachedProvider = new EHCachingHistoricalDataSource(inMemoryHistoricalDataProvider, EHCacheUtils.createCacheManager());
    Set<IdentifierBundle> identifiers = providerAndDsids.getSecond();
    IdentifierBundle[] dsids = identifiers.toArray(new IdentifierBundle[] {});
    String[] dataSources = new String[] {"BLOOMBERG", "REUTERS", "JPM"};
    String[] dataProviders = new String[] {"UNKNOWN", "CMPL", "CMPT"};
    String[] fields = new String[] {"PX_LAST", "VOLUME"};
    for (int i = 0; i < 10000; i++) {
      IdentifierBundle ids = dsids[random(dsids.length)];
      String dataSource = dataSources[random(dataSources.length)];
      String dataProvider = dataProviders[random(dataProviders.length)];
      String field = fields[random(fields.length)];
      Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> inMemPair = inMemoryHistoricalDataProvider.getHistoricalData(ids, dataSource, dataProvider, field);
      Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> cachedPair = cachedProvider.getHistoricalData(ids, dataSource, dataProvider, field);
      assertEquals(inMemPair, cachedPair);
      assertEquals(inMemoryHistoricalDataProvider.getHistoricalData(inMemPair.getFirst()), cachedProvider.getHistoricalData(cachedPair.getFirst()));
      
      cachedPair = cachedProvider.getHistoricalData(ids, dataSource, dataProvider, field, inMemPair.getValue().getEarliestTime(), true, inMemPair.getValue().getLatestTime(), false);
      assertEquals(inMemPair, cachedPair);
    }
  }
}
