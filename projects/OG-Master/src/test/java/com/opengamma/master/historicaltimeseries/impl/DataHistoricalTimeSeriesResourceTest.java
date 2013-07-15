/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataHistoricalTimeSeriesResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataHistoricalTimeSeriesResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private HistoricalTimeSeriesMaster _underlying;
  private DataHistoricalTimeSeriesResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HistoricalTimeSeriesMaster.class);
    _resource = new DataHistoricalTimeSeriesResource(new DataHistoricalTimeSeriesMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetHistoricalTimeSeries() {
    final ManageableHistoricalTimeSeriesInfo target = new ManageableHistoricalTimeSeriesInfo();
    target.setDataField("DF");
    target.setDataProvider("DP");
    target.setDataSource("DS");
    final HistoricalTimeSeriesInfoDocument result = new HistoricalTimeSeriesInfoDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateHistoricalTimeSeries() {
    final ManageableHistoricalTimeSeriesInfo target = new ManageableHistoricalTimeSeriesInfo();
    target.setDataField("DF");
    target.setDataProvider("DP");
    target.setDataSource("DS");
    final HistoricalTimeSeriesInfoDocument request = new HistoricalTimeSeriesInfoDocument(target);
    request.setUniqueId(OID.atLatestVersion());
    
    final HistoricalTimeSeriesInfoDocument result = new HistoricalTimeSeriesInfoDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteHistoricalTimeSeries() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
