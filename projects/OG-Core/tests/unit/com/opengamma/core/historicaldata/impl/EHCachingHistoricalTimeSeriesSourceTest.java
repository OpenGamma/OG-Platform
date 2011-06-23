/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaldata.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Test EHCachingHistoricalTimeSeriesSource.
 */
@Test
public class EHCachingHistoricalTimeSeriesSourceTest {

  private HistoricalTimeSeriesSource _underlyingSource;
  private EHCachingHistoricalTimeSeriesSource _cachingSource;

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");

  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingSource = mock(HistoricalTimeSeriesSource.class);
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingHistoricalTimeSeriesSource(_underlyingSource, cm);
  }

  //-------------------------------------------------------------------------
  public void getHistoricalTimeSeries_UniqueIdentifier() {
    LocalDate[] dates = {LocalDate.of(2011, 6, 30)};
    double[] values = {12.34d};
    ArrayLocalDateDoubleTimeSeries timeSeries = new ArrayLocalDateDoubleTimeSeries(dates, values);
    HistoricalTimeSeries series = new HistoricalTimeSeriesImpl(UID, timeSeries);
    
    when(_underlyingSource.getHistoricalTimeSeries(UID)).thenReturn(series);
    
    HistoricalTimeSeries series1 = _cachingSource.getHistoricalTimeSeries(UID);
    HistoricalTimeSeries series2 = _cachingSource.getHistoricalTimeSeries(UID);
    assertEquals(series, series1);
    assertEquals(series, series2);
    assertEquals(series1, series2);
    
    verify(_underlyingSource, times(1)).getHistoricalTimeSeries(UID);
  }

}
