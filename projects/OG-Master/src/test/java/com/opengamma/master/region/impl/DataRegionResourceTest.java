/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

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
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataRegionResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataRegionResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private RegionMaster _underlying;
  private DataRegionResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(RegionMaster.class);
    _resource = new DataRegionResource(new DataRegionMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetRegion() {
    final ManageableRegion target = new ManageableRegion();
    target.setFullName("Bananaville");
    final RegionDocument result = new RegionDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateRegion() {
    final ManageableRegion target = new ManageableRegion();
    target.setFullName("Bananaville");
    final RegionDocument request = new RegionDocument(target);
    request.setUniqueId(OID.atLatestVersion());
    
    final RegionDocument result = new RegionDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteRegion() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
