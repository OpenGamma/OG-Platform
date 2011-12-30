/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

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

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataHistoricalTimeSeriessResource.
 */
public class DataHistoricalTimeSeriesMasterResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private HistoricalTimeSeriesMaster _underlying;
  private UriInfo _uriInfo;
  private DataHistoricalTimeSeriesMasterResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HistoricalTimeSeriesMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataHistoricalTimeSeriesMasterResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddHistoricalTimeSeries() {
    final ManageableHistoricalTimeSeriesInfo target = new ManageableHistoricalTimeSeriesInfo();
    target.setDataField("DF");
    target.setDataProvider("DP");
    target.setDataSource("DS");
    final HistoricalTimeSeriesInfoDocument request = new HistoricalTimeSeriesInfoDocument(target);
    
    final HistoricalTimeSeriesInfoDocument result = new HistoricalTimeSeriesInfoDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindHistoricalTimeSeries() {
    DataHistoricalTimeSeriesResource test = _resource.findHistoricalTimeSeries("Test~A");
    assertSame(_resource, test.getParentResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlInfoId());
  }

}
