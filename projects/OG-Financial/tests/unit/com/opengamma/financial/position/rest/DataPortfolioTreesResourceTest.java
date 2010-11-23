/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.position.master.rest.DataPortfolioTreeResource;
import com.opengamma.financial.position.master.rest.DataPortfolioTreesResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PositionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfoliosResource.
 */
public class DataPortfolioTreesResourceTest {

  private PositionMaster _underlying;
  private UriInfo _uriInfo;
  private DataPortfolioTreesResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataPortfolioTreesResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddPortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio A");
    portfolio.getRootNode().setName("RootNode");
    portfolio.getRootNode().addChildNode(new ManageablePortfolioNode("Child"));
    final PortfolioTreeDocument request = new PortfolioTreeDocument(portfolio);
    
    final PortfolioTreeDocument result = new PortfolioTreeDocument(portfolio);
    result.setPortfolioId(UniqueIdentifier.of("Test", "PortA"));
    when(_underlying.addPortfolioTree(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindPortfolio() {
    DataPortfolioTreeResource test = _resource.findPortfolio("Test::PortA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(UniqueIdentifier.of("Test", "PortA"), test.getUrlPortfolioId());
  }

}
