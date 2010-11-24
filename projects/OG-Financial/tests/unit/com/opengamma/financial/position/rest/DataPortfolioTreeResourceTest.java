/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.position.master.rest.DataPortfolioTreeResource;
import com.opengamma.financial.position.master.rest.DataPortfolioTreesResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PositionMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfolioResource.
 */
public class DataPortfolioTreeResourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("Test", "PortA");
  private PositionMaster _underlying;
  private DataPortfolioTreeResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataPortfolioTreeResource(new DataPortfolioTreesResource(_underlying), UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio");
    final PortfolioTreeDocument result = new PortfolioTreeDocument(portfolio);
    when(_underlying.getPortfolioTree(eq(UID))).thenReturn(result);
    
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio");
    final PortfolioTreeDocument request = new PortfolioTreeDocument(portfolio);
    request.setUniqueId(UID);
    
    final PortfolioTreeDocument result = new PortfolioTreeDocument(portfolio);
    result.setUniqueId(UID);
    when(_underlying.updatePortfolioTree(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeletePortfolio() {
    Response test = _resource.delete();
    verify(_underlying).removePortfolioTree(UID);
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
