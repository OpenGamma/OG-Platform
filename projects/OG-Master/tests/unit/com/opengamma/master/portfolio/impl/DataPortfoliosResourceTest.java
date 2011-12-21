/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

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
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfoliosResource.
 */
public class DataPortfoliosResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private PortfolioMaster _underlying;
  private UriInfo _uriInfo;
  private DataPortfoliosResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataPortfoliosResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddPortfolio() {
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio A");
    target.getRootNode().setName("RootNode");
    target.getRootNode().addChildNode(new ManageablePortfolioNode("Child"));
    final PortfolioDocument request = new PortfolioDocument(target);
    
    final PortfolioDocument result = new PortfolioDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindPortfolio() {
    DataPortfolioResource test = _resource.findPortfolio("Test~PortA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(ObjectId.of("Test", "PortA"), test.getUrlPortfolioId());
  }

  @Test
  public void testFindPortfolioNode() {
    DataPortfolioNodeResource test = _resource.findPortfolioNode("Test~NodeA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(UniqueId.of("Test", "NodeA"), test.getUrlNodeId());
  }

}
