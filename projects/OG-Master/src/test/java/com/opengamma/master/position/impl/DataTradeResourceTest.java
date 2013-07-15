/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

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
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataTradeResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "TrdA");
  private PositionMaster _underlying;
  private DataTradeResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataTradeResource(new DataPositionMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetTrade() {
    final ManageablePosition target = new ManageablePosition();
    target.setQuantity(BigDecimal.ONE);
    final ManageableTrade result = new ManageableTrade();
    result.setQuantity(BigDecimal.ONE);
    when(_underlying.getTrade(OID.atLatestVersion())).thenReturn(result);
    
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testGetTradeVersion() {
    final ManageablePosition target = new ManageablePosition();
    target.setQuantity(BigDecimal.ONE);
    final ManageableTrade result = new ManageableTrade();
    result.setQuantity(BigDecimal.ONE);
    when(_underlying.getTrade(OID.atVersion("6"))).thenReturn(result);
    
    Response test = _resource.getVersioned("6");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

}
