/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfolioNodeResource.
 */
public class DataPortfolioNodeResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "NodeA");
  private PortfolioMaster _underlying;
  private DataPortfolioNodeResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);
    _resource = new DataPortfolioNodeResource(new DataPortfoliosResource(_underlying), UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final ManageablePortfolioNode target = new ManageablePortfolioNode("Node");
    when(_underlying.getNode(UID)).thenReturn(target);
    
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

}
