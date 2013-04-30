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

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbConfigMaster.
 */
public abstract class AbstractIntegrationDbConfigMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 1000;
  private ConfigMaster _cfgMaster;

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _cfgMaster = getTestHelper().getConfigMaster();
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _cfgMaster = null;
  }

  protected ConfigMaster getConfigMaster() {
    return _cfgMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final ConfigSearchRequest<?> request = new ConfigSearchRequest<Object>(Object.class);
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getConfigMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking config master, page " + request.getPagingRequest());
      try {
        final ConfigSearchResult<?> result = getConfigMaster().search(request);
        for (final ConfigItem<?> doc : result.getValues()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getValue());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex);
      }
    }
    System.out.println("Checked config master, size: " + total);
  }

  private void findError(final ConfigSearchRequest<?> request, final RuntimeException ex) throws Exception {
    final int item = request.getPagingRequest().getFirstItem();
    for (int i = 0; i < item + PAGE_SIZE; i++) {
      request.setPagingRequest(PagingRequest.ofIndex(item, 1));
      try {
        getConfigMaster().search(request);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load config matching " + request.getPagingRequest(), ex);
      }
    }
  }

}
