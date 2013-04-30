/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.masterdb;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbPortfolioMaster.
 */
public abstract class AbstractIntegrationDbPortfolioMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 1000;

  private PortfolioMaster _prtMaster;

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _prtMaster = getTestHelper().getPortfolioMaster();
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _prtMaster = null;
  }

  protected PortfolioMaster getPortfolioMaster() {
    return _prtMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getPortfolioMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking portfolio master, page " + request.getPagingRequest());
      try {
        final PortfolioSearchResult result = getPortfolioMaster().search(request);
        for (final PortfolioDocument doc : result.getDocuments()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getPortfolio());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex);
      }
    }
    System.out.println("Checked portfolio master, size: " + total);
  }

  private void findError(final PortfolioSearchRequest request, final RuntimeException ex) throws Exception {
    final int item = request.getPagingRequest().getFirstItem();
    for (int i = 0; i < item + PAGE_SIZE; i++) {
      request.setPagingRequest(PagingRequest.ofIndex(item, 1));
      try {
        getPortfolioMaster().search(request);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load portfolio matching " + request.getPagingRequest(), ex);
      }
    }
  }

}
