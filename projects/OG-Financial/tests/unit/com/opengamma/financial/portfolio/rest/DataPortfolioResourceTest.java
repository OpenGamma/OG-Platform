/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.rest;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfolioResource.
 */
public class DataPortfolioResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "PortA");
  private PortfolioMaster _underlying;
  private DataPortfolioResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);
    _resource = new DataPortfolioResource(new DataPortfoliosResource(_underlying), UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio");
    final PortfolioDocument result = new PortfolioDocument(portfolio);
    when(_underlying.get(UID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
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
