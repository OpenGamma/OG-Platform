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
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.rest.DataPositionResource;
import com.opengamma.financial.position.rest.DataPositionsResource;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionsResource.
 */
public class DataPositionsResourceTest {

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
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    final PositionDocument request = new PositionDocument(position);
    
    final PositionDocument result = new PositionDocument(position);
    result.setUniqueId(UniqueIdentifier.of("Test", "PosA"));
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindPosition() {
    DataPositionResource test = _resource.findPosition("Test~PosA");
    assertSame(_resource, test.getPositionsResource());
    assertEquals(UniqueIdentifier.of("Test", "PosA"), test.getUrlPositionId());
  }

}
