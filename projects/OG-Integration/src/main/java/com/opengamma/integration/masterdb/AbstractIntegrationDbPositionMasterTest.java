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

import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbPositionMaster.
 */
public abstract class AbstractIntegrationDbPositionMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 5000;

  private PositionMaster _posMaster;

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _posMaster = getTestHelper().getPositionMaster();
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _posMaster = null;
  }

  protected PositionMaster getPositionMaster() {
    return _posMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getPositionMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking position master, page " + request.getPagingRequest());
      try {
        final PositionSearchResult result = getPositionMaster().search(request);
        for (final PositionDocument doc : result.getDocuments()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getPosition());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex);
      }
    }
    System.out.println("Checked position master, size: " + total);
  }

  private void findError(final PositionSearchRequest request, final RuntimeException ex) throws Exception {
    final int item = request.getPagingRequest().getFirstItem();
    for (int i = 0; i < item + PAGE_SIZE; i++) {
      request.setPagingRequest(PagingRequest.ofIndex(item, 1));
      try {
        getPositionMaster().search(request);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load position matching " + request.getPagingRequest(), ex);
      }
    }
  }

}
