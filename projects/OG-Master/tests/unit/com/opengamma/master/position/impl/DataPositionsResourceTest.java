/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.math.BigDecimal;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionsResource.
 */
public class DataPositionsResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private PositionMaster _underlying;
  private UriInfo _uriInfo;
  private DataPositionsResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataPositionsResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddPosition() {
    final ManageablePosition target = new ManageablePosition();
    target.setQuantity(BigDecimal.ONE);
    final PositionDocument request = new PositionDocument(target);
    
    final PositionDocument result = new PositionDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindPosition() {
    DataPositionResource test = _resource.findPosition("Test~A");
    assertSame(_resource, test.getPositionsResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlPositionId());
  }

}
