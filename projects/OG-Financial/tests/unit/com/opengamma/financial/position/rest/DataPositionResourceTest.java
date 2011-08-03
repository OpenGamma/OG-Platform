/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.ws.rs.core.Response;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionResource.
 */
public class DataPositionResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "PosA");
  private PositionMaster _underlying;
  private DataPositionResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataPositionResource(new DataPositionsResource(_underlying), UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPosition() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    final PositionDocument result = new PositionDocument(position);
    when(_underlying.get(UID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePosition() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    final PositionDocument request = new PositionDocument(position);
    request.setUniqueId(UID);
    
    final PositionDocument result = new PositionDocument(position);
    result.setUniqueId(UID);
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeletePosition() {
    Response test = _resource.delete();
    verify(_underlying).remove(UID);
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
