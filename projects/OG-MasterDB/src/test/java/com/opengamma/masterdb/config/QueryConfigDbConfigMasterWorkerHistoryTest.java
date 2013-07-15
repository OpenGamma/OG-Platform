/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryConfigDbConfigMasterWorkerHistoryTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryConfigDbConfigMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_invalid_historyRequest() {
    ConfigHistoryRequest<ExternalId> request = new ConfigHistoryRequest<ExternalId>();
    _cfgMaster.history(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  private ConfigHistoryRequest<ExternalId> createRequest(ObjectId oid) {
    ConfigHistoryRequest<ExternalId> request = new ConfigHistoryRequest<ExternalId>(oid, ExternalId.class);
    return request;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    PagingRequest pr = PagingRequest.ofPage(1, 1);
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setPagingRequest(pr);
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    PagingRequest pr = PagingRequest.ofPage(2, 1);
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setPagingRequest(pr);
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setVersionsFromInstant(_version1aInstant.minusSeconds(5));
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setVersionsFromInstant(_version1cInstant.plusSeconds(5));
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setVersionsToInstant(_version1aInstant.minusSeconds(5));
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setVersionsToInstant(_version1cInstant.plusSeconds(5));
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    ObjectId oid = ObjectId.of("DbCfg", "201");
    ConfigHistoryRequest<ExternalId> request = createRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    ConfigHistoryResult<ExternalId> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
