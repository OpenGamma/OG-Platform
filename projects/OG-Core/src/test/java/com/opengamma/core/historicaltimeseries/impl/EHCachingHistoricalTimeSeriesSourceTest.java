/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache"})
public class EHCachingHistoricalTimeSeriesSourceTest {

  private static final UniqueId UID = UniqueId.of("A", "B");

  private HistoricalTimeSeriesSource _underlyingSource;
  private EHCachingHistoricalTimeSeriesSource _cachingSource;
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingHistoricalTimeSeriesSourceTest.class);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod
  public void setUp() {
    _underlyingSource = mock(HistoricalTimeSeriesSource.class);
    when(_underlyingSource.changeManager()).thenReturn(new BasicChangeManager());
    _cachingSource = new EHCachingHistoricalTimeSeriesSource(_underlyingSource, _cacheManager);
  }

  @AfterMethod
  public void tearDown() {
    _cachingSource.shutdown();
  }

  //-------------------------------------------------------------------------
  public void getHistoricalTimeSeries_UniqueId() {
    LocalDate[] dates = {LocalDate.of(2011, 6, 30)};
    double[] values = {12.34d};
    ImmutableLocalDateDoubleTimeSeries timeSeries = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    HistoricalTimeSeries series = new SimpleHistoricalTimeSeries(UID, timeSeries);
    
    when(_underlyingSource.getHistoricalTimeSeries(UID)).thenReturn(series);
    
    // Fetching same series twice should return same result
    HistoricalTimeSeries series1 = _cachingSource.getHistoricalTimeSeries(UID);
    HistoricalTimeSeries series2 = _cachingSource.getHistoricalTimeSeries(UID);
    assertEquals(series, series1);
    assertEquals(series, series2);
    assertEquals(series1, series2);
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingSource, times(1)).getHistoricalTimeSeries(UID);
  }
  
  public void getExternalIdBundle_UniqueId() {
    ExternalId djxTicker = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "DJX Index");
    ExternalId djxBUID = ExternalId.of(ExternalSchemes.BLOOMBERG_BUID, "EI09JDX");
    ExternalIdBundle idBundle = ExternalIdBundle.of(djxTicker, djxBUID);
    
    when(_underlyingSource.getExternalIdBundle(UID)).thenReturn(idBundle);
    
    // Fetching same series twice should return same result
    ExternalIdBundle bundle1 = _cachingSource.getExternalIdBundle(UID);
    ExternalIdBundle bundle2 = _cachingSource.getExternalIdBundle(UID);
    assertEquals(idBundle, bundle1);
    assertEquals(idBundle, bundle2);
    assertEquals(bundle1, bundle2);
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingSource, times(1)).getExternalIdBundle(UID);
  }

}
