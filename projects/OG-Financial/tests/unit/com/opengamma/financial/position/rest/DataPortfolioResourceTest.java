/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

import com.opengamma.financial.position.master.rest.DataPortfolioResource;
import com.opengamma.financial.position.master.rest.DataPortfoliosResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfolioResource.
 */
public class DataPortfolioResourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("Test", "PortA");
  private PortfolioMaster _underlying;
  private DataPortfolioResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);
    _resource = new DataPortfolioResource(new DataPortfoliosResource(_underlying), UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio");
    final PortfolioDocument result = new PortfolioDocument(portfolio);
    when(_underlying.get(eq(UID))).thenReturn(result);
    
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio");
    final PortfolioDocument request = new PortfolioDocument(portfolio);
    request.setUniqueId(UID);
    
    final PortfolioDocument result = new PortfolioDocument(portfolio);
    result.setUniqueId(UID);
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeletePortfolio() {
    Response test = _resource.delete();
    verify(_underlying).remove(UID);
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
