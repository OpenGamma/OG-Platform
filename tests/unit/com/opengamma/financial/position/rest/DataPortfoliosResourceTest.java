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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.position.master.ManageablePortfolio;
import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.rest.DataPortfolioResource;
import com.opengamma.financial.position.master.rest.DataPortfoliosResource;
import com.opengamma.id.UniqueIdentifier;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.uri.UriBuilderImpl;

/**
 * Tests DataPortfoliosResource.
 */
public class DataPortfoliosResourceTest {

  private PositionMaster _underlying;
  private UriInfo _uriInfo;
  private DataPortfoliosResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl().host("testhost"));
    _resource = new DataPortfoliosResource(_underlying);
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
    DataPortfolioResource test = _resource.findPortfolio("Test::PortA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(UniqueIdentifier.of("Test", "PortA"), test.getUrlPortfolioId());
  }

}
