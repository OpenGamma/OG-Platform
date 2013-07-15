/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfolioResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataPortfolioResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PortA");
  private PortfolioMaster _underlying;
  private DataPortfolioResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);
    _resource = new DataPortfolioResource(new DataPortfolioMasterResource(_underlying), OID);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio");
    final PortfolioDocument result = new PortfolioDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePortfolio() {
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio");
    final PortfolioDocument request = new PortfolioDocument(target);
    request.setUniqueId(OID.atVersion("1"));
    
    final PortfolioDocument result = new PortfolioDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeletePortfolio() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
