/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NoneFoundHistoricalTimeSeriesProviderTest {

  @Test
  public void test_get_single() {
    NoneFoundHistoricalTimeSeriesProvider test = new NoneFoundHistoricalTimeSeriesProvider();
    assertEquals(null, test.getHistoricalTimeSeries(ExternalIdBundle.of("A", "B"), "FOO", "BAR", "BAZ", LocalDateRange.ALL));
  }

  @Test
  public void test_get_bulk() {
    NoneFoundHistoricalTimeSeriesProvider test = new NoneFoundHistoricalTimeSeriesProvider();
    HashMap<ExternalIdBundle, LocalDateDoubleTimeSeries> expected = new HashMap<ExternalIdBundle, LocalDateDoubleTimeSeries>();
    assertEquals(expected, test.getHistoricalTimeSeries(ImmutableSet.of(ExternalIdBundle.of("A", "B")), "FOO", "BAR", "BAZ", LocalDateRange.ALL));
  }

  @Test
  public void test_get_request() {
    NoneFoundHistoricalTimeSeriesProvider test = new NoneFoundHistoricalTimeSeriesProvider();
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(ExternalIdBundle.of("A", "B"), "FOO", "BAR", "BAZ", LocalDateRange.ALL);
    HistoricalTimeSeriesProviderGetResult expected = new HistoricalTimeSeriesProviderGetResult();
    assertEquals(expected, test.getHistoricalTimeSeries(request));
  }

}
