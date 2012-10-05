/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.provider.historicaltimeseries.impl.EHCachingHistoricalTimeSeriesProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test.
 */
@Test(groups="unit")
public class EHCachingHistoricalTimeSeriesProviderTest {

  private HistoricalTimeSeriesProvider _underlyingProvider;
  private EHCachingHistoricalTimeSeriesProvider _cachingProvider;

  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private static final LocalDateDoubleTimeSeries BIG_HTS;
  private static final LocalDateDoubleTimeSeries SMALL_HTS;
  static {
    LocalDate[] dates1 = {LocalDate.of(2011, 6, 30), LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 3)};
    double[] values1 = {12.34d, 12.45d, 12.79d};
    BIG_HTS = new ArrayLocalDateDoubleTimeSeries(dates1, values1);
    
    LocalDate[] dates2 = {LocalDate.of(2011, 7, 3)};
    double[] values2 = {12.79d};
    SMALL_HTS = new ArrayLocalDateDoubleTimeSeries(dates2, values2);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    _underlyingProvider = mock(HistoricalTimeSeriesProvider.class);
    CacheManager cm = EHCacheUtils.createCacheManager();
    cm.clearAll();
    _cachingProvider = new EHCachingHistoricalTimeSeriesProvider(_underlyingProvider, cm);
  }

  //-------------------------------------------------------------------------
  public void test_get_all() {
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    result.getResultMap().put(BUNDLE, BIG_HTS);
    
    when(_underlyingProvider.getHistoricalTimeSeries(request)).thenReturn(result);
    
    // Fetching same series twice should return same result
    HistoricalTimeSeriesProviderGetResult test1 = _cachingProvider.getHistoricalTimeSeries(request);
    HistoricalTimeSeriesProviderGetResult test2 = _cachingProvider.getHistoricalTimeSeries(request);
    assertEquals(test1, test2);
    assertEquals(BIG_HTS, test1.getResultMap().get(BUNDLE));
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(request);
  }

  public void test_get_subset() {
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    request.setMaxPoints(-1);
    HistoricalTimeSeriesProviderGetRequest allRequest = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    result.getResultMap().put(BUNDLE, BIG_HTS);
    
    when(_underlyingProvider.getHistoricalTimeSeries(allRequest)).thenReturn(result);
    
    // Fetching same series twice should return same result
    HistoricalTimeSeriesProviderGetResult test1 = _cachingProvider.getHistoricalTimeSeries(request);
    HistoricalTimeSeriesProviderGetResult test2 = _cachingProvider.getHistoricalTimeSeries(request);
    assertEquals(test1, test2);
    assertEquals(SMALL_HTS, test1.getResultMap().get(BUNDLE));
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(allRequest);
    verify(_underlyingProvider, times(0)).getHistoricalTimeSeries(request);
    
    // no further underlying hits
    HistoricalTimeSeriesProviderGetResult test3 = _cachingProvider.getHistoricalTimeSeries(allRequest);
    assertEquals(BIG_HTS, test3.getResultMap().get(BUNDLE));
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(allRequest);
    verify(_underlyingProvider, times(0)).getHistoricalTimeSeries(request);
  }

  //-------------------------------------------------------------------------
  public void test_get_all_notFound() {
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    result.getResultMap().put(BUNDLE, null);
    
    when(_underlyingProvider.getHistoricalTimeSeries(request)).thenReturn(result);
    
    // Fetching same series twice should return same result
    HistoricalTimeSeriesProviderGetResult test1 = _cachingProvider.getHistoricalTimeSeries(request);
    HistoricalTimeSeriesProviderGetResult test2 = _cachingProvider.getHistoricalTimeSeries(request);
    assertEquals(test1, test2);
    assertEquals(null, test1.getResultMap().get(BUNDLE));
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(request);
  }

}
