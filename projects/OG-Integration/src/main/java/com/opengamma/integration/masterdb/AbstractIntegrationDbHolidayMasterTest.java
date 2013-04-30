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

import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbHolidayMaster.
 */
public abstract class AbstractIntegrationDbHolidayMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 1000;

  private HolidayMaster _holMaster;

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _holMaster = getTestHelper().getHolidayMaster();
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _holMaster = null;
  }

  protected HolidayMaster getHolidayMaster() {
    return _holMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final HolidaySearchRequest request = new HolidaySearchRequest();
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getHolidayMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking holiday master, page " + request.getPagingRequest());
      try {
        final HolidaySearchResult result = getHolidayMaster().search(request);
        for (final HolidayDocument doc : result.getDocuments()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getHoliday());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex);
      }
    }
    System.out.println("Checked holiday master, size: " + total);
  }

  private void findError(final HolidaySearchRequest request, final RuntimeException ex) throws Exception {
    final int item = request.getPagingRequest().getFirstItem();
    for (int i = 0; i < item + PAGE_SIZE; i++) {
      request.setPagingRequest(PagingRequest.ofIndex(item, 1));
      try {
        getHolidayMaster().search(request);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load holiday matching " + request.getPagingRequest(), ex);
      }
    }
  }

}
