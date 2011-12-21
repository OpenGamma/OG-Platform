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

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataRegionResource.
 */
public class DataRegionResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private RegionMaster _underlying;
  private DataRegionResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(RegionMaster.class);
    _resource = new DataRegionResource(new DataRegionsResource(_underlying), OID.getObjectId());
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
    result.setUniqueId(OID.atLatestVersion());
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteRegion() {
    Response test = _resource.delete();
    verify(_underlying).remove(OID.atLatestVersion());
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
