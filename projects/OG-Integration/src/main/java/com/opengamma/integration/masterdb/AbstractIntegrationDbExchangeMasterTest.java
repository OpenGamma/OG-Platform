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

import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbExchangeMaster.
 */
public abstract class AbstractIntegrationDbExchangeMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 1000;
  private ExchangeMaster _exgMaster;

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _exgMaster = getTestHelper().getExchangeMaster();
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _exgMaster = null;
  }

  protected ExchangeMaster getExchangeMaster() {
    return _exgMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getExchangeMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking exchange master, page " + request.getPagingRequest());
      try {
        final ExchangeSearchResult result = getExchangeMaster().search(request);
        for (final ExchangeDocument doc : result.getDocuments()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getExchange());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex);
      }
    }
    System.out.println("Checked exchange master, size: " + total);
  }

  private void findError(final ExchangeSearchRequest request, final RuntimeException ex) throws Exception {
    final int item = request.getPagingRequest().getFirstItem();
    for (int i = 0; i < item + PAGE_SIZE; i++) {
      request.setPagingRequest(PagingRequest.ofIndex(item, 1));
      try {
        getExchangeMaster().search(request);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load exchange matching " + request.getPagingRequest(), ex);
      }
    }
  }

}
