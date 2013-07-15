/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.masterdb;

import static org.testng.AssertJUnit.assertNotNull;
import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.masterdb.security.EHCachingSecurityMasterDetailProvider;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDetailProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbSecurityMaster.
 */
public abstract class AbstractIntegrationDbSecurityMasterTest extends AbstractLocalMastersTest {

  private static final int PAGE_SIZE = 1000;

  private DbSecurityMaster _secMaster;
  private CacheManager _cacheManager;

  @BeforeClass(groups = TestGroup.INTEGRATION)
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass(groups = TestGroup.INTEGRATION)
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod(groups = TestGroup.INTEGRATION)
  public void setUp() throws Exception {
    _secMaster = (DbSecurityMaster) getTestHelper().getSecurityMaster();
    _secMaster.setDetailProvider(new EHCachingSecurityMasterDetailProvider(new HibernateSecurityMasterDetailProvider(), _cacheManager));
  }

  @AfterMethod(groups = TestGroup.INTEGRATION)
  public void tearDown() throws Exception {
    _secMaster = null;
  }

  protected SecurityMaster getSecurityMaster() {
    return _secMaster;
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false, description = "Queries the entire database")
  public void test_queryAll() throws Exception {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(PagingRequest.NONE);
    final int total = getSecurityMaster().search(request).getPaging().getTotalItems();
    final int pages = (total / PAGE_SIZE) + 1;
    for (int page = 1; page <= pages; page++) {
      request.setPagingRequest(PagingRequest.ofPage(page, PAGE_SIZE));
      System.out.println("Checking security master, page " + request.getPagingRequest());
      try {
        final SecuritySearchResult result = getSecurityMaster().search(request);
        for (final SecurityDocument doc : result.getDocuments()) {
          assertNotNull(doc);
          assertNotNull(doc.getUniqueId());
          assertNotNull(doc.getSecurity());
        }
      } catch (final RuntimeException ex) {
        findError(request, ex, total);
      }
    }
    System.out.println("Checked security master, size: " + total);
  }

  private void findError(final SecuritySearchRequest request, final RuntimeException ex, final int total) throws Exception {
    SecuritySearchResult result;
    try {
      request.setFullDetail(false);
      result = _secMaster.search(request);
    } catch (final RuntimeException ex2) {
      throw new RuntimeException("Unable to load securities (or basic info) matching " + request.getPagingRequest(), ex);
    }
    for (final SecurityDocument doc : result.getDocuments()) {
      final UniqueId uniqueId = doc.getUniqueId();
      try {
        getSecurityMaster().get(uniqueId);
      } catch (final RuntimeException ex2) {
        throw new RuntimeException("Unable to load security " + uniqueId + "(" + doc.getName() + ") in " +
            request.getPagingRequest() + " total " + total, ex);
      }
    }
    throw new RuntimeException("Unable to load securities matching " + request.getPagingRequest(), ex);
  }

}
