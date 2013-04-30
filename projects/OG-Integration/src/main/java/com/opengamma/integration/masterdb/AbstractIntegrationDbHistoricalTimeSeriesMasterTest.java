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

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbExchangeMaster.
 */
public abstract class AbstractIntegrationDbHistoricalTimeSeriesMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 1000;
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _historicalTimeSeriesMaster = getTestHelper().getHistoricalTimeSeriesMaster();
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _historicalTimeSeriesMaster = null;
  }

  protected HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getHistoricalTimeSeriesMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking exchange master, page " + request.getPagingRequest());
      try {
        final HistoricalTimeSeriesInfoSearchResult result = getHistoricalTimeSeriesMaster().search(request);
        for (final HistoricalTimeSeriesInfoDocument doc : result.getDocuments()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getInfo());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex);
      }
    }
    System.out.println("Checked exchange master, size: " + total);
  }

  private void findError(final HistoricalTimeSeriesInfoSearchRequest request, final RuntimeException ex) throws Exception {
    final int item = request.getPagingRequest().getFirstItem();
    for (int i = 0; i < item + PAGE_SIZE; i++) {
      request.setPagingRequest(PagingRequest.ofIndex(item, 1));
      try {
        getHistoricalTimeSeriesMaster().search(request);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load time series info matching " + request.getPagingRequest(), ex);
      }
    }
  }

}
