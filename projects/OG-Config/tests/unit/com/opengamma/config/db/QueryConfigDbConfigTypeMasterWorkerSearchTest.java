/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
public class QueryConfigDbConfigTypeMasterWorkerSearchTest extends AbstractDbConfigTypeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigTypeMasterWorkerSearchTest.class);

  private DbConfigTypeMasterWorker<Identifier> _worker;

  public QueryConfigDbConfigTypeMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryConfigDbConfigTypeMasterWorker<Identifier>();
    _worker.init(_cfgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalConfigs, test.getPaging().getTotalItems());
    
    assertEquals(_totalConfigs, test.getDocuments().size());
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc1.getConfigId());
    assertEquals(_version1Instant, doc1.getVersionFromInstant());
    assertEquals(null, doc1.getVersionToInstant());
    assertEquals(Identifier.of("A", "B"), doc1.getValue());
    assertEquals("TestConfig102", doc1.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 2));
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalConfigs, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "101", "0"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc1.getConfigId());
  }

  @Test
  public void test_search_pageTwo() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalConfigs, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("FooBar");
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("TestConfig102");
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc0.getConfigId());
  }

  @Test
  public void test_search_name_case() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("TESTConfig102");
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc0.getConfigId());
  }

  @Test
  public void test_search_name_wildcard() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("TestConfig1*");
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "101", "0"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc1.getConfigId());
  }

  @Test
  public void test_search_name_wildcardCase() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("TESTConfig1*");
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "101", "0"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc1.getConfigId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    ConfigDocument<Identifier> doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbCfg", "101", "0"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc1.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc2.getConfigId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    ConfigSearchResult<Identifier> test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    ConfigDocument<Identifier> doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbCfg", "101", "0"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc1.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc2.getConfigId());  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbCfg]", _worker.toString());
  }

}
