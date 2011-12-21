/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

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
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataRegionsResource.
 */
public class DataRegionsResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private RegionMaster _underlying;
  private UriInfo _uriInfo;
  private DataRegionsResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(RegionMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataRegionsResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddRegion() {
    final ManageableRegion target = new ManageableRegion();
    target.setFullName("Bananaville");
    final RegionDocument request = new RegionDocument(target);
    
    final RegionDocument result = new RegionDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindRegion() {
    DataRegionResource test = _resource.findRegion("Test~A");
    assertSame(_resource, test.getRegionsResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlRegionId());
  }

}
