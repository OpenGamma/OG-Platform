/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
public class QueryConfigDbConfigTypeMasterWorkerHistoryTest extends AbstractDbConfigTypeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigTypeMasterWorkerHistoryTest.class);

  public QueryConfigDbConfigTypeMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsFromInstant(_version1aInstant.minusSeconds(5));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsFromInstant(_version1cInstant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsToInstant(_version1aInstant.minusSeconds(5));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsToInstant(_version1cInstant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _cfgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_cfgMaster.getClass().getSimpleName() + "[DbCfg]", _cfgMaster.toString());
  }

}
