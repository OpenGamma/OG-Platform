/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.historicaltimeseries;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.bbg.BloombergConstants.DEFAULT_DATA_PROVIDER;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.LocalDateRange;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergBpipeHistoricalTimeSeriesProviderTest {

  private static final String AUTH_OPTION = "app=opengamma:OpenGamma Application B-Pipe BPS";

  private static final ExternalIdBundle SIMPLE_BUNDLE = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("IBM US Equity"));
  private static final ExternalIdBundle COMPLEX_BUNDLE = ExternalIdBundle.of(
      ExternalId.of("BLOOMBERG_BUID", "EQ0010121400001000"), ExternalId.of("BLOOMBERG_TICKER", "C US Equity"),
      ExternalId.of("CUSIP", "172967101"), ExternalId.of("ISIN", "US1729671016"), ExternalId.of("SEDOL1", "2297907"));
  private static final String BBG_DATA_SOURCE = BLOOMBERG_DATA_SOURCE_NAME;
  private static final String PX_LAST = "PX_LAST";

  private BloombergHistoricalTimeSeriesProvider _provider;

  @BeforeMethod
  public void setUp() throws Exception {
    BloombergConnector connector = BloombergTestUtils.getBloombergBipeConnector();
    BloombergHistoricalTimeSeriesProvider provider = new BloombergHistoricalTimeSeriesProvider(connector, AUTH_OPTION);
    
    provider.start();
    _provider = provider;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_provider != null) {
      _provider.stop();
    }
    _provider = null;
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getHistoricalTimeSeries_single_wrongDataSource() {
    LocalDateRange range = LocalDateRange.of(LocalDate.of(2009, 11, 4), LocalDate.of(2009, 11, 4), true);
    _provider.getHistoricalTimeSeries(SIMPLE_BUNDLE, "RUBBISH", DEFAULT_DATA_PROVIDER, PX_LAST, range);
  }

  @Test
  public void test_getHistoricalTimeSeries_single_sameDate() {
    LocalDateRange range = LocalDateRange.of(LocalDate.of(2009, 11, 4), LocalDate.of(2009, 11, 4), true);
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(
        SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, range);
    HistoricalTimeSeriesProviderGetResult result = _provider.getHistoricalTimeSeries(request);
    assertNotNull(result);
    LocalDateDoubleTimeSeries hts = result.getResultMap().get(SIMPLE_BUNDLE);
    assertNotNull(hts);
    assertEquals(1, hts.size());
    Set<String> permissions = result.getPermissionsMap().get(SIMPLE_BUNDLE);
    assertNotNull(permissions);
    assertEquals(1, permissions.size());
    assertTrue(permissions.contains(BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME + ":" + 39491));
  }

  @Test
  public void getHistoricalTimeSeriesWithZeroMaxPoints() throws Exception {    
    LocalDate endDate = LocalDate.of(2012, 03, 07);
    LocalDate startDate = endDate.minusMonths(1);
    
    LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);
    HistoricalTimeSeriesProviderGetRequest realRequest = HistoricalTimeSeriesProviderGetRequest.createGet(SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, dateRange);
    realRequest.setMaxPoints(0);
    LocalDateDoubleTimeSeries realHts = _provider.getHistoricalTimeSeries(realRequest).getResultMap().get(SIMPLE_BUNDLE);
    
    assertNotNull(realHts);
    assertEquals(0, realHts.size());
  }

  @Test
  public void getHistoricalTimeSeriesWithAllMaxPoints() throws Exception {    
    LocalDate endDate = LocalDate.of(2012, 03, 07);
    LocalDate startDate = endDate.minusMonths(1);
    
    LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);
    HistoricalTimeSeriesProviderGetRequest referenceRequest = HistoricalTimeSeriesProviderGetRequest.createGet(SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, dateRange);
    LocalDateDoubleTimeSeries reference = _provider.getHistoricalTimeSeries(referenceRequest).getResultMap().get(SIMPLE_BUNDLE);
    HistoricalTimeSeriesProviderGetRequest realRequest = HistoricalTimeSeriesProviderGetRequest.createGet(SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, dateRange);
    realRequest.setMaxPoints(-9999);
    LocalDateDoubleTimeSeries realHts = _provider.getHistoricalTimeSeries(realRequest).getResultMap().get(SIMPLE_BUNDLE);
    
    assertNotNull(realHts);
    assertEquals(reference.size(), realHts.size());
    assertEquals(reference, realHts);
  }

  @Test
  public void getHistoricalTimeSeriesWithTwoMaxPoints() throws Exception {    
    LocalDate endDate = LocalDate.of(2012, 03, 07);
    LocalDate startDate = endDate.minusMonths(1);
    
    LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);
    HistoricalTimeSeriesProviderGetRequest referenceRequest = HistoricalTimeSeriesProviderGetRequest.createGet(SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, dateRange);
    LocalDateDoubleTimeSeries reference = _provider.getHistoricalTimeSeries(referenceRequest).getResultMap().get(SIMPLE_BUNDLE);
    HistoricalTimeSeriesProviderGetRequest realRequest = HistoricalTimeSeriesProviderGetRequest.createGet(SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, dateRange);
    realRequest.setMaxPoints(-2);
    LocalDateDoubleTimeSeries realHts = _provider.getHistoricalTimeSeries(realRequest).getResultMap().get(SIMPLE_BUNDLE);
    
    assertNotNull(realHts);
    assertEquals(2, realHts.size());
    assertEquals(reference.tail(2), realHts);
  }

  @Test
  public void test_getHistoricalTimeSeries_single_dates() throws Exception {
    LocalDate startDate = LocalDate.of(2009, 10, 29);
    LocalDate endDate = LocalDate.of(2009, 11, 4);
    LocalDateRange range = LocalDateRange.of(startDate, endDate, true);
    LocalDateDoubleTimeSeries timeSeriesExpected = _provider.getHistoricalTimeSeries(SIMPLE_BUNDLE,  BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, range);
    assertNotNull(timeSeriesExpected);
    
    ExecutorService threadPool = Executors.newFixedThreadPool(4);
    List<Future<LocalDateDoubleTimeSeries>> results = new ArrayList<Future<LocalDateDoubleTimeSeries>>();
    for (int i = 0; i < 20; i++) {
      results.add(threadPool.submit(new BHDPgetHistoricalTimeSeriesWithDates(SIMPLE_BUNDLE, BBG_DATA_SOURCE, DEFAULT_DATA_PROVIDER, PX_LAST, startDate, endDate)));
    }
    
    for (Future<LocalDateDoubleTimeSeries> future : results) {
      LocalDateDoubleTimeSeries timeSeriesActual = future.get();
      assertNotNull(timeSeriesActual);
      assertEquals(timeSeriesExpected, timeSeriesActual);
    }
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getHistoricalTimeSeries_Map_wrongDataSource() {
    LocalDateRange range = LocalDateRange.of(LocalDate.of(2009, 11, 4), LocalDate.of(2009, 11, 4), true);
    _provider.getHistoricalTimeSeries(COMPLEX_BUNDLE, "RUBBISH", DEFAULT_DATA_PROVIDER, PX_LAST, range);
  }

  @Test
  public void test_getHistoricalTimeSeries_Map() {
    LocalDateRange range = LocalDateRange.of(LocalDate.of(2009, 10, 4), LocalDate.of(2009, 11, 29), true);
    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> test = _provider.getHistoricalTimeSeries(
        Collections.singleton(COMPLEX_BUNDLE), BBG_DATA_SOURCE, "CMPL", "PX_LAST", range);
    assertNotNull(test);
    assertEquals(1, test.size());
    LocalDateDoubleTimeSeries series = test.get(COMPLEX_BUNDLE);
    assertNotNull(series);
    assertFalse(series.isEmpty());
  }

  //-------------------------------------------------------------------------
  private class BHDPgetHistoricalTimeSeriesWithDates implements Callable<LocalDateDoubleTimeSeries> {
    private ExternalIdBundle _secDes;
    private String _dataSource;
    private String _dataProvider;
    private String _field;
    private LocalDate _startDate;
    private LocalDate _endDate;
    
    public BHDPgetHistoricalTimeSeriesWithDates(ExternalIdBundle secDes, String dataSource, String dataProvider, String field, LocalDate startDate, LocalDate endDate) {
      assertNotNull(secDes);
      assertNotNull(startDate);
      assertNotNull(endDate);
      _secDes = secDes;
      _dataSource = dataSource;
      _dataProvider = dataProvider;
      _field = field;
      _startDate = startDate;
      _endDate = endDate;
    }

    @Override
    public LocalDateDoubleTimeSeries call() throws Exception {
      LocalDateRange range = LocalDateRange.of(_startDate, _endDate, true);
      return _provider.getHistoricalTimeSeries(_secDes, _dataSource, _dataProvider, _field, range);
    }
  }
  
  @Test
  public void test_getAllAvailiableSeries() {
    LocalDateDoubleTimeSeries hts = _provider.getHistoricalTimeSeries(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("JPY3M Curncy")), BBG_DATA_SOURCE, "CMPL", PX_LAST);
    assertNotNull(hts);
    assertEquals(getLatestWeekDay(), hts.getLatestTime());
  }
  
  private LocalDate getLatestWeekDay() {
    Clock clock = Clock.systemUTC();
    return DateUtils.previousWeekDay(LocalDate.now(clock).plusDays(1));
  }

}
