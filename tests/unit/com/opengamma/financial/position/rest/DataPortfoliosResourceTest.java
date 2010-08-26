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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.position.master.ManageablePortfolio;
import com.opengamma.financial.position.master.ManageablePortfolioNode;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests DataPortfoliosResource.
 */
public class DataPortfoliosResourceTest {

  private PositionMaster _underlying;
  private DataPortfoliosResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataPortfoliosResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testSearchPortfolios() {
    final PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 20));
    request.setDepth(1);
    
    final PortfolioTreeSearchResult result = new PortfolioTreeSearchResult();
    result.setPaging(new Paging(1, 20, 0));
    when(_underlying.searchPortfolioTrees(eq(request))).thenReturn(result);
    
    PortfolioTreeSearchResult test = _resource.get(1, 20, null, 1);
    assertSame(result, test);
  }

  @Test
  public void testAddPortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio A");
    portfolio.getRootNode().setName("RootNode");
    portfolio.getRootNode().addChildNode(new ManageablePortfolioNode("Child"));
    final PortfolioTreeDocument request = new PortfolioTreeDocument(portfolio);
    
    final PortfolioTreeDocument result = new PortfolioTreeDocument(portfolio);
    result.setPortfolioId(UniqueIdentifier.of("Test", "PortA"));
    when(_underlying.addPortfolioTree(same(request))).thenReturn(result);
    
    PortfolioTreeDocument test = _resource.post(request);
    assertSame(result, test);
  }

  @Test
  public void testFindPortfolio() {
    DataPortfolioResource test = _resource.findPortfolio("Test::PortA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(UniqueIdentifier.of("Test", "PortA"), test.getUrlPortfolioId());
  }

}
