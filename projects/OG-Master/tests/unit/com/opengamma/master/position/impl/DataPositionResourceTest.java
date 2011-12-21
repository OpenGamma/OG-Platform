/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.math.BigDecimal;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionResource.
 */
public class DataPositionResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private PositionMaster _underlying;
  private DataPositionResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataPositionResource(new DataPositionsResource(_underlying), OID.getObjectId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPosition() {
    final ManageablePosition target = new ManageablePosition();
    target.setQuantity(BigDecimal.ONE);
    final PositionDocument result = new PositionDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePosition() {
    final ManageablePosition target = new ManageablePosition();
    target.setQuantity(BigDecimal.ONE);
    final PositionDocument request = new PositionDocument(target);
    request.setUniqueId(OID.atLatestVersion());
    
    final PositionDocument result = new PositionDocument(target);
    result.setUniqueId(OID.atLatestVersion());
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeletePosition() {
    Response test = _resource.delete();
    verify(_underlying).remove(OID.atLatestVersion());
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
