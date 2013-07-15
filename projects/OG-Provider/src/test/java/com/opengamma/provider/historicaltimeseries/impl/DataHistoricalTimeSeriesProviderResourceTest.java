/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataHistoricalTimeSeriesProviderResourceTest {

  private HistoricalTimeSeriesProvider _underlying;
  private UriInfo _uriInfo;
  private DataHistoricalTimeSeriesProviderResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HistoricalTimeSeriesProvider.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataHistoricalTimeSeriesProviderResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGet() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(
        ExternalIdBundle.of("A", "B"), "S", "P", "F", LocalDateRange.ALL);
    final HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    
    when(_underlying.getHistoricalTimeSeries(same(request))).thenReturn(result);
    
    Response test = _resource.getHistoricalTimeSeries(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

}
